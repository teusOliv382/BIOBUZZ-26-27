import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;


import Subsystem.Robot;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp
public class TeleOp extends OpMode {

    public int intakeOn = 0;

    public Robot r;


    @Override
    public void init() {
        r = new Robot(hardwareMap);
        r.follower.setPose(new Pose(0, 0, Math.toRadians(0)));
        r.turret.resetTurret();
    }



    @Override
    public void loop() {

        DrivePowers powers = ManualDrive.fieldCentric(
                gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                r.follower.pose().heading()
        );
        r.follower.manual(powers);


        if (gamepad1.right_trigger > 0.1 )
            r.shooter.setVelocity();
        else r.shooter.stop();



        //INTAKE
        if (gamepad1.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
        if (gamepad1.leftBumperWasPressed()) intakeOn = (intakeOn == 2) ? 0 : 2;


        else if (intakeOn == 1) r.intake.spin();
        else if (intakeOn == 2) r.intake.out();
        else r.intake.stop();


        if (gamepad1.a){
            r.shooter.open();
        }else {
            r.shooter.close();
        }

        r.update();


        r.follower.update();
        Pose robotPose = r.follower.pose();


        telemetry.addData("x", r.follower.pose().x());
        telemetry.addData("y", r.follower.pose().y());
        telemetry.addData("heading deg", Math.toDegrees(r.follower.pose().heading()));

        telemetry.addData("erro", r.turret.getError());
        telemetry.update();
    }
}
