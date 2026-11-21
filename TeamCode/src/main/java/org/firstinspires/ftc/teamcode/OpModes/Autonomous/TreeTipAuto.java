//package org.firstinspires.ftc.teamcode.OpModes.Autonomous;
//
//import static com.pedropathing.api.Paths.line;
//import static com.pedropathing.ivy.commands.Commands.waitMs;
//import static com.pedropathing.ivy.groups.Groups.sequential;
//import static com.pedropathing.ivy.pedro.PedroCommands.follow;
//
//import com.pedropathing.api.PoseFactory;
//import com.pedropathing.ivy.Command;
//import com.pedropathing.ivy.commands.Commands;
//import com.pedropathing.math.Pose;
//
//import static com.pedropathing.api.Paths.curve;
//import static com.pedropathing.api.Paths.line;
//import org.firstinspires.ftc.teamcode.pedro.CommandOpMode;
//import com.pedropathing.paths.Path;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//
//
//import Subsystem.Robot;
//
//@Autonomous
//public class TreeTipAuto extends CommandOpMode {
//
//    Robot r;
//
//    Command pause = waitMs(1000);
//
//    /** A torreta só se mexe depois do START (regra: o robô não pode se mover no init). */
//    private boolean started = false;
//
//    private final PoseFactory p = PoseFactory.degrees();
//
//    public Pose shoot = p.of(0, 0, 0);
//
//    public Pose preGarden = p.of(47, -18, 90);
//    public Pose garden = p.of(47, -0.8, 90);
//    public Pose controlShoot2 = p.of(25, -9, 0);
//    public Pose controlFLor = p.of(25, -38, 0);
//    public Pose flor = p.of(39.2, -38, 0);
//    private final Pose park = p.of(38, -113, 180);
//
//
//    public  Path antesGarden(){
//        return line(shoot, preGarden).linear(shoot, preGarden);
//    }
//
//    public Path garden(){
//        return line(preGarden, garden).constant(garden);
//    }
//
//    public Path volta(){
//        return line(garden, preGarden).constant(preGarden);
//    }
//
//    public Path secondShoot(){
//        return curve(preGarden, controlShoot2, shoot).linear(preGarden, shoot);
//    }
//
//    public Path controlFlor(){
//        return line(shoot, controlFLor).constant(controlFLor);
//    }
//
//    public Path flower(){
//        return line(controlFLor, flor).constant(flor);
//    }
//
//    public Path voltaFlor(){
//        return line(flor, controlFLor).constant(controlFLor);
//    }
//
//    public Path shoot3(){
//        return line(controlFLor, shoot).constant(shoot);
//    }
//
//    public Path parking() {
//        return curve(shoot, controlFLor, park).linear(shoot, park);
//    }
//
//
//
//    @Override
//    public void init(){
//        r = new Robot(hardwareMap);
//        r.follower.setPose(shoot);
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
//                        Commands.instant(r.intake::stop),
//                        Commands.instant(r.intake::up),
//                        Commands.instant(r.shooter::onAuto),
//                        Commands.waitMs(2000),
//                        Commands.instant(r.intake::spin),
//                        Commands.waitMs(700),
//                        Commands.instant(r.shooter::close),
//                        follow(r.follower, antesGarden()),
//                        follow(r.follower, garden()),
//                        Commands.waitMs(1500),
//                        follow(r.follower, volta()),
//                        follow(r.follower, secondShoot()),
//                        Commands.waitMs(7000),
//                        Commands.instant(r.shooter::open),
//                        Commands.waitMs(600),
//                        Commands.instant(r.shooter::close),
//                        Commands.instant(r.intake::stop),
//                        follow(r.follower, controlFlor()),
//                        Commands.instant(r.intake::down),
//                        follow(r.follower, flower()),
//                        Commands.waitMs(1000),
//                        follow(r.follower, voltaFlor()),
//                        follow(r.follower, flower()),
//                        Commands.waitMs(1000),
//                        follow(r.follower, voltaFlor()),
//                        follow(r.follower, shoot3()),
//                        Commands.instant(r.shooter::open),
//                        Commands.waitMs(600),
//                        follow(r.follower, parking())
//                        )
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
