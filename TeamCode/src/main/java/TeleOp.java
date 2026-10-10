import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import Subsystem.Robot;
import Subsystem.Turret;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp
public class TeleOp extends OpMode {

    public int intakeOn = 0;

    public Robot r;

    @Override
    public void init() {
        r = new Robot(hardwareMap);
        r.follower.setPose(new Pose(Robot.defaultPose.x(), Robot.defaultPose.y(), Robot.defaultPose.heading()));
        r.turret.resetTurret();
    }

    @Override
    public void loop() {
        r.update();
        Pose robotPose = r.follower.pose();

        // 2) DRIVE
        DrivePowers powers = ManualDrive.fieldCentric(
                gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                robotPose.heading()
        );r.follower.manual(powers);

        double distance = r.turret.distanceToGoal(robotPose);


            r.shooter.aim(distance);

        if (gamepad2.right_trigger > 0.1) {
            r.shooter.stop();
        }

        if (gamepad1.a) {
            r.shooter.open();
        } else {
            r.shooter.close();
        }

        if (gamepad2.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
        if (gamepad2.leftBumperWasPressed())  intakeOn = (intakeOn == 2) ? 0 : 2;

        if (intakeOn == 1) r.intake.spin();
        else if (intakeOn == 2) r.intake.out();
        else r.intake.stop();

        if (gamepad2.dpad_up)   r.turret.setTarget(Turret.Target.A);
        if (gamepad2.dpad_down) r.turret.setTarget(Turret.Target.B);
        if (gamepad2.dpad_left) r.turret.setTarget(Turret.Target.AUTO);

        // TELEMETRIA
        telemetry.addData("x", robotPose.x());
        telemetry.addData("y", robotPose.y());
        telemetry.addData("heading deg", Math.toDegrees(robotPose.heading()));
        telemetry.addData("hive", r.turret.isUsingA() ? "A" : "B");
        telemetry.addData("distancia", distance);
        telemetry.addData("erro torreta (deg)", Math.toDegrees(r.turret.getError()));
        telemetry.addData("alcancavel", r.turret.isReachable());
        telemetry.addData("torreta pronta", r.turret.isReady());
        telemetry.addData("vel alvo", r.shooter.getTargetVel());
        telemetry.addData("vel atual", r.shooter.getVel());
        telemetry.addData("shooter pronto", r.shooter.atSpeed());
        telemetry.update();
    }
}