package Test;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Subsystem.Intake;
import Subsystem.Shooter;

@Configurable
@TeleOp
public class TestShooter extends OpMode {
    Shooter shooter;
    Intake intake;

    public int intakeOn = 1;

    public static double vel = 0;

    @Override
    public void init() {
        shooter = new Shooter(hardwareMap);
        intake = new Intake(hardwareMap);
    }

    @Override
    public void loop() {
        if (gamepad1.right_trigger > 0.1) {
            shooter.setVelocity(vel);
        } else {
            shooter.stop();
        }
        shooter.update();

        //INTAKE
        if (gamepad1.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
        if (gamepad1.leftBumperWasPressed()) intakeOn = (intakeOn == 2) ? 0 : 2;

        if (intakeOn == 1) intake.spin();
        else if (intakeOn == 2) intake.out();
        else intake.stop();

        telemetry.addData("vel alvo", "%.0f", shooter.getTargetVel());
        telemetry.addData("vel atual", "%.0f", shooter.getVel());
        telemetry.addData("posicao encoder", shooter.shooter.getCurrentPosition());
        telemetry.addData("potencia", "%.3f", shooter.getPower());
        telemetry.addData("bateria", "%.2f", shooter.getVoltage());
        telemetry.addData("pronto", shooter.atSpeed());
    }
}