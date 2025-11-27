package items;

public class Weapon {
    private String name;
    private int level;
    private int damage;
    private String rarity;

    private static final String[] WEAPON_TYPES = {
            "Sword", "Axe", "Spear", "Mace", "Dagger", "Hammer", "Scythe", "Staff"
    };

    private static final String[] PREFIXES = {
            "Rusty", "Iron", "Steel", "Silver", "Golden", "Diamond", "Legendary", "Mythical"
    };

    public Weapon(String name, int level, int damage, String rarity) {
        this.name = name;
        this.level = level;
        this.damage = damage;
        this.rarity = rarity;
    }

    public static Weapon generateRandom(int floorLevel, java.util.Random rand) {
        // level por andar
        int weaponLevel = floorLevel + rand.nextInt(3);

        String rarity;
        if (weaponLevel < 3)
            rarity = "Common";
        else if (weaponLevel < 6)
            rarity = "Rare";
        else if (weaponLevel < 10)
            rarity = "Epic";
        else
            rarity = "Legendary";

        // level scala por floor tbm
        int baseDamage = 3 + (weaponLevel * 2);
        int damage = baseDamage + rand.nextInt(weaponLevel + 3);

        // nome random
        String prefix = PREFIXES[Math.min(weaponLevel, PREFIXES.length - 1)];
        String type = WEAPON_TYPES[rand.nextInt(WEAPON_TYPES.length)];
        String name = prefix + " " + type;

        return new Weapon(name, weaponLevel, damage, rarity);
    }

    public String name() {
        return name;
    }

    public int level() {
        return level;
    }

    public int damage() {
        return damage;
    }

    public String rarity() {
        return rarity;
    }

    @Override
    public String toString() {
        return String.format("%s (Lv.%d) [%d DMG]", name, level, damage);
    }
}