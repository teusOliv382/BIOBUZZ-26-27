package Test;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import Subsystem.Intake;
import Subsystem.Shooter;

@Configurable
@TeleOp
public class TestShooter extends OpMode {
    Shooter shooter;
    Intake intake;
    Follower follower;
    public int intakeOn = 0;

    public static double targetServo = 0.4;


    public static double vel = 0;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        shooter = new Shooter(hardwareMap);
        intake = new Intake(hardwareMap);
        follower.setPose(new Pose(0, 0, 0));

    }

    @Override
    public void loop() {

        follower.update();

        Pose robotPose = follower.pose();
        double distance = robotPose.distance(new Pose(-4, -63.5));

        shooter.testHood(targetServo);

        if (gamepad1.right_trigger > 0.1){
            shooter.setVelocity(vel);
        }else{
            shooter.stop();
        }
        shooter.update();

        if (gamepad1.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
        if (gamepad1.leftBumperWasPressed()) intakeOn = (intakeOn == 2) ? 0 : 2;

        if (intakeOn == 1) {
            intake.spin();
        } else if (intakeOn == 2) {
            intake.out();
        } else {
            intake.stop();
        }
        if (gamepad1.a){
            shooter.open();
        }else {
            shooter.close();
        }

        telemetry.addData( "shooter vel", shooter.getVel());
        telemetry.addData("Distance to Goal", distance);

    }
}
