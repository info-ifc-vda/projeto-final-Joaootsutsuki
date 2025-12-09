package mundo;

import java.util.Random;

public class DungeonLevel {
    private Room[][] roomGrid;
    private int gridSize;
    private int currentRoomX;
    private int currentRoomY;
    private Random rand;
    private int floorNumber;

    public DungeonLevel(int floorNumber) {
        this.gridSize = 5;
        this.roomGrid = new Room[gridSize][gridSize];
        this.rand = new Random();
        this.floorNumber = floorNumber;

        generateDungeon();
    }

    private void generateDungeon() {
        // Start in the center
        currentRoomX = gridSize / 2;
        currentRoomY = gridSize / 2;

        // Create starting room
        roomGrid[currentRoomY][currentRoomX] = new Room(Room.RoomType.START);

        // Generate connected rooms using random walk
        int roomsToGenerate = 8 + rand.nextInt(5);
        int roomsCreated = 1;

        int x = currentRoomX;
        int y = currentRoomY;

        while (roomsCreated < roomsToGenerate) {
            // Pick a random direction
            int dir = rand.nextInt(4);
            int newX = x;
            int newY = y;

            switch (dir) {
                case 0 -> newY--;
                case 1 -> newY++;
                case 2 -> newX++;
                case 3 -> newX--;
            }

            // Check bounds
            if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize) {
                // Create room if doesn't exist
                if (roomGrid[newY][newX] == null) {
                    Room.RoomType type = Room.RoomType.NORMAL;

                    // Last room is boss
                    if (roomsCreated == roomsToGenerate - 1) {
                        type = Room.RoomType.BOSS;
                    }
                    // Random treasure rooms
                    else if (rand.nextInt(100) < 20) {
                        type = Room.RoomType.TREASURE;
                    }

                    roomGrid[newY][newX] = new Room(type);
                    roomsCreated++;
                }

                // Connect rooms with doors
                connectRooms(x, y, newX, newY);

                // Move to new room
                x = newX;
                y = newY;
            }
        }

        // Generate maps for all rooms AFTER doors are set
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (roomGrid[i][j] != null) {
                    roomGrid[i][j].generateMap();
                }
            }
        }
    }

    private void connectRooms(int x1, int y1, int x2, int y2) {
        Room room1 = roomGrid[y1][x1];
        Room room2 = roomGrid[y2][x2];

        if (room1 == null || room2 == null)
            return;

        // Determine direction
        if (y2 < y1) {
            room1.setDoor("north", true);
            room2.setDoor("south", true);
        } else if (y2 > y1) {
            room1.setDoor("south", true);
            room2.setDoor("north", true);
        } else if (x2 > x1) {
            room1.setDoor("east", true);
            room2.setDoor("west", true);
        } else if (x2 < x1) {
            room1.setDoor("west", true);
            room2.setDoor("east", true);
        }
    }

    public Room getCurrentRoom() {
        return roomGrid[currentRoomY][currentRoomX];
    }

    public boolean canMoveNorth() {
        return getCurrentRoom().hasNorthDoor() && currentRoomY > 0 &&
                roomGrid[currentRoomY - 1][currentRoomX] != null;
    }

    public boolean canMoveSouth() {
        return getCurrentRoom().hasSouthDoor() && currentRoomY < gridSize - 1 &&
                roomGrid[currentRoomY + 1][currentRoomX] != null;
    }

    public boolean canMoveEast() {
        return getCurrentRoom().hasEastDoor() && currentRoomX < gridSize - 1 &&
                roomGrid[currentRoomY][currentRoomX + 1] != null;
    }

    public boolean canMoveWest() {
        return getCurrentRoom().hasWestDoor() && currentRoomX > 0 &&
                roomGrid[currentRoomY][currentRoomX - 1] != null;
    }

    public void moveNorth() {
        if (canMoveNorth()) {
            currentRoomY--;
        }
    }

    public void moveSouth() {
        if (canMoveSouth()) {
            currentRoomY++;
        }
    }

    public void moveEast() {
        if (canMoveEast()) {
            currentRoomX++;
        }
    }

    public void moveWest() {
        if (canMoveWest()) {
            currentRoomX--;
        }
    }

    public int currentRoomX() {
        return currentRoomX;
    }

    public int currentRoomY() {
        return currentRoomY;
    }

    public Room[][] roomGrid() {
        return roomGrid;
    }

    public int gridSize() {
        return gridSize;
    }
}