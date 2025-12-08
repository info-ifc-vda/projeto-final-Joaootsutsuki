package entidades;

import mundo.Map;
import mundo.Position;

public abstract class Entity {
    protected String name;
    protected String[][] sprite;
    protected Position position;

    protected int maxHp;
    protected int currentHp;
    protected int attack;
    protected int defense;
    protected boolean alive;

    public Entity(String name, String[][] sprite, Position position, int hp, int attack, int defense) {
        this.name = name;
        this.sprite = sprite;
        this.position = position;
        this.maxHp = hp;
        this.currentHp = hp;
        this.attack = attack;
        this.defense = defense;
        this.alive = true;
    }

    public void move(int dx, int dy, Map map) {
        int nx = position.x() + dx;
        int ny = position.y() + dy;

        if (map.canWalk(nx, ny))
            position = new Position(nx, ny);
    }

    public void takeDamage(int damage) {
        int realDamage = Math.max(1, damage - defense);
        currentHp -= realDamage;
        if (currentHp <= 0) {
            currentHp = 0;
            alive = false;
        }
    }

    public int attack() {
        return attack + (int) (Math.random() * 3) - 1;
    }

    public Position position() {
        return position;
    }

    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }

    public String[][] sprite() {
        return sprite;
    }

    public String name() {
        return name;
    }

    public int currentHp() {
        return currentHp;
    }

    public int maxHp() {
        return maxHp;
    }

    public int attackPower() {
        return attack;
    }

    public int defense() {
        return defense;
    }

    public boolean alive() {
        return alive;
    }
}