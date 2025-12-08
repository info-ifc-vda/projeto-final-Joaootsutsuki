package engine;

import entidades.Player;
import entidades.Monster;
import items.Chest;
import items.Weapon;
import mundo.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private Dungeon dungeon;
    private Player player;
    private MessageLog log;
    private boolean inCombat;
    private Monster currentEnemy;
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
        player = new Player("Hero", startRoom.mapa().randomPosition());

        // Place stairs in starting room (doors are already set up during map
        // generation)
        startRoom.mapa().placeStairs(startRoom.hasStairsUp(), startRoom.hasStairsDown());

        // Don't spawn monsters in starting room

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Input.cleanup();
            Render.clearScreen();
        }));

        log.add("You enter the dungeon...");
        log.add("Press [I] inventory, [E] interact, [M] minimap");
        gameLoop();
    }

    private void setupRoom(Room room) {
        // Room.generateMap() now handles doors automatically
        // Just place stairs after
        room.mapa().placeStairs(room.hasStairsUp(), room.hasStairsDown());
    }

    private void spawnMonstersInRoom(Room room, int floorLevel) {
        if (room.type() == Room.RoomType.START || !room.monstros().isEmpty()) {
            return;
        }

        int qty = switch (room.type()) {
            case BOSS -> 1;
            case TREASURE -> 3 + rand.nextInt(2);
            default -> 2 + rand.nextInt(3);
        };

        Map Map = room.mapa();
        Position playerPos = player.position();
        List<Position> usedPositions = new ArrayList<>();
        
        // divide room into zones to spread monsters
        int zoneWidth = Map.getWidth() / 3;
        int zoneHeight = Map.getHeight() / 3;

        for (int i = 0; i < qty; i++) {
            Position pos = null;
            int attempts = 0;
            int maxAttempts = 500;
            
            // assign each monster to a different zone
            int zoneX = i % 3;
            int zoneY = (i / 3) % 3;
            
            int minX = zoneX * zoneWidth + 2;
            int maxX = Math.min((zoneX + 1) * zoneWidth - 2, Map.getWidth() - 3);
            int minY = zoneY * zoneHeight + 2;
            int maxY = Math.min((zoneY + 1) * zoneHeight - 2, Map.getHeight() - 3);

            while (attempts < maxAttempts && pos == null) {
                int randX = minX + rand.nextInt(Math.max(1, maxX - minX));
                int randY = minY + rand.nextInt(Math.max(1, maxY - minY));
                
                Position candidate = new Position(randX, randY);
                
                if (Map.canWalk(candidate.x(), candidate.y())) {
                    double distancePlayer = Math.hypot(
                        candidate.x() - playerPos.x(), 
                        candidate.y() - playerPos.y()
                    );
                    
                    if (distancePlayer >= 7) {
                        // check distance from other spawned monsters
                        boolean free = true;
                        for (Position used : usedPositions) {
                            double distToOther = Math.hypot(
                                candidate.x() - used.x(), 
                                candidate.y() - used.y()
                            );
                            if (distToOther < 6) {
                                free = false;
                                break;
                            }
                        }
                        
                        if (free) {
                            pos = candidate;
                        }
                    }
                }
                attempts++;
            }

            // fallback
            if (pos == null) {
                attempts = 0;
                while (attempts < 200 && pos == null) {
                    int randX = minX + rand.nextInt(Math.max(1, maxX - minX));
                    int randY = minY + rand.nextInt(Math.max(1, maxY - minY));
                    
                    Position candidate = new Position(randX, randY);
                    if (Map.canWalk(candidate.x(), candidate.y())) {
                        boolean free = true;
                        for (Position used : usedPositions) {
                            double distToOther = Math.hypot(
                                candidate.x() - used.x(), 
                                candidate.y() - used.y()
                            );
                            if (distToOther < 3) {
                                free = false;
                                break;
                            }
                        }
                        if (free) {
                            pos = candidate;
                        }
                    }
                    attempts++;
                }
            }

            // Last resort
            if (pos == null) {
                pos = Map.randomPosition();
            }

            usedPositions.add(pos);

            boolean boss = room.type() == Room.RoomType.BOSS;
            Monster m = new Monster(
                    boss ? "Boss Goblin King" : "Goblin",
                    "G",
                    boss ? "\u001B[31m" : "\u001B[32m",
                    pos,
                    (15 + floorLevel * 6) * (boss ? 8 : 1),
                    (4 + floorLevel) * (boss ? 6 : 1),
                    2);
            room.monstros().add(m);
        }
    }

    private Position findValidPosition(Map Map, List<Position> usedPositions, int minimumDistance) {
        int attempts = 0;

        while (attempts < 100) {
            Position newPosition = Map.randomPositionFarFromPlayer(player.position(), minimumDistance);

            boolean tooClose = false;
            for (Position used : usedPositions) {
                double dist = Math.sqrt(Math.pow(newPosition.x() - used.x(), 2) +
                        Math.pow(newPosition.y() - used.y(), 2));
                if (dist < minimumDistance) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                return newPosition;
            }

            attempts++;
        }

        return Map.randomPositionFarFromPlayer(player.position(), 5);
    }

    private void gameLoop() {
        while (player.alive()) {
            try {
                Render.clearScreen();

                if (showingInventory) {
                    showInventory();
                } else if (showingChestLoot) {
                    showChestLoot();
                } else {
                    Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
                    Monster[] arrayMonsters = currentRoom.monstros().toArray(new Monster[0]);
                    Render.render(currentRoom.mapa(), player, log,
                            currentRoom.chests(), arrayMonsters);
                }

                char key = Input.getKey();

                if (showingInventory) {
                    processInventory(key);
                } else if (showingChestLoot) {
                    processChestLoot(key);
                } else if (inCombat) {
                    processCombat(key);
                } else {
                    processMovement(key);
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

    private void showInventory() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                         INVENTORY                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        if (player.inventory().size() == 0) {
            System.out.println("  Your inventory is empty.");
        } else {
            System.out.println("  Press number to equip weapon, [I] to close");
            System.out.println();

            List<Weapon> weapons = player.inventory().getWeapons();
            Weapon equipped = player.inventory().getEquippedWeapon();

            for (int i = 0; i < weapons.size(); i++) {
                Weapon w = weapons.get(i);
                String equipMark = (w == equipped) ? " [EQUIPPED]" : "";
                String color = getRarityColor(w.rarity());

                int actualDamage = w.getDamageWithStats(
                        player.stats().strength(),
                        player.stats().dexterity(),
                        player.stats().intelligence());

                System.out.printf("  [%d] %s%-35s%s Lv.%-2d [%d→%d DMG] <%s> %.1fkg%s\n",
                        i + 1, color, w.name(), "\u001B[0m", w.level(),
                        w.baseDamage(), actualDamage, w.getTypeSymbol(), w.weight(), equipMark);
            }
        }

        System.out.println();
        System.out.println("─".repeat(64));
    }

    private void showChestLoot() {
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
                player.inventory().size(),
                player.inventory().getCurrentWeight(),
                player.stats().maxCarryWeight());
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

    private void processInventory(char key) {
        if (key == 'i') {
            showingInventory = false;
            return;
        }

        if (key >= '1' && key <= '9') {
            int index = key - '1';
            if (index < player.inventory().size()) {
                player.inventory().equipWeapon(index);
                log.add("Equipped: " + player.inventory().getEquippedWeapon().name());
            }
        }
    }

    private void processChestLoot(char key) {
        if (key == 'e') {
            showingChestLoot = false;
            currentChest = null;
            return;
        }

        if (key >= '1' && key <= '9') {
            int index = key - '1';
            if (index < currentChest.weapons().size()) {
                double maxWeight = player.stats().maxCarryWeight();
                Weapon toTake = currentChest.weapons().get(index);

                if (player.inventory().isFull()) {
                    log.add("Inventory is full!");
                } else if (player.inventory().getCurrentWeight() + toTake.weight() > maxWeight) {
                    log.add("Too heavy! Need more endurance");
                } else {
                    Weapon taken = currentChest.weapons().remove(index);
                    player.inventory().addWeapon(taken, maxWeight);
                    log.add("Took: " + taken.name());

                    if (currentChest.weapons().isEmpty()) {
                        currentChest.open();
                        showingChestLoot = false;
                        currentChest = null;
                    }
                }
            }
        }
    }

    private void processMovement(char key) {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map Map = currentRoom.mapa();

        int oldX = player.position().x();
        int oldY = player.position().y();

        switch (key) {
            case 'w' -> player.move(0, -1, Map);
            case 's' -> player.move(0, 1, Map);
            case 'a' -> player.move(-1, 0, Map);
            case 'd' -> player.move(1, 0, Map);
            case 'i' -> {
                showingInventory = true;
                return;
            }
            case 'e' -> {
                checkChest(currentRoom);
                return;
            }
            case 'm' -> {
                showMinimap();
                Input.getKey();
                return;
            }
            case 'q' -> {
                Input.cleanup();
                System.exit(0);
            }
        }
        checkForRoomChange();

        if (player.position().x() != oldX || player.position().y() != oldY) {
            checkStairs();
            checkRoomTransition();
            checkCollision(currentRoom);
            currentRoom.checkCleared();
        }
    }

    private void checkForRoomChange() {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map Map = currentRoom.mapa();
        Position p = player.position();

        // Safety check
        if (p.y() < 0 || p.x() < 0 || p.y() >= Map.getHeight() || p.x() >= Map.getWidth()) {
            return;
        }

        Tile tile = Map.getTile(p.y(), p.x());

        // Only continue if standing on a door
        if (!tile.isDoor()) {
            return;
        }

        DungeonLevel level = dungeon.getCurrentLevel();
        Tile doorEntered = tile;
        boolean changed = false;

        if (tile == Tile.DOOR_NORTH && level.canMoveNorth()) {
            level.moveNorth();
            changed = true;
        }
        if (tile == Tile.DOOR_SOUTH && level.canMoveSouth()) {
            level.moveSouth();
            changed = true;
        }
        if (tile == Tile.DOOR_EAST && level.canMoveEast()) {
            level.moveEast();
            changed = true;
        }
        if (tile == Tile.DOOR_WEST && level.canMoveWest()) {
            level.moveWest();
            changed = true;
        }

        if (!changed) {
            return;
        }

        Room newRoom = level.getCurrentRoom();
        Map newMap = newRoom.mapa();

        // 1. FIRST spawn player near opposite door
        Position newPos = getSpawnPositionFromDoor(doorEntered, newMap);
        player.setPosition(newPos);

        // 2. THEN check if room needs monsters spawned
        if (!newRoom.discovered()) {
            newRoom.setDiscovered(true);
            spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber());
            log.add("You entered a new room!");
        } else {
            log.add("You entered a cleared room.");
        }
    }

    private Position getSpawnPositionFromDoor(Tile doorEntered, Map Map) {
        int centerX = Map.getWidth() / 2;
        int centerY = Map.getHeight() / 2;

        // Spawn near the opposite door
        return switch (doorEntered) {
            case DOOR_NORTH -> findSafeSpawnNear(Map, centerX, Map.getHeight() - 5); // Came from north, spawn near
                                                                                       // south
            case DOOR_SOUTH -> findSafeSpawnNear(Map, centerX, 5); // Came from south, spawn near north
            case DOOR_EAST -> findSafeSpawnNear(Map, 5, centerY); // Came from east, spawn near west
            case DOOR_WEST -> findSafeSpawnNear(Map, Map.getWidth() - 5, centerY); // Came from west, spawn near east
            default -> Map.randomPosition();
        };
    }

    private void checkStairs() {
        Position pos = player.position();
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map Map = currentRoom.mapa();

        // Check all 4 tiles of player sprite for stairs
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int checkX = pos.x() + dx;
                int checkY = pos.y() + dy;

                if (Map.isStairs(checkX, checkY)) {
                    Tile stairsType = Map.getStairsType(checkX, checkY);

                    if (stairsType == Tile.STAIRS_DOWN) {
                        dungeon.descendFloor();
                        log.clear();
                        log.add("Descended to Floor " + dungeon.getCurrentFloorNumber());

                        // Setup new floor's starting room
                        Room newRoom = dungeon.getCurrentLevel().getCurrentRoom();
                        if (!newRoom.discovered()) {
                            setupRoom(newRoom);
                            spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber());
                        }

                        // Move player to stairs up position
                        Position newPos = newRoom.mapa().randomPosition();
                        player.move(newPos.x() - player.position().x(),
                                newPos.y() - player.position().y(), newRoom.mapa());

                    } else if (stairsType == Tile.STAIRS_UP) {
                        dungeon.ascendFloor();
                        log.clear();
                        log.add("Ascended to Floor " + dungeon.getCurrentFloorNumber());

                        // Return to previous floor
                        Room oldRoom = dungeon.getCurrentLevel().getCurrentRoom();
                        Position returnPos = oldRoom.mapa().randomPosition();
                        player.move(returnPos.x() - player.position().x(),
                                returnPos.y() - player.position().y(), oldRoom.mapa());
                    }

                    return;
                }
            }
        }
    }

    private void checkRoomTransition() {
        Position pos = player.position();
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map Map = currentRoom.mapa();

        // Check all 4 tiles of player sprite for door collision
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int checkX = pos.x() + dx;
                int checkY = pos.y() + dy;

                if (Map.isDoor(checkX, checkY)) {
                    Tile doorType = Map.getDoorType(checkX, checkY);
                    transitionToRoom(doorType);
                    return;
                }
            }
        }
    }

    private void transitionToRoom(Tile doorType) {
        DungeonLevel currentLevel = dungeon.getCurrentLevel();

        log.add("DEBUG: Transitioning room...");

        switch (doorType) {
            case DOOR_NORTH -> currentLevel.moveNorth();
            case DOOR_SOUTH -> currentLevel.moveSouth();
            case DOOR_EAST -> currentLevel.moveEast();
            case DOOR_WEST -> currentLevel.moveWest();
        }

        Room newRoom = currentLevel.getCurrentRoom();

        log.add("DEBUG: Room discovered? " + newRoom.discovered());
        log.add("DEBUG: Room type: " + newRoom.type());
        log.add("DEBUG: Current monsters: " + newRoom.monstros().size());

        // If entering room for first time, spawn monsters
        if (!newRoom.discovered()) {
            newRoom.setDiscovered(true);
            log.add("DEBUG: Spawning monsters...");
            spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber());
            log.add("DEBUG: After spawn, monsters: " + newRoom.monstros().size());
        }

        // Place player at opposite door - ensure it's on floor
        Map newMap = newRoom.mapa();
        Position newPos;

        switch (doorType) {
            case DOOR_NORTH -> {
                newPos = findSafeSpawnNear(newMap, newMap.asStringArray()[0].length / 2,
                        newMap.asStringArray().length - 8);
            }
            case DOOR_SOUTH -> {
                newPos = findSafeSpawnNear(newMap, newMap.asStringArray()[0].length / 2, 8);
            }
            case DOOR_EAST -> {
                newPos = findSafeSpawnNear(newMap, 8, newMap.asStringArray().length / 2);
            }
            case DOOR_WEST -> {
                newPos = findSafeSpawnNear(newMap, newMap.asStringArray()[0].length - 8,
                        newMap.asStringArray().length / 2);
            }
            default -> newPos = newMap.randomPosition();
        }
        ;

        player.move(newPos.x() - player.position().x(),
                newPos.y() - player.position().y(), newMap);

        log.add("Entered " + getRoomDescription(newRoom));
    }

    private Position findSafeSpawnNear(Map Map, int targetX, int targetY) {
        // try to find a walkable 2x2 area near the target position
        for (int radius = 0; radius < 15; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = targetX + dx;
                    int y = targetY + dy;

                    // check if this position and the 2x2 area is walkable
                    if (Map.canWalk(x, y) && Map.canWalk(x + 1, y) &&
                            Map.canWalk(x, y + 1) && Map.canWalk(x + 1, y + 1)) {
                        return new Position(x, y);
                    }
                }
            }
        }

        // fallback to random position if no safe spot found
        return Map.randomPosition();
    }

    private String getRoomDescription(Room room) {
        return switch (room.type()) {
            case START -> "the starting room";
            case BOSS -> "a BOSS ROOM!";
            case TREASURE -> "a treasure room";
            default -> room.cleared() ? "a cleared room" : "a new room";
        };
    }

    private void showMinimap() {
        Render.clearScreen();
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

    private void checkChest(Room room) {
        Position pos = player.position();

        for (Chest chest : room.chests()) {
            if (!chest.isOpened() && collide(pos, chest.position())) {
                currentChest = chest;
                showingChestLoot = true;
                log.add("Looting " + chest.corpseName() + "...");
                return;
            }
        }

        log.add("No corpse nearby.");
    }

    private void processCombat(char key) {
        switch (key) {
            case '1' -> basicAttack();
            case '2' -> powerAttack();
            case '3' -> fireSpell();
            case 'r' -> tryEscape();
        }
    }

    private void basicAttack() {
        int damage = player.attack();
        boolean wasCrit = Math.random() < player.stats().criticalChance();

        currentEnemy.takeDamage(damage);

        String critText = wasCrit ? " CRITICAL!" : "";
        log.add(String.format("You attack %s for %d damage!%s",
                currentEnemy.name(), damage, critText));

        if (checkEnemyDeath())
            return;
        enemyTurn();
    }

    private void powerAttack() {
        int manaCost = 5;
        if (!player.spendMana(manaCost)) {
            log.add("Not enough mana!");
            return;
        }

        int damage = (int) (player.attack() * 1.5);
        currentEnemy.takeDamage(damage);
        log.add(String.format("Power Attack! %d damage!", damage));

        if (checkEnemyDeath())
            return;
        enemyTurn();
    }

    private void fireSpell() {
        int manaCost = 8;
        if (!player.spendMana(manaCost)) {
            log.add("Not enough mana!");
            return;
        }

        int damage = 10 + (int) (Math.random() * 5);
        currentEnemy.takeDamage(damage);
        log.add(String.format("Fireball! %d damage!", damage));

        if (checkEnemyDeath())
            return;
        enemyTurn();
    }

    private void tryEscape() {
        if (Math.random() < 0.5) {
            log.add("You fled from combat!");
            inCombat = false;
            currentEnemy = null;
            player.move(-3, 0, dungeon.getCurrentLevel().getCurrentRoom().mapa());
        } else {
            log.add("You failed to escape!");
            enemyTurn();
        }
    }

    private void enemyTurn() {
        int hpBefore = player.currentHp();
        int damage = currentEnemy.attack();
        player.takeDamage(damage);

        if (player.currentHp() == hpBefore) {
            log.add("You evaded the attack!");
        } else {
            log.add(String.format("%s attacks you for %d damage!",
                    currentEnemy.name(), damage));
        }

        if (!player.alive()) {
            log.add("You were defeated...");
        }
    }

    private boolean checkEnemyDeath() {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();

        if (!currentEnemy.alive()) {
            int xpGained = 10 * dungeon.getCurrentFloorNumber();
            player.gainXp(xpGained);
            log.add(String.format("%s defeated! +%d XP",
                    currentEnemy.name(), xpGained));

            Chest newChest = new Chest(currentEnemy.position(), dungeon.getCurrentFloorNumber(),
                    player.stats().lootBonus(), currentEnemy.name());
            currentRoom.chests().add(newChest);
            log.add("Enemy left a corpse! Press [E] to loot");

            inCombat = false;
            currentEnemy = null;
            return true;
        }
        return false;
    }

    private void checkCollision(Room room) {
        Position pj = player.position();

        for (Monster monster : room.monstros()) {
            if (monster.alive() && collide(pj, monster.position())) {
                inCombat = true;
                currentEnemy = monster;
                log.add("You encountered a " + monster.name() + "!");
                log.add("=== COMBAT ===");
                log.add("[1] Basic Attack  [2] Power Attack (5 MP)");
                log.add("[3] Fireball (8 MP)  [R] Run");
                break;
            }
        }
    }

    private boolean collide(Position p1, Position p2) {
        return Math.abs(p1.x() - p2.x()) < 2 && Math.abs(p1.y() - p2.y()) < 2;
    }

    private void gameOver() {
        Render.clearScreen();
        System.out.println();
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║        YOU DIED!               ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.println();
        System.out.println("Floor reached: " + dungeon.getCurrentFloorNumber());
        System.out.println("Total XP: " + player.xp());
        System.out.println();
        System.out.println("Press Q to quit...");

        while (Input.getKey() != 'q');
        Input.cleanup();
        System.exit(0);
    }
}