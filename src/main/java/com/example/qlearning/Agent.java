package com.example.qlearning;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Agent {
    private Map<String, double[]> qTable;
    private double alpha; // Taxa de aprendizado
    private double gamma; // Fator de desconto
    private double epsilon; // Taxa de exploração

    public Agent(int stateSize, int actionSize, double alpha, double gamma, double epsilon) {
        this.qTable = new HashMap<>();
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
    }

    public int chooseAction(String state) {
        qTable.putIfAbsent(state, new double[4]); // 4 ações: cima, baixo, esquerda, direita
        if (new Random().nextDouble() < epsilon) {
            return new Random().nextInt(4); // Ação aleatória
        } else {
            return getBestAction(state);
        }
    }

    private int getBestAction(String state) {
        double[] actions = qTable.get(state);
        int bestAction = 0;
        for (int i = 1; i < actions.length; i++) {
            if (actions[i] > actions[bestAction]) {
                bestAction = i;
            }
        }
        return bestAction;
    }

    public void updateQTable(String state, int action, double reward, String nextState) {
        qTable.putIfAbsent(state, new double[4]);
        qTable.putIfAbsent(nextState, new double[4]);

        double oldQValue = qTable.get(state)[action];
        double nextMaxQ = qTable.get(nextState)[getBestAction(nextState)];

        double newQValue = oldQValue + alpha * (reward + gamma * nextMaxQ - oldQValue);
        qTable.get(state)[action] = newQValue;
    }
}
