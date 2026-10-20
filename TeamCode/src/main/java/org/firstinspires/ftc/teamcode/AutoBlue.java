package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
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
    public final Pose hive2 = p.of(17, -117, 70);
    public final Pose preFlor = p.of(4, -103, 270);
    public final Pose flor1 = p.of(4, -117.7, 270);
    public final Pose shootHive2 = p.of(3, -114, 45);
    public final Pose prepFlor2 = p.of(33, -114, 90);

    public final Pose controlflor2 = p.of(30, -39, 0);
    public final Pose flor2 = p.of(41, -39, 0);

    public final Pose ultimoShoot = p.of(0, -10, 0);

    public final Pose controlPark = p.of(30, -18, 160);
    private final Pose park = p.of(45, -84, 270);


    public Path IntakeFlor() {
        return line(start, controlflor2).constant(flor2);
    }
    public Path Flor() {
        return line(controlflor2, flor2).constant(flor2);
    }
    public Path preHive(){return line(flor2, controlflor2).constant(flor2);}
    public Path hiveShoot() {
        return line(controlflor2, hive2).linear(flor2, hive2);
    }
    public Path prepFlor() {
        return line(hive2, preFlor).linear(hive2, preFlor);
    }
    public Path flor() {
        return line(preFlor, flor1).constant(flor1);
    }
    public Path secondTip() {
        return line(preFlor, shootHive2).linear(flor1, shootHive2);
    }
    public Path volta(){return line(flor1, preFlor).constant(flor1);}
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

    private Command autoRotine(){
        return sequential(

                Commands.instant(r.intake::spin),
                waitMs(600),
                Commands.instant(r.shooter::close),
                follow(r.follower, IntakeFlor()),
                follow(r.follower, Flor()),
                follow(r.follower, hiveShoot()),
                instant(r.intake::down),
                waitMs(1000),
                follow(r.follower, flor()),
                waitMs(1000),
                follow(r.follower, secondTip()),
                waitMs(400),
                Commands.instant(r.intake::down)
        );
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
                    r.follower.update();

                    telemetry.addData("Pose", r.follower.pose());
                    telemetry.addData("Follower Busy", r.follower.isBusy());
                    telemetry.addData("Hive", r.turret.isUsingA() ? "A" : "B");
                    telemetry.addData("Erro torreta (graus)", Math.toDegrees(r.turret.getError()));
                    telemetry.addData("Torreta pronta", r.turret.isReady());
                    telemetry.addData("Shooter pronto", r.shooter.atSpeed());
                    telemetry.update();
                }),
                sequential(
                        Commands.instant(r.intake::up),
                        Commands.instant(r.shooter::onAuto),
                        Commands.waitMs(2000),
                        Commands.instant(r.intake::up),
                        Commands.instant(r.intake::spin),
                        Commands.waitMs(1000),
                        Commands.instant(r.shooter::close),
                        follow(r.follower, IntakeFlor()),
                        Commands.instant(r.intake::down),
                        Commands.waitMs(500),
                        follow(r.follower, Flor()),
                        Commands.waitMs(1500),
                        follow(r.follower, preHive()),
                        Commands.instant(r.intake::up),
                        follow(r.follower, hiveShoot()),
                        Commands.instant(r.intake::spin),
                        Commands.instant(r.shooter::open),
                        Commands.waitMs(700)
                )
        );
    }

    @Override
    public void start() {
        super.start();
        started = true;
        r.follower.update();
        r.turret.on();
    }

    @Override
    public void stop() {
        r.shooterOn = false;
        r.shooter.stop();
        r.turret.off();
        r.saveEnd();
    }
}