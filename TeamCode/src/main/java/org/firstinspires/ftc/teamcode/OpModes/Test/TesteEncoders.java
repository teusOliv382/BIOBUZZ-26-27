
package org.firstinspires.ftc.teamcode.OpModes.Test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp(name = "Teste Encoders")
public class TesteEncoders extends LinearOpMode {

    @Override
    public void runOpMode() {

        DcMotorEx FL = hardwareMap.get(DcMotorEx.class, "FL");
        DcMotorEx FR = hardwareMap.get(DcMotorEx.class, "FR");
        DcMotorEx BL = hardwareMap.get(DcMotorEx.class, "BL");
        DcMotorEx BR = hardwareMap.get(DcMotorEx.class, "BR");

        telemetry.addLine("MOTORES RECONHECIDOS!");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            telemetry.addData("FL", "Ticks: %d | Vel: %.1f",
                    FL.getCurrentPosition(), FL.getVelocity());

            telemetry.addData("FR", "Ticks: %d | Vel: %.1f",
                    FR.getCurrentPosition(), FR.getVelocity());

            telemetry.addData("BL", "Ticks: %d | Vel: %.1f",
                    BL.getCurrentPosition(), BL.getVelocity());

            telemetry.addData("BR", "Ticks: %d | Vel: %.1f",
                    BR.getCurrentPosition(), BR.getVelocity());

            telemetry.update();
            idle();
        }
    }
}
