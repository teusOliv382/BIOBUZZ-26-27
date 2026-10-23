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
    public DcMotorEx shooter, shooter2;
    public Servo gate, trava, hood;
    private Servo led;

    private final VoltageSensor battery;

    public static double minVel = 0, maxVel = 1100;

    /** Limites do hood (valor enviado ao setPosition). NUNCA passa disso. */
    public static double minHood = 0.45;
    public static double maxHood = 0.8;

    public static double POLY_MIN_DIST = 0, POLY_MAX_DIST = 80;

    /** Filtro da distância usada no flywheel (0..1, maior = reage mais rápido). */
    public static double DIST_FILTER = 0.3;

    // ================= Anti-oscilação do hood =================
    /** Filtro separado (mais lento) só para o hood. */
    public static double HOOD_FILTER = 0.12;
    /** O hood só começa a se mexer se o alvo estiver mais longe que isso. */
    public static double HOOD_DEADBAND = 0.015;
    /** Quando já está se mexendo, para quando chegar perto assim do alvo. */
    public static double HOOD_SETTLE = 0.003;
    /** Velocidade máxima do hood (posição de servo por segundo). */
    public static double HOOD_MAX_RATE = 0.8;
    /** Leituras de distância fora disso são ignoradas (alvo perdido, 0, lixo). */
    public static double MIN_VALID_DIST = 5, MAX_VALID_DIST = 150;

    public static double testShooter1 = 0;
    public static double testShooter2 = 0;

    public static double kS = 0.09;
    /** Potência por tick/s. Ex.: 1100 ticks/s * 0.0004 = 0.44 de potência. */
    public static double kV = 0.0006;
    /** Potência extra por tick/s de erro (recupera depois do tiro). */
    public static double kP = 0.1;
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
    private double distance = Double.NaN;      // distância filtrada (flywheel)
    private double hoodDist = Double.NaN;      // distância filtrada (hood, mais lenta)
    private double hoodCmd = Double.NaN;       // posição atual comandada no servo
    private boolean hoodMoving = false;
    private final ElapsedTime hoodTimer = new ElapsedTime();

    public Shooter(HardwareMap hMap) {
        shooter = hMap.get(DcMotorEx.class, "shooter");
        shooter2 = hMap.get(DcMotorEx.class, "shooter2");
        gate = hMap.get(Servo.class, "gate");
        trava = hMap.get(Servo.class, "trava");
        hood = hMap.get(Servo.class, "hood");
        battery = hMap.voltageSensor.iterator().next();

        led = hMap.get(Servo.class, "RGB");

        shooter.setDirection(DcMotorSimple.Direction.REVERSE);
        // IMPORTANTE: RUN_WITHOUT_ENCODER desliga o PID interno do hub.
        // O encoder continua funcionando (getVelocity() funciona normalmente).
        shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        shooter2.setDirection(DcMotorSimple.Direction.FORWARD);
        shooter2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // ATENÇÃO: com REVERSE, setPosition(p) vira 1 - p no servo físico.
        // minHood/maxHood valem para o valor do CÓDIGO (0.45..0.67 => físico 0.33..0.55).
        hood.setDirection(Servo.Direction.REVERSE);

        voltage = battery.getVoltage();
        voltageTimer.reset();
        loopTimer.reset();
        hoodTimer.reset();
    }

    // ================= Uso principal =================
    /** Chame UMA vez por loop com a distância. Já chama update(). */
    public void aim(double dist) {
        if (isValidDist(dist)) {
            if (Double.isNaN(distance)) distance = dist;
            else distance += DIST_FILTER * (dist - distance);

            if (Double.isNaN(hoodDist)) hoodDist = dist;
            else hoodDist += HOOD_FILTER * (dist - hoodDist);
        }
        // Leitura inválida: mantém a última distância boa (não mexe em nada).

        if (!Double.isNaN(distance)) setVelocity(flywheelSpeed(distance));

        updateHood();
        update();
    }

    /** No autônomo: chame onAuto() e depois update() TODO loop. */
    public void onAuto() {
        setVelocity(980);
        setHood(0.60);   // 0.4 seria cortado pelo minHood de qualquer jeito
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

        double ff = kS + kV * targetVel;
        double fb = kP * error + kD * derivative;

        double power = ff + fb;

        if (VOLTAGE_COMP && voltage > 1) {
            power *= NOMINAL_VOLTAGE / voltage;
        }

        power = Math.max(0, Math.min(1, power));
        writePower(power);
    }

    /** Evita escrever no hub se a potência praticamente não mudou. */
    private void writePower(double power) {
        if (Double.isNaN(lastPower) || Math.abs(power - lastPower) > 0.001) {
            shooter.setPower(power);
            shooter2.setPower(power);
            lastPower = power;
        }
    }

    // ================= Hood =================
    /**
     * Move o hood em direção ao ângulo da distância:
     *  - só começa a mexer se o erro passar de HOOD_DEADBAND (histerese)
     *  - anda no máximo HOOD_MAX_RATE por segundo (sem pulo)
     *  - para quando chega a HOOD_SETTLE do alvo
     *  - SEMPRE preso entre minHood e maxHood
     */
    private void updateHood() {
        double dt = Math.min(hoodTimer.seconds(), 0.1);
        hoodTimer.reset();

        if (Double.isNaN(hoodDist)) return;

        double target = clampHood(hoodAngle(hoodDist));

        if (Double.isNaN(hoodCmd)) {          // primeira vez: vai direto
            writeHood(target);
            return;
        }

        double err = target - hoodCmd;

        if (!hoodMoving && Math.abs(err) >= HOOD_DEADBAND) hoodMoving = true;
        if (hoodMoving && Math.abs(err) <= HOOD_SETTLE) hoodMoving = false;
        if (!hoodMoving) return;              // parado: segura a posição

        double maxStep = HOOD_MAX_RATE * dt;
        double step = Math.max(-maxStep, Math.min(maxStep, err));
        writeHood(hoodCmd + step);
    }

    /** Posição manual (teste / autônomo). Também respeita os limites. */
    public void setHood(double servoPos) {
        hoodMoving = false;
        writeHood(servoPos);
    }

    private void writeHood(double pos) {
        double p = clampHood(pos);
        if (Double.isNaN(hoodCmd) || Math.abs(p - hoodCmd) > 0.0005) {
            hood.setPosition(p);
            hoodCmd = p;
        }
    }

    private static double clampHood(double p) {
        double lo = Math.min(minHood, maxHood);
        double hi = Math.max(minHood, maxHood);
        return Math.max(lo, Math.min(hi, p));
    }

    private static boolean isValidDist(double d) {
        return !Double.isNaN(d) && !Double.isInfinite(d)
                && d >= MIN_VALID_DIST && d <= MAX_VALID_DIST;
    }

    public void testHood(double targetServo) { setHood(targetServo); }

    public void angle(double dist) { setHood(hoodAngle(dist)); }

    /** Velocidade lida só do encoder do primeiro motor ("shooter"). */
    public double getVel() { return shooter.getVelocity(); }
    public double getTargetVel() { return targetVel; }
    public double getDistance() { return distance; }
    public double getHood() { return Double.isNaN(hoodCmd) ? 0 : hoodCmd; }
    public double getHoodTarget() { return Double.isNaN(hoodDist) ? 0 : clampHood(hoodAngle(hoodDist)); }
    public double getPower() { return Double.isNaN(lastPower) ? 0 : lastPower; }
    public double getVoltage() { return voltage; }

    public void stop() {
        targetVel = 0;
        lastError = 0;
        distance = Double.NaN;
        hoodDist = Double.NaN;   // hood fica parado onde está
        hoodMoving = false;
        writePower(0);
    }

    // ================= Gate / trava =================
    public void open() {
        gate.setPosition(0);
        trava.setPosition(0.35);
        blue();
    }

    public void close() {
        gate.setPosition(0.26);
        trava.setPosition(0.06);
        violet();
    }

    // ================= Calibração =================
    private static double clampDist(double dist) {
        return Math.max(POLY_MIN_DIST, Math.min(POLY_MAX_DIST, dist));
    }

    public static double hoodAngle(double dist) {
        double x = clampDist(dist);

        double h = 0.000140476 * Math.pow(x, 2)
                - 0.0218214   * x
                + 1.33625;

        return Math.max(minHood, Math.min(maxHood, h));
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
        shooter.setVelocity(testShooter1);
    }

    public void shoter2() {
        shooter2.setVelocity(testShooter2);   // antes estava usando "shooter"
    }

    public void violet() { led.setPosition(0.722); }
    public void blue()   { led.setPosition(0.611); }
    public void azure()  { led.setPosition(0.555); }
}