package Test;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import Subsystem.Robot;

@TeleOp
@Configurable
public class ShooterAllDistance extends OpMode {

    Robot r;

    public static double TargetVelocity = 0;
    public static double targetServo = 0.4;

    public int intakeOn = 0;



    @Override
    public void init() {
        r = new Robot(hardwareMap);
        r.follower.setPose(new Pose(0,0,0));
        }

    @Override
    public void loop() {
        r.follower.update();

        Pose robotPose = r.follower.pose();
        Pose goalPose = new Pose(-4, -50);

        double distance = robotPose.distance(goalPose);

        telemetry.addData("Distance to Goal", distance);
        telemetry.addData("Shooter Velocity", r.shooter.getVel());
        telemetry.addData("Target Velocity", TargetVelocity);
        telemetry.addData("hood test", targetServo);

            r.shooter.setVelocity(TargetVelocity);


        if (gamepad2.a) {
            r.shooter.open();
        } else {
            r.shooter.close();
        }

        if (gamepad2.rightBumperWasPressed())
            intakeOn = (intakeOn == 1) ? 0 : 1;

        if (gamepad2.leftBumperWasPressed())
            intakeOn = (intakeOn == 2) ? 0 : 2;

        if (intakeOn == 1) {
            r.intake.spin();
        } else if (intakeOn == 2) {
            r.intake.out();
        } else {
            r.intake.stop();
        }

        r.shooter.testHood(targetServo);

        telemetry.update();

    }
}
