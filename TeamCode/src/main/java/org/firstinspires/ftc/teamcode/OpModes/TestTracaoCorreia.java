package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "TESTE CORREIA DE DANTUUUU")
public class TestTracaoCorreia extends OpMode {

    public Follower follower;
    public static Pose defaultPose = new Pose(0, 0, 0);


    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        follower.setPose(defaultPose);
    }

    @Override
    public void loop() {
        follower.update();
        Pose robotPose = follower.pose();

        DrivePowers powers = ManualDrive.fieldCentric(
                gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                robotPose.heading()
        );
        follower.manual(powers);
    }
}
