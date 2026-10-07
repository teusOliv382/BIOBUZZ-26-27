package Util;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class test extends OpMode {

     public DcMotor test;
    @Override
    public void init() {
        test = hardwareMap.get(DcMotor.class, "t");
        test.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    @Override
    public void loop() {
        if (gamepad1.a) {
            test.setPower(-1);
        } else if (gamepad1.x) {
            test.setPower(1);
        } else {
            test.setPower(0);
        }

    }
}
