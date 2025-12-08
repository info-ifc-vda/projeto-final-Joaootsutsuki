package entidades;

import mundo.Map;
import mundo.Position;
import java.util.Random;

public class Monster extends Entity {

    private Random rand = new Random();

    public Monster(String name, String symbol, String color, Position position, int hp, int attack, int defense) {
        super(name, generateSprite(symbol, color), position, hp, attack, defense);
    }

    private static String[][] generateSprite(String symbol, String color) {
        String RESET = "\u001B[0m";

        return new String[][] {
                { color + symbol + RESET, color + symbol + RESET },
                { color + symbol + RESET, color + symbol + RESET }
        };
    }

    // static monster for now
    public void moveRandom(Map map) {
    }
}