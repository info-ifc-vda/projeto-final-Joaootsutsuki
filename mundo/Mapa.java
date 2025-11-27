package mundo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Algoritmo pra gerar os mapas: Usei uma Room-based generation, não ficou muito orgânico, se precisar alterar pode alterar
// só tomar cuidado com os gaps do personagem pra ele não ficar preso, e também limitar os spawns dos mobs pra eles não spawnarem em cima do player.

public class Mapa {
    private Bloco[][] grade;
    private Random r = new Random();
    private List<Sala> salas;
    private Posicao escadaPos;

    public Mapa(int largura, int altura) {
        grade = new Bloco[altura][largura];
        salas = new ArrayList<>();

        // fill with walls first
        for (int y = 0; y < altura; y++)
            for (int x = 0; x < largura; x++)
                grade[y][x] = Bloco.PAREDE;

        gerarSalas();
        colocarEscada();
    }

    private void gerarSalas() {
        int tentativas = 0;
        int maxTentativas = 50;
        int salasCriadas = 0;
        int salasDesejadas = 8 + r.nextInt(5); // 8-12 rooms

        while (salasCriadas < salasDesejadas && tentativas < maxTentativas) {
            int largura = 6 + r.nextInt(8); // 6-13
            int altura = 5 + r.nextInt(6); // 5-10
            int x = 1 + r.nextInt(grade[0].length - largura - 2);
            int y = 1 + r.nextInt(grade.length - altura - 2);

            Sala novaSala = new Sala(x, y, largura, altura);

            // check if room overlaps with existing rooms
            boolean sobrepoe = false;
            for (Sala sala : salas) {
                if (novaSala.intersecta(sala)) {
                    sobrepoe = true;
                    break;
                }
            }

            if (!sobrepoe) {
                criarSala(novaSala);

                // connect to previous room
                if (!salas.isEmpty()) {
                    Sala salaAnterior = salas.get(salas.size() - 1);

                    // randomly choose connection style
                    if (r.nextBoolean()) {
                        conectarSalasLinear(salaAnterior, novaSala);
                    } else {
                        // sometimes connect to a random earlier room
                        Sala salaAleatoria = salas.get(r.nextInt(salas.size()));
                        conectarSalasLinear(salaAleatoria, novaSala);
                    }
                }

                salas.add(novaSala);
                salasCriadas++;
            }

            tentativas++;
        }
    }

    private void criarSala(Sala sala) {
        // create room with wall borders
        for (int y = sala.y(); y < sala.y() + sala.altura(); y++) {
            for (int x = sala.x(); x < sala.x() + sala.largura(); x++) {
                if (dentroDosLimites(x, y)) {
                    // check if it's a border tile
                    boolean isBorder = (x == sala.x() || x == sala.x() + sala.largura() - 1 ||
                            y == sala.y() || y == sala.y() + sala.altura() - 1);

                    if (isBorder) {
                        grade[y][x] = Bloco.PAREDE;
                    } else {
                        grade[y][x] = Bloco.CHAO;
                    }
                }
            }
        }
    }

    private void conectarSalasLinear(Sala sala1, Sala sala2) {
        int x1 = sala1.centroX();
        int y1 = sala1.centroY();
        int x2 = sala2.centroX();
        int y2 = sala2.centroY();

        // randomly choose L-shape direction
        if (r.nextBoolean()) {
            // horizontal then vertical - 2 tiles wide
            criarCorredorHorizontal(x1, x2, y1);
            criarCorredorVertical(y1, y2, x2);
        } else {
            // vertical then horizontal - 2 tiles wide
            criarCorredorVertical(y1, y2, x1);
            criarCorredorHorizontal(x1, x2, y2);
        }

        // break through room walls where corridors connect
        breakWallsAtConnection(sala1);
        breakWallsAtConnection(sala2);
    }

