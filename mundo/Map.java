package mundo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Map {
    private Tile[][] grid;
    private Random r = new Random();
    private List<RoomBounds> rooms;
    private Position northDoorPos;
    private Position southDoorPos;
    private Position eastDoorPos;
    private Position westDoorPos;

    public Map(int width, int height) {
        grid = new Tile[height][width];
        rooms = new ArrayList<>();

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                grid[y][x] = Tile.WALL;

        // Note: placeDoors will be called BEFORE gerarrooms in setupRoom()
        // This is handled in Jogo.java
    }

    public Map(int width, int height, boolean north, boolean south, boolean east, boolean west) {
        grid = new Tile[height][width];
        rooms = new ArrayList<>();

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                grid[y][x] = Tile.WALL;

        // generate rooms FIRST
        generateRooms();

        // THEN place doors and force carve paths to them
        placeDoors(north, south, east, west);
    }

    public Tile getTile(int y, int x) {
        if (y < 0 || y >= grid.length || x < 0 || x >= grid[0].length) {
            return Tile.WALL;
        }
        return grid[y][x];
    }
    
    public int getHeight() {
        return grid.length;
    }

    public int getWidth() {
        return grid[0].length;
    }

    public void placeDoors(boolean north, boolean south, boolean east, boolean west) {
        int centerX = grid[0].length / 2;
        int centerY = grid.length / 2;

        if (north) {
            northDoorPos = new Position(centerX, 0);
            for (int i = 0; i <= 1; i++) {
                grid[0][centerX + i] = Tile.DOOR_NORTH;
            }
            for (int y = 1; y <= centerY; y++) {
                grid[y][centerX] = Tile.FLOOR;
                grid[y][centerX + 1] = Tile.FLOOR;
            }
        }

        if (south) {
            southDoorPos = new Position(centerX, grid.length - 1);
            for (int i = 0; i <= 1; i++) {
                grid[grid.length - 1][centerX + i] = Tile.DOOR_SOUTH;
            }
            for (int y = grid.length - 2; y >= centerY; y--) {
                grid[y][centerX] = Tile.FLOOR;
                grid[y][centerX + 1] = Tile.FLOOR;
            }
        }

        if (east) {
            eastDoorPos = new Position(grid[0].length - 1, centerY);
            for (int i = 0; i <= 1; i++) {
                grid[centerY + i][grid[0].length - 1] = Tile.DOOR_EAST;
            }
            for (int x = grid[0].length - 2; x >= centerX; x--) {
                grid[centerY][x] = Tile.FLOOR;
                grid[centerY + 1][x] = Tile.FLOOR;
            }
        }

        if (west) {
            westDoorPos = new Position(0, centerY);
            for (int i = 0; i <= 1; i++) {
                grid[centerY + i][0] = Tile.DOOR_WEST;
            }
            for (int x = 1; x <= centerX; x++) {
                grid[centerY][x] = Tile.FLOOR;
                grid[centerY + 1][x] = Tile.FLOOR;
            }
        }
    }

    private void generateRooms() {
        int maxAttempts = 100;
        int roomCount = 5 + r.nextInt(6); // 5-10 rooms

        for (int i = 0; i < maxAttempts && rooms.size() < roomCount; i++) {
            int w = 6 + r.nextInt(12);
            int h = 4 + r.nextInt(8);
            int x = 2 + r.nextInt(grid[0].length - w - 4);
            int y = 2 + r.nextInt(grid.length - h - 4);

            RoomBounds newRoom = new RoomBounds(x, y, w, h);

            boolean overlaps = false;
            for (RoomBounds room : rooms) {
                if (newRoom.intersects(room)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                rooms.add(newRoom);
                carveRoom(newRoom);
            }
        }

        connectRooms();
    }

    private void carveRoom(RoomBounds room) {
        for (int y = room.y(); y < room.y() + room.height(); y++) {
            for (int x = room.x(); x < room.x() + room.width(); x++) {
                grid[y][x] = Tile.FLOOR;
            }
        }
    }

    private void connectRooms() {
        if (rooms.isEmpty()) return;

        RoomBounds first = rooms.get(0);
        for (int i = 1; i < rooms.size(); i++) {
            RoomBounds a = rooms.get(i - 1);
            RoomBounds b = rooms.get(i);
            carveCorridor(a.centerX(), a.centerY(), b.centerX(), b.centerY());
        }

        // Connect first room to center (for doors)
        carveCorridor(first.centerX(), first.centerY(), grid[0].length / 2, grid.length / 2);
    }

    private void carveCorridor(int x1, int y1, int x2, int y2) {
        int x = x1;
        int y = y1;

        while (x != x2) {
            grid[y][x] = Tile.FLOOR;
            if (x < x2) x++; else x--;
        }
        while (y != y2) {
            grid[y][x] = Tile.FLOOR;
            if (y < y2) y++; else y--;
        }
        grid[y][x] = Tile.FLOOR;
    }

    public boolean canWalk(int x, int y) {
        if (x < 0 || y < 0 || y >= grid.length || x >= grid[0].length) return false;
        Tile t = grid[y][x];
        return t == Tile.FLOOR || t.isDoor() || t.isStairs();
    }

    public boolean isDoor(int x, int y) {
        if (x < 0 || y < 0 || y >= grid.length || x >= grid[0].length) return false;
        return grid[y][x].isDoor();
    }

    public boolean isStairs(int x, int y) {
        if (x < 0 || y < 0 || y >= grid.length || x >= grid[0].length) return false;
        return grid[y][x].isStairs();
    }

    public Tile getDoorType(int x, int y) {
        if (isDoor(x, y)) {
            return grid[y][x];
        }
        return null;
    }

    public Tile getStairsType(int x, int y) {
        if (isStairs(x, y)) {
            return grid[y][x];
        }
        return null;
    }

    public Position randomPosition() {
        if (rooms.isEmpty()) {
            return new Position(grid[0].length / 2, grid.length / 2);
        }

        RoomBounds firstRoom = rooms.get(0);
        int margin = 3;
        int usableWidth = Math.max(1, firstRoom.width() - 2 * margin);
        int usableHeight = Math.max(1, firstRoom.height() - 2 * margin);

        int x = firstRoom.x() + margin + r.nextInt(usableWidth);
        int y = firstRoom.y() + margin + r.nextInt(usableHeight);

        if (canWalk(x, y) && canWalk(x + 1, y) && canWalk(x, y + 1) && canWalk(x + 1, y + 1)) {
            return new Position(x, y);
        }

        return new Position(firstRoom.centerX(), firstRoom.centerY());
    }

    public Position randomPositionFarFromPlayer(Position playerPos, int minDistance) {
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            RoomBounds randomRoom = rooms.get(1 + r.nextInt(Math.max(1, rooms.size() - 1)));

            int margin = 3;
            int usableWidth = Math.max(1, randomRoom.width() - 2 * margin);
            int usableHeight = Math.max(1, randomRoom.height() - 2 * margin);

            int x = randomRoom.x() + margin + r.nextInt(usableWidth);
            int y = randomRoom.y() + margin + r.nextInt(usableHeight);

            if (canWalk(x, y) && canWalk(x + 1, y) && canWalk(x, y + 1) && canWalk(x + 1, y + 1)) {
                double distance = Math.sqrt(Math.pow(x - playerPos.x(), 2) + Math.pow(y - playerPos.y(), 2));
                if (distance >= minDistance) {
                    return new Position(x, y);
                }
            }
            attempts++;
        }

        if (!rooms.isEmpty()) {
            RoomBounds lastRoom = rooms.get(rooms.size() - 1);
            return new Position(lastRoom.centerX(), lastRoom.centerY());
        }

        return new Position(grid[0].length / 2, grid.length / 2);
    }

    public String[][] asStringArray() {
        String[][] arr = new String[grid.length][grid[0].length];
        for (int y = 0; y < grid.length; y++)
            for (int x = 0; x < grid[0].length; x++)
                arr[y][x] = grid[y][x].symbol;
        return arr;
    }
    
    public void placeStairs(boolean hasUp, boolean hasDown) {
        if (rooms.isEmpty())
            return;

        // place stairs in center of last room
        RoomBounds lastRoom = rooms.get(rooms.size() - 1);
        int centerX = lastRoom.centerX();
        int centerY = lastRoom.centerY();

        if (hasUp) {
            // place stairs up at top-left of center
            grid[centerY][centerX] = Tile.STAIRS_UP;
            grid[centerY][centerX + 1] = Tile.STAIRS_UP;
            grid[centerY + 1][centerX] = Tile.FLOOR;
            grid[centerY + 1][centerX + 1] = Tile.FLOOR;
        }

        if (hasDown) {
            // place stairs down at bottom-right of center
            int offsetX = hasUp ? 4 : 0;
            grid[centerY][centerX + offsetX] = Tile.STAIRS_DOWN;
            grid[centerY][centerX + offsetX + 1] = Tile.STAIRS_DOWN;
            grid[centerY + 1][centerX + offsetX] = Tile.FLOOR;
            grid[centerY + 1][centerX + offsetX + 1] = Tile.FLOOR;
        }
    }
}
