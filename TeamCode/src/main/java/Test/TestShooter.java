package Test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Subsystem.Intake;
import Subsystem.Shooter;

@TeleOp
public class TestShooter extends OpMode {
    Shooter shooter;
    Intake intake;

    @Override
    public void init() {
        shooter = new Shooter(hardwareMap);
        intake = new Intake(hardwareMap);
    }

    @Override
    public void loop() {

        if (gamepad1.right_trigger > 0.1){
            shooter.setVelocity(1000);
        }else{
            shooter.stop();
        }

        if (gamepad1.right_bumper){
            intake.spin();
        }else {
            intake.stop();
        }

        if (gamepad1.a){
            shooter.open();
        }else {
            shooter.close();
        }

        telemetry.addData( "shooter vel", shooter.getVel());
    }
}
