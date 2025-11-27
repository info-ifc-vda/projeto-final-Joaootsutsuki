package items;

import mundo.Posicao;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Chest {
    private Posicao posicao;
    private List<Weapon> weapons;
    private boolean opened;
    private String[][] sprite;

    public Chest(Posicao posicao, int floorLevel) {
        this.posicao = posicao;
        this.opened = false;
        this.weapons = new ArrayList<>();

        Random rand = new Random();
        int numWeapons = 5 + rand.nextInt(2);

        for (int i = 0; i < numWeapons; i++) {
            weapons.add(Weapon.generateRandom(floorLevel, rand));
        }

        // sprite 
        String YELLOW = "\u001B[33m";
        String RESET = "\u001B[0m";
        this.sprite = new String[][] {
                { YELLOW + "C" + RESET, YELLOW + "C" + RESET },
                { YELLOW + "C" + RESET, YELLOW + "C" + RESET }
        };
    }

    public Posicao posicao() {
        return posicao;
    }

    public List<Weapon> weapons() {
        return weapons;
    }

    public boolean isOpened() {
        return opened;
    }

    public void open() {
        this.opened = true;
    }

    public String[][] sprite() {
        return sprite;
    }
}