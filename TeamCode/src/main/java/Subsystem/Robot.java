package Subsystem;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

public class Robot {

    public final Intake intake;
    public final Shooter shooter;
    public final Follower follower;
    public final Turret turret;

    private final List<LynxModule> hubs;

    public Robot(HardwareMap h) {
        // Bulk caching MANUAL: todas as leituras de encoder do loop saem de UMA leitura por hub.
        hubs = h.getAll(LynxModule.class);
        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        intake = new Intake(h);
        shooter = new Shooter(h);
        follower = Constants.create(h);
        turret = new Turret(h);
    }

    /** OBRIGATÓRIO no começo de todo loop (senão os encoders ficam congelados). */
    public void clearCache() {
        for (LynxModule hub : hubs) hub.clearBulkCache();
    }

    /** Ordem certa: cache -> localização -> mira -> controle. */
    public void update() {
        clearCache();
        follower.update();
        turret.aim(follower.pose(), follower.velocity().toVector2D());
        turret.periodic();
    }
}