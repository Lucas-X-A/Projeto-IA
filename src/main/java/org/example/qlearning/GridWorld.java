package org.example.qlearning;

import java.util.List;

public class GridWorld {
    private final int largura;
    private final int altura;
    private final int xInicial, yInicial;
    private final int xObjetivo, yObjetivo;
    private final boolean[][] armadilhas;

    private int xAgente, yAgente;

    public GridWorld(int largura, int altura, List<Point> armadilhas) {
        this.largura = largura;
        this.altura = altura;
        this.xInicial = 0;
        this.yInicial = 0;
        this.xObjetivo = largura - 1;
        this.yObjetivo = altura - 1;

        // Posições das armadilhas
        this.armadilhas = new boolean[largura][altura];
        for (Point p : armadilhas) {
            if (p.getX() >= 0 && p.getX() < largura && p.getY() >= 0 && p.getY() < altura) {
                this.armadilhas[p.getX()][p.getY()] = true;
            }
        }

        reiniciar();
    }

    public void reiniciar() {
        this.xAgente = this.xInicial;
        this.yAgente = this.yInicial;
    }

    public String getEstadoAtual() {
        return xAgente + ":" + yAgente;
    }

    // Retorna [recompensa, novoEstado, finalizado]
    public Object[] executarPasso(int acao) {
        // 0: cima, 1: baixo, 2: esquerda, 3: direita
        int proximoX = xAgente;
        int proximoY = yAgente;

        if (acao == 0) proximoY--; // Cima
        else if (acao == 1) proximoY++; // Baixo
        else if (acao == 2) proximoX--; // Esquerda
        else if (acao == 3) proximoX++; // Direita

        // Verifica limites
        if (proximoX < 0 || proximoX >= largura || proximoY < 0 || proximoY >= altura) {
            // Permanece no mesmo lugar se a ação for inválida
            proximoX = xAgente;
            proximoY = yAgente;
        }

        xAgente = proximoX;
        yAgente = proximoY;

        double recompensa = -0.1; // Custo de movimento
        boolean finalizado = false;

        if (xAgente == xObjetivo && yAgente == yObjetivo) {
            recompensa = 10.0;
            finalizado = true;
        } else if (armadilhas[xAgente][yAgente]) {
            recompensa = -10.0;
            finalizado = true;
        }

        return new Object[]{recompensa, getEstadoAtual(), finalizado};
    }

    public boolean isFinalState() {
        return (xAgente == xObjetivo && yAgente == yObjetivo) || armadilhas[xAgente][yAgente];
    }

    public boolean isArmadilha(int x, int y) { return armadilhas[x][y]; }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    public int getxInicial() {
        return xInicial;
    }

    public int getyInicial() {
        return yInicial;
    }

    public int getxObjetivo() {
        return xObjetivo;
    }

    public int getyObjetivo() {
        return yObjetivo;
    }

    public boolean[][] getArmadilhas() {
        return armadilhas;
    }

    public int getxAgente() {
        return xAgente;
    }

    public void setxAgente(int xAgente) {
        this.xAgente = xAgente;
    }

    public int getyAgente() {
        return yAgente;
    }

    public void setyAgente(int yAgente) {
        this.yAgente = yAgente;
    }
}