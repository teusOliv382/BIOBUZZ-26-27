package org.firstinspires.ftc.teamcode.OpModes.Autonomous;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.ArrayList;
import java.util.List;

@TeleOp(name = "AutoTempoOBR")
public class TeleOPoOBR extends OpMode {

    // =====================================================================
    //  COREOGRAFIA  —  edite aqui
    // =====================================================================
    //  passo(minuto, segundo, TIPO, força, valor)
    //
    //  Por TEMPO (valor = segundos):
    //    FRENTE, TRAS, STRAFE_DIREITA, STRAFE_ESQUERDA, TURN_DIREITA, TURN_ESQUERDA
    //
    //  Por ÂNGULO com IMU (valor = graus)  —  0° = FRENTE do robô no Play
    //    GIRAR_DIREITA 45   -> fica virado 45° pra direita da frente
    //    GIRAR_ESQUERDA 45  -> fica virado 45° pra esquerda da frente
    //    GIRAR_PARA         -> mesmo, com sinal (90 = direita, -90 = esquerda, 0 = frente)
    //    (o robô vai pelo caminho mais curto até o ângulo)
    //
    //  Giros prontos (não precisa de valor):
    //    GIRO_360_DIREITA, GIRO_360_ESQUERDA -> volta completa e termina
    //                                           na direção do último giro
    //
    //  Dança (valor = graus de cada lado)  —  0° = onde o robô está quando ela começa
    //    DANCA -> X° pra direita, volta pro meio, X° pra esquerda, volta pro meio
    //    MANAMAUE -> a mesma dança, 8 vezes seguidas sem parar
    //
    //  Triângulo no chão (valor = segundos de cada diagonal), sempre olhando pro heading 0:
    //    TRIANGULO_DIREITA  -> diagonal frente-direita, diagonal trás-direita (bico),
    //                          strafe pra esquerda de volta ao ponto inicial
    //    TRIANGULO_ESQUERDA -> o mesmo, espelhado pro lado esquerdo
    //
    //  Coloque os passos em ordem de tempo.
    // =====================================================================
    private void montarSequencia() {
        passo(0, 9.2,   Tipo.FRENTE,          0.4, 1.5);

        passo(0, 47.4,  Tipo.GIRAR_DIREITA,   0.45, 60);
        passo(0, 50.0,  Tipo.GIRAR_PARA,  0.45, 0);

        passo(0, 52.55, Tipo.STRAFE_ESQUERDA, 0.45, 0.8);
        passo(0, 54.75, Tipo.STRAFE_DIREITA,  0.45, 1.4);
        passo(0, 57.35, Tipo.STRAFE_ESQUERDA, 0.45, 0.7);
        passo(1, 0 , Tipo.GIRO_360_ESQUERDA, 0.45);

        passo(1, 7 , Tipo.GIRO_360_DIREITA, 0.45);

        passo(1, 30, Tipo.DANCA, 0.4, 30);
        passo(1, 40, Tipo.GIRO_360_DIREITA, 0.5);


        passo(1, 55, Tipo.DANCA, 0.4, 35);
        passo(2, 0, Tipo.GIRO_360_DIREITA, 0.5);

        passo(2, 32, Tipo.GIRAR_DIREITA, 0.5, 180);
        passo(2, 37, Tipo.GIRAR_PARA, 0.5, 0);

        passo(2, 45, Tipo.GIRO_360_DIREITA, 0.6);
        passo(2, 47.5, Tipo.GIRO_360_ESQUERDA, 0.6);
        passo(2, 50, Tipo.FRENTE, 0.4, 0.7);
        passo(2, 52, Tipo.TRAS, 0.4, 0.7);


        passo(3, 7, Tipo.MANAMAUE, 0.6, 40);
        passo(3, 17, Tipo.GIRAR_PARA, 0.5, 0);


        passo(3, 28, Tipo.GIRO_360_ESQUERDA, 0.5);
        passo(3, 42, Tipo.GIRO_360_DIREITA, 0.5);

        passo(3, 50, Tipo.MANAMAUE, 0.6, 40);
        passo(3, 17, Tipo.GIRAR_PARA, 0.5, 0);

        passo(4, 10, Tipo.STRAFE_DIREITA,  0.45, 0.8);

        passo(4, 20, Tipo.TRIANGULO_DIREITA, 0.5, 1);
        passo(4, 23, Tipo.TRIANGULO_ESQUERDA, 0.5, 1);


        // Exemplos:
        // passo(1, 30, Tipo.DANCA,             0.4, 45);
        // passo(1, 40, Tipo.GIRO_360_ESQUERDA, 0.4);
    }

