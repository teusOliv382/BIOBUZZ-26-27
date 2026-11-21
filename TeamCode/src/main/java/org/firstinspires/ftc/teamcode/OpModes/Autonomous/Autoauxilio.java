//package org.firstinspires.ftc.teamcode.OpModes.Autonomous;
//
//import static com.pedropathing.api.Paths.curve;
//import static com.pedropathing.api.Paths.line;
//import static com.pedropathing.ivy.commands.Commands.waitMs;
//import static com.pedropathing.ivy.groups.Groups.sequential;
//import static com.pedropathing.ivy.pedro.PedroCommands.follow;
//
//import com.pedropathing.api.PoseFactory;
//import com.pedropathing.ivy.commands.Commands;
//import com.pedropathing.math.Pose;
//import com.pedropathing.paths.Path;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//
//import org.firstinspires.ftc.teamcode.pedro.CommandOpMode;
//import org.firstinspires.ftc.teamcode.pedro.Constants;
//
//import Subsystem.Robot;
//
//@Autonomous
//public class Autoauxilio extends CommandOpMode {
//
//    Robot r;
//
//    private boolean started = false;
//
//
//    private final PoseFactory p = PoseFactory.degrees();
//
//    public final Pose hive2 = p.of(-14, -125, 0);
//    public final Pose preFlor = p.of(0, -103, 270);
//    public final Pose controlFLor2 = p.of(6, -103, 90);
//    public final Pose flor1 = p.of(1, -118, 270);
//    public final Pose controlHive = p.of(35, -110, 90);
//    private final Pose park = p.of(38, -113, 180);
//
//
//
//
//
//    public Path Preflower(){
//    return line(hive2, preFlor).linear(hive2,preFlor);
//}
//
//public Path secondShoot(){
//    return line(preFlor, hive2).linear(preFlor, hive2);
//}
//    public Path volta(){return line(flor1, preFlor).constant(flor1);}
//
//    public Path flor() {
//        return line(preFlor, flor1).constant(flor1).with(Constants.foresightConfig.timeoutConstraint.at(0.100));
//    }
//
//    public Path parking() {
//        return curve(hive2, controlHive,park).linear(hive2, park);
//    }
//
//
//    @Override
//    public void init(){
//        r = new Robot(hardwareMap);
//        r.follower.setPose(hive2);
//        r.turret.resetTurret();
//        r.turret.off();
//
//        schedule(
//                Commands.infinite(() -> {
//                    r.updateAuto();
//                    r.follower.update();
//
//                    telemetry.addData("Pose", r.follower.pose());
//                    telemetry.addData("Follower Busy", r.follower.isBusy());
//                    telemetry.addData("Hive", r.turret.isUsingA() ? "A" : "B");
//                    telemetry.addData("Erro torreta (graus)", Math.toDegrees(r.turret.getError()));
//                    telemetry.addData("Torreta pronta", r.turret.isReady());
//                    telemetry.addData("Shooter pronto", r.shooter.atSpeed());
//                    telemetry.update();
//                }),
//
//                sequential(
//
//                        Commands.instant(r.intake::stop),
//                        Commands.instant(r.intake::up),
//                        Commands.instant(r.shooter::onAuto),
//                        Commands.waitMs(10000),
//                        Commands.instant(r.intake::spin),
//                        Commands.waitMs(700),
//                        Commands.instant(r.shooter::close),
//                        follow(r.follower, Preflower()),
//                        Commands.instant(r.intake::down),
//
//
//                        follow(r.follower, flor()),
//                        Commands.waitMs(1500),
//                        follow(r.follower,volta()),
//                        follow(r.follower, flor()),
//                        Commands.waitMs(1500),
//                        follow(r.follower, volta()),
//                        Commands.instant(r.intake::up),
//                        follow(r.follower, secondShoot()),
//                        Commands.waitMs(800),
//                        Commands.instant(r.shooter::open),
//                        waitMs(900),
//                        Commands.instant(r.shooter::close),
//                        follow(r.follower, parking()),
//                        Commands.instant(r.intake::stop),
//                        Commands.instant(r.shooter::stop)
//                )
//        );
//    }
//
//    @Override
//    public void start() {
//        super.start();
//        started = true;
//        r.follower.update();
//        r.turret.on();
//    }
//
//    @Override
//    public void stop() {
//        r.shooterOn = false;
//        r.shooter.stop();
//        r.saveEnd();
//    }
//
//
//}
