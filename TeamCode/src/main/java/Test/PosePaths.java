//package Test;
//
//import com.pedropathing.follower.Follower;
//import com.pedropathing.math.Pose;
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//
//import org.firstinspires.ftc.teamcode.pedro.Constants;
//
//@TeleOp
//public class PosePaths extends OpMode {
//    Follower f;
//
//    @Override
//    public void init() {
//        f = Constants.create(hardwareMap);
//        f.setPose(new Pose(0, 0, Math.toRadians(0)));
//    }
//
//    @Override
//    public void loop() {
//
//        f.update();
//        Pose robotPose = f.pose();
//
//        telemetry.addData("x", robotPose.x());
//        telemetry.addData("y", robotPose.y());
//        telemetry.addData("heading deg", Math.toDegrees(robotPose.heading()));
//
//    }
//}
