package Subsystem;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class Shooter {
    public DcMotorEx shooter;
    public Servo gate, trava, hood;
    private final VoltageSensor battery;

    public static double minVel = 0, maxVel = 1100;

    public static double minHood = 0.4;
    public static double maxHood = 0.69;

    public static double POLY_MIN_DIST = 30, POLY_MAX_DIST = 65;

    public static double DIST_FILTER = 0.3;

    public static double HOOD_DEADBAND = 0.01;

    public static double HOOD_DIST_STEP = 3.0;


    public static double kS = 0.1;
    /** Potência por tick/s. Ex.: 1100 ticks/s * 0.0004 = 0.44 de potência. */
    public static double kV = 0.00059
            ;
    /** Potência extra por tick/s de erro (recupera depois do tiro). */
    public static double kP = 2;
    /** Derivativo (opcional, deixe 0 até o resto estar bom). */
    public static double kD = 0.0;

    /** Compensa a tensão da bateria (a potência é escalada para NOMINAL_VOLTAGE). */
    public static boolean VOLTAGE_COMP = true;
    public static double NOMINAL_VOLTAGE = 12.0;
    /** Ler a tensão é lento, então só lê a cada X ms. */
    public static double VOLTAGE_READ_MS = 250;

    public static double VEL_TOL = 40;   // ticks/s: erro aceitável para considerar "na velocidade"

    public static double vel = 0;        // para testar pelo Panels
    public static double posGate = 0;
    public static double posTrava = 0.35;
    public static double posTravac = 0.0;
    public static double posGateC = 0.25;

    // estado do controlador
    private double targetVel = 0;
    private double lastError = 0;
    private double lastPower = Double.NaN;
    private double voltage = 12.0;
    private final ElapsedTime loopTimer = new ElapsedTime();
    private final ElapsedTime voltageTimer = new ElapsedTime();

    // estado do hood / mira
    private double lastHood = Double.NaN;
    private double distance = Double.NaN;
    private double hoodDist = Double.NaN;

    public Shooter(HardwareMap hMap) {
        shooter = hMap.get(DcMotorEx.class, "shooter");
        gate = hMap.get(Servo.class, "gate");
        trava = hMap.get(Servo.class, "trava");
        hood = hMap.get(Servo.class, "hood");
        battery = hMap.voltageSensor.iterator().next();

        shooter.setDirection(DcMotorSimple.Direction.REVERSE);
        // IMPORTANTE: RUN_WITHOUT_ENCODER desliga o PID interno do hub.
        // O encoder continua funcionando (getVelocity() funciona normalmente).
        shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        hood.setDirection(Servo.Direction.REVERSE);

        voltage = battery.getVoltage();
        voltageTimer.reset();
        loopTimer.reset();
    }

    // ================= Uso principal =================
    /** Chame uma vez por loop com a distância. Já chama update(). */
    public void aim(double dist) {
        if (Double.isNaN(distance)) distance = dist;
        else distance += DIST_FILTER * (dist - distance);

        setVelocity(flywheelSpeed(distance));

        if (Double.isNaN(hoodDist) || Math.abs(distance - hoodDist) >= HOOD_DIST_STEP) {
            setHood(hoodAngle(distance));
            hoodDist = distance;
        }

        update();
    }

    /** No autônomo: chame onAuto() e depois update() TODO loop. */
    public void onAuto() {
        setVelocity(960);
        setHood(0.5);
    }

    /** Pronto para atirar: flywheel dentro da tolerância da velocidade pedida. */
    public boolean atSpeed() {
        return targetVel > 0 && Math.abs(getVel() - targetVel) < VEL_TOL;
    }

    // ================= Controlador =================
    /** Só define o alvo. Quem aplica a potência é o update(). */
    public void setVelocity(double v) {
        targetVel = Math.max(0, v);
    }

    /** Calcula e aplica a potência. PRECISA ser chamado uma vez por loop. */
    public void update() {
        double dt = loopTimer.seconds();
        loopTimer.reset();

        if (targetVel <= 0) {
            lastError = 0;
            writePower(0);
            return;
        }

        if (VOLTAGE_COMP && voltageTimer.milliseconds() >= VOLTAGE_READ_MS) {
            voltage = battery.getVoltage();
            voltageTimer.reset();
        }

        double error = targetVel - getVel();
        double derivative = (dt > 1e-4) ? (error - lastError) / dt : 0;
        lastError = error;

        // Feedforward: antecipa a potência necessária
        double ff = kS + kV * targetVel;
        // Feedback: corrige pelo erro
        double fb = kP * error + kD * derivative;

        double power = ff + fb;

        if (VOLTAGE_COMP && voltage > 1) {
            power *= NOMINAL_VOLTAGE / voltage;
        }

        // Nunca roda ao contrário (frear o flywheel com potência negativa só gasta motor)
        power = Math.max(0, Math.min(1, power));
        writePower(power);
    }

    /** Evita escrever no hub se a potência praticamente não mudou. */
    private void writePower(double power) {
        if (Double.isNaN(lastPower) || Math.abs(power - lastPower) > 0.001) {
            shooter.setPower(power);
            lastPower = power;
        }
    }

    // ================= Hood =================
    public void setHood(double servoPos) {
        double p = Math.max(minHood, Math.min(maxHood, servoPos));
        if (Double.isNaN(lastHood) || Math.abs(p - lastHood) >= HOOD_DEADBAND) {
            hood.setPosition(p);
            lastHood = p;
        }
    }

    public void testHood(double targetServo) { setHood(targetServo); }

    public void angle(double dist) { setHood(hoodAngle(dist)); }

    public double getVel() { return shooter.getVelocity(); }
    public double getTargetVel() { return targetVel; }
    public double getDistance() { return distance; }
    public double getHood() { return lastHood; }
    public double getPower() { return Double.isNaN(lastPower) ? 0 : lastPower; }
    public double getVoltage() { return voltage; }

    public void stop() {
        targetVel = 0;
        lastError = 0;
        distance = Double.NaN;
        hoodDist = Double.NaN;
        writePower(0);
    }

    // ================= Gate / trava =================
    public void open() {
        gate.setPosition(0);
        trava.setPosition(0.35);
    }

    public void close() {
        gate.setPosition(0.26);
        trava.setPosition(0.06);
    }

    // ================= Calibração =================
    private static double clampDist(double dist) {
        return Math.max(POLY_MIN_DIST, Math.min(POLY_MAX_DIST, dist));
    }

    public static double hoodAngle(double dist) {
        double x = clampDist(dist);

        double h = 0.000291719 * Math.pow(x, 2)
                - 0.0471096   * x
                + 2.2853;

        return Math.max(minHood, Math.min(maxHood, h));
    }

//    public static double VEL_OFFSET = -8;

    public static double flywheelSpeed(double dist) {
        double x = clampDist(dist);

        double v = 0.000056006 * Math.pow(x, 4)
                - 0.0156865   * Math.pow(x, 3)
                + 1.60228     * Math.pow(x, 2)
                - 67.07099    * x
                + 1878.80529;

        return Math.max(minVel, Math.min(maxVel, v));
    }
}