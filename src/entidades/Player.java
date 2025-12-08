package entidades;

import mundo.Position;
import items.Inventory;
import items.Weapon;

public class Player extends Entity {
    private int maxMana;
    private int currentMana;
    private int level;
    private int xp;
    private Inventory inventory;
    private Stats stats;

    public Player(String name, Position position) {
        super(name, new String[][] { { "@" } }, position, 35, 5, 2);

        this.stats = new Stats(10, 10, 10, 10, 10, 10);

        this.maxHp = stats.maxHp();
        this.currentHp = this.maxHp;

        this.maxMana = 20;
        this.currentMana = 20;
        this.level = 1;
        this.xp = 0;
        this.inventory = new Inventory();
    }

    public int maxMana() {
        return maxMana;
    }

    public int currentMana() {
        return currentMana;
    }

    public int level() {
        return level;
    }

    public int xp() {
        return xp;
    }

    public Inventory inventory() {
        return inventory;
    }

    public Stats stats() {
        return stats;
    }

    public boolean spendMana(int cost) {
        if (currentMana >= cost) {
            currentMana -= cost;
            return true;
        }
        return false;
    }

    public void gainXp(int amount) {
        xp += amount;
    }

    @Override
    public int attack() {
        int baseAttack = attack;

        if (inventory.getEquippedWeapon() != null) {
            Weapon weapon = inventory.getEquippedWeapon();
            int weaponDamage = weapon.getDamageWithStats(
                    stats.strength(),
                    stats.dexterity(),
                    stats.intelligence());
            baseAttack += weaponDamage;
        }

        baseAttack += (int) (Math.random() * 3) - 1;

        if (Math.random() < stats.criticalChance()) {
            baseAttack = (int) (baseAttack * 1.5);
        }

        return baseAttack;
    }

    @Override
    public void takeDamage(int damage) {
        if (Math.random() < stats.evasionChance()) {
            return;
        }
        super.takeDamage(damage);
    }
}