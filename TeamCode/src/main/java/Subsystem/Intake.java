package Subsystem;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
public class Intake {

    public DcMotor intake;


    public Intake(HardwareMap h){
        intake = h.get(DcMotorEx.class, "intake");
    }



    public void spin(){
        intake.setPower(1);
    }

    public void stop(){
        intake.setPower(0);
    }

    public void out(){
        intake.setPower(-1);
    }
}
