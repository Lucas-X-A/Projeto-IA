package org.example.qlearning;

import java.util.ArrayList;
import java.util.List;

public class TrainingMonitor {
    private List<Double> episodeRewards;

    public TrainingMonitor() {
        this.episodeRewards = new ArrayList<>();
    }

    public void recordEpisode(double totalReward) {
        episodeRewards.add(totalReward);
    }

    public void printTrainingStats() {
        System.out.println("\nEstatísticas de Treinamento:");
        System.out.printf("Média de recompensa por episódio: %.2f%n",
                episodeRewards.stream().mapToDouble(Double::doubleValue).average().orElse(0));

        // Podemos adicionar mais estatísticas aqui
    }
}