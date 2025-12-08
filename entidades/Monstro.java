package entidades;

import mundo.Position;

public class Monstro extends Entidade {

    public Monstro(String nome, String simbolo, String cor, Position position, int hp, int ataque, int defesa) {
        super(nome, gerarSprite(simbolo, cor), position, hp, ataque, defesa);
    }

    private static String[][] gerarSprite(String simbolo, String cor) {
        String RESET = "\u001B[0m";

        return new String[][] {{ cor + simbolo + RESET}};
    }
}