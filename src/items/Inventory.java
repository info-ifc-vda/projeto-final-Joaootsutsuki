package items;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Weapon> weapons;
    private Weapon equippedWeapon;
    private static final int MAX_SIZE = 20;

    public Inventory() {
        this.weapons = new ArrayList<>();
        this.equippedWeapon = null;
    }

    public boolean addWeapon(Weapon weapon, double maxWeight) {
        if (weapons.size() >= MAX_SIZE) {
            return false;
        }
        if (getCurrentWeight() + weapon.weight() > maxWeight) {
            return false; // Too heavy
        }
        weapons.add(weapon);
        return true;
    }

    public void equipWeapon(int index) {
        if (index >= 0 && index < weapons.size()) {
            equippedWeapon = weapons.get(index);
        }
    }

    public void removeWeapon(int index) {
        if (index >= 0 && index < weapons.size()) {
            Weapon removed = weapons.remove(index);
            if (removed == equippedWeapon) {
                equippedWeapon = null;
            }
        }
    }

    public double getCurrentWeight() {
        double total = 0;
        for (Weapon w : weapons) {
            total += w.weight();
        }
        return total;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public int size() {
        return weapons.size();
    }

    public boolean isFull() {
        return weapons.size() >= MAX_SIZE;
    }
}