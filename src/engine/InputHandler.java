package engine;

import entidades.Player;
import items.Chest;
import items.Weapon;
import mundo.*;

public class InputHandler {
    private Player player;
    private MessageLog log;
    private GameUI ui;
    private Dungeon dungeon;

    private boolean showingInventory;
    private boolean showingChestLoot;
    private Chest currentChest;

    public InputHandler(Player player, MessageLog log, GameUI ui, Dungeon dungeon) {
        this.player = player;
        this.log = log;
        this.ui = ui;
        this.dungeon = dungeon;
        this.showingInventory = false;
        this.showingChestLoot = false;
    }

    public boolean isShowingInventory() {
        return showingInventory;
    }

    public boolean isShowingChestLoot() {
        return showingChestLoot;
    }

    public Chest getCurrentChest() {
        return currentChest;
    }

    public void processInventory(char key) {
        if (key == 'i') {
            showingInventory = false;
            return;
        }

        if (key >= '1' && key <= '9') {
            int index = key - '1';
            if (index < player.inventory().size()) {
                player.inventory().equipWeapon(index);
                log.add("Equipou: " + player.inventory().getEquippedWeapon().name());
            }
        }
    }

    public void processChestLoot(char key) {
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
                    log.add("Inventario cheio!");
                } else if (player.inventory().getCurrentWeight() + toTake.weight() > maxWeight) {
                    log.add("Peso excedido! Precisa mais resistencia");
                } else {
                    Weapon taken = currentChest.weapons().remove(index);
                    player.inventory().addWeapon(taken, maxWeight);
                    log.add("Pegou: " + taken.name());

                    if (currentChest.weapons().isEmpty()) {
                        currentChest.open();
                        showingChestLoot = false;
                        currentChest = null;
                    }
                }
            }
        }
    }

    public void processMovement(char key) {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map map = currentRoom.mapa();

        int oldX = player.position().x();
        int oldY = player.position().y();

        switch (key) {
            case 'w' -> player.move(0, -1, map);
            case 's' -> player.move(0, 1, map);
            case 'a' -> player.move(-1, 0, map);
            case 'd' -> player.move(1, 0, map);
            case 'i' -> {
                showingInventory = true;
                return;
            }
            case 'e' -> {
                checkChest(currentRoom);
                return;
            }
            case 'm' -> {
                ui.showMinimap(dungeon);
                Input.getKey();
                return;
            }
            case 'q' -> {
                Input.cleanup();
                System.exit(0);
            }
        }

        // confere se realmente se moveu
        if (player.position().x() != oldX || player.position().y() != oldY) {
            currentRoom.checkCleared();
        }
    }

    private void checkChest(Room room) {
        Position pos = player.position();

        for (Chest chest : room.chests()) {
            if (!chest.isOpened() && collide(pos, chest.position())) {
                currentChest = chest;
                showingChestLoot = true;
                log.add("Vasculhando " + chest.corpseName() + "...");
                return;
            }
        }

        log.add("Nenhum corpo proximo.");
    }

    private boolean collide(Position p1, Position p2) {
        return Math.abs(p1.x() - p2.x()) < 2 && Math.abs(p1.y() - p2.y()) < 2;
    }

    public void openInventory() {
        showingInventory = true;
    }

    public void closeInventory() {
        showingInventory = false;
    }
}
