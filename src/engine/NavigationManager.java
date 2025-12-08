package engine;

import entidades.Player;
import mundo.*;

public class NavigationManager {
    private Player player;
    private Dungeon dungeon;
    private MessageLog log;
    private RoomManager roomManager;

    public NavigationManager(Player player, Dungeon dungeon, MessageLog log, RoomManager roomManager) {
        this.player = player;
        this.dungeon = dungeon;
        this.log = log;
        this.roomManager = roomManager;
    }

    public void checkForRoomChange() {
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map map = currentRoom.mapa();
        Position p = player.position();

        if (p.y() < 0 || p.x() < 0 || p.y() >= map.getHeight() || p.x() >= map.getWidth()) {
            return;
        }

        Tile tile = map.getTile(p.y(), p.x());

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

        Position newPos = roomManager.getSpawnPositionFromDoor(doorEntered, newMap);
        player.setPosition(newPos);

        if (!newRoom.discovered()) {
            newRoom.setDiscovered(true);
            roomManager.spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber(), player);
            log.add("You entered a new room!");
        } else {
            log.add("You entered a cleared room.");
        }
    }

    public void checkStairs() {
        Position pos = player.position();
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map map = currentRoom.mapa();

        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int checkX = pos.x() + dx;
                int checkY = pos.y() + dy;

                if (map.isStairs(checkX, checkY)) {
                    Tile stairsType = map.getStairsType(checkX, checkY);

                    if (stairsType == Tile.STAIRS_DOWN) {
                        descendFloor();
                    } else if (stairsType == Tile.STAIRS_UP) {
                        ascendFloor();
                    }

                    return;
                }
            }
        }
    }

    private void descendFloor() {
        dungeon.descendFloor();
        log.clear();
        log.add("Descended to Floor " + dungeon.getCurrentFloorNumber());

        Room newRoom = dungeon.getCurrentLevel().getCurrentRoom();
        if (!newRoom.discovered()) {
            roomManager.setupRoom(newRoom);
            roomManager.spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber(), player);
        }

        Position newPos = newRoom.mapa().randomPosition();
        player.move(
                newPos.x() - player.position().x(),
                newPos.y() - player.position().y(),
                newRoom.mapa()
        );
    }

    private void ascendFloor() {
        dungeon.ascendFloor();
        log.clear();
        log.add("Ascended to Floor " + dungeon.getCurrentFloorNumber());

        Room oldRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Position returnPos = oldRoom.mapa().randomPosition();
        player.move(
                returnPos.x() - player.position().x(),
                returnPos.y() - player.position().y(),
                oldRoom.mapa()
        );
    }

    public void checkRoomTransition() {
        Position pos = player.position();
        Room currentRoom = dungeon.getCurrentLevel().getCurrentRoom();
        Map map = currentRoom.mapa();

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
        DungeonLevel currentLevel = dungeon.getCurrentLevel();

        switch (doorType) {
            case DOOR_NORTH -> currentLevel.moveNorth();
            case DOOR_SOUTH -> currentLevel.moveSouth();
            case DOOR_EAST -> currentLevel.moveEast();
            case DOOR_WEST -> currentLevel.moveWest();
        }

        Room newRoom = currentLevel.getCurrentRoom();

        if (!newRoom.discovered()) {
            newRoom.setDiscovered(true);
            roomManager.spawnMonstersInRoom(newRoom, dungeon.getCurrentFloorNumber(), player);
        }

        Map newMap = newRoom.mapa();
        Position newPos;

        switch (doorType) {
            case DOOR_NORTH -> {
                newPos = roomManager.findSafeSpawnNear(
                        newMap,
                        newMap.asStringArray()[0].length / 2,
                        newMap.asStringArray().length - 8
                );
            }
            case DOOR_SOUTH -> {
                newPos = roomManager.findSafeSpawnNear(
                        newMap,
                        newMap.asStringArray()[0].length / 2,
                        8
                );
            }
            case DOOR_EAST -> {
                newPos = roomManager.findSafeSpawnNear(
                        newMap,
                        8,
                        newMap.asStringArray().length / 2
                );
            }
            case DOOR_WEST -> {
                newPos = roomManager.findSafeSpawnNear(
                        newMap,
                        newMap.asStringArray()[0].length - 8,
                        newMap.asStringArray().length / 2
                );
            }
            default -> newPos = newMap.randomPosition();
        }

        player.move(
                newPos.x() - player.position().x(),
                newPos.y() - player.position().y(),
                newMap
        );

        log.add("Entered " + roomManager.getRoomDescription(newRoom));
    }
}