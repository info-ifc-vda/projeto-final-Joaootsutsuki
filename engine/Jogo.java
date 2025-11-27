package engine;

import entidades.Jogador;
import entidades.Monstro;
import items.Chest;
import items.Weapon;
import mundo.Mapa;
import mundo.Posicao;
import java.util.ArrayList;
import java.util.List;

public class Jogo {
    private Mapa mapa;
    private Jogador jogador;
    private List<Monstro> monstros;
    private List<Chest> chests;
    private MessageLog log;
    private boolean emCombate;
    private Monstro inimigoAtual;
    private int nivelAtual;
    private boolean showingInventory;
    private boolean showingChestLoot;
    private Chest currentChest;

    public void iniciar() {
        nivelAtual = 1;
        log = new MessageLog();
        showingInventory = false;
        showingChestLoot = false;

        carregarNivel();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Input.cleanup();
            Renderizador.limparTela();
        }));

        log.adicionar("You enter the dungeon...");
        log.adicionar("Press [I] to open inventory, [E] to interact with chests");
        loopDoJogo();
    }

    private void carregarNivel() {
        mapa = new Mapa(60, 30);
        chests = new ArrayList<>();

        if (nivelAtual == 1) {
            jogador = new Jogador("Hero", mapa.posicaoAleatoria());
        } else {
            Posicao novaPos = mapa.posicaoAleatoria();
            jogador.mover(novaPos.x() - jogador.posicao().x(),
                    novaPos.y() - jogador.posicao().y(), mapa);
        }

        monstros = new ArrayList<>();
        int numMonstros = 2 + nivelAtual;

        String VERDE = "\u001B[32m";
        List<Posicao> posicoesUsadas = new ArrayList<>();
        posicoesUsadas.add(jogador.posicao());

        for (int i = 0; i < numMonstros; i++) {
            Posicao pos = encontrarPosicaoValida(posicoesUsadas, 10);
            posicoesUsadas.add(pos);

            int hp = 10 + nivelAtual * 3;
            int atk = 2 + nivelAtual;
            Monstro goblin = new Monstro("Goblin", "■", VERDE, pos, hp, atk, 1);
            monstros.add(goblin);
        }

        log.adicionar("=== Level " + nivelAtual + " ===");
    }

    private Posicao encontrarPosicaoValida(List<Posicao> posicoesUsadas, int distanciaMinima) {
        int tentativas = 0;

        while (tentativas < 100) {
            Posicao novaPosicao = mapa.posicaoAleatoriaLongeDoJogador(jogador.posicao(), distanciaMinima);

            boolean muitoPerto = false;
            for (Posicao usada : posicoesUsadas) {
                double dist = Math.sqrt(Math.pow(novaPosicao.x() - usada.x(), 2) +
                        Math.pow(novaPosicao.y() - usada.y(), 2));
                if (dist < distanciaMinima) {
                    muitoPerto = true;
                    break;
                }
            }

            if (!muitoPerto) {
                return novaPosicao;
            }

            tentativas++;
        }

        return mapa.posicaoAleatoriaLongeDoJogador(jogador.posicao(), 5);
    }

    private void loopDoJogo() {
        while (jogador.vivo()) {
            Renderizador.limparTela();

            if (showingInventory) {
                mostrarInventario();
            } else if (showingChestLoot) {
                mostrarBauLoot();
            } else {
                Monstro[] arrayMonstros = monstros.toArray(new Monstro[0]);
                Renderizador.renderizar(mapa, jogador, log, chests, arrayMonstros);
            }

            char tecla = Input.getKey();

            if (showingInventory) {
                processarInventario(tecla);
            } else if (showingChestLoot) {
                processarBauLoot(tecla);
            } else if (emCombate) {
                processarCombate(tecla);
            } else {
                processarMovimento(tecla);
            }
        }

        gameOver();
    }

    private void mostrarInventario() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                         INVENTORY                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        if (jogador.inventory().size() == 0) {
            System.out.println("  Your inventory is empty.");
        } else {
            System.out.println("  Press number to equip weapon, [D]+number to drop, [I] to close");
            System.out.println();

            List<Weapon> weapons = jogador.inventory().getWeapons();
            Weapon equipped = jogador.inventory().getEquippedWeapon();

            for (int i = 0; i < weapons.size(); i++) {
                Weapon w = weapons.get(i);
                String equipMark = (w == equipped) ? " [EQUIPPED]" : "";
                String color = getRarityColor(w.rarity());
                System.out.printf("  [%d] %s%-40s%s Lv.%-3d DMG: %d%s\n",
                        i + 1, color, w.name(), "\u001B[0m", w.level(), w.damage(), equipMark);
            }
        }

        System.out.println();
        System.out.println("─".repeat(64));
    }

    private void mostrarBauLoot() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      CHEST CONTENTS                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Press number to take weapon, [E] to close chest");
        System.out.println();

        List<Weapon> weapons = currentChest.weapons();
        for (int i = 0; i < weapons.size(); i++) {
            Weapon w = weapons.get(i);
            String color = getRarityColor(w.rarity());
            System.out.printf("  [%d] %s%-40s%s Lv.%-3d DMG: %d\n",
                    i + 1, color, w.name(), "\u001B[0m", w.level(), w.damage());
        }

        System.out.println();
        System.out.println("─".repeat(64));
        System.out.println("Inventory: " + jogador.inventory().size() + "/20");
    }

    private String getRarityColor(String rarity) {
        return switch (rarity) {
            case "Common" -> "\u001B[37m"; // White
            case "Rare" -> "\u001B[34m"; // Blue
            case "Epic" -> "\u001B[35m"; // Magenta
            case "Legendary" -> "\u001B[33m"; // Yellow
            default -> "\u001B[0m";
        };
    }

    private void processarInventario(char tecla) {
        if (tecla == 'i') {
            showingInventory = false;
            return;
        }

        if (tecla >= '1' && tecla <= '9') {
            int index = tecla - '1';
            if (index < jogador.inventory().size()) {
                jogador.inventory().equipWeapon(index);
                log.adicionar("Equipped: " + jogador.inventory().getEquippedWeapon().name());
            }
        }
    }

    private void processarBauLoot(char tecla) {
        if (tecla == 'e') {
            showingChestLoot = false;
            currentChest = null;
            return;
        }

        if (tecla >= '1' && tecla <= '9') {
            int index = tecla - '1';
            if (index < currentChest.weapons().size()) {
                if (jogador.inventory().isFull()) {
                    log.adicionar("Inventory is full!");
                } else {
                    Weapon taken = currentChest.weapons().remove(index);
                    jogador.inventory().addWeapon(taken);
                    log.adicionar("Took: " + taken.name());

                    if (currentChest.weapons().isEmpty()) {
                        showingChestLoot = false;
                        currentChest = null;
                    }
                }
            }
        }
    }

    private void processarMovimento(char tecla) {
        int oldX = jogador.posicao().x();
        int oldY = jogador.posicao().y();

        switch (tecla) {
            case 'w' -> jogador.mover(0, -1, mapa);
            case 's' -> jogador.mover(0, 1, mapa);
            case 'a' -> jogador.mover(-1, 0, mapa);
            case 'd' -> jogador.mover(1, 0, mapa);
            case 'i' -> {
                showingInventory = true;
                return;
            }
            case 'e' -> {
                verificarBau();
                return;
            }
            case 'q' -> {
                Input.cleanup();
                System.exit(0);
            }
        }

        if (jogador.posicao().x() != oldX || jogador.posicao().y() != oldY) {
            verificarEscada();
            verificarColisao();
        }
    }

    private void verificarBau() {
        Posicao pos = jogador.posicao();

        for (Chest chest : chests) {
            if (!chest.isOpened() && colidem(pos, chest.posicao())) {
                currentChest = chest;
                showingChestLoot = true;
                log.adicionar("You opened a chest!");
                return;
            }
        }

        log.adicionar("No chest nearby.");
    }

    private void verificarEscada() {
        Posicao pos = jogador.posicao();

        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                if (mapa.isEscada(pos.x() + dx, pos.y() + dy)) {
                    nivelAtual++;
                    log.limpar();
                    log.adicionar("You descend to level " + nivelAtual + "...");
                    carregarNivel();
                    return;
                }
            }
        }
    }

    private void processarCombate(char tecla) {
        switch (tecla) {
            case '1' -> ataqueBasico();
            case '2' -> ataquePoderoso();
            case '3' -> magiaFogo();
            case 'r' -> tentarFugir();
        }
    }


    // por ventura coloquei todas as classes de ataque aqui, mas como nós vamos fazer a classe de mago e guerreiro
    // acho que dá pra mover tudo isso pra abstração do personagem e daí so dar override nas heranças, enfim, se quiser mexer nisso se divirta

    private void ataqueBasico() {
        int dano = jogador.atacar();
        inimigoAtual.receberDano(dano);
        log.adicionar(String.format("You attack %s for %d damage!",
                inimigoAtual.nome(), dano));

        if (verificarMorteInimigo())
            return;
        turnoInimigo();
    }

    private void ataquePoderoso() {
        int custoMana = 5;
        if (!jogador.usarMana(custoMana)) {
            log.adicionar("Not enough mana!");
            return;
        }

        int dano = (int) (jogador.atacar() * 1.5);
        inimigoAtual.receberDano(dano);
        log.adicionar(String.format("Power Attack! %d damage!", dano));

        if (verificarMorteInimigo())
            return;
        turnoInimigo();
    }

    private void magiaFogo() {
        int custoMana = 8;
        if (!jogador.usarMana(custoMana)) {
            log.adicionar("Not enough mana!");
            return;
        }

        int dano = 10 + (int) (Math.random() * 5);
        inimigoAtual.receberDano(dano);
        log.adicionar(String.format("Fireball! %d damage!", dano));

        if (verificarMorteInimigo())
            return;
        turnoInimigo();
    }

    private void tentarFugir() {
        if (Math.random() < 0.5) {
            log.adicionar("You fled from combat!");
            emCombate = false;
            inimigoAtual = null;
            jogador.mover(-3, 0, mapa);
        } else {
            log.adicionar("You failed to escape!");
            turnoInimigo();
        }
    }

    private void turnoInimigo() {
        int dano = inimigoAtual.atacar();
        jogador.receberDano(dano);
        log.adicionar(String.format("%s attacks you for %d damage!",
                inimigoAtual.nome(), dano));

        if (!jogador.vivo()) {
            log.adicionar("You were defeated...");
        }
    }

    private boolean verificarMorteInimigo() {
        if (!inimigoAtual.vivo()) {
            int xpGanho = 10 * nivelAtual;
            jogador.ganharXP(xpGanho);
            log.adicionar(String.format("%s defeated! +%d XP",
                    inimigoAtual.nome(), xpGanho));

            // spawn chest at monster's position
            Chest newChest = new Chest(inimigoAtual.posicao(), nivelAtual);
            chests.add(newChest);
            log.adicionar("A chest appeared!");

            emCombate = false;
            inimigoAtual = null;
            return true;
        }
        return false;
    }

    private void verificarColisao() {
        Posicao pj = jogador.posicao();

        for (Monstro monstro : monstros) {
            if (monstro.vivo() && colidem(pj, monstro.posicao())) {
                emCombate = true;
                inimigoAtual = monstro;
                log.adicionar("You encountered a " + monstro.nome() + "!");
                log.adicionar("=== COMBAT ===");
                log.adicionar("[1] Basic Attack  [2] Power Attack (5 MP)");
                log.adicionar("[3] Fireball (8 MP)  [R] Run");
                break;
            }
        }
    }

    private boolean colidem(Posicao p1, Posicao p2) {
        return Math.abs(p1.x() - p2.x()) < 2 && Math.abs(p1.y() - p2.y()) < 2;
    }

    private void gameOver() {
        Renderizador.limparTela();
        System.out.println();
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║        YOU DIED!               ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.println();
        System.out.println("Level reached: " + nivelAtual);
        System.out.println("Total XP: " + jogador.xp());
        System.out.println();
        System.out.println("Press Q to quit...");

        while (Input.getKey() != 'q')
            ;
        Input.cleanup();
        System.exit(0);
    }
}