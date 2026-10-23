package Subsystem;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Torreta com motor + encoder, com dois hives (A e B) e compensação de velocidade.
 *
 * A DISTÂNCIA para o shooter é calculada aqui, no mesmo instante e do mesmo ponto
 * de onde a torreta mira: a POSE DO ROBÔ (odometria), já com a posição prevista
 * para quando a bola chegar. Assim mira e distância nunca discordam.
 */
@Configurable
public class Turret {

    // ---------- Mecânica ----------
    public static double RPT = 0.0040992;     // rad por tick
    public static double LIMIT = Math.toRadians(130); // ±90°

    // ---------- Controle ----------
    public static double kP = 1.6;    // potência por rad de erro
    public static double kD = 0.04;   // amortece (rad/s)
    public static double kS = 0.08;   // vence o atrito
    public static double kV = 0.06;   // feedforward: potência por rad/s que o alvo está andando
    public static double READY_DEG = 2.0;

    // ---------- Compensação de velocidade ----------
    public static double TOF = 0.40;        // s de voo da bola. 0 = sem compensação
    public static double ROBOT_VEL_FILTER = 0.4; // filtro da velocidade estimada do robô (0..1, 1 = sem filtro)

    // ---------- Hives ----------
    public static Pose hiveA = new Pose(-5, -50);
    public static Pose hiveB = new Pose(-5, -70);
    public static double SPLIT_Y = -60;     // acima = A, abaixo = B
    public static double HYSTERESIS = 3.0;  // pol além da linha para trocar

    public static boolean on = true;

    private static final double DEADBAND = 0.001;     // rad
    private static final double LIMIT_MARGIN = 0.05;  // rad
    private static final double MAX_STEP = 0.4;       // rad: pulo maior que isso = troca de alvo, não movimento

    private final DcMotorEx m;

    private double target = 0, targetRate = 0, error = 0;
    private double lastTarget = Double.NaN;
    private long lastTime = 0;
    private boolean usingA = true, reachable = true;

    // velocidade do robô estimada pela própria odometria (pol/s, campo)
    private double robotVx = 0, robotVy = 0;
    private double lastPoseX = Double.NaN, lastPoseY = Double.NaN;
    private long lastPoseTime = 0;

    // distância usada no último aim() — é ESSA que o shooter deve usar
    private double shotDistance = Double.NaN;

    public Turret(HardwareMap hw) {
        m = hw.get(DcMotorEx.class, "t");
        m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        m.setDirection(DcMotorSimple.Direction.REVERSE);
        m.setPower(0);
    }

    // ================= Mira =================

    /**
     * Chame UMA vez por loop. Mira no hive ativo e calcula a distância do tiro.
     * A velocidade do robô é estimada aqui mesmo a partir da pose.
     */
    public void aim(Pose robot) {
        updateRobotVelocity(robot);
        aimInternal(robot, robotVx, robotVy);
    }

    /** Mesma coisa, mas usando a velocidade que você já tem (pol/s, referencial do campo). */
    public void aim(Pose robot, Vector2D velocity) {
        updateRobotVelocity(robot); // mantém o histórico em dia
        if (velocity == null) aimInternal(robot, 0, 0);
        else aimInternal(robot, velocity.x(), velocity.y());
    }

    private void aimInternal(Pose robot, double vx, double vy) {
        updateSide(robot.y());
        Pose goal = getGoalPose();

        // onde o ROBÔ vai estar quando a bola chegar (pose da odometria + velocidade * TOF)
        double t = Math.max(0, TOF);
        Pose shotFrom = new Pose(robot.x() + vx * t, robot.y() + vy * t, robot.heading());

        // 3) distância e mira saem DO MESMO PONTO
        shotDistance = Math.hypot(goal.x() - shotFrom.x(), goal.y() - shotFrom.y());
        face(goal, shotFrom);
    }

    /** Aponta para targetPose estando em robotPose. */
    public void face(Pose targetPose, Pose robotPose) {
        double angleToTarget = Math.atan2(
                targetPose.y() - robotPose.y(),
                targetPose.x() - robotPose.x());
        setYaw(angleToTarget - robotPose.heading());
    }

    /** Onde o robô vai estar quando a bola chegar. */
    public static Pose getPredictedPose(Pose current, Vector2D velocity) {
        if (velocity == null || TOF <= 0) return current;
        return new Pose(
                current.x() + velocity.x() * TOF,
                current.y() + velocity.y() * TOF,
                current.heading());
    }

