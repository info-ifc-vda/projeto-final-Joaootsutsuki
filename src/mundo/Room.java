package mundo;

import entidades.Monster;
import items.Chest;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private Map mapa;
    private List<Monster> monstros;
    private List<Chest> chests;
    private boolean discovered;
    private boolean cleared;

    // Doors in each direction
    private boolean hasNorthDoor;
    private boolean hasSouthDoor;
    private boolean hasEastDoor;
    private boolean hasWestDoor;

    // Stairs
    private boolean hasStairsUp;
    private boolean hasStairsDown;

    // Room type
    private RoomType type;

    public enum RoomType {
        NORMAL,
        START,
        BOSS,
        TREASURE
    }

    public Room(RoomType type) {
        this.type = type;
        this.monstros = new ArrayList<>();
        this.chests = new ArrayList<>();
        this.discovered = false;
        this.cleared = false;
        this.hasNorthDoor = false;
        this.hasSouthDoor = false;
        this.hasEastDoor = false;
        this.hasWestDoor = false;
        this.hasStairsUp = false;
        this.hasStairsDown = false;
    }

    public void generateMap() {
        // Generate map with doors already placed
        mapa = new Map(60, 30, hasNorthDoor, hasSouthDoor, hasEastDoor, hasWestDoor);
    }

    public void setDoor(String direction, boolean value) {
        switch (direction.toLowerCase()) {
            case "north" -> hasNorthDoor = value;
            case "south" -> hasSouthDoor = value;
            case "east" -> hasEastDoor = value;
            case "west" -> hasWestDoor = value;
        }
    }

    // Getters
    public Map mapa() {
        return mapa;
    }

    public List<Monster> monstros() {
        return monstros;
    }

    public List<Chest> chests() {
        return chests;
    }

    public boolean discovered() {
        return discovered;
    }

    public boolean cleared() {
        return cleared;
    }

    public RoomType type() {
        return type;
    }

    public boolean hasNorthDoor() {
        return hasNorthDoor;
    }

    public boolean hasSouthDoor() {
        return hasSouthDoor;
    }

    public boolean hasEastDoor() {
        return hasEastDoor;
    }

    public boolean hasWestDoor() {
        return hasWestDoor;
    }

    public boolean hasStairsUp() {
        return hasStairsUp;
    }

    public boolean hasStairsDown() {
        return hasStairsDown;
    }

    // Setters
    public void setDiscovered(boolean value) {
        this.discovered = value;
    }

    public void setCleared(boolean value) {
        this.cleared = value;
    }

    public void setStairsUp(boolean value) {
        this.hasStairsUp = value;
    }

    public void setStairsDown(boolean value) {
        this.hasStairsDown = value;
    }

    public void checkCleared() {
        boolean allDead = true;
        for (Monster m : monstros) {
            if (m.alive()) {
                allDead = false;
                break;
            }
        }
        cleared = allDead;
    }
}