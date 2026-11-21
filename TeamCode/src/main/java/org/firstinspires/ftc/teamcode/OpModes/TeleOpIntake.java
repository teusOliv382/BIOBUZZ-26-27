package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Subsystem.Intake;

@TeleOp
public class TeleOpIntake extends OpMode {

    Intake intake;

    public int intakeOn = 1;
    @Override
    public void init() {
        intake = new Intake(hardwareMap);
    }

    @Override
    public void loop() {

        //INTAKE
        if (gamepad1.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
        if (gamepad1.leftBumperWasPressed()) intakeOn = (intakeOn == 2) ? 0 : 2;

        if (intakeOn == 1) intake.spin();
        else if (intakeOn == 2) intake.out();
        else intake.stop();
    }
}
