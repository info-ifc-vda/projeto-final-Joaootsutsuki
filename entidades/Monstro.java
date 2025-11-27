package entidades;

import mundo.Mapa;
import mundo.Posicao;
import java.util.Random;

public class Monstro extends Entidade {

    private Random rand = new Random();

    public Monstro(String nome, String simbolo, String cor, Posicao posicao, int hp, int ataque, int defesa) {
        super(nome, gerarSprite(simbolo, cor), posicao, hp, ataque, defesa);
    }

    private static String[][] gerarSprite(String simbolo, String cor) {
        String RESET = "\u001B[0m";

        return new String[][] {
                { cor + simbolo + RESET, cor + simbolo + RESET },
                { cor + simbolo + RESET, cor + simbolo + RESET }
        };
    }

    // monstro estatico
    public void moverAleatorio(Mapa mapa) {
    }
}