package org.firstinspires.ftc.teamcode.OpModes.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class TeleOPoOBR extends OpMode {

    public DcMotor BL, BR, FL, FR;

    @Override
    public void init() {
        FL = hardwareMap.get(DcMotor.class, "FL");
        FR = hardwareMap.get(DcMotor.class, "FR");
        BL = hardwareMap.get(DcMotor.class, "BL");
        BR = hardwareMap.get(DcMotor.class, "BR");

        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        BL.setDirection(DcMotorSimple.Direction.REVERSE);
        FR.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void loop() {
        double y = -gamepad1.left_stick_y;  // frente/trás
        double x = gamepad1.left_stick_x;   // lados
        double r = gamepad1.right_stick_x;  // girar

        // velocidade máxima 0.5, mesmo combinando movimentos
        double v = 0.75 / Math.max(Math.abs(y) + Math.abs(x) + Math.abs(r), 1);

        FL.setPower((y + x + r) * v);
        BL.setPower((y - x + r) * v);
        FR.setPower((y - x - r) * v);
        BR.setPower((y + x - r) * v);
    }
}