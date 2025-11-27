package mundo;

public class Sala {
    private int x, y, largura, altura;

    public Sala(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.largura = width;
        this.altura = height;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int largura() {
        return largura;
    }

    public int altura() {
        return altura;
    }

    public int centroX() {
        return x + largura / 2;
    }

    public int centroY() {
        return y + altura / 2;
    }

    public boolean intersecta(Sala outra) {
        return x < outra.x + outra.largura &&
                x + largura > outra.x &&
                y < outra.y + outra.altura &&
                y + altura > outra.y;
    }
}