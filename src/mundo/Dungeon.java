package mundo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// stairs not working xdddd

public class Dungeon {
    private List<DungeonLevel> floors;
    private int currentFloor;
    private Random rand;

    public Dungeon() {
        this.floors = new ArrayList<>();
        this.currentFloor = 0;
        this.rand = new Random();

        
        generateFloor(1);
    }

    private void generateFloor(int floorNumber) {
        DungeonLevel newFloor = new DungeonLevel(floorNumber);
        floors.add(newFloor);

        
        placeStairs(newFloor, floorNumber);
    }

    private void placeStairs(DungeonLevel floor, int floorNumber) {
        Room[][] grid = floor.roomGrid();
        int size = floor.gridSize();
        List<Room> availableRooms = new ArrayList<>();

        // Collect all discovered normal rooms (not start, not boss)
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Room room = grid[y][x];
                if (room != null && room.type() != Room.RoomType.START) {
                    availableRooms.add(room);
                }
            }
        }

        if (availableRooms.isEmpty())
            return;

        
        if (floorNumber > 1) {
            Room stairUpRoom = availableRooms.remove(rand.nextInt(availableRooms.size()));
            stairUpRoom.setStairsUp(true);
        }

        
        if (!availableRooms.isEmpty()) {
            
            Room bossRoom = null;
            for (Room room : availableRooms) {
                if (room.type() == Room.RoomType.BOSS) {
                    bossRoom = room;
                    break;
                }
            }

            Room stairDownRoom = (bossRoom != null) ? bossRoom
                    : availableRooms.get(rand.nextInt(availableRooms.size()));
            stairDownRoom.setStairsDown(true);
        }
    }

    public void descendFloor() {
        currentFloor++;

        // Generate new floor if it doesn't exist
        if (currentFloor >= floors.size()) {
            generateFloor(currentFloor + 1);
        }
    }

    public void ascendFloor() {
        if (currentFloor > 0) {
            currentFloor--;
        }
    }

    public DungeonLevel getCurrentLevel() {
        return floors.get(currentFloor);
    }

    public int getCurrentFloorNumber() {
        return currentFloor + 1;
    }

    public int getTotalFloorsGenerated() {
        return floors.size();
    }
}