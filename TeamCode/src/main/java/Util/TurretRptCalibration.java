//package Util; // ajuste para o package da pasta onde voce colocar o arquivo
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.org.firstinspires.ftc.teamcode.OpModes.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorEx;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//
///**
// * Calibracao do RPT (radianos por tick) da torreta.
// *
// * Metodo: zera o encoder com a torreta olhando para a FRENTE do robo, leva a torreta
// * a +REF graus (esquerda / anti-horario) e a -REF graus (direita / horario), le os ticks
// * e calcula  RPT = (2 * REF_rad) / (ticks(+REF) - ticks(-REF)).
// * Usar os dois lados cancela erro de zero. Cada par medido entra numa media.
// *
// * CONTROLES (gamepad1):
// *   A            zera o encoder (torreta apontando EXATAMENTE para a frente do robo)
// *   Stick esq X  move a torreta devagar (ou empurre com a mao: o motor fica solto em 0)
// *   X            captura a posicao +REF (esquerda, anti-horario visto de cima)
// *   Y            captura a posicao -REF (direita, horario)
// *   D-pad cima/baixo   REF +/- 5 graus     D-pad dir/esq   REF +/- 1 grau
// *   B            apaga as medidas
// */
//@org.firstinspires.ftc.teamcode.OpModes.TeleOp(name = "Turret RPT Calibration", group = "Calibracao")
//public class TurretRptCalibration extends LinearOpMode {
//
//    // Valor atual (copie de Turret.RPT) para comparar
//    static final double CURRENT_RPT = 0.0040992;
//
//    // Calculo teorico (AJUSTE para o seu mecanismo)
//    static final double MOTOR_CPR = 537.7;  // ticks por volta do eixo de saida do motor
//    static final double GEAR_RATIO = 2.85;  // voltas do motor por 1 volta da torreta (ex.: 114T / 40T)
//
//    static final double MAX_MANUAL_POWER = 0.25; // limite de potencia no controle manual
//
//    @Override
//    public void runOpMode() {
//        DcMotorEx m = hardwareMap.get(DcMotorEx.class, "t");
//        m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
//        m.setDirection(DcMotorSimple.Direction.REVERSE); // mesma direcao do Turret.java
//        m.setPower(0);
//
//        final double theoryRpt = 2 * Math.PI / (MOTOR_CPR * GEAR_RATIO);
//
//        boolean lastA = false, lastB = false, lastX = false, lastY = false;
//        boolean lastUp = false, lastDown = false, lastLeft = false, lastRight = false;
//
//        double refDeg = 90.0;
//        int posTicks = 0, negTicks = 0;
//        boolean hasPos = false, hasNeg = false;
//        double sum = 0, lastPair = 0;
//        int count = 0;
//        boolean signWarn = false;
//
//        telemetry.addLine("Aponte a torreta para a FRENTE do robo, de START e aperte A para zerar.");
//        telemetry.update();
//        waitForStart();
//
//        while (opModeIsActive()) {
//            // movimento manual lento (esquerda = positivo)
//            m.setPower(-gamepad1.left_stick_x * MAX_MANUAL_POWER);
//
//            int ticks = m.getCurrentPosition();
//
//            // ---- botoes (detecao de borda) ----
//            if (gamepad1.a && !lastA) {
//                m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//                m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//                hasPos = false; hasNeg = false;
//            }
//            if (gamepad1.b && !lastB) {
//                hasPos = false; hasNeg = false;
//                sum = 0; count = 0; lastPair = 0; signWarn = false;
//            }
//            if (gamepad1.x && !lastX) { posTicks = ticks; hasPos = true; }
//            if (gamepad1.y && !lastY) { negTicks = ticks; hasNeg = true; }
//            if (gamepad1.dpad_up && !lastUp) refDeg += 5;
//            if (gamepad1.dpad_down && !lastDown) refDeg -= 5;
//            if (gamepad1.dpad_right && !lastRight) refDeg += 1;
//            if (gamepad1.dpad_left && !lastLeft) refDeg -= 1;
//            refDeg = Math.max(5, Math.min(170, refDeg));
//
//            lastA = gamepad1.a; lastB = gamepad1.b; lastX = gamepad1.x; lastY = gamepad1.y;
//            lastUp = gamepad1.dpad_up; lastDown = gamepad1.dpad_down;
//            lastLeft = gamepad1.dpad_left; lastRight = gamepad1.dpad_right;
//
//            // ---- par completo: calcula e acumula ----
//            if (hasPos && hasNeg) {
//                int diff = posTicks - negTicks;
//                signWarn = diff <= 0;
//                if (!signWarn) {
//                    lastPair = (2 * Math.toRadians(refDeg)) / diff;
//                    sum += lastPair;
//                    count++;
//                }
//                hasPos = false; hasNeg = false;
//            }
//
//            double avg = count > 0 ? sum / count : 0;
//
//            // ---- telemetria ----
//            telemetry.addLine("=== CALIBRACAO DO RPT ===");
//            telemetry.addData("REF (graus)", "+/- %.0f", refDeg);
//            telemetry.addData("Ticks agora", ticks);
//            telemetry.addData("Capturado +REF / -REF",
//                    "%s / %s",
//                    hasPos ? String.valueOf(posTicks) : "-",
//                    hasNeg ? String.valueOf(negTicks) : "-");
//            telemetry.addLine();
//            telemetry.addData("Angulo (RPT atual)", "%.2f deg", Math.toDegrees(ticks * CURRENT_RPT));
//            if (count > 0) {
//                telemetry.addData("Angulo (RPT medido)", "%.2f deg", Math.toDegrees(ticks * avg));
//            }
//            telemetry.addLine();
//            telemetry.addData("RPT atual", "%.7f", CURRENT_RPT);
//            telemetry.addData("RPT teorico", "%.7f", theoryRpt);
//            if (count > 0) {
//                telemetry.addData("Ultimo par", "%.7f", lastPair);
//                telemetry.addData("RPT MEDIDO (media de " + count + ")", "%.7f", avg);
//                telemetry.addData("Diferenca p/ atual", "%+.2f %%", (avg / CURRENT_RPT - 1) * 100);
//                telemetry.addData("Ticks por volta da torreta", "%.1f", 2 * Math.PI / avg);
//            }
//            if (signWarn) {
//                telemetry.addLine("ATENCAO: sinal invertido! +REF deve dar MAIS ticks que -REF. "
//                        + "Troque setDirection (FORWARD/REVERSE) ou confira qual lado e +.");
//            }
//            telemetry.update();
//        }
//        m.setPower(0);
//    }
//}