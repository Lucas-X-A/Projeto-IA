package com.example.qlearning;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Agent {
    private Map<String, double[]> tabelaQ;
    private double alpha; // Taxa de aprendizado
    private double gamma; // Fator de desconto
    private double epsilon; // Taxa de exploração

    public Agent(double alpha, double gamma, double epsilon) {
        this.tabelaQ = new HashMap<>();
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
    }

    public int escolherAcao(String estado) {
        tabelaQ.putIfAbsent(estado, new double[4]); // 4 ações: cima, baixo, esquerda, direita
        if (new Random().nextDouble() < epsilon) {
            return new Random().nextInt(4); // Ação aleatória
        } else {
            return obterMelhorAcao(estado);
        }
    }

    private int obterMelhorAcao(String estado) {
        double[] acoes = tabelaQ.get(estado);
        int melhorAcao = 0;
        for (int i = 1; i < acoes.length; i++) {
            if (acoes[i] > acoes[melhorAcao]) {
                melhorAcao = i;
            }
        }
        return melhorAcao;
    }

    public void atualizarTabelaQ(String estado, int acao, double recompensa, String proximoEstado) {
        tabelaQ.putIfAbsent(estado, new double[4]);
        tabelaQ.putIfAbsent(proximoEstado, new double[4]);

        double valorQAntigo = tabelaQ.get(estado)[acao];
        double proximoQMax = tabelaQ.get(proximoEstado)[obterMelhorAcao(proximoEstado)];

        double novoValorQ = valorQAntigo + alpha * (recompensa + gamma * proximoQMax - valorQAntigo);
        tabelaQ.get(estado)[acao] = novoValorQ;
    }
}