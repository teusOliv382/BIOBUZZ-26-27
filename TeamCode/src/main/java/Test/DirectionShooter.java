//package Test;
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//
//import Subsystem.Shooter;
//
//@TeleOp
//public class DirectionShooter extends OpMode {
//    Shooter shooter;
//
//    @Override
//    public void init() {
//        shooter = new Shooter(hardwareMap);
//    }
//
//    @Override
//    public void loop() {
//
//        shooter.update();
//
//        if (gamepad1.right_trigger>0.1){
//            shooter.shooter1();
//        } else if (gamepad1.left_trigger> 0.1) {
//            shooter.shoter2();
//        } else {
//            shooter.stop();
//        }
//    }
//}
