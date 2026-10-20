package Test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Subsystem.Intake;
import Subsystem.Shooter;


@TeleOp
public class TravaTest extends OpMode {
    Intake intake;
    @Override
    public void init() {
        intake = new Intake(hardwareMap);
    }

    @Override
    public void loop() {

        if (gamepad1.x){
            intake.down();
        }if (gamepad1.a){
            intake.up();
        }
    }
}
