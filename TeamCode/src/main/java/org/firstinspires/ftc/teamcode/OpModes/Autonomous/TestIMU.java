
package org.firstinspires.ftc.teamcode.OpModes.Autonomous;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@Autonomous(name = "Teste IMU")
public class TestIMU extends LinearOpMode {

    @Override
    public void runOpMode() {

        IMU imu = hardwareMap.get(IMU.class, "imu");

        imu.initialize(new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.LEFT
                )
        ));

        // Atualiza continuamente antes de iniciar
        while (opModeInInit() && !isStopRequested()) {
            mostrarIMU(imu);
        }

        waitForStart();

        if (isStopRequested()) return;

        imu.resetYaw();

        // Atualiza continuamente durante o teste
        while (opModeIsActive()) {
            mostrarIMU(imu);
        }
    }

    private void mostrarIMU(IMU imu) {

        telemetry.addData("Yaw", "%.2f",
                imu.getRobotYawPitchRollAngles()
                        .getYaw(AngleUnit.DEGREES));

        telemetry.addData("Pitch", "%.2f",
                imu.getRobotYawPitchRollAngles()
                        .getPitch(AngleUnit.DEGREES));

        telemetry.addData("Roll", "%.2f",
                imu.getRobotYawPitchRollAngles()
                        .getRoll(AngleUnit.DEGREES));

        telemetry.update();
    }
}
