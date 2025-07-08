package com.example.qlearning;

import java.util.Map;

public class QTableVisualizer {
    public static void printQTable(Map<String, double[]> qTable, int gridSize) {
        System.out.println("\nQ-Table Visualização:");

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                String state = x + ":" + y;
                if (qTable.containsKey(state)) {
                    System.out.printf("Estado (%d,%d): ", x, y);
                    double[] actions = qTable.get(state);
                    System.out.printf("↑%.2f ↓%.2f ←%.2f →%.2f%n",
                            actions[0], actions[1], actions[2], actions[3]);
                }
            }
        }
    }

    public static void printOptimalPath(Map<String, double[]> qTable, GridWorld grid) {
        System.out.println("\nCaminho Ótimo:");

        grid.reiniciar();
        String currentState = grid.getEstadoAtual();
        boolean finalizado = false;
        int maxSteps = 20;
        int step = 0;

        while (!finalizado && step < maxSteps) {
            step++;
            int bestAction = getBestAction(qTable, currentState);
            String actionSymbol = getActionSymbol(bestAction);
            System.out.printf("Estado %s → %s ", currentState, actionSymbol);

            Object[] result = grid.executarPasso(bestAction);
            currentState = (String) result[1];
            finalizado = (boolean) result[2];

            System.out.printf("→ %s (Recompensa: %.1f)%n", currentState, (double) result[0]);
        }
    }

    private static int getBestAction(Map<String, double[]> qTable, String state) {
        if (!qTable.containsKey(state)) return 0;

        double[] actions = qTable.get(state);
        int bestAction = 0;
        for (int i = 1; i < actions.length; i++) {
            if (actions[i] > actions[bestAction]) {
                bestAction = i;
            }
        }
        return bestAction;
    }

    private static String getActionSymbol(int action) {
        switch (action) {
            case 0: return "↑";
            case 1: return "↓";
            case 2: return "←";
            case 3: return "→";
            default: return "?";
        }
    }
}