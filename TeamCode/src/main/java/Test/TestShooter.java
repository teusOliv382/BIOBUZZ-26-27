package Test;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import Subsystem.Intake;
import Subsystem.Shooter;
import Subsystem.Turret;

@Configurable
@TeleOp
public class TestShooter extends OpMode {
    Shooter shooter;
    Intake intake;
    Turret turret;
    Follower follower;

    public int intakeOn = 0;
    public boolean turretOn = true;

    public static double targetServo = 0.4;
    public static double vel = 0;

    // MESMAS posições do org.firstinspires.ftc.teamcode.OpModes.TeleOp (se mudar lá, mude aqui)
    public static Pose HIVE_A = new Pose(-3, -50);
    public static Pose HIVE_B = new Pose(-3, -70);

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        shooter = new Shooter(hardwareMap);
        intake = new Intake(hardwareMap);
        turret = new Turret(hardwareMap);

        follower.setPose(new Pose(0, 0, 0));
        turret.resetTurret();
        turret.setHives(HIVE_A, HIVE_B);
    }

    @Override
    public void loop() {
        follower.update();
        Pose robotPose = follower.pose();

        // ---------- TURRET ----------
        if (gamepad1.xWasPressed()) turretOn = !turretOn;

//        if (turretOn) {
//            turret.on();
//            turret.aim(robotPose, follower.velocity().toVector2D());
//            turret.periodic();
//        } else {
//            turret.off();
//        }

        // Distância até o hive ATIVO (A ou B), sem previsão de movimento.
        // Para calibrar o robô fica parado, então essa é a distância certa para anotar.
        double distance = turret.distanceToGoal(robotPose);

        // ---------- SHOOTER ----------
        shooter.testHood(targetServo);

        if (gamepad1.right_trigger > 0.1) {
            shooter.setVelocity(vel);
        } else {
            shooter.stop();
        }
        shooter.update();

        // ---------- INTAKE ----------
        if (gamepad1.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
        if (gamepad1.leftBumperWasPressed()) intakeOn = (intakeOn == 2) ? 0 : 2;

        if (intakeOn == 1) intake.spin();
        else if (intakeOn == 2) intake.out();
        else intake.stop();

        if (gamepad1.a) shooter.open();
        else shooter.close();

        // ---------- TELEMETRIA ----------
        telemetry.addData("hive", turret.isUsingA() ? "A" : "B");
        telemetry.addData("turret", turretOn ? "ON" : "OFF");
        telemetry.addData("erro turret (deg)", "%.1f", turret.getErrorDeg());
        telemetry.addData("turret pronta", turret.isReady());
        telemetry.addLine("--- Shooter ---");
        telemetry.addData("vel alvo / atual", "%.0f / %.0f", shooter.getTargetVel(), shooter.getVel());
        telemetry.addData("shooter pronto", shooter.atSpeed());
        telemetry.addData("hood", targetServo);
        telemetry.addLine("--- ANOTAR ---");
        telemetry.addData("dist | vel | hood", "%.1f | %.0f | %.3f", distance, vel, targetServo);
        telemetry.addData("x / y", "%.1f / %.1f", robotPose.x(), robotPose.y());
    }
}