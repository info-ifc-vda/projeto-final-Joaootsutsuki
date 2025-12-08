package entidades;

public class Stats {
    private int vitality; // HP
    private int endurance; // carry weight
    private int strength; // heavy weapons scaling
    private int dexterity; // light weapons scaling
    private int intelligence; // magic weapons scaling
    private int luck; // chance-based stats

    public Stats(int vit, int end, int str, int dex, int intel, int lck) {
        this.vitality = vit;
        this.endurance = end;
        this.strength = str;
        this.dexterity = dex;
        this.intelligence = intel;
        this.luck = lck;
    }

    public int vitality() {
        return vitality;
    }

    public int endurance() {
        return endurance;
    }

    public int strength() {
        return strength;
    }

    public int dexterity() {
        return dexterity;
    }

    public int intelligence() {
        return intelligence;
    }

    public int luck() {
        return luck;
    }

    public void addVitality(int amount) {
        vitality += amount;
    }

    public void addEndurance(int amount) {
        endurance += amount;
    }

    public void addStrength(int amount) {
        strength += amount;
    }

    public void addDexterity(int amount) {
        dexterity += amount;
    }

    public void addIntelligence(int amount) {
        intelligence += amount;
    }

    public void addLuck(int amount) {
        luck += amount;
    }

    public int maxHp() {
        return 30 + (vitality * 5);
    }

    public int maxCarryWeight() {
        return 20 + (endurance * 5);
    }

    public double evasionChance() {
        return Math.min(0.3, luck * 0.02);
    }

    public double criticalChance() {
        return Math.min(0.25, luck * 0.015);
    }

    public int lootBonus() {
        return luck / 5;
    }
}