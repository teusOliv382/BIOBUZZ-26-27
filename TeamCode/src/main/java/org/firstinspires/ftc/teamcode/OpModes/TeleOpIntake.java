package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import Subsystem.Intake;

public class TeleOpIntake extends OpMode {

    Intake intake;

    @Override
    public void init() {
        intake = new Intake(hardwareMap);
    }

    @Override
    public void loop() {

        if (gamepad1.right_bumper){
            intake.spin();
        }else if(gamepad1.left_bumper){
            intake.stop();
        }
    }
}
