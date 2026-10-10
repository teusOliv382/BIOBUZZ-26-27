package Subsystem;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

public class Robot {

    public final Intake intake;
    public final Shooter shooter;
    public final Follower follower;
    public final Turret turret;
    public static Pose defaultPose = new Pose(0, 0, 0);

    /** No autônomo: liga/desliga o flywheel. A velocidade e o hood seguem a distância sozinhos. */
    public boolean shooterOn = false;

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

    /** TELEOP. Chame UMA vez, no começo do loop: cache -> localização -> mira -> controle da torreta. */
    public void update() {
        clearCache();
        follower.update();
        turret.aim(follower.pose());
        turret.periodic();
    }

    /**
     * AUTÔNOMO. Mesma coisa que o update(), mas SEM follower.update(),
     * porque no autônomo quem atualiza o follower é o CommandOpMode / follow().
     * Também mantém o shooter acompanhando a distância do hive ativo.
     */
    public void updateAuto() {
        clearCache();
        turret.aim(follower.pose());
        turret.periodic();

        if (shooterOn) shooter.aim(turret.distanceToGoal(follower.pose()));
        else shooter.stop();
    }

    /** Pronto para atirar: torreta mirada + flywheel na velocidade. */
    public boolean readyToShoot() {
        return shooterOn && turret.isReady() && shooter.atSpeed();
    }

    public void saveEnd() {
        defaultPose = follower.pose();
    }
}