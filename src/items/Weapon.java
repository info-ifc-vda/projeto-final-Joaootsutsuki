package items;

public class Weapon {
    private String name;
    private int level;
    private int baseDamage;
    private String rarity;
    private WeaponType type;
    private double weight;

    public enum WeaponType {
        HEAVY, // scales with STR
        LIGHT, // scales with DEX
        MAGIC // scales with INT
    }

    private static final String[][] WEAPON_NAMES = {
            { "Greatsword", "Battle Axe", "War Hammer", "Claymore" }, // Heavy
            { "Dagger", "Rapier", "Short Sword", "Katana" }, // Light
            { "Staff", "Wand", "Tome", "Catalyst" } // Magic
    };

    private static final String[] PREFIXES = {
            "Rusty", "Iron", "Steel", "Silver", "Golden", "Diamond", "Legendary", "Mythical"
    };

    public Weapon(String name, int level, int baseDamage, String rarity, WeaponType type, double weight) {
        this.name = name;
        this.level = level;
        this.baseDamage = baseDamage;
        this.rarity = rarity;
        this.type = type;
        this.weight = weight;
    }

    public static Weapon generateRandom(int floorLevel, int luckBonus, java.util.Random rand) {
        // level scale + a luck
        int weaponLevel = floorLevel + rand.nextInt(3) + (luckBonus / 2);

        // raridade + luck
        String rarity;
        int rarityRoll = rand.nextInt(100) + (luckBonus * 5);
        if (rarityRoll < 40)
            rarity = "Common";
        else if (rarityRoll < 70)
            rarity = "Rare";
        else if (rarityRoll < 90)
            rarity = "Epic";
        else
            rarity = "Legendary";

        // buff por raridade
        int rarityBonus = switch (rarity) {
            case "Rare" -> 1;
            case "Epic" -> 2;
            case "Legendary" -> 4;
            default -> 0;
        };
        weaponLevel += rarityBonus;

        WeaponType type = WeaponType.values()[rand.nextInt(WeaponType.values().length)];

        // dano escalando com nivel do floor
        int baseDamage = 3 + (weaponLevel * 2) + rand.nextInt(weaponLevel + 3);

        // peso baseado por tipo de arma
        double weight = switch (type) {
            case HEAVY -> 8.0 + rand.nextDouble() * 6.0; // 8-14
            case LIGHT -> 2.0 + rand.nextDouble() * 3.0; // 2-5
            case MAGIC -> 3.0 + rand.nextDouble() * 4.0; // 3-7
        };

        // gerar nome
        String prefix = PREFIXES[Math.min(weaponLevel, PREFIXES.length - 1)];
        String[] typeNames = WEAPON_NAMES[type.ordinal()];
        String weaponName = typeNames[rand.nextInt(typeNames.length)];
        String fullName = prefix + " " + weaponName;

        return new Weapon(fullName, weaponLevel, baseDamage, rarity, type, weight);
    }

    public int getDamageWithStats(int str, int dex, int intel) {
        double scaling = switch (type) {
            case HEAVY -> str * 0.5;
            case LIGHT -> dex * 0.5;
            case MAGIC -> intel * 0.5;
        };

        return (int) (baseDamage + scaling);
    }

    public String name() {
        return name;
    }

    public int level() {
        return level;
    }

    public int baseDamage() {
        return baseDamage;
    }

    public String rarity() {
        return rarity;
    }

    public WeaponType type() {
        return type;
    }

    public double weight() {
        return weight;
    }

    public String getTypeSymbol() {
        return switch (type) {
            case HEAVY -> "STR";
            case LIGHT -> "DEX";
            case MAGIC -> "INT";
        };
    }

    @Override
    public String toString() {
        return String.format("%s (Lv.%d) [%d DMG] <%s> %.1fkg",
                name, level, baseDamage, getTypeSymbol(), weight);
    }
}