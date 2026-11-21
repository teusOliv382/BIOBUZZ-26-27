//package org.firstinspires.ftc.teamcode.OpModes;
//
//import static Subsystem.Robot.defaultPose;
//
//import com.bylazar.configurables.annotations.Configurable;
//import com.pedropathing.drivetrain.DrivePowers;
//import com.pedropathing.follower.ManualDrive;
//import com.pedropathing.math.Pose;
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//
//
//import Subsystem.Robot;
//
//@Configurable
//@com.qualcomm.robotcore.eventloop.opmode.TeleOp (name = "TELEOP")
//public class PosAuto extends OpMode {
//
//    public int intakeOn = 1;
//    public boolean shooterOn = true;
//    public boolean turretOn = true;
//    public static double DIST_OFFSET_A = 0;   // pol, ajuste no Panels
//    public static double DIST_OFFSET_B = 2;// X do gamepad1 liga/desliga
//
//    public Robot r;
//
//
//    @Override
//    public void init() {
//        r = new Robot(hardwareMap);
//        r.follower.setPose(defaultPose);
//    }
//
//
//
//    @Override
//    public void loop() {
//        r.update();
//        Pose robotPose = r.follower.pose();
//
//        DrivePowers powers = ManualDrive.fieldCentric(
//                gamepad1.left_stick_y,
//                gamepad1.left_stick_x,
//                -gamepad1.right_stick_x,
//                robotPose.heading()
//        );
//        r.follower.manual(powers);
//
//        //INTAKE
//        if (gamepad1.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
//        if (gamepad1.leftBumperWasPressed()) intakeOn = (intakeOn == 2) ? 0 : 2;
//
//        if (intakeOn == 1) r.intake.spin();
//        else if (intakeOn == 2) r.intake.out();
//        else r.intake.stop();
//
//
//        if (gamepad1.right_trigger > 0.1){
//            r.shooter.open();
//        }else {
//            r.shooter.close();
//        }
//
//
//        if (gamepad2.dpad_down){
//            r.intake.down();
//        } else if (gamepad2.dpad_up) {
//            r.intake.up();
//        }
//
//        //reset
//        if (gamepad2.right_bumper){
//            r.follower.setPose(new Pose(0, 0));
//        }
//
//        double distHive = r.turret.getShotDistance();
//        if (Double.isNaN(distHive)) distHive = r.turret.distanceToGoal(robotPose);
//
//
//        if (gamepad2.xWasPressed()) shooterOn = !shooterOn;
//
//        double offset = r.turret.isUsingA() ? DIST_OFFSET_A: DIST_OFFSET_B;
//        if (shooterOn) r.shooter.aim(distHive + offset);
//        else r.shooter.stop();
//
//        if (gamepad2.aWasPressed()) turretOn = !turretOn;
//
//        if (turretOn) {
//            r.turret.aim(r.follower.pose(), r.follower.velocity().toVector2D());
//            r.turret.periodic();
//        }else {
//            r.turret.off();
//        }
//
//        if (r.turret.isUsingA()){
//            r.shooter.blue();
//        } else if (!r.turret.isUsingA()) {
//            r.shooter.violet();
//        }
//
//
//
//
//        telemetry.addData("x", robotPose.x());
//        telemetry.addData("y", robotPose.y());
//        telemetry.addData("heading deg", Math.toDegrees(robotPose.heading()));
//
//        telemetry.addData("hive", r.turret.isUsingA() ? "A" : "B");
//        telemetry.addData("erro torreta (deg)", r.turret.getErrorDeg());
//        telemetry.addLine("--- Shooter ---");
//        telemetry.addData("shooter", shooterOn ? "ON" : "OFF");
//        telemetry.addData("distância até o hive (pol)", distHive);
//        telemetry.addData("velocidade alvo / atual", "%.0f / %.0f", r.shooter.getTargetVel(), r.shooter.getVel());
//        telemetry.addData("hood", r.shooter.getHood());
//        telemetry.addData("shooter pronto", r.shooter.atSpeed());
//        telemetry.addData("PODE ATIRAR", shooterOn && r.shooter.atSpeed() && r.turret.isReady());
//        telemetry.update();
//    }
//
//    @Override
//    public void stop(){
//        r.saveEnd();
//    }
//}