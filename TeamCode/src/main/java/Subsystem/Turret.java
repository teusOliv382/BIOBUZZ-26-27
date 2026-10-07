package Subsystem;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Configurable
public class Turret {

    // ---------- Encoder / limites ----------
    public static double RPT = 0.0040992;            // rad por tick (2*PI / ticks por volta da torreta)
    public static double MIN_RAD = -3.0, MAX_RAD = 3.0;
    public static double LIMIT_MARGIN = 0.05;        // rad: passou do limite -> só aceita potência de volta

    // ---------- Controle ----------
    public static double kP = 1.2;       // potência por rad de erro
    public static double kI = 0.0;       // só atua perto do alvo (I_ZONE) e com alvo parado
    public static double I_ZONE = 0.05;  // rad
    public static double I_MAX = 0.15;   // potência máxima do termo integral
    public static double kD = 0.03;      // potência por rad/s de erro de velocidade
    public static double kV = 0.0;       // potência por rad/s (~ 1 / velocidade máxima)
    public static double kS = 0.05;      // vence atrito estático
    public static double DEADBAND = 0.001; // rad
    public static boolean ENABLED = true;  // chave do painel (o liga/desliga do código é por instância)

    // ---------- Geometria ----------
    // Alvos no campo (polegadas, mesmo referencial do Pedro)
    public static double AX = -4, AY = -50;
    public static double BX = -4, BY = -80;
    // Ajuste fino por alvo (rad). Positivo = gira no sentido anti-horário.
    public static double TRIM_A = 0.0, TRIM_B = 0.0;
    // Eixo da torreta em relação ao centro do robô do Pedro (referencial do robô: X frente, Y esquerda)
    public static double OFFSET_X = -2.008, OFFSET_Y = 0.0;
    // Linha que divide as duas metades + histerese para não ficar trocando de alvo em cima da linha
    public static double SPLIT_Y =-70;
    public static double HYSTERESIS = 3.0;

    // ---------- Compensação de movimento ----------
    public static boolean COMPENSATE = true;
    public static double LATENCY = 0.04;                         // s (sensor -> motor)
    public static double TOF_BASE = 0.15, TOF_PER_INCH = 0.004;  // tempo de voo = base + k*dist (CALIBRE)
    public static int ITERATIONS = 4;
    public static double OMEGA_FILTER = 0.5;                     // 0..1 (1 = sem filtro)

    // ---------- Pronto para atirar ----------
    public static double TARGET_HALF_WIDTH = 5.0;  // pol, meia largura efetiva do alvo
    public static double READY_VEL_TOL = 0.6;      // rad/s de erro de velocidade aceitável
    public static double READY_TIME = 0.05;        // s que precisa ficar dentro da tolerância

    private final DcMotorEx m;

    private boolean enabled = true;
    private boolean usingA = true;
    private boolean reachable = true;
    private double targetRad = 0, targetVel = 0, error = 0, trim = 0;
    private double distance = 1e9;   // "infinito" até o primeiro aim(): nunca fica pronto sem mira
    private double integral = 0;

    // leituras do loop atual
    private double angle = 0, angVel = 0;

    // estimativa de velocidade angular do robô
    private double lastHeading = 0, omega = 0;
    private long lastNanos = 0;
    private boolean first = true;

    private long lastPeriodicNanos = 0;
    private long readySinceNanos = -1;

    public Turret(HardwareMap hw) {
        m = hw.get(DcMotorEx.class, "t");
        m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        m.setDirection(DcMotorSimple.Direction.REVERSE);
        m.setPower(0);
    }

    // ================= Medição =================
    /** Ângulo cru em rad (sem normalizar). Zero = posição do reset do encoder (torreta olhando para a frente do robô). */
    public double getAngle() { return m.getCurrentPosition() * RPT; }
    public double getVelocity() { return m.getVelocity() * RPT; }

    // ================= Mira =================
    /** Chame uma vez por loop, depois do follower.update() e antes do periodic(). */
    public void aim(Pose pose, Vector2D vel) {
        aim(pose, vel.x(), vel.y(), Double.NaN);
    }

    /**
     * vx, vy: velocidade do robô no referencial do CAMPO (pol/s).
     * omegaRobot: velocidade angular do robô (rad/s). Passe Double.NaN para estimar pelo heading.
     */
    public void aim(Pose pose, double vx, double vy, double omegaRobot) {
        updateOmega(pose.heading());
        double wIn = Double.isNaN(omegaRobot) ? omega : omegaRobot;

        if (!COMPENSATE) { vx = 0; vy = 0; }
        double w   = COMPENSATE ? wIn : 0;
        double lat = COMPENSATE ? LATENCY : 0;

        // 1) pose prevista (compensa a latência)
        double h  = pose.heading() + w * lat;
        double px = pose.x() + vx * lat;
        double py = pose.y() + vy * lat;

        // 2) escolhe o alvo pela metade do campo, com histerese
        if (usingA && py < SPLIT_Y - HYSTERESIS) usingA = false;
        else if (!usingA && py > SPLIT_Y + HYSTERESIS) usingA = true;
        double gx = usingA ? AX : BX, gy = usingA ? AY : BY;
        double targetTrim = usingA ? TRIM_A : TRIM_B;

        // 3) posição e velocidade do EIXO da torreta no campo
        double c = Math.cos(h), s = Math.sin(h);
        double ox = OFFSET_X * c - OFFSET_Y * s;
        double oy = OFFSET_X * s + OFFSET_Y * c;
        double tx = px + ox, ty = py + oy;
        double tvx = vx - w * oy;
        double tvy = vy + w * ox;

        // 4) alvo virtual (compensa a velocidade do robô durante o voo da bola)
        double vtx = gx, vty = gy;
        if (COMPENSATE) {
            for (int i = 0; i < ITERATIONS; i++) {
                double d = Math.hypot(vtx - tx, vty - ty);
                double tof = TOF_BASE + TOF_PER_INCH * d;
                vtx = gx - tvx * tof;
                vty = gy - tvy * tof;
            }
        }
        double dx = vtx - tx, dy = vty - ty;
        distance = Math.max(Math.hypot(dx, dy), 1e-6);

        // 5) ângulo no referencial do robô + feedforward de velocidade
        double desired = Math.atan2(dy, dx) - h + trim + targetTrim;
        double fieldRate = (dy * tvx - dx * tvy) / (distance * distance);
        double ffVel = fieldRate - w;

        setTarget(desired, ffVel);
    }

