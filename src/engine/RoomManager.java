package engine;

import entidades.Monster;
import entidades.Player;
import mundo.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomManager {
    private Random rand;
    private MessageLog log;

    public RoomManager(MessageLog log) {
        this.rand = new Random();
        this.log = log;
    }

    public void spawnMonstersInRoom(Room room, Player player) {
        if (room.type() == Room.RoomType.START || !room.monstros().isEmpty()) {
            return;
        }

        int qty = switch (room.type()) {
            case BOSS -> 1;
            case TREASURE -> 3 + rand.nextInt(2);
            default -> 2 + rand.nextInt(3);
        };

        Map map = room.mapa();
        Position playerPos = player.position();
        List<Position> usedPositions = new ArrayList<>();

        int zoneWidth = map.getWidth() / 3;
        int zoneHeight = map.getHeight() / 3;

        for (int i = 0; i < qty; i++) {
            Position pos = findMonsterSpawnPosition(
                    map, playerPos, usedPositions, i,
                    zoneWidth, zoneHeight
            );

            if (pos != null) {
                usedPositions.add(pos);
                boolean boss = room.type() == Room.RoomType.BOSS;
                Monster m = createMonster(pos, boss);
                room.monstros().add(m);
            }
        }
    }

    private Position findMonsterSpawnPosition(
            Map map, Position playerPos, List<Position> usedPositions,
            int monsterIndex, int zoneWidth, int zoneHeight
    ) {
        int zoneX = monsterIndex % 3;
        int zoneY = (monsterIndex / 3) % 3;

        int minX = zoneX * zoneWidth + 2;
        int maxX = Math.min((zoneX + 1) * zoneWidth - 2, map.getWidth() - 3);
        int minY = zoneY * zoneHeight + 2;
        int maxY = Math.min((zoneY + 1) * zoneHeight - 2, map.getHeight() - 3);

        // Try with strict distance requirements
        Position pos = trySpawnWithDistance(
                map, playerPos, usedPositions,
                minX, maxX, minY, maxY, 7, 6, 500
        );

        // Fallback with relaxed distance
        if (pos == null) {
            pos = trySpawnWithDistance(
                    map, playerPos, usedPositions,
                    minX, maxX, minY, maxY, 0, 3, 200
            );
        }

        // Last resort
        if (pos == null) {
            pos = map.randomPosition();
        }

        return pos;
    }

    private Position trySpawnWithDistance(
            Map map, Position playerPos, List<Position> usedPositions,
            int minX, int maxX, int minY, int maxY,
            int minPlayerDist, int minMonsterDist, int maxAttempts
    ) {
        int attempts = 0;

        while (attempts < maxAttempts) {
            int randX = minX + rand.nextInt(Math.max(1, maxX - minX));
            int randY = minY + rand.nextInt(Math.max(1, maxY - minY));

            Position candidate = new Position(randX, randY);

            if (map.canWalk(candidate.x(), candidate.y())) {
                double distancePlayer = Math.hypot(
                        candidate.x() - playerPos.x(),
                        candidate.y() - playerPos.y()
                );

                if (distancePlayer >= minPlayerDist) {
                    boolean free = true;
                    for (Position used : usedPositions) {
                        double distToOther = Math.hypot(
                                candidate.x() - used.x(),
                                candidate.y() - used.y()
                        );
                        if (distToOther < minMonsterDist) {
                            free = false;
                            break;
                        }
                    }

                    if (free) {
                        return candidate;
                    }
                }
            }
            attempts++;
        }

        return null;
    }

    private Monster createMonster(Position pos, boolean isBoss) {
        String name = isBoss ? "Boss Goblin King" : "Goblin";
        String symbol = "G";
        String color = isBoss ? "\u001B[31m" : "\u001B[32m"; // vermelho = boss, verde = normal

        int baseHp = 40;
        int hp = isBoss ? baseHp * 4 : baseHp;

        int baseDamage = 4;
        int damage = isBoss ? baseDamage * 4 : baseDamage;

        int defense = 2;

        return new Monster(name, symbol, color, pos, hp, damage, defense);
    }

    public Position getSpawnPositionFromDoor(Tile doorEntered, Map map) {
        int centerX = map.getWidth() / 2;
        int centerY = map.getHeight() / 2;

        return switch (doorEntered) {
            case DOOR_NORTH -> findSafeSpawnNear(map, centerX, map.getHeight() - 5);
            case DOOR_SOUTH -> findSafeSpawnNear(map, centerX, 5);
            case DOOR_EAST -> findSafeSpawnNear(map, 5, centerY);
            case DOOR_WEST -> findSafeSpawnNear(map, map.getWidth() - 5, centerY);
            default -> map.randomPosition();
        };
    }

    public Position findSafeSpawnNear(Map map, int targetX, int targetY) {
        for (int radius = 0; radius < 15; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = targetX + dx;
                    int y = targetY + dy;

                    if (map.canWalk(x, y) && map.canWalk(x + 1, y) &&
                            map.canWalk(x, y + 1) && map.canWalk(x + 1, y + 1)) {
                        return new Position(x, y);
                    }
                }
            }
        }

        return map.randomPosition();
    }

    public String getRoomDescription(Room room) {
        return switch (room.type()) {
            case START -> "Sala Inicial";
            case BOSS -> "Sala do BOSS!!";
            case TREASURE -> "Sala de tesouros";
            default -> room.cleared() ? "sala vazia" : "nova sala";
        };
    }
}