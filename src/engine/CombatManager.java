package engine;

import entidades.Monster;
import entidades.Player;
import items.Chest;
import mundo.Room;

public class CombatManager {
    private Player player;
    private MessageLog log;
    private Monster currentEnemy;
    private boolean inCombat;

    public CombatManager(Player player, MessageLog log) {
        this.player = player;
        this.log = log;
        this.inCombat = false;
    }

    public boolean isInCombat() {
        return inCombat;
    }

    public void startCombat(Monster enemy) {
        this.inCombat = true;
        this.currentEnemy = enemy;

        log.clearNonFixed();
        log.add(String.format("⚔️  Você encontrou %s! Prepare-se!", enemy.name()));

        log.addFixed("═════════════════════ COMBATE ═════════════════════");
        log.addFixed(" 🎯 Ações disponíveis:");
        log.addFixed("   [1] Ataque Básico");
        log.addFixed("   [2] Ataque Poderoso (5 MP)");
        log.addFixed("   [3] Bola de Fogo (8 MP)");
        log.addFixed("   [R] Fugir");
        log.addFixed("════════════════════════════════════════════════════");
    }

    public void processCombat(char key, Room currentRoom, int currentFloor) {
        switch (key) {
            case '1' -> basicAttack(currentRoom, currentFloor);
            case '2' -> powerAttack(currentRoom, currentFloor);
            case '3' -> fireSpell(currentRoom, currentFloor);
            case 'r' -> tryEscape();
        }
    }

    private void basicAttack(Room room, int floor) {
        int damage = player.attack();
        boolean wasCrit = Math.random() < player.stats().criticalChance();

        currentEnemy.takeDamage(damage);

        if (wasCrit)
            log.add(String.format("🗡️ Ataque Crítico! %s sofreu %d de dano!", currentEnemy.name(), damage));
        else
            log.add(String.format("🗡️ Você atacou %s causando %d de dano.", currentEnemy.name(), damage));

        if (checkEnemyDeath(room, floor)) return;
        enemyTurn();
    }

    private void powerAttack(Room room, int floor) {
        int manaCost = 5;
        if (!player.spendMana(manaCost)) {
            log.add("❌ Mana insuficiente para Ataque Poderoso!");
            return;
        }

        int damage = (int) (player.attack() * 1.5);
        currentEnemy.takeDamage(damage);

        log.add(String.format("⚡ Ataque Poderoso! %s recebeu %d de dano!", currentEnemy.name(), damage));

        if (checkEnemyDeath(room, floor)) return;
        enemyTurn();
    }

    private void fireSpell(Room room, int floor) {
        int manaCost = 8;
        if (!player.spendMana(manaCost)) {
            log.add("❌ Mana insuficiente para lançar Bola de Fogo!");
            return;
        }

        int damage = 10 + (int) (Math.random() * 5);
        currentEnemy.takeDamage(damage);

        log.add(String.format("🔥 Você lançou Bola de Fogo! %s sofreu %d de dano!", currentEnemy.name(), damage));

        if (checkEnemyDeath(room, floor)) return;
        enemyTurn();
    }

    private void tryEscape() {
        if (Math.random() < 0.5) {
            log.add("🏃 Você conseguiu fugir do combate!");
            inCombat = false;
            currentEnemy = null;
        } else {
            log.add("⚠️ Você tentou fugir mas falhou!");
            enemyTurn();
        }
    }

    private void enemyTurn() {
        int hpBefore = player.currentHp();
        int damage = currentEnemy.attack();
        player.takeDamage(damage);

        if (player.currentHp() == hpBefore)
            log.add("✨ Você esquivou do ataque inimigo!");
        else
            log.add(String.format("💢 %s atacou você causando %d de dano!", currentEnemy.name(), damage));

        if (!player.alive()) {
            log.add("☠️ Você foi derrotado...");
        }
    }

    private boolean checkEnemyDeath(Room room, int floor) {
        if (!currentEnemy.alive()) {

            int xpGained = 10 * floor;
            player.gainXp(xpGained);

            log.add(String.format("🏆 %s foi derrotado! Você ganhou %d XP.", currentEnemy.name(), xpGained));

            Chest newChest = new Chest(
                    currentEnemy.position(),
                    floor,
                    player.stats().lootBonus(),
                    currentEnemy.name()
            );
            room.chests().add(newChest);

            log.add("📦 O inimigo deixou um corpo. Pressione [E] para saquear.");

            inCombat = false;
            currentEnemy = null;
            return true;
        }
        return false;
    }

}