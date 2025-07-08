package org.example.qlearning;

public class Main {
    public static void main(String[] args) {
        int tamanhoGrade = 5;
        int episodios = 1000;

        GridWorld ambiente = new GridWorld(tamanhoGrade, tamanhoGrade);
        Agent agente = new Agent(0.1, 0.9, 0.1);
        TrainingMonitor monitor = new TrainingMonitor();

        for (int i = 0; i < episodios; i++) {
            ambiente.reiniciar();
            String estado = ambiente.getEstadoAtual();
            boolean finalizado = false;
            double recompensaAcumulada = 0;

            while (!finalizado) {
                int acao = agente.escolherAcao(estado);
                Object[] resultado = ambiente.executarPasso(acao);
                double recompensa = (double) resultado[0];
                String proximoEstado = (String) resultado[1];
                finalizado = (boolean) resultado[2];

                agente.atualizarTabelaQ(estado, acao, recompensa, proximoEstado);
                estado = proximoEstado;
                recompensaAcumulada += recompensa;
            }

            monitor.recordEpisode(recompensaAcumulada);

            if ((i + 1) % 100 == 0) {
                System.out.printf("Episódio %d - Recompensa: %.1f%n", (i + 1), recompensaAcumulada);
            }
        }

        System.out.println("\nTreinamento concluído!");

        // Visualizações
        monitor.printTrainingStats();
        QTableVisualizer.printQTable(agente.getTabelaQ(), tamanhoGrade);
        QTableVisualizer.printOptimalPath(agente.getTabelaQ(), new GridWorld(tamanhoGrade, tamanhoGrade));
    }
}