    private void breakWallsAtConnection(Sala sala) {
        for (int y = sala.y(); y < sala.y() + sala.altura(); y++) {
            for (int x = sala.x(); x < sala.x() + sala.largura(); x++) {
                if (dentroDosLimites(x, y) && grade[y][x] == Bloco.PAREDE) {
                    boolean nextToFloor = false;
                    int[][] dirs = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
                    for (int[] dir : dirs) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        if (dentroDosLimites(nx, ny) && grade[ny][nx] == Bloco.CHAO) {
                            if (nx < sala.x() || nx >= sala.x() + sala.largura() ||
                                    ny < sala.y() || ny >= sala.y() + sala.altura()) {
                                nextToFloor = true;
                                break;
                            }
                        }
                    }
                    if (nextToFloor) {
                        grade[y][x] = Bloco.CHAO;
                    }
                }
            }
        }
    }

    private void criarCorredorHorizontal(int x1, int x2, int y) {
        int inicio = Math.min(x1, x2);
        int fim = Math.max(x1, x2);

        // gap min de 2x2
        for (int x = inicio; x <= fim; x++) {
            if (dentroDosLimites(x, y)) {
                grade[y][x] = Bloco.CHAO;
            }
            if (dentroDosLimites(x, y + 1)) {
                grade[y + 1][x] = Bloco.CHAO;
            }
        }
    }

    private void criarCorredorVertical(int y1, int y2, int x) {
        int inicio = Math.min(y1, y2);
        int fim = Math.max(y1, y2);

        // gap min de 2x2
        for (int y = inicio; y <= fim; y++) {
            if (dentroDosLimites(x, y)) {
                grade[y][x] = Bloco.CHAO;
            }
            if (dentroDosLimites(x + 1, y)) {
                grade[y][x + 1] = Bloco.CHAO;
            }
        }
    }

    private void colocarEscada() {
        // gerar escadas
        if (!salas.isEmpty()) {
            Sala ultimaSala = salas.get(salas.size() - 1);
            int x = ultimaSala.centroX();
            int y = ultimaSala.centroY();

            // pelo menos 2x2
            escadaPos = new Posicao(x, y);
            for (int dy = 0; dy < 2; dy++) {
                for (int dx = 0; dx < 2; dx++) {
                    if (dentroDosLimites(x + dx, y + dy)) {
                        grade[y + dy][x + dx] = Bloco.ESCADA;
                    }
                }
            }
        }
    }

    public boolean dentroDosLimites(int x, int y) {
        return y >= 0 && y < grade.length && x >= 0 && x < grade[0].length;
    }

    public boolean podeAndar(int x, int y) {
        return dentroDosLimites(x, y) &&
                (grade[y][x] == Bloco.CHAO || grade[y][x] == Bloco.ESCADA);
    }

    public boolean isEscada(int x, int y) {
        return dentroDosLimites(x, y) && grade[y][x] == Bloco.ESCADA;
    }

    public Posicao posicaoAleatoria() {
        // spawn in the first room (not where stairs are)
        if (salas.isEmpty()) {
            return new Posicao(grade[0].length / 2, grade.length / 2);
        }

        Sala primeiraLala = salas.get(0);
        // gap de 2 tiles min
        int margem = 3;
        int larguraUtil = Math.max(1, primeiraLala.largura() - 2 * margem);
        int alturaUtil = Math.max(1, primeiraLala.altura() - 2 * margem);

        int x = primeiraLala.x() + margem + r.nextInt(larguraUtil);
        int y = primeiraLala.y() + margem + r.nextInt(alturaUtil);

        if (podeAndar(x, y) && podeAndar(x + 1, y) && podeAndar(x, y + 1) && podeAndar(x + 1, y + 1)) {
            return new Posicao(x, y);
        }

        // fallback: try room center
        return new Posicao(primeiraLala.centroX(), primeiraLala.centroY());
    }

    public Posicao posicaoAleatoriaLongeDoJogador(Posicao jogadorPos, int distanciaMinima) {
        int tentativas = 0;
        int maxTentativas = 100;

        while (tentativas < maxTentativas) {
            // escolhe uma sala random
            Sala salaAleatoria = salas.get(1 + r.nextInt(Math.max(1, salas.size() - 1)));

            int margem = 3;
            int larguraUtil = Math.max(1, salaAleatoria.largura() - 2 * margem);
            int alturaUtil = Math.max(1, salaAleatoria.altura() - 2 * margem);

            int x = salaAleatoria.x() + margem + r.nextInt(larguraUtil);
            int y = salaAleatoria.y() + margem + r.nextInt(alturaUtil);

            // check se a posição é válida
            if (podeAndar(x, y) && podeAndar(x + 1, y) && podeAndar(x, y + 1) && podeAndar(x + 1, y + 1)) {
                double distancia = Math.sqrt(Math.pow(x - jogadorPos.x(), 2) + Math.pow(y - jogadorPos.y(), 2));
                if (distancia >= distanciaMinima) {
                    return new Posicao(x, y);
                }
            }

            tentativas++;
        }

        // fallback: use last room center
        if (!salas.isEmpty()) {
            Sala ultimaSala = salas.get(salas.size() - 1);
            return new Posicao(ultimaSala.centroX(), ultimaSala.centroY());
        }

        return new Posicao(grade[0].length / 2, grade.length / 2);
    }

    public String[][] comoArrayString() {
        String[][] arr = new String[grade.length][grade[0].length];
        for (int y = 0; y < grade.length; y++)
            for (int x = 0; x < grade[0].length; x++)
                arr[y][x] = grade[y][x].simbolo;
        return arr;
    }
}