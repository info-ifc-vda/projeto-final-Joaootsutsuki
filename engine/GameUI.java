package engine;

import entidades.Player;
import items.Chest;
import items.Weapon;
import mundo.*;
import java.util.List;

public class GameUI {

    public void showInventory(Player player) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                         INVENTARIO                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        if (player.inventory().size() == 0) {
            System.out.println("  Seu inventario esta vazio.");
        } else {
            System.out.println("  Pressione um numero para equipar a arma, [I] para fechar");
            System.out.println();

            List<Weapon> weapons = player.inventory().getWeapons();
            Weapon equipped = player.inventory().getEquippedWeapon();

            for (int i = 0; i < weapons.size(); i++) {
                Weapon w = weapons.get(i);
                String equipMark = (w == equipped) ? " [Equipado]" : "";
                String color = getRarityColor(w.rarity());

                int actualDamage = w.getDamageWithStats(
                        player.stats().strength(),
                        player.stats().dexterity(),
                        player.stats().intelligence()
                );

                System.out.printf("  [%d] %s%-35s%s Lv.%-2d [%d→%d DMG] <%s> %.1fkg%s\n",
                        i + 1, color, w.name(), "\u001B[0m", w.level(),
                        w.baseDamage(), actualDamage, w.getTypeSymbol(), w.weight(), equipMark);
            }
        }

        System.out.println();
        System.out.println("─".repeat(64));
    }

    public void showChestLoot(Chest chest, Player player) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        String title = chest.corpseName();
        int padding = (62 - title.length()) / 2;
        System.out.printf("║%s%s%s║\n",
                " ".repeat(padding),
                title,
                " ".repeat(62 - padding - title.length())
        );
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Pressione um numero para pegar uma arma, [E] para fechar");
        System.out.println();

        List<Weapon> weapons = chest.weapons();
        for (int i = 0; i < weapons.size(); i++) {
            Weapon w = weapons.get(i);
            String color = getRarityColor(w.rarity());
            System.out.printf("  [%d] %s%-35s%s Lv.%-2d [%d DMG] <%s> %.1fkg\n",
                    i + 1, color, w.name(), "\u001B[0m",
                    w.level(), w.baseDamage(), w.getTypeSymbol(), w.weight());
        }

        System.out.println();
        System.out.println("─".repeat(64));
        System.out.printf("Inventario: %d/20 | Peso: %.1f/%d\n",
                player.inventory().size(),
                player.inventory().getCurrentWeight(),
                player.stats().maxCarryWeight());
    }

    public void showMinimap(Dungeon dungeon) {
        Render.clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.printf ("║                 MINIMAPA - ANDAR %02d                ║\n", dungeon.getCurrentFloorNumber());
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        DungeonLevel level = dungeon.getCurrentLevel();
        Room[][] grid = level.roomGrid();
        int size = level.gridSize();

        for (int y = 0; y < size; y++) {
            System.out.print("   ");

            for (int x = 0; x < size; x++) {
                Room room = grid[y][x];

                if (x == level.currentRoomX() && y == level.currentRoomY()) {
                    System.out.print(" [\u001B[32m@\u001B[0m]");
                } else if (room == null) {
                    System.out.print(" [ ]");
                } else if (!room.discovered()) {
                    System.out.print(" [?]");
                } else {
                    String symbol = switch (room.type()) {
                        case START -> "S";
                        case BOSS -> "\u001B[31mB\u001B[0m";
                        case TREASURE -> "\u001B[33mT\u001B[0m";
                        default -> room.cleared() ? "-" : "#";
                    };
                    System.out.print(" [" + symbol + "]");
                }
            }
            System.out.println();
        }

        System.out.println("\n─────────────────────────── INFO ─────────────────────────────\n");

        System.out.printf(" Andar atual: %d/%d\n", dungeon.getCurrentFloorNumber(), dungeon.getTotalFloorsGenerated());

        System.out.println("\n──────────────────────── LEGENDA ─────────────────────────────");
        System.out.println(" [\u001B[32m@\u001B[0m] Você");
        System.out.println(" [S] Início");
        System.out.println(" [\u001B[31mB\u001B[0m] Chefe");
        System.out.println(" [#] Inimigos");
        System.out.println(" [-] Sala Limpa");
        System.out.println(" [#] Inimigos");
        System.out.println(" [\u001B[33mT\u001B[0m] Tesouro");
        System.out.println(" [?] Não descoberto");
        System.out.println(" [ ] Sem sala gerada");
        System.out.println(" ↑ Escada Cima | ↓ Escada Baixo");
        System.out.println("──────────────────────────────────────────────────────────────\n");

        System.out.print(" Pressione qualquer tecla para fechar...");
    }


    public void showGameOver(Player player, int currentFloor) {
        Render.clearScreen();
        System.out.println();
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║         VOCE MORREU            ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.println();
        System.out.println("Andar alcançado: " + currentFloor);
        System.out.println("XP total: " + player.xp());
        System.out.println();
        System.out.println("Pressione Q para sair...");
    }

    private String getRarityColor(String rarity) {
        return switch (rarity) {
            case "Common" -> "\u001B[37m";
            case "Rare" -> "\u001B[34m";
            case "Epic" -> "\u001B[35m";
            case "Legendary" -> "\u001B[33m";
            default -> "\u001B[0m";
        };
    }
}