    private void updateOmega(double heading) {
        long now = System.nanoTime();
        if (first) { lastHeading = heading; lastNanos = now; first = false; return; }
        double dt = (now - lastNanos) * 1e-9;
        if (dt > 1e-4) {
            double dh = wrap(heading - lastHeading);
            omega += OMEGA_FILTER * (dh / dt - omega);
            lastHeading = heading;
            lastNanos = now;
        }
    }

    private void setTarget(double desiredRad, double velRadPerSec) {
        double cur = getAngle();
        double best = Double.NaN, bestDist = Double.MAX_VALUE;
        for (int k = -2; k <= 2; k++) {
            double cand = desiredRad + k * 2 * Math.PI;
            if (cand >= MIN_RAD && cand <= MAX_RAD) {
                double d = Math.abs(cand - cur);
                if (d < bestDist) { bestDist = d; best = cand; }
            }
        }
        reachable = !Double.isNaN(best);
        if (!reachable) {
            // Alvo na zona morta (atrás dos limites): fica no limite em que a torreta JÁ está,
            // em vez de dar a volta inteira e voltar quando o alvo cruzar a zona morta.
            best = (Math.abs(cur - MAX_RAD) <= Math.abs(cur - MIN_RAD)) ? MAX_RAD : MIN_RAD;
            velRadPerSec = 0;
        }
        targetRad = best;
        targetVel = velRadPerSec;
    }

    /** Chame uma vez por loop, depois do aim(). */
    public void periodic() {
        long now = System.nanoTime();
        double dt = lastPeriodicNanos == 0 ? 0 : (now - lastPeriodicNanos) * 1e-9;
        lastPeriodicNanos = now;

        // lê o encoder UMA vez por loop
        angle = getAngle();
        angVel = getVelocity();
        error = targetRad - angle;

        if (!ENABLED || !enabled) {
            m.setPower(0);
            integral = 0;
            readySinceNanos = -1;
            return;
        }

        // integral só perto do alvo e com o alvo parado (evita overshoot nas viradas rápidas)
        if (kI != 0 && Math.abs(error) < I_ZONE && Math.abs(targetVel) < 0.2) {
            integral = clamp(integral + kI * error * dt, -I_MAX, I_MAX);
        } else {
            integral = 0;
        }

        double u = kP * error
                + kD * (targetVel - angVel)
                + kV * targetVel
                + integral;

        if (Math.abs(error) > DEADBAND) {
            u += kS * Math.signum(Math.abs(targetVel) > 0.05 ? targetVel : error);
        }

        // proteção de limite: passou do limite, só deixa voltar
        if (angle >= MAX_RAD + LIMIT_MARGIN && u > 0) u = 0;
        if (angle <= MIN_RAD - LIMIT_MARGIN && u < 0) u = 0;

        m.setPower(clamp(u, -1, 1));

        // "pronto" = alvo alcançável + erro dentro da largura do alvo + velocidade casada, por READY_TIME
        boolean inTol = reachable
                && Math.abs(error) < getTolerance()
                && Math.abs(targetVel - angVel) < READY_VEL_TOL;
        if (inTol) { if (readySinceNanos < 0) readySinceNanos = now; }
        else readySinceNanos = -1;
    }

    // ================= Utilidades =================
    public boolean isReady() {
        return readySinceNanos >= 0
                && (System.nanoTime() - readySinceNanos) * 1e-9 >= READY_TIME;
    }

    /** Erro angular máximo (rad) para a bola ainda entrar no alvo. */
    public double getTolerance() { return Math.atan2(TARGET_HALF_WIDTH, distance); }

    public void setTrim(double rad) { trim = rad; }          // ajuste geral / correção por visão
    public double getError() { return error; }
    public double getTargetAngle() { return targetRad; }
    public double getTargetVelocity() { return targetVel; }
    /** Distância até o alvo VIRTUAL (já compensado). Use para escolher a velocidade do shooter. */
    public double getDistance() { return distance; }
    public boolean isUsingA() { return usingA; }
    public boolean isReachable() { return reachable; }
    public double getOmega() { return omega; }

    public void resetTurret() {
        m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        targetRad = 0; targetVel = 0; error = 0; integral = 0;
        readySinceNanos = -1;
    }

    public void off() { enabled = false; m.setPower(0); }
    public void on() { enabled = true; }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double wrap(double a) {
        a = a % (2 * Math.PI);
        if (a <= -Math.PI) a += 2 * Math.PI;
        if (a > Math.PI) a -= 2 * Math.PI;
        return a;
    }
}