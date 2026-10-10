package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.ivy.pedro.PedroCommands;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.commands.Commands.*;

import org.firstinspires.ftc.teamcode.pedro.CommandOpMode;

import Subsystem.Robot;

@Autonomous
public class AutoBlue extends CommandOpMode {

    Robot r;

    Command pause = waitMs(1000);

    /** A torreta só se mexe depois do START (regra: o robô não pode se mover no init). */
    private boolean started = false;

    private final PoseFactory p = PoseFactory.degrees();

    public Pose start = p.of(0, 0, 0);
    public final Pose preIntake = p.of(30, 0, 0);
    public final Pose garden = p.of(43, -1, 0);
    public final Pose giroTip2 = p.of(30, -15, 270);
    public final Pose hive2 = p.of(17, -111, 224);
    public final Pose preFlor = p.of(8, -110, 270);
    public final Pose flor1 = p.of(8, -117, 270);
    public final Pose shootHive2 = p.of(3, -114, 0);
    public final Pose prepFlor2 = p.of(33, -114, 90);

    public final Pose controlflor2 = p.of(30, -37, 0);
    public final Pose flor2 = p.of(35, -37, 0);

    public final Pose ultimoShoot = p.of(0, -10, 0);

    public final Pose controlPark = p.of(30, -18, 160);
    private final Pose park = p.of(45, -84, 270);


    public Path prepGarden() {
        return line(start, preIntake).constant(preIntake);
    }
    public Path intakeGarden() {
        return line(start, garden).constant(garden);
    }
    public Path giro1() {
        return line(garden, giroTip2).linear(garden, giroTip2);
    }
    public Path hiveShoot() {
        return line(giroTip2, hive2).linear(giroTip2, hive2);
    }
    public Path prepFlor() {
        return line(hive2, preFlor).linear(hive2, preFlor);
    }
    public Path flor() {
        return line(preFlor, flor1).constant(flor1);
    }
    public Path secondTip() {
        return line(flor1, shootHive2).linear(flor1, shootHive2);
    }
    public Path controlFlor() {
        return line(shootHive2, prepFlor2).linear(shootHive2, prepFlor2);
    }
    public Path prepfinalFlor() {
        return line(prepFlor2, controlflor2).linear(prepFlor2, controlflor2);
    }
    public Path finalFlor() {
        return line(controlflor2, flor2).constant(flor2);
    }
    public Path finalShoot() {
        return line(flor2, ultimoShoot).linear(flor2, ultimoShoot);
    }
    public Path parking() {
        return curve(ultimoShoot, controlPark, park).linear(ultimoShoot, park);
    }


    @Override
    public void init() {
        r = new Robot(hardwareMap);
        r.follower.setPose(start);
        r.turret.resetTurret();   // torreta olhando para a FRENTE do robô aqui
        r.turret.off();           // parada até o START

        schedule(
                // Roda em TODO loop: mira da torreta + shooter seguindo a distância do hive ativo
                Commands.infinite(() -> {
                    r.updateAuto();

                    telemetry.addData("Pose", r.follower.pose());
                    telemetry.addData("Follower Busy", r.follower.isBusy());
                    telemetry.addData("Hive", r.turret.isUsingA() ? "A" : "B");
                    telemetry.addData("Distancia", r.turret.distanceToGoal(r.follower.pose()));
                    telemetry.addData("Erro torreta (graus)", Math.toDegrees(r.turret.getError()));
                    telemetry.addData("Torreta pronta", r.turret.isReady());
                    telemetry.addData("Shooter pronto", r.shooter.atSpeed());
                    telemetry.update();
                }),
                sequential(
//                        Commands.instant(() -> r.shooterOn = true),  // liga o flywheel (vel/hood pela distância)
//                        Commands.waitMs(2500),
//                        Commands.instant(r.intake::spin),
//                        waitMs(600),
//                        Commands.instant(r.shooter::close),
                        PedroCommands.follow(r.follower, prepGarden()),
                        PedroCommands.follow(r.follower, intakeGarden())
                )
        );
    }

    @Override
    public void start() {
        super.start();
        started = true;
        r.turret.on();   // a partir daqui a torreta mira sozinha o caminho todo
    }

    @Override
    public void stop() {
        r.shooterOn = false;
        r.shooter.stop();
        r.turret.off();
        r.saveEnd();
    }
}