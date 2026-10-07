package Subsystem;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Configurable
public class Intake {

    public DcMotor intake;

    public static double vel;

    public Intake(HardwareMap hMap){
        intake = hMap.get(DcMotor.class, "intake");
        intake.setDirection(DcMotorSimple.Direction.FORWARD);
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
