package Subsystem;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
public class Shooter {
    public DcMotorEx shooter;
    public Servo gate, hood;

    public static double minVel = 1000, maxVel = 2200;

    public static double minHood = 0.0;
    public static double maxHood = 0.85;

    // Faixa de distância em que você CALIBROU os polinômios.
    // Fora dela o polinômio extrapola e pode dar valores absurdos, então a distância é travada aqui.
    public static double MIN_DIST = 0, MAX_DIST = 1000;

    public static double kP = 100;
    public static double kI = 0;
    public static double kD = 0;
    public static double kF = 21;

    public static double VEL_TOL = 40;   // ticks/s: erro aceitável para considerar "na velocidade"

    public static double vel = 0;        // para testar pelo Panels
    public static double pos = 0.26;
    public static double posC = 0.1;

    // últimos valores enviados (evita escrever no hub toda hora)
    private double lastP = Double.NaN, lastI, lastD, lastF;
    private double lastVel = Double.NaN, lastHood = Double.NaN;
    private double targetVel = 0;
    private double distance = 0;

    public Shooter(HardwareMap hMap) {
        shooter = hMap.get(DcMotorEx.class, "shooter");
        shooter.setDirection(DcMotorSimple.Direction.FORWARD);
        gate = hMap.get(Servo.class, "gate");
        hood = hMap.get(Servo.class, "hood");

        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        applyPIDF();
    }

    /** Só manda os coeficientes para o hub quando eles mudarem (é uma escrita lenta). */
    public void applyPIDF() {
        if (kP != lastP || kI != lastI || kD != lastD || kF != lastF) {
            shooter.setVelocityPIDFCoefficients(kP, kI, kD, kF);
            lastP = kP; lastI = kI; lastD = kD; lastF = kF;
        }
    }

    // ================= Uso principal =================
    /**
     * Chame uma vez por loop com a distância do hive ATIVO,
     * ou seja, robot.turret.getDistance(). A torreta decide o hive e o shooter só obedece.
     */
    public void aim(double dist) {
        distance = Math.max(MIN_DIST, Math.min(MAX_DIST, dist));
        setVelocity(flywheelSpeed(distance));
        setHood(hoodAngle(distance));
    }

    /** Pronto para atirar: flywheel dentro da tolerância da velocidade pedida. */
    public boolean atSpeed() {
        return targetVel > 0 && Math.abs(getVel() - targetVel) < VEL_TOL;
    }

    // ================= Baixo nível =================
    public void setVelocity(double v) {
        applyPIDF();
        targetVel = v;
        if (v != lastVel) {
            shooter.setVelocity(v);
            lastVel = v;
        }
    }

    public void setHood(double servoPos) {
        double p = Math.max(minHood, Math.min(maxHood, servoPos));
        if (p != lastHood) {
            hood.setPosition(p);
            lastHood = p;
        }
    }

    public void testHood(double targetServo) { setHood(targetServo); }

    public void angle(double dist) { setHood(hoodAngle(dist)); }

    public double getVel() { return shooter.getVelocity(); }
    public double getTargetVel() { return targetVel; }
    public double getDistance() { return distance; }

    public void stop() {
        targetVel = 0;
        lastVel = Double.NaN;
        shooter.setPower(0);
    }

    public void open() { gate.setPosition(pos); }
    public void close() { gate.setPosition(posC); }

    // ================= Calibração =================
    public static double hoodAngle(double dist) {
        double p =  0.0000166667 * Math.pow(dist, 3)
                -  0.00278571   * Math.pow(dist, 2)
                +  0.137619     * dist
                -  1.31143;

        return Math.max(minHood, Math.min(maxHood, p));
    }

    public static double flywheelSpeed(double dist) {
        double v =  0.000833333 * Math.pow(dist, 3)
                -  0.228571   * Math.pow(dist, 2)
                +  23.84524   * dist
                +  677.85714;

        return Math.max(minVel, Math.min(maxVel, v));
    }

}