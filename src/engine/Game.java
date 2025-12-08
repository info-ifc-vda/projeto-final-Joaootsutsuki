package engine;

import entidades.Monster;
import entidades.Player;
import mundo.*;

public class Game {
    private Dungeon dungeon;
    private Player player;
    private MessageLog log;

    private CombatManager combatManager;
    private RoomManager roomManager;
    private NavigationManager navigationManager;
    private InputHandler inputHandler;
    private CollisionDetector collisionDetector;
    private GameUI gameUI;

    public void start() {
        initializeGame();
        setupShutdownHook();
        showWelcomeMessage();
        gameLoop();
    }

    private void initializeGame() {
        log = new MessageLog();
        gameUI = new GameUI();

        // Create dungeon
        dungeon = new Dungeon();

        // Create player
        Room startRoom = dungeon.getCurrentLevel().getCurrentRoom();
        player = new Player("Hero", startRoom.mapa().randomPosition());

        // Initialize managers
        combatManager = new CombatManager(player, log);
        roomManager = new RoomManager(log);
        navigationManager = new NavigationManager(player, dungeon, log, roomManager);
        inputHandler = new InputHandler(player, log, gameUI, dungeon);
        collisionDetector = new CollisionDetector(player, combatManager);

        // Setup starting room
        roomManager.setupRoom(startRoom);
    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Input.cleanup();
            Render.clearScreen();
        }));
    }

    private void showWelcomeMessage() {
        log.add("Voce entrou em uma dungeon...");
        log.add("Precione [I] Inventario, [E] Interagir, [M] Minimapa");
    }

    private void gameLoop() {
        while (player.alive()) {
            try {
                Render.clearScreen();

                renderCurrentView();
                char key = Input.getKey();
                processInput(key);

            } catch (Exception e) {
                handleError(e);
            }
        }

        gameOver();
    }

    private void renderCurrentView() {
        if (inputHandler.isShowingInventory()) {
            gameUI.showInventory(player);
        } else if (inputHandler.isShowingChestLoot()) {
            gameUI.showChestLoot(inputHandler.getCurrentChest(), player);
        } else {
            renderGameWorld();
        }
    }

    private void renderGameWorld() {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Monster[] arrayMonsters = currentRoom.monstros().toArray(new Monster[0]);
        Render.render(
                currentRoom.mapa(),
                player,
                log,
                currentRoom.chests(),
                arrayMonsters
        );
    }

    private void processInput(char key) {
        if (inputHandler.isShowingInventory()) {
            inputHandler.processInventory(key);
        } else if (inputHandler.isShowingChestLoot()) {
            inputHandler.processChestLoot(key);
        } else if (combatManager.isInCombat()) {
            Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
            combatManager.processCombat(
                    key,
                    currentRoom,
                    dungeon.getCurrentFloorNumber()
            );
        } else {
            handleMovement(key);
        }
    }

    private void handleMovement(char key) {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        int oldX = player.position().x();
        int oldY = player.position().y();

        inputHandler.processMovement(key);

        // Only check for interactions if player actually moved
        if (player.position().x() != oldX || player.position().y() != oldY) {
            navigationManager.checkForRoomChange();
            navigationManager.checkStairs();
            navigationManager.checkRoomTransition();
            collisionDetector.checkCollision(currentRoom);
        }
    }

    private void handleError(Exception e) {
        System.err.println("ERROR: " + e.getMessage());
        e.printStackTrace();
        System.err.println("Precione qualquer tecla para continuar...");
        Input.getKey();
    }

    private void gameOver() {
        gameUI.showGameOver(player, dungeon.getCurrentFloorNumber());

        while (Input.getKey() != 'q');
        Input.cleanup();
        System.exit(0);
    }
}