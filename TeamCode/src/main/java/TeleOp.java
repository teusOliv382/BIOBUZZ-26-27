import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;


import Subsystem.Robot;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp
public class TeleOp extends OpMode {

    public int intakeOn = 0;
    public boolean shooterOn = true;   // X do gamepad1 liga/desliga

    public Robot r;


    @Override
    public void init() {
        r = new Robot(hardwareMap);
        r.follower.setPose(new Pose(0, 0, Math.toRadians(0)));
        r.turret.resetTurret();

        // posições dos hives (basta definir uma vez)
        r.turret.setHiveA(new Pose(-3, -55));
        r.turret.setHiveB(new Pose(-3, -65));
    }



    @Override
    public void loop() {
        r.update();
        Pose robotPose = r.follower.pose();

        DrivePowers powers = ManualDrive.fieldCentric(
                gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                robotPose.heading()
        );
        r.follower.manual(powers);

        //INTAKE
        if (gamepad1.rightBumperWasPressed()) intakeOn = (intakeOn == 1) ? 0 : 1;
        if (gamepad1.leftBumperWasPressed()) intakeOn = (intakeOn == 2) ? 0 : 2;

        if (intakeOn == 1) r.intake.spin();
        else if (intakeOn == 2) r.intake.out();
        else r.intake.stop();


        if (gamepad1.a){
            r.shooter.open();
        }else {
            r.shooter.close();
        }


        if (gamepad1.dpad_down){
            r.intake.down();
        } else if (gamepad1.dpad_up) {
            r.intake.up();
        }

        if (gamepad2.right_bumper){
            r.follower.setPose(new Pose(0, 0));
        }

        double distHive = robotPose.distance(new Pose(-4, -63.5));;
       if (shooterOn) r.shooter.aim(distHive);

        if (gamepad2.xWasPressed()) shooterOn = !shooterOn;

        if (shooterOn) r.shooter.aim(distHive);
        else r.shooter.stop();


        telemetry.addData("x", robotPose.x());
        telemetry.addData("y", robotPose.y());
        telemetry.addData("heading deg", Math.toDegrees(robotPose.heading()));

        telemetry.addData("hive", r.turret.isUsingA() ? "A" : "B");
        telemetry.addData("erro torreta (deg)", r.turret.getErrorDeg());
        telemetry.addLine("--- Shooter ---");
        telemetry.addData("shooter", shooterOn ? "ON" : "OFF");
        telemetry.addData("distância até o hive (pol)", distHive);
        telemetry.addData("velocidade alvo / atual", "%.0f / %.0f", r.shooter.getTargetVel(), r.shooter.getVel());
        telemetry.addData("hood", r.shooter.getHood());
        telemetry.addData("shooter pronto", r.shooter.atSpeed());
        telemetry.addData("PODE ATIRAR", shooterOn && r.shooter.atSpeed() && r.turret.isReady());
        telemetry.update();
    }

    @Override
    public void stop(){
        r.saveEnd();
    }
}