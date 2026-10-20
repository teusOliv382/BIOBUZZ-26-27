package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("FLmotor");
        c.frontRightName.set("FRmotor");
        c.backLeftName.set("BLmotor");
        c.backRightName.set("BRmotor");
        c.frontLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        c.xPodOffset.set(0.5762);
        c.yPodOffset.set(-4.79);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.3917090427359592);
                Controller secondaryTranslationalForward = Controller.proportional(0.1447260246325478);
                Controller primaryTranslationalLateral = Controller.proportional(0.7645919978892927);
                Controller secondaryTranslationalLateral = Controller.proportional(0.28249631294564026);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.013768551359144525));
                c.brake.set(Controller.proportionalFeedforward(0.011703268655272846));

                c.headingFeedback.set(Controller.proportional(4.710815245981127));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.06367266388544932, 0.010744793001105321));

                c.linearBrakeCoefficients.set(Matrix.diag(0.11646887716294704, 0.058283856544397904));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.001547167796745061, 0.0027137065244519985));

                c.maxAchievableForwardVelocity.set(71.38473430002172);
                c.maxAchievableStrafeVelocity.set(54.99275490876304);
                c.naturalForwardDeceleration.set(30.832799518826995);
                c.naturalStrafeDeceleration.set(65.01931314827306);
            }
    );


    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }

}