    // ===== AJUSTES =====
    private static final double OFFSET_MUSICA = 0.0;      // diferença música x Play (s)
    private static final double DURACAO_MUSICA = 5 * 60;  // 5:00
    private static final double ANTECIPACAO = 2.0;        // para o motor X graus ANTES do alvo

    // Correção no fim do giro: para, espera assentar, confere o IMU e,
    // se ficou torto, dá um "empurrãozinho" devagar até chegar no alvo
    // (versão SUAVE: corrige só erro grande, com pouca força e no máximo 1 vez)
    private static final boolean CORRIGIR_GIRO = true;
    private static final double TOLERANCIA = 1.5;         // graus de erro aceitáveis
    private static final double FORCA_CORRECAO = 0.15;    // força do empurrãozinho  (antes 0.2)
    private static final double TEMPO_ASSENTAR = 0.2;     // s parado antes de conferir (antes 0.15)
    private static final int MAX_CORRECOES = 1;           // tentativas por giro      (antes 3)

    // Manter direção nos movimentos por tempo (frente, trás, strafe, triângulo):
    // só começa a corrigir quando entorta mais que ZONA_MORTA_MANTER
    // e para de corrigir quando volta pra menos de 1° (não fica "tremendo")
    private static final boolean MANTER_DIRECAO = true;
    private static final boolean MANTER_NO_STRAFE = true;  // false = não corrige durante strafe
    private static final double FORCA_MANTER = 0.05;       // (antes 0.08)
    private static final double ZONA_MORTA_MANTER = 1.0;   // graus

    // Manamauê: quantas vezes repete o conjunto da dança
    private static final int REPETICOES_MANAMAUE = 8;

    // Triângulo
    private static final double PAUSA_TRIANGULO = 0.1;       // s parado em cada canto (0 = sem parar)
    private static final double FATOR_STRAFE_TRIANGULO = 1.0; // tempo do strafe = valor x isso
    // (aumente se não voltar até o ponto inicial)

    // =====================================================================

    enum Tipo {
        FRENTE, TRAS, STRAFE_DIREITA, STRAFE_ESQUERDA, TURN_DIREITA, TURN_ESQUERDA,
        GIRAR_DIREITA, GIRAR_ESQUERDA, GIRAR_PARA,
        GIRO_360_DIREITA, GIRO_360_ESQUERDA,
        DANCA, MANAMAUE,
        TRIANGULO_DIREITA, TRIANGULO_ESQUERDA
    }

    enum Estado { AGUARDANDO, EXECUTANDO, FIM }

    enum FaseGiro { GIRANDO, ASSENTANDO, CORRIGINDO }

    static class Passo {
        final double inicio; final Tipo tipo; final double forca; final double valor;
        Passo(double inicio, Tipo tipo, double forca, double valor) {
            this.inicio = inicio; this.tipo = tipo; this.forca = forca; this.valor = valor;
        }
    }

    public DcMotor BL, BR, FL, FR;
    private IMU imu;

    private final ElapsedTime cronometro = new ElapsedTime();     // tempo da música
    private final ElapsedTime timerMovimento = new ElapsedTime(); // duração do passo / sub-giro

    private final List<Passo> passos = new ArrayList<>();
    private final List<String> historico = new ArrayList<>();
    private int indice = 0;
    private Estado estado = Estado.AGUARDANDO;

    // Giro: lista de alvos (1 alvo nos giros simples, 4 alvos na dança)
    private final List<Double> alvosGiro = new ArrayList<>();
    private int subGiro = 0;
    private double alvoAngulo = 0;
    private boolean giroParaDireita = true;
    private double timeoutGiro = 0;
    private double forcaGiro = 0;
    private boolean houveTimeout = false;
    private FaseGiro fase = FaseGiro.GIRANDO;
    private int correcoes = 0;
    private boolean corrigindoParaDireita = true;
    private final ElapsedTime timerFase = new ElapsedTime();

    // Movimento em partes (triângulo): cada parte = {fl, fr, bl, br, segundos}
    private final List<double[]> partes = new ArrayList<>();
    private final List<String> nomesPartes = new ArrayList<>();
    private int parte = 0;
    private boolean pausandoParte = false;

    // Manter direção: true enquanto está puxando de volta
    private boolean corrigindoDirecao = false;

    // Ângulo acumulado (positivo = direita), dá pra girar 360, 720...
    private double ultimoYaw = 0;
    private double anguloTotal = 0;

    // Direção que o robô DEVERIA estar (último alvo programado).
    // Os giros usam isso como referência, então o erro não acumula.
    private double direcaoProgramada = 0;

    private double ultimoAtraso = 0;
    private double maiorAtraso = 0;

    // ================= OPMODE =================

    @Override
    public void init() {
        FL = hardwareMap.get(DcMotor.class, "FL");
        FR = hardwareMap.get(DcMotor.class, "FR");
        BL = hardwareMap.get(DcMotor.class, "BL");
        BR = hardwareMap.get(DcMotor.class, "BR");

        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        BL.setDirection(DcMotorSimple.Direction.FORWARD);
        FR.setDirection(DcMotorSimple.Direction.REVERSE);
        BR.setDirection(DcMotorSimple.Direction.REVERSE);

        FL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // AJUSTE conforme o Control Hub está montado (logo e portas USB)
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)));
        imu.resetYaw();

        montarSequencia();
    }

    @Override
    public void init_loop() {
        atualizarAngulo();
        telemetry.addLine("Pronto - aperte PLAY junto com a música");
        telemetry.addData("Passos programados", passos.size());
        telemetry.addData("Ângulo (direita +)", "%.1f°", anguloTotal);
        telemetry.addLine("Teste: girando pra DIREITA o número tem que AUMENTAR");
        telemetry.update();
    }

    @Override
    public void start() {
        cronometro.reset(); // 0:00 da música
        // Zera o ângulo a partir de onde o robô está agora
        ultimoYaw = lerYaw();
        anguloTotal = 0;          // 0° = frente do robô no Play
        direcaoProgramada = 0;
        indice = 0;
        estado = Estado.AGUARDANDO;
    }

    @Override
    public void loop() {
        atualizarAngulo();
        double t = tempoMusica();

        switch (estado) {
            case AGUARDANDO:
                pararMotores();
                if (indice >= passos.size()) {
                    estado = Estado.FIM;
                } else if (t >= passos.get(indice).inicio) {
                    iniciarPasso(passos.get(indice), t);
                    estado = Estado.EXECUTANDO;
                }
                break;

            case EXECUTANDO:
                Passo p = passos.get(indice);
                if (!ehGiro(p)) aplicarMovimento(p); // atualiza correção de direção
                if (passoTerminou(p)) {
                    finalizarPasso(p);
                    indice++;
                    estado = Estado.AGUARDANDO;
                }
                break;

            case FIM:
                pararMotores();
                break;
        }

        mostrarTelemetria(t);
    }

    @Override
    public void stop() {
        pararMotores();
    }

    // ================= PASSOS =================

    private void passo(int minuto, double segundo, Tipo tipo, double forca, double valor) {
        passos.add(new Passo(minuto * 60 + segundo, tipo, forca, valor));
    }

    /** Para passos que não precisam de valor (ex: GIRO_360_DIREITA). */
    private void passo(int minuto, double segundo, Tipo tipo, double forca) {
        passo(minuto, segundo, tipo, forca, 0);
    }

    private void iniciarPasso(Passo p, double t) {
        ultimoAtraso = t - p.inicio;
        maiorAtraso = Math.max(maiorAtraso, ultimoAtraso);
        timerMovimento.reset();
        double f = p.forca;

        if (ehGiro(p)) {
            montarAlvos(p);
            forcaGiro = f;
            subGiro = 0;
            houveTimeout = false;
            correcoes = 0;
            comecarSubGiro();
            return;
        }

        corrigindoDirecao = false;

        if (ehEmPartes(p)) {
            montarPartes(p);
            parte = 0;
            pausandoParte = false;
            direcaoProgramada = paraAbsoluto(0); // segura o heading 0 (frente)
        }

        aplicarMovimento(p);
    }

    private boolean ehEmPartes(Passo p) {
        return p.tipo == Tipo.TRIANGULO_DIREITA || p.tipo == Tipo.TRIANGULO_ESQUERDA;
    }

    /** Monta as partes do triângulo. */
    private void montarPartes(Passo p) {
        partes.clear();
        nomesPartes.clear();
        double f = p.forca;
        double t = p.valor;
        double tStrafe = t * FATOR_STRAFE_TRIANGULO;

        if (p.tipo == Tipo.TRIANGULO_DIREITA) {
            partes.add(new double[]{ f, 0, 0, f, t });         nomesPartes.add("Diagonal frente-dir");
            partes.add(new double[]{ 0, -f, -f, 0, t });       nomesPartes.add("Diagonal trás-dir");
            partes.add(new double[]{ -f, f, f, -f, tStrafe }); nomesPartes.add("Strafe esq (volta)");
        } else {
            partes.add(new double[]{ 0, f, f, 0, t });         nomesPartes.add("Diagonal frente-esq");
            partes.add(new double[]{ -f, 0, 0, -f, t });       nomesPartes.add("Diagonal trás-esq");
            partes.add(new double[]{ f, -f, -f, f, tStrafe }); nomesPartes.add("Strafe dir (volta)");
        }
    }

    /** Movimentos por tempo. Frente/trás/strafe mantêm a direção com o IMU. */
    private void aplicarMovimento(Passo p) {
        double f = p.forca;
        double fl = 0, fr = 0, bl = 0, br = 0;

        if (ehEmPartes(p)) {
            if (pausandoParte) { pararMotores(); return; } // parado no canto
            double[] pt = partes.get(parte);
            fl = pt[0]; fr = pt[1]; bl = pt[2]; br = pt[3];
        } else switch (p.tipo) {
            case FRENTE:          fl = f;  fr = f;  bl = f;  br = f;  break;
            case TRAS:            fl = -f; fr = -f; bl = -f; br = -f; break;
            case STRAFE_DIREITA:  fl = f;  fr = -f; bl = -f; br = f;  break;
            case STRAFE_ESQUERDA: fl = -f; fr = f;  bl = f;  br = -f; break;
            case TURN_DIREITA:    mover(f, -f, f, -f); return;   // giro por tempo: sem correção
            case TURN_ESQUERDA:   mover(-f, f, -f, f); return;
            default: return;
        }

        boolean ehStrafe = p.tipo == Tipo.STRAFE_DIREITA || p.tipo == Tipo.STRAFE_ESQUERDA;
        if (MANTER_DIRECAO && (MANTER_NO_STRAFE || !ehStrafe)) {
            double erro = direcaoProgramada - anguloTotal; // > 0 = precisa ir pra direita

            // Liga a correção só com erro grande, desliga quando já está quase certo
            if (Math.abs(erro) > ZONA_MORTA_MANTER) corrigindoDirecao = true;
            else if (Math.abs(erro) < 1.0)          corrigindoDirecao = false;

            if (corrigindoDirecao) {
                double giro = erro > 0 ? FORCA_MANTER : -FORCA_MANTER;
                // giro > 0 = vira pra direita (mesmo padrão do turnDireita)
                fl += giro; fr -= giro; bl += giro; br -= giro;
            }
        }

        mover(fl, fr, bl, br);
    }

    /** Define os ângulos-alvo do giro (todos absolutos). */
    private void montarAlvos(Passo p) {
        alvosGiro.clear();
        double v = Math.abs(p.valor);
        double alvo;

        switch (p.tipo) {
            // Absolutos: 0 = frente do robô no Play
            case GIRAR_DIREITA:     alvo = paraAbsoluto(v); break;
            case GIRAR_ESQUERDA:    alvo = paraAbsoluto(-v); break;
            case GIRAR_PARA:        alvo = paraAbsoluto(p.valor); break;

            // Volta completa a partir da direção programada (corrige erro anterior)
            case GIRO_360_DIREITA:  alvo = direcaoProgramada + 360; break;
            case GIRO_360_ESQUERDA: alvo = direcaoProgramada - 360; break;

            case DANCA: {
                // Relativa: 0 = onde o robô está quando a dança começa
                double meio = anguloTotal;
                alvosGiro.add(meio + v);   // direita
                alvosGiro.add(meio);       // volta pro meio
                alvosGiro.add(meio - v);   // esquerda
                alvosGiro.add(meio);       // volta pro meio
                return;
            }

            case MANAMAUE: {
                // A mesma dança repetida, sempre com o mesmo meio
                double meio = anguloTotal;
                for (int i = 0; i < REPETICOES_MANAMAUE; i++) {
                    alvosGiro.add(meio + v);   // direita
                    alvosGiro.add(meio);       // volta pro meio
                    alvosGiro.add(meio - v);   // esquerda
                    alvosGiro.add(meio);       // volta pro meio
                }
                return;
            }
            default: return;
        }

        alvosGiro.add(alvo);
        direcaoProgramada = alvo;
    }

    /**
     * Converte um ângulo em relação à frente (ex: 45, -90) para o ângulo
     * acumulado mais próximo de onde o robô está (funciona mesmo depois de 360s).
     */
    private double paraAbsoluto(double anguloFrente) {
        return anguloTotal + normalizar(anguloFrente - anguloTotal);
    }

    /** Liga os motores na direção do alvo atual. */
    private void comecarSubGiro() {
        alvoAngulo = alvosGiro.get(subGiro);
        double graus = alvoAngulo - anguloTotal;
        giroParaDireita = graus > 0;
        timeoutGiro = Math.abs(graus) / 60.0 + 2.0; // segurança
        timerMovimento.reset();
        fase = FaseGiro.GIRANDO;

        double f = forcaGiro;
        if (giroParaDireita) mover(f, -f, f, -f);
        else                 mover(-f, f, -f, f);
    }

    private boolean ehGiro(Passo p) {
        switch (p.tipo) {
            case GIRAR_DIREITA: case GIRAR_ESQUERDA: case GIRAR_PARA:
            case GIRO_360_DIREITA: case GIRO_360_ESQUERDA: case DANCA: case MANAMAUE:
                return true;
            default:
                return false;
        }
    }

    private boolean passoTerminou(Passo p) {
        if (ehEmPartes(p)) {
            if (pausandoParte) {
                if (timerMovimento.seconds() >= PAUSA_TRIANGULO) {
                    pausandoParte = false;
                    timerMovimento.reset();
                }
                return false;
            }
            if (timerMovimento.seconds() >= partes.get(parte)[4]) {
                parte++;
                if (parte >= partes.size()) return true; // triângulo completo
                timerMovimento.reset();
                if (PAUSA_TRIANGULO > 0) {
                    pausandoParte = true;
                    pararMotores();
                }
            }
            return false;
        }

        if (!ehGiro(p)) {
            return timerMovimento.seconds() >= p.valor;
        }

        boolean ultimaParte = subGiro >= alvosGiro.size() - 1;

        switch (fase) {
            case GIRANDO: {
                // Para ANTECIPACAO graus antes do alvo
                boolean chegou = giroParaDireita ? anguloTotal >= alvoAngulo - ANTECIPACAO
                        : anguloTotal <= alvoAngulo + ANTECIPACAO;
                boolean timeout = timerMovimento.seconds() >= timeoutGiro;
                if (timeout) houveTimeout = true;

                if (chegou || timeout) {
                    if (!ultimaParte) {
                        // Dança: troca de lado direto, sem parar
                        subGiro++;
                        comecarSubGiro();
                    } else {
                        pararMotores();
                        fase = FaseGiro.ASSENTANDO;
                        timerFase.reset();
                    }
                }
                return false;
            }

            case ASSENTANDO: {
                // Espera o robô parar de vez antes de conferir
                if (timerFase.seconds() < TEMPO_ASSENTAR) return false;

                double erro = alvoAngulo - anguloTotal;
                if (CORRIGIR_GIRO && Math.abs(erro) > TOLERANCIA && correcoes < MAX_CORRECOES) {
                    correcoes++;
                    corrigindoParaDireita = erro > 0;
                    double c = FORCA_CORRECAO;
                    if (corrigindoParaDireita) mover(c, -c, c, -c);
                    else                       mover(-c, c, -c, c);
                    timerMovimento.reset();
                    fase = FaseGiro.CORRIGINDO;
                    return false;
                }
                return true; // giro terminado
            }

            case CORRIGINDO: {
                // Empurrãozinho devagar: para assim que chegar no alvo
                boolean chegou = corrigindoParaDireita ? anguloTotal >= alvoAngulo
                        : anguloTotal <= alvoAngulo;
                if (chegou || timerMovimento.seconds() > 1.5) {
                    pararMotores();
                    fase = FaseGiro.ASSENTANDO;
                    timerFase.reset();
                }
                return false;
            }
        }
        return false;
    }

    private void finalizarPasso(Passo p) {
        pararMotores();
        String resultado;
        if (ehGiro(p)) {
            resultado = String.format("%s: parou em %.1f° (alvo %.1f°, erro %.1f°, %d corr.)%s",
                    descrever(p), anguloTotal, alvoAngulo, anguloTotal - alvoAngulo,
                    correcoes, houveTimeout ? " TIMEOUT!" : "");
        } else {
            // Giro por tempo muda a direção: atualiza a referência
            if (p.tipo == Tipo.TURN_DIREITA || p.tipo == Tipo.TURN_ESQUERDA) {
                direcaoProgramada = anguloTotal;
            }
            resultado = String.format("%s: ok (direção %.1f°)", descrever(p), normalizar(anguloTotal));
        }
        historico.add(0, String.format("[%s] %s | atraso %.2fs",
                formatar(p.inicio), resultado, ultimoAtraso));
        if (historico.size() > 4) historico.remove(historico.size() - 1);
    }

    private String descrever(Passo p) {
        switch (p.tipo) {
            case FRENTE:            return String.format("Frente %.1fs", p.valor);
            case TRAS:              return String.format("Trás %.1fs", p.valor);
            case STRAFE_DIREITA:    return String.format("Strafe Dir %.1fs", p.valor);
            case STRAFE_ESQUERDA:   return String.format("Strafe Esq %.1fs", p.valor);
            case TURN_DIREITA:      return String.format("Turn Dir %.1fs", p.valor);
            case TURN_ESQUERDA:     return String.format("Turn Esq %.1fs", p.valor);
            case GIRAR_DIREITA:     return String.format("Girar Dir %.0f°", p.valor);
            case GIRAR_ESQUERDA:    return String.format("Girar Esq %.0f°", p.valor);
            case GIRAR_PARA:        return String.format("Girar para %.0f°", p.valor);
            case GIRO_360_DIREITA:  return "Giro 360 Dir";
            case GIRO_360_ESQUERDA: return "Giro 360 Esq";
            case DANCA:             return String.format("Dança %.0f°", p.valor);
            case MANAMAUE:          return String.format("Manamauê %.0f° x%d", p.valor, REPETICOES_MANAMAUE);
            case TRIANGULO_DIREITA: return String.format("Triângulo Dir %.2fs", p.valor);
            case TRIANGULO_ESQUERDA:return String.format("Triângulo Esq %.2fs", p.valor);
        }
        return p.tipo.toString();
    }

    // ================= TELEMETRIA =================

    private void mostrarTelemetria(double t) {
        telemetry.addData("CRONÔMETRO", "%s / %s", formatar(t), formatar(DURACAO_MUSICA));
        telemetry.addData("Passo", "%d / %d", Math.min(indice + 1, passos.size()), passos.size());

        if (estado == Estado.EXECUTANDO) {
            Passo p = passos.get(indice);
            telemetry.addData("AGORA", descrever(p));
            if (ehGiro(p)) {
                telemetry.addData("Giro", "%.1f° → alvo %.1f°", anguloTotal, alvoAngulo);
                String nomeFase = fase == FaseGiro.GIRANDO ? "Girando"
                        : fase == FaseGiro.ASSENTANDO ? "Conferindo"
                        : "Corrigindo " + correcoes + "/" + MAX_CORRECOES;
                telemetry.addData("Fase", nomeFase);
                if (alvosGiro.size() > 1) {
                    telemetry.addData("Parte", "%d / %d", Math.min(subGiro + 1, alvosGiro.size()), alvosGiro.size());
                }
            } else if (ehEmPartes(p)) {
                int n = Math.min(parte, partes.size() - 1);
                telemetry.addData("Parte", "%d / %d  %s", n + 1, partes.size(),
                        pausandoParte ? "(canto)" : nomesPartes.get(n));
                telemetry.addData("Tempo", "%.2f / %.2f s", timerMovimento.seconds(),
                        pausandoParte ? PAUSA_TRIANGULO : partes.get(n)[4]);
                telemetry.addData("Desvio da direção", "%.1f°", anguloTotal - direcaoProgramada);
            } else {
                telemetry.addData("Tempo", "%.1f / %.1f s", timerMovimento.seconds(), p.valor);
                telemetry.addData("Desvio da direção", "%.1f°", anguloTotal - direcaoProgramada);
            }
        } else if (estado == Estado.AGUARDANDO && indice < passos.size()) {
            Passo p = passos.get(indice);
            telemetry.addData("AGORA", "Parado");
            telemetry.addData("Próximo", "%s às %s (em %.1f s)",
                    descrever(p), formatar(p.inicio), Math.max(0, p.inicio - t));
        } else {
            telemetry.addData("AGORA", t < DURACAO_MUSICA ? "Sequência terminou" : "Música terminou");
        }

        telemetry.addData("Direção (0 = frente)", "%.1f°", normalizar(anguloTotal));
        telemetry.addData("Ângulo acumulado", "%.1f°", anguloTotal);
        telemetry.addData("Atraso último / maior", "%.2f s / %.2f s", ultimoAtraso, maiorAtraso);

        telemetry.addLine("--- Últimos passos ---");
        for (String linha : historico) telemetry.addLine(linha);

        telemetry.update();
    }

    // ================= IMU / CRONÔMETRO =================

    /** Yaw com positivo = direita. */
    private double lerYaw() {
        return -imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private void atualizarAngulo() {
        double yaw = lerYaw();
        anguloTotal += normalizar(yaw - ultimoYaw);
        ultimoYaw = yaw;
    }

    private double normalizar(double graus) {
        while (graus > 180) graus -= 360;
        while (graus < -180) graus += 360;
        return graus;
    }

    private double tempoMusica() {
        return cronometro.seconds() + OFFSET_MUSICA;
    }

    private String formatar(double segundos) {
        if (segundos < 0) segundos = 0;
        int min = (int) (segundos / 60);
        double seg = segundos - min * 60;
        return String.format("%d:%04.1f", min, seg);
    }

    // ================= MOTORES =================

    private void mover(double fl, double fr, double bl, double br) {
        FL.setPower(Range.clip(fl, -1, 1));
        FR.setPower(Range.clip(fr, -1, 1));
        BL.setPower(Range.clip(bl, -1, 1));
        BR.setPower(Range.clip(br, -1, 1));
    }

    public void pararMotores() {
        FL.setPower(0);
        FR.setPower(0);
        BL.setPower(0);
        BR.setPower(0);
    }
}