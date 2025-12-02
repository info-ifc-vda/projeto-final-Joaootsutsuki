package engine;

import entidades.Jogador;
import entidades.Monstro;
import items.Chest;
import items.Weapon;
import mundo.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private Dungeon dungeon;
    private Jogador jogador;
    private MessageLog log;
    private boolean emCombate;
    private Monstro inimigoAtual;
    private boolean showingInventory;
    private boolean showingChestLoot;
    private Chest currentChest;
    private Random rand;

    public void start() {
        rand = new Random();
        log = new MessageLog();
        showingInventory = false;
        showingChestLoot = false;

        // Create dungeon with floors
        dungeon = new Dungeon();

        // Place player in starting room
        Room startRoom = dungeon.getCurrentLevel().getCurrentRoom();
        jogador = new Jogador("Hero", startRoom.mapa().posicaoAleatoria());

        // Place stairs in starting room (doors are already set up during map
        // generation)
        startRoom.mapa().placeStairs(startRoom.hasStairsUp(), startRoom.hasStairsDown());

        // Don't spawn monsters in starting room

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Input.cleanup();
            Renderizador.limparTela();
        }));

        log.adicionar("You enter the dungeon...");
        log.adicionar("Press [I] inventory, [E] interact, [M] minimap");
        loopDoJogo();
    }

    private void setupRoom(Room room) {
        // Room.generateMap() now handles doors automatically
        // Just place stairs after
        room.mapa().placeStairs(room.hasStairsUp(), room.hasStairsDown());
    }

    private void spawnMonstersInRoom(Room room, int floorLevel) {
        // Skip if start room or already has monsters
        if (room.type() == Room.RoomType.START) {
            return;
        }

        if (!room.monstros().isEmpty()) {
            return; // Already spawned
        }

        int numMonstros = switch (room.type()) {
            case BOSS -> 1; // Boss room - 1 strong enemy
            case TREASURE -> 2 + rand.nextInt(2); // 2-3 monsters
            default -> 2 + rand.nextInt(3); // 2-4 monsters
        };

        String VERDE = "\u001B[32m";
        String VERMELHO = "\u001B[31m";

        for (int i = 0; i < numMonstros; i++) {
            Posicao pos = room.mapa().posicaoAleatoria();

            boolean isBoss = (room.type() == Room.RoomType.BOSS);
            int hpMultiplier = isBoss ? 3 : 1;
            int hp = (10 + floorLevel * 3) * hpMultiplier;
            int atk = (2 + floorLevel) * hpMultiplier;

            String name = isBoss ? "Boss Goblin" : "Goblin";
            String color = isBoss ? VERMELHO : VERDE;
            Monstro goblin = new Monstro(name, "■", color, pos, hp, atk, 1);
            room.monstros().add(goblin);
        }

        log.adicionar("Spawned " + numMonstros + " enemies!");
    }

    private Posicao encontrarPosicaoValida(Mapa mapa, List<Posicao> posicoesUsadas, int distanciaMinima) {
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
            try {
                Renderizador.limparTela();

                if (showingInventory) {
                    mostrarInventario();
                } else if (showingChestLoot) {
                    mostrarBauLoot();
                } else {
                    Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
                    Monstro[] arrayMonstros = currentRoom.monstros().toArray(new Monstro[0]);
                    Renderizador.renderizar(currentRoom.mapa(), jogador, log,
                            currentRoom.chests(), arrayMonstros);
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
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
                e.printStackTrace();
                System.err.println("Press any key to continue...");
                Input.getKey();
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
            System.out.println("  Press number to equip weapon, [I] to close");
            System.out.println();

            List<Weapon> weapons = jogador.inventory().getWeapons();
            Weapon equipped = jogador.inventory().getEquippedWeapon();

            for (int i = 0; i < weapons.size(); i++) {
                Weapon w = weapons.get(i);
                String equipMark = (w == equipped) ? " [EQUIPPED]" : "";
                String color = getRarityColor(w.rarity());

                int actualDamage = w.getDamageWithStats(
                        jogador.stats().strength(),
                        jogador.stats().dexterity(),
                        jogador.stats().intelligence());

                System.out.printf("  [%d] %s%-35s%s Lv.%-2d [%d→%d DMG] <%s> %.1fkg%s\n",
                        i + 1, color, w.name(), "\u001B[0m", w.level(),
                        w.baseDamage(), actualDamage, w.getTypeSymbol(), w.weight(), equipMark);
            }
        }

        System.out.println();
        System.out.println("─".repeat(64));
    }

    private void mostrarBauLoot() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        String title = currentChest.corpseName();
        int padding = (62 - title.length()) / 2;
        System.out.printf("║%s%s%s║\n", " ".repeat(padding), title, " ".repeat(62 - padding - title.length()));
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Press number to take weapon, [E] to close");
        System.out.println();

        List<Weapon> weapons = currentChest.weapons();
        for (int i = 0; i < weapons.size(); i++) {
            Weapon w = weapons.get(i);
            String color = getRarityColor(w.rarity());
            System.out.printf("  [%d] %s%-35s%s Lv.%-2d [%d DMG] <%s> %.1fkg\n",
                    i + 1, color, w.name(), "\u001B[0m", w.level(), w.baseDamage(), w.getTypeSymbol(), w.weight());
        }

        System.out.println();
        System.out.println("─".repeat(64));
        System.out.printf("Inventory: %d/20 | Weight: %.1f/%d\n",
                jogador.inventory().size(),
                jogador.inventory().getCurrentWeight(),
                jogador.stats().maxCarryWeight());
    }

    private String getRarityColor(String rarity) {
        return switch (rarity) {
            case "Common" -> "\u001B[37m";
            case "Rare" -> "\u001B[34m";
            case "Epic" -> "\u001B[35m";
            case "Legendary" -> "\u001B[33m";
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
                double maxWeight = jogador.stats().maxCarryWeight();
                Weapon toTake = currentChest.weapons().get(index);

                if (jogador.inventory().isFull()) {
                    log.adicionar("Inventory is full!");
                } else if (jogador.inventory().getCurrentWeight() + toTake.weight() > maxWeight) {
                    log.adicionar("Too heavy! Need more endurance");
                } else {
                    Weapon taken = currentChest.weapons().remove(index);
                    jogador.inventory().addWeapon(taken, maxWeight);
                    log.adicionar("Took: " + taken.name());

                    if (currentChest.weapons().isEmpty()) {
                        currentChest.open();
                        showingChestLoot = false;
                        currentChest = null;
                    }
                }
            }
        }
    }

    private void processarMovimento(char tecla) {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Mapa mapa = currentRoom.mapa();

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
                verificarBau(currentRoom);
                return;
            }
            case 'm' -> {
                mostrarMiniMapa();
                Input.getKey();
                return;
            }
            case 'q' -> {
                Input.cleanup();
                System.exit(0);
            }
        }

        if (jogador.posicao().x() != oldX || jogador.posicao().y() != oldY) {
            verificarEscada();
            verificarTransicaoSala();
            verificarColisao(currentRoom);
            currentRoom.checkCleared();
        }
    }

    private void verificarEscada() {
        Posicao pos = jogador.posicao();
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Mapa mapa = currentRoom.mapa();

        // Check all 4 tiles of player sprite for stairs
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int checkX = pos.x() + dx;
                int checkY = pos.y() + dy;

                if (mapa.isStairs(checkX, checkY)) {
                    Bloco stairsType = mapa.getStairsType(checkX, checkY);

                    if (stairsType == Bloco.STAIRS_DOWN) {
                        dungeon.descendFloor();
                        log.limpar();
                        log.adicionar("Descended to Floor " + dungeon.getCurrentFloorNumber());

                        // Setup new floor's starting room
                        Room newRoom = dungeon.getCurrentLevel().getCurrentRoom();
                        if (!newRoom.discovered()) {
                            setupRoom(newRoom);
                            spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber());
                        }

                        // Move player to stairs up position
                        Posicao newPos = newRoom.mapa().posicaoAleatoria();
                        jogador.mover(newPos.x() - jogador.posicao().x(),
                                newPos.y() - jogador.posicao().y(), newRoom.mapa());

                    } else if (stairsType == Bloco.STAIRS_UP) {
                        dungeon.ascendFloor();
                        log.limpar();
                        log.adicionar("Ascended to Floor " + dungeon.getCurrentFloorNumber());

                        // Return to previous floor
                        Room oldRoom = dungeon.getCurrentLevel().getCurrentRoom();
                        Posicao returnPos = oldRoom.mapa().posicaoAleatoria();
                        jogador.mover(returnPos.x() - jogador.posicao().x(),
                                returnPos.y() - jogador.posicao().y(), oldRoom.mapa());
                    }

                    return;
                }
            }
        }
    }

    private void verificarTransicaoSala() {
        Posicao pos = jogador.posicao();
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Mapa mapa = currentRoom.mapa();

        // Check all 4 tiles of player sprite for door collision
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int checkX = pos.x() + dx;
                int checkY = pos.y() + dy;

                if (mapa.isDoor(checkX, checkY)) {
                    Bloco doorType = mapa.getDoorType(checkX, checkY);
                    transitionToRoom(doorType);
                    return;
                }
            }
        }
    }

    private void transitionToRoom(Bloco doorType) {
        DungeonLevel currentLevel = dungeon.getCurrentLevel();

        log.adicionar("DEBUG: Transitioning room...");

        switch (doorType) {
            case DOOR_NORTH -> currentLevel.moveNorth();
            case DOOR_SOUTH -> currentLevel.moveSouth();
            case DOOR_EAST -> currentLevel.moveEast();
            case DOOR_WEST -> currentLevel.moveWest();
        }

        Room newRoom = currentLevel.getCurrentRoom();

        log.adicionar("DEBUG: Room discovered? " + newRoom.discovered());
        log.adicionar("DEBUG: Room type: " + newRoom.type());
        log.adicionar("DEBUG: Current monsters: " + newRoom.monstros().size());

        // If entering room for first time, spawn monsters
        if (!newRoom.discovered()) {
            newRoom.setDiscovered(true);
            log.adicionar("DEBUG: Spawning monsters...");
            spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber());
            log.adicionar("DEBUG: After spawn, monsters: " + newRoom.monstros().size());
        }

        // Place player at opposite door - ensure it's on floor
        Mapa newMapa = newRoom.mapa();
        Posicao newPos;

        switch (doorType) {
            case DOOR_NORTH -> {
                newPos = findSafeSpawnNear(newMapa, newMapa.comoArrayString()[0].length / 2,
                        newMapa.comoArrayString().length - 8);
            }
            case DOOR_SOUTH -> {
                newPos = findSafeSpawnNear(newMapa, newMapa.comoArrayString()[0].length / 2, 8);
            }
            case DOOR_EAST -> {
                newPos = findSafeSpawnNear(newMapa, 8, newMapa.comoArrayString().length / 2);
            }
            case DOOR_WEST -> {
                newPos = findSafeSpawnNear(newMapa, newMapa.comoArrayString()[0].length - 8,
                        newMapa.comoArrayString().length / 2);
            }
            default -> newPos = newMapa.posicaoAleatoria();
        }
        ;

        jogador.mover(newPos.x() - jogador.posicao().x(),
                newPos.y() - jogador.posicao().y(), newMapa);

        log.adicionar("Entered " + getRoomDescription(newRoom));
    }

    private Posicao findSafeSpawnNear(Mapa mapa, int targetX, int targetY) {
        // try to find a walkable 2x2 area near the target position
        for (int radius = 0; radius < 15; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = targetX + dx;
                    int y = targetY + dy;

                    // check if this position and the 2x2 area is walkable
                    if (mapa.podeAndar(x, y) && mapa.podeAndar(x + 1, y) &&
                            mapa.podeAndar(x, y + 1) && mapa.podeAndar(x + 1, y + 1)) {
                        return new Posicao(x, y);
                    }
                }
            }
        }

        // fallback to random position if no safe spot found
        return mapa.posicaoAleatoria();
    }

    private String getRoomDescription(Room room) {
        return switch (room.type()) {
            case START -> "the starting room";
            case BOSS -> "a BOSS ROOM!";
            case TREASURE -> "a treasure room";
            default -> room.cleared() ? "a cleared room" : "a new room";
        };
    }

    private void mostrarMiniMapa() {
        Renderizador.limparTela();
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║   MINIMAP - Floor " + dungeon.getCurrentFloorNumber() + "          ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.println();

        DungeonLevel level = dungeon.getCurrentLevel();
        Room[][] grid = level.roomGrid();
        int size = level.gridSize();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Room room = grid[y][x];

                if (x == level.currentRoomX() && y == level.currentRoomY()) {
                    System.out.print(" @@ "); // current position
                } else if (room == null) {
                    System.out.print(" .. ");
                } else if (!room.discovered()) {
                    System.out.print(" ?? ");
                } else {
                    String symbol = switch (room.type()) {
                        case START -> " SS ";
                        case BOSS -> " BB ";
                        case TREASURE -> " TT ";
                        default -> room.cleared() ? " -- " : " ## ";
                    };
                    System.out.print(symbol);
                }
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("Floor: " + dungeon.getCurrentFloorNumber() + "/" + dungeon.getTotalFloorsGenerated());
        System.out.println();
        System.out.println("Legend: @@ You | SS Start | BB Boss | TT Treasure");
        System.out.println("        ## Enemies | -- Cleared | ?? Undiscovered");
        System.out.println("        ↑ Stairs Up | ↓ Stairs Down");
        System.out.println();
        System.out.println("Press any key to close...");
    }

    private void verificarBau(Room room) {
        Posicao pos = jogador.posicao();

        for (Chest chest : room.chests()) {
            if (!chest.isOpened() && colidem(pos, chest.posicao())) {
                currentChest = chest;
                showingChestLoot = true;
                log.adicionar("Looting " + chest.corpseName() + "...");
                return;
            }
        }

        log.adicionar("No corpse nearby.");
    }

    private void processarCombate(char tecla) {
        switch (tecla) {
            case '1' -> ataqueBasico();
            case '2' -> ataquePoderoso();
            case '3' -> magiaFogo();
            case 'r' -> tentarFugir();
        }
    }

    private void ataqueBasico() {
        int dano = jogador.atacar();
        boolean wasCrit = Math.random() < jogador.stats().criticalChance();

        inimigoAtual.receberDano(dano);

        String critText = wasCrit ? " CRITICAL!" : "";
        log.adicionar(String.format("You attack %s for %d damage!%s",
                inimigoAtual.nome(), dano, critText));

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
            jogador.mover(-3, 0, dungeon.getCurrentLevel().getCurrentRoom().mapa());
        } else {
            log.adicionar("You failed to escape!");
            turnoInimigo();
        }
    }

    private void turnoInimigo() {
        int hpBefore = jogador.hpAtual();
        int dano = inimigoAtual.atacar();
        jogador.receberDano(dano);

        if (jogador.hpAtual() == hpBefore) {
            log.adicionar("You evaded the attack!");
        } else {
            log.adicionar(String.format("%s attacks you for %d damage!",
                    inimigoAtual.nome(), dano));
        }

        if (!jogador.vivo()) {
            log.adicionar("You were defeated...");
        }
    }

    private boolean verificarMorteInimigo() {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();

        if (!inimigoAtual.vivo()) {
            int xpGanho = 10 * dungeon.getCurrentFloorNumber();
            jogador.ganharXP(xpGanho);
            log.adicionar(String.format("%s defeated! +%d XP",
                    inimigoAtual.nome(), xpGanho));

            Chest newChest = new Chest(inimigoAtual.posicao(), dungeon.getCurrentFloorNumber(),
                    jogador.stats().lootBonus(), inimigoAtual.nome());
            currentRoom.chests().add(newChest);
            log.adicionar("Enemy left a corpse! Press [E] to loot");

            emCombate = false;
            inimigoAtual = null;
            return true;
        }
        return false;
    }

    private void verificarColisao(Room room) {
        Posicao pj = jogador.posicao();

        for (Monstro monstro : room.monstros()) {
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
        System.out.println("Floor reached: " + dungeon.getCurrentFloorNumber());
        System.out.println("Total XP: " + jogador.xp());
        System.out.println();
        System.out.println("Press Q to quit...");

        while (Input.getKey() != 'q');
        Input.cleanup();
        System.exit(0);
    }
}