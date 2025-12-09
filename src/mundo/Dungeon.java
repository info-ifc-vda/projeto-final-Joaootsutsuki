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