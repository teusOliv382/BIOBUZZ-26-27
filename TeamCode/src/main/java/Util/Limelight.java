package Util;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

public class Limelight {

    private Limelight3A lime;
    private Telemetry telemetry;

    private static final int PIPELINE_DETECTOR = 8;
    private static final String POLLEN_CLASS = "pollen"; // ajusta pro nome da sua classe
    private static final double MIN_AREA = 0.0001;          // área mínima pra considerar válido

    public Limelight(HardwareMap hMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        lime = hMap.get(Limelight3A.class, "limelight");
        lime.pipelineSwitch(PIPELINE_DETECTOR);
        lime.setPollRateHz(20);
        lime.start();
    }

    /**
     */
    public boolean pollenOK() {
        LLResult result = lime.getLatestResult();


        if (result == null || !result.isValid()) return false;

        List<LLResultTypes.DetectorResult> detectorResults = result.getDetectorResults();

        if (detectorResults == null || detectorResults.isEmpty()) return false;

        for (LLResultTypes.DetectorResult dr : detectorResults) {
            telemetry.addData("Detector", "Class: %s, Area: %.2f",
                    dr.getClassName(), dr.getTargetArea());

            if (dr.getClassName().equalsIgnoreCase(POLLEN_CLASS)
                    && dr.getTargetArea() >= MIN_AREA) {
                return true;
            }
        }

        return false;
    }
}