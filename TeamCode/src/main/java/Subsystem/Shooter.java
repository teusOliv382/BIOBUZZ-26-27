package Subsystem;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class Shooter {
    public DcMotorEx shooter;

    private final VoltageSensor battery;

    public static double minVel = 0, maxVel = 1100;

    public static double POLY_MIN_DIST = 0, POLY_MAX_DIST = 80;

    public static double DIST_FILTER = 0.3;
    public static double MIN_VALID_DIST = 5, MAX_VALID_DIST = 150;

    public static double testShooter1 = 0;

    public static double kS = 0.09;
    public static double kV = 0.0006;
    public static double kP = 0.1;
    public static double kD = 0.0;

    public static boolean VOLTAGE_COMP = true;
    public static double NOMINAL_VOLTAGE = 12.0;
    public static double VOLTAGE_READ_MS = 250;

    public static double VEL_TOL = 40;

    public static double vel = 0;

    private double targetVel = 0;
    private double lastError = 0;
    private double lastPower = Double.NaN;
    private double voltage = 12.0;
    private final ElapsedTime loopTimer = new ElapsedTime();
    private final ElapsedTime voltageTimer = new ElapsedTime();

    private double distance = Double.NaN;

    public Shooter(HardwareMap hMap) {
        shooter = hMap.get(DcMotorEx.class, "shooter");
        battery = hMap.voltageSensor.iterator().next();

        shooter.setDirection(DcMotorSimple.Direction.FORWARD);
        shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        voltage = battery.getVoltage();
        voltageTimer.reset();
        loopTimer.reset();
    }

    public void aim(double dist) {
        if (isValidDist(dist)) {
            if (Double.isNaN(distance)) distance = dist;
            else distance += DIST_FILTER * (dist - distance);
        }

        if (!Double.isNaN(distance)) setVelocity(flywheelSpeed(distance));

        update();
    }

    public void onAuto() {
        setVelocity(980);
    }

    public boolean atSpeed() {
        return targetVel > 0 && Math.abs(getVel() - targetVel) < VEL_TOL;
    }

    public void setVelocity(double v) {
        targetVel = Math.max(0, v);
    }

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

        double power = kS + kV * targetVel + kP * error + kD * derivative;

        if (VOLTAGE_COMP && voltage > 1) {
            power *= NOMINAL_VOLTAGE / voltage;
        }

        power = Math.max(0, Math.min(1, power));
        writePower(power);
    }

    private void writePower(double power) {
        if (Double.isNaN(lastPower) || Math.abs(power - lastPower) > 0.001) {
            shooter.setPower(power);
            lastPower = power;
        }
    }

    private static boolean isValidDist(double d) {
        return !Double.isNaN(d) && !Double.isInfinite(d)
                && d >= MIN_VALID_DIST && d <= MAX_VALID_DIST;
    }

    public double getVel() { return shooter.getVelocity(); }
    public double getTargetVel() { return targetVel; }
    public double getDistance() { return distance; }
    public double getPower() { return Double.isNaN(lastPower) ? 0 : lastPower; }
    public double getVoltage() { return voltage; }

    public void stop() {
        targetVel = 0;
        lastError = 0;
        distance = Double.NaN;
        writePower(0);
    }

    private static double clampDist(double dist) {
        return Math.max(POLY_MIN_DIST, Math.min(POLY_MAX_DIST, dist));
    }

    public static double flywheelSpeed(double dist) {
        double x = clampDist(dist);

        double v = -0.0000757576 * Math.pow(x, 4)
                + 0.015303     * Math.pow(x, 3)
                - 1.07841      * Math.pow(x, 2)
                + 33.56656     * x
                + 554.43182;

        return Math.max(minVel, Math.min(maxVel, v));
    }

    public void shooter1() {
        setVelocity(testShooter1);
        update();
    }
}