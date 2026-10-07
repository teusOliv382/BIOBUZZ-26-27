package Subsystem;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;


import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
public class Shooter {
    public DcMotorEx shooter;
    public Servo gate;

    public static double kP = 100;
    public static double kI = 0;
    public static double kD = 0;
    public static double kF = 21;

    public static double vel = 0;
    public static double pos = 0;
    public static double pos1 = 0;
    public Shooter(HardwareMap hMap) {
        shooter = hMap.get(DcMotorEx.class, "shooter");
        shooter.setDirection(DcMotorSimple.Direction.FORWARD);
        gate = hMap.get(Servo.class, "gate");

        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        applyPIDF();
    }

    public void applyPIDF() {
        shooter.setVelocityPIDFCoefficients(kP, kI, kD, kF);
    }

    public void setVelocity(){
        applyPIDF();
        shooter.setVelocity(1500);
    }

    public double getVel(){
       return shooter.getVelocity();
    }

    public void stop(){
        shooter.setPower(0);
    }

    public void open(){
        gate.setPosition(0.35);
    }

    public void close(){
        gate.setPosition(0.02);
    }


}
