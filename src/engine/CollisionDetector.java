package engine;

import entidades.Monster;
import entidades.Player;
import mundo.Position;
import mundo.Room;

public class CollisionDetector {
    private CombatManager combatManager;
    private Player player;

    public CollisionDetector(Player player, CombatManager combatManager) {
        this.player = player;
        this.combatManager = combatManager;
    }

    public void checkCollision(Room room) {
        Position pj = player.position();

        for (Monster monster : room.monstros()) {
            if (monster.alive() && collide(pj, monster.position())) {
                combatManager.startCombat(monster);
                break;
            }
        }
    }

    private boolean collide(Position p1, Position p2) {
        return p1.x() == p2.x() && p1.y() == p2.y();
    }
}