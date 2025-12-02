package entidades;

import mundo.Posicao;
import items.Inventory;
import items.Weapon;

public class Jogador extends Entidade {
    private int manaMax;
    private int manaAtual;
    private int nivel;
    private int xp;
    private Inventory inventory;
    private Stats stats;

    public Jogador(String nome, Posicao posicao) {
        super(nome, new String[][] {{"@"}}, posicao, 35, 5, 2); // base HP will be overridden by stats

        // starting stats (like Dark Souls deprived class)
        this.stats = new Stats(10, 10, 10, 10, 10, 10);

        // recalculate HP based on vitality
        this.hpMax = stats.maxHP();
        this.hpAtual = this.hpMax;

        this.manaMax = 20;
        this.manaAtual = 20;
        this.nivel = 1;
        this.xp = 0;
        this.inventory = new Inventory();
    }

    public int manaMax() {
        return manaMax;
    }

    public int manaAtual() {
        return manaAtual;
    }

    public int nivel() {
        return nivel;
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

    public boolean usarMana(int custo) {
        if (manaAtual >= custo) {
            manaAtual -= custo;
            return true;
        }
        return false;
    }

    public void ganharXP(int quantidade) {
        xp += quantidade;
    }

    @Override
    public int atacar() {
        int baseAttack = ataque;

        // add weapon damage with stat scaling
        if (inventory.getEquippedWeapon() != null) {
            Weapon weapon = inventory.getEquippedWeapon();
            int weaponDamage = weapon.getDamageWithStats(
                    stats.strength(),
                    stats.dexterity(),
                    stats.intelligence());
            baseAttack += weaponDamage;
        }

        // add some randomness
        baseAttack += (int) (Math.random() * 3) - 1;

        // critical hit chance based on luck
        if (Math.random() < stats.criticalChance()) {
            baseAttack = (int) (baseAttack * 1.5);
        }

        return baseAttack;
    }

    @Override
    public void receberDano(int dano) {
        // evasion chance based on luck
        if (Math.random() < stats.evasionChance()) {
            // evaded!
            return;
        }

        super.receberDano(dano);
    }
}