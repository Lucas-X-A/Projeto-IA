package com.example.qlearning;

public class Main {
    public static void main(String[] args) {
        int tamanhoGrade = 5;
        int episodios = 1000;

        GridWorld ambiente = new GridWorld(tamanhoGrade, tamanhoGrade);
        Agent agente = new Agent(0.1, 0.9, 0.1);

        for (int i = 0; i < episodios; i++) {
            ambiente.reiniciar();
            String estado = ambiente.getEstadoAtual();
            boolean finalizado = false;

            while (!finalizado) {
                int acao = agente.escolherAcao(estado);
                Object[] resultado = ambiente.executarPasso(acao);
                double recompensa = (double) resultado[0];
                String proximoEstado = (String) resultado[1];
                finalizado = (boolean) resultado[2];

                agente.atualizarTabelaQ(estado, acao, recompensa, proximoEstado);
                estado = proximoEstado;
            }
            System.out.println("Episódio " + (i + 1) + " concluído.");
        }

        System.out.println("\nTreinamento concluído!");
    }
}