package entidades;

import mundo.Posicao;
import items.Inventory;

public class Jogador extends Entidade {
    private int manaMax;
    private int manaAtual;
    private int nivel;
    private int xp;
    private Inventory inventory;

    public Jogador(String nome, Posicao posicao) {
        super(nome, new String[][] {
                { "@", "@" },
                { "@", "@" }
        }, posicao, 30, 5, 2);

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
        int baseAttack = super.atacar();
        if (inventory.getEquippedWeapon() != null) {
            baseAttack += inventory.getEquippedWeapon().damage();
        }
        return baseAttack;
    }
}