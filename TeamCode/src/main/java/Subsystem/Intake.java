package Subsystem;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
public class Intake {

    public DcMotor intake;
    public Servo fourBar;

    public static double servoVal, srvo;

    public static double vel;

    public static double off = 0;
    public static double in = 1;
    public static double out = -1;

    public Intake(HardwareMap hMap) {
        intake = hMap.get(DcMotor.class, "intake");
        fourBar = hMap.get(Servo.class, "four");
        intake.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void down() {
        fourBar.setPosition(0.5);
    }

    public void up() {
        fourBar.setPosition(0);
    }

    public void spin() {
        intake.setPower(1);
    }

    public void stop() {
        intake.setPower(0);
    }

    public void slow(){
        intake.setPower(0.8);
    }
    public void out() {
        intake.setPower(-1);
    }

    public void set(double power) {
        intake.setPower(power);
    }


    public CommandBuilder off() {
        return Commands.instant(() -> set(off));
    }

    public CommandBuilder in() {
        return Commands.instant(() -> set(in));
    }

    public CommandBuilder outAuto() {
        return Commands.instant(() -> set(out));
    }
}