    private void updateRobotVelocity(Pose robot) {
        long now = System.nanoTime();
        if (!Double.isNaN(lastPoseX)) {
            double dt = (now - lastPoseTime) * 1e-9;
            if (dt > 1e-3 && dt < 0.5) {
                double vx = (robot.x() - lastPoseX) / dt;
                double vy = (robot.y() - lastPoseY) / dt;
                robotVx += ROBOT_VEL_FILTER * (vx - robotVx);
                robotVy += ROBOT_VEL_FILTER * (vy - robotVy);
            }
        }
        lastPoseX = robot.x();
        lastPoseY = robot.y();
        lastPoseTime = now;
    }

    public void setYaw(double radians) {
        radians = normalizeAngle(radians);
        reachable = Math.abs(radians) <= LIMIT;
        double newTarget = clamp(radians, -LIMIT, LIMIT);

        long now = System.nanoTime();
        if (!Double.isNaN(lastTarget)) {
            double dt = (now - lastTime) * 1e-9;
            double step = newTarget - lastTarget;
            double rate = (dt > 1e-3 && Math.abs(step) < MAX_STEP) ? step / dt : 0;
            targetRate += 0.5 * (rate - targetRate);
        }
        if (!reachable) targetRate = 0;

        lastTarget = newTarget;
        lastTime = now;
        target = newTarget;
    }

    // ================= Controle =================

    /** Chame uma vez por loop, depois do aim(). */
    public void periodic() {
        double angle = getAngle();
        double vel = getVelocity();
        error = target - angle;

        if (!on) { m.setPower(0); return; }

        double u = kP * error
                + kD * (targetRate - vel)
                + kV * targetRate;
        if (Math.abs(error) > DEADBAND) u += kS * Math.signum(error);

        if (angle >= LIMIT + LIMIT_MARGIN && u > 0) u = 0;
        if (angle <= -LIMIT - LIMIT_MARGIN && u < 0) u = 0;

        m.setPower(clamp(u, -1, 1));
    }

    // ================= Hives =================

    private void updateSide(double y) {
        if (usingA && y < SPLIT_Y - HYSTERESIS) usingA = false;
        else if (!usingA && y > SPLIT_Y + HYSTERESIS) usingA = true;
    }

    public void setHiveA(Pose p) { hiveA = p; }
    public void setHiveB(Pose p) { hiveB = p; }
    public void setHives(Pose a, Pose b) { hiveA = a; hiveB = b; }

    public boolean isUsingA() { return usingA; }
    public Pose getGoalPose() { return usingA ? hiveA : hiveB; }

    /** Distância do tiro calculada no último aim() (pivô previsto -> hive ativo). USE ESTA no shooter. */
    public double getShotDistance() { return shotDistance; }

    /** Distância "crua" do robô (sem previsão) até o hive ativo. */
    public double distanceToGoal(Pose robot) {
        updateSide(robot.y());
        Pose g = getGoalPose();
        return Math.hypot(g.x() - robot.x(), g.y() - robot.y());
    }

    public double getRobotVx() { return robotVx; }
    public double getRobotVy() { return robotVy; }

    public double getSplitY() { return SPLIT_Y; }
    public double sideOf(double x, double y) { return y - SPLIT_Y; }

    // ================= Leituras =================

    public double getAngle() { return m.getCurrentPosition() * RPT; }
    public double getVelocity() { return m.getVelocity() * RPT; }
    public double getYaw() { return getAngle(); }
    public double getTargetAngle() { return target; }
    public double getError() { return error; }
    public double getErrorDeg() { return Math.toDegrees(error); }
    public boolean isReachable() { return reachable; }
    public boolean isReady() { return reachable && Math.abs(getErrorDeg()) < READY_DEG; }

    public void resetTurret() {
        m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        target = 0; targetRate = 0; error = 0; lastTarget = Double.NaN;
        robotVx = 0; robotVy = 0; lastPoseX = Double.NaN; lastPoseY = Double.NaN;
        shotDistance = Double.NaN;
    }

    public void on() { on = true; }
    public void off() { on = false; m.setPower(0); }

    // ================= Utilidades =================

    public static double normalizeAngle(double a) {
        a = a % (Math.PI * 2);
        if (a <= -Math.PI) a += Math.PI * 2;
        if (a > Math.PI) a -= Math.PI * 2;
        return a;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}