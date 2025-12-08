package entidades;

import mundo.Map;
import mundo.Position;

public class Monster extends Entity {

    public Monster(String name, String symbol, String color, Position position, int hp, int attack, int defense) {
        super(name, generateSprite(symbol, color), position, hp, attack, defense);
    }

    private static String[][] generateSprite(String symbol, String color) {
        String RESET = "\u001B[0m";

        return new String[][] { { color + symbol + RESET } };
    }

}