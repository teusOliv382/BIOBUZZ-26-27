import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import Util.Limelight;
import Util.RGBLight;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp
public class TeleOp extends OpMode {

    DcMotor FL, BL, FR, BR;
    Limelight limelight;

    RGBLight led;
    Follower f;

    @Override
    public void init() {
        FL = hardwareMap.get(DcMotor.class, "FLmotor");
        BL = hardwareMap.get(DcMotor.class, "BLmotor");
        FR = hardwareMap.get(DcMotor.class, "FRmotor");
        BR = hardwareMap.get(DcMotor.class, "BRmotor");

        FL.setDirection(DcMotorSimple.Direction.FORWARD);
        BL.setDirection(DcMotorSimple.Direction.FORWARD);
        FR.setDirection(DcMotorSimple.Direction.REVERSE);
        BR.setDirection(DcMotorSimple.Direction.FORWARD);

        limelight = new Limelight(hardwareMap, telemetry);
        led = new RGBLight(hardwareMap);

    }



    @Override
    public void loop() {
        double y  = -gamepad1.left_stick_y;  // frente/trás
        double x  =  gamepad1.left_stick_x;  // strafe
        double rx = - gamepad1.right_stick_x; // rotação

        FL.setPower(y + x + rx);
        BL.setPower(y - x + rx);
        FR.setPower(y - x - rx);
        BR.setPower(y + x - rx);

        if (gamepad1.a){
            FL.setPower(1);
        }else if (gamepad1.b){
            FR.setPower(1);
        }else if (gamepad1.y){
            BR.setPower(1);
        } else if (gamepad1.x) {
            BL.setPower(1);
        }else {


            FL.setPower(0);
            BL.setPower(0);
            FR.setPower(0);
            BR.setPower(0);
        }


        if (limelight.pollenOK()){
            led.green();
        }else{
            led.red();
        }

    }
}
