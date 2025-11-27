package mundo;

public enum Bloco {
    CHAO("."),
    PAREDE("#"),
    ESCADA("D"); // Stairs to next level

    public final String simbolo;

    Bloco(String simbolo) {
        this.simbolo = simbolo;
    }
}