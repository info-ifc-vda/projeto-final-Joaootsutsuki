package engine;

import entidades.Player;
import mundo.*;

public class NavigationManager {
    private final Player player;
    private final Dungeon dungeon;
    private final MessageLog log;
    private final RoomManager roomManager;

    public NavigationManager(Player player, Dungeon dungeon, MessageLog log, RoomManager roomManager) {
        this.player = player;
        this.dungeon = dungeon;
        this.log = log;
        this.roomManager = roomManager;
    }


    public void checkForRoomChange() {
        Room currentRoom = dungeon.getLevel().getCurrentRoom();
        Map map = currentRoom.mapa();
        Position p = player.position();

        if (p.y() < 0 || p.x() < 0 || p.y() >= map.getHeight() || p.x() >= map.getWidth()) {
            return;
        }

        Tile tile = map.getTile(p.y(), p.x());

        if (!tile.isDoor()) {
            return;
        }

        DungeonLevel level = dungeon.getLevel();
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

        Position newPos = roomManager.getSpawnPositionFromDoor(doorEntered, newMap);
        player.setPosition(newPos);

        if (!newRoom.discovered()) {
            newRoom.setDiscovered(true);
            roomManager.spawnMonstersInRoom(newRoom, player);
            log.add("Voce entrou em uma nova sala");
        } else {
            log.add("Voce entrou em uma sala vazia");
        }
    }

    /**
     * Verifica colisão do sprite 2x2 do player com portas
     */
    public void checkRoomTransition() {
        Position pos = player.position();
        Room currentRoom = dungeon.getLevel().getCurrentRoom();
        Map map = currentRoom.mapa();

        // Verifica os 4 tiles do sprite 2x2
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int checkX = pos.x() + dx;
                int checkY = pos.y() + dy;

                if (map.isDoor(checkX, checkY)) {
                    Tile doorType = map.getDoorType(checkX, checkY);
                    transitionToRoom(doorType);
                    return;
                }
            }
        }
    }

    private void transitionToRoom(Tile doorType) {
        DungeonLevel currentLevel = dungeon.getLevel();

        switch (doorType) {
            case DOOR_NORTH -> currentLevel.moveNorth();
            case DOOR_SOUTH -> currentLevel.moveSouth();
            case DOOR_EAST -> currentLevel.moveEast();
            case DOOR_WEST -> currentLevel.moveWest();
        }

        Room newRoom = currentLevel.getCurrentRoom();

        if (!newRoom.discovered()) {
            newRoom.setDiscovered(true);
            roomManager.spawnMonstersInRoom(newRoom, player);
        }

        Map newMap = newRoom.mapa();
        Position newPos = getSpawnPositionForDoor(doorType, newMap);

        player.move(
                newPos.x() - player.position().x(),
                newPos.y() - player.position().y(),
                newMap
        );

        log.add("Entrou na " + roomManager.getRoomDescription(newRoom));
    }

    /**
     * Determina posição de spawn baseado na porta de entrada
     */
    private Position getSpawnPositionForDoor(Tile doorType, Map map) {
        int width = map.getWidth();
        int height = map.getHeight();

        return switch (doorType) {
            case DOOR_NORTH -> roomManager.findSafeSpawnNear(map, width / 2, height - 8);
            case DOOR_SOUTH -> roomManager.findSafeSpawnNear(map, width / 2, 8);
            case DOOR_EAST -> roomManager.findSafeSpawnNear(map, 8, height / 2);
            case DOOR_WEST -> roomManager.findSafeSpawnNear(map, width - 8, height / 2);
            default -> map.randomPosition();
        };
    }
}