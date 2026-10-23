package org.firstinspires.ftc.teamcode.OpModes.Autonomous;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.commands.Commands.*;

import org.firstinspires.ftc.teamcode.pedro.CommandOpMode;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import Subsystem.Robot;

public class secondAutonomous extends CommandOpMode {

    Robot r;

    private boolean started = false;


    private final PoseFactory p = PoseFactory.degrees();

    public Pose start = p.of(0, 0, 0);
    public Pose prePark = p.of(44, -55, 270);

    public Pose parkAlliance = p.of(43, 80, 270);
    public Pose controlGarden = p.of(34, -40, 90);
    public final Pose garden = p.of(47, -1.4, 90);
    public final Pose giroTip2 = p.of(30, -15, 270);
    public final Pose hive2 = p.of(-6, -121, 90);
    public final Pose preFlor = p.of(4, -103, 270);
    public final Pose controlHive = p.of(17, 110, 270);
    public final Pose flor1 = p.of(2, -117.9, 270);

    private final Pose park = p.of(40, -110, 180);


    public Path preAlliance(){
        return line(start, prePark).linear(start, prePark);
    }

    public Path alliancePark(){
        return line(prePark, parkAlliance).constant(parkAlliance);
    }

    public Path garden(){
        return curve(parkAlliance, controlGarden, garden).linear(parkAlliance, garden);
    }

    public Path ControlHive(){
        return curve(garden, giroTip2, controlHive).linear(garden, controlHive);
    }

    public Path Preflower(){
        return line(hive2, preFlor).linear(hive2,preFlor);
    }

    public Path secondShoot(){
        return line(preFlor, hive2).linear(preFlor, hive2);
    }

    public Path flor() {
        return line(preFlor, flor1).constant(flor1).with(Constants.foresightConfig.timeoutConstraint.at(0.100));
    }

    public Path parking() {
        return line(hive2, park).linear(hive2, park);
    }



    @Override
    public void init(){
        r = new Robot(hardwareMap);
        r.follower.setPose(start);
        r.turret.resetTurret();
        r.turret.off();

        schedule(
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
                        Commands.instant(r.intake::stop),
                        Commands.instant(r.intake::up),
                        Commands.instant(r.shooter::onAuto),
                        Commands.waitMs(2500),
                        Commands.instant(r.intake::spin),
                        Commands.waitMs(1000),
                        Commands.instant(r.shooter::close),
                        Commands.instant(r.intake::stop),
                        follow(r.follower, preAlliance()),
                        follow(r.follower, alliancePark()),
                        Commands.instant(r.intake::spin),
                        follow(r.follower, garden()),
                        Commands.waitMs(1000),
                        follow(r.follower, ControlHive()),
                        Commands.waitMs(500),
                        Commands.instant(r.shooter::open),
                        Commands.waitMs(1000),
                        Commands.instant(r.shooter::close),
                        follow(r.follower, Preflower()),
                        follow(r.follower, flor()),
                        Commands.waitMs(1500),
                        follow(r.follower, secondShoot()),
                        Commands.waitMs(800),
                        Commands.instant(r.shooter::open),
                        waitMs(900),
                        follow(r.follower, parking())



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
        r.saveEnd();
    }

}
