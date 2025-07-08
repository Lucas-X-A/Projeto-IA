package org.example.projeto_ia;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.qlearning.Agent;
import org.example.qlearning.GridWorld;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GridWorldFX extends Application {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private TextArea logArea;
    private GridPane gridVisualization;
    private TextArea qTableArea;
    private TextArea pathArea;
    private Button startButton;

    @Override
    public void start(Stage primaryStage) {
        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 1. Painel superior - Controles
        HBox controlsPanel = createControlsPanel(primaryStage);
        root.setTop(controlsPanel);

        // 2. Painel central - Visualização do grid e logs
        SplitPane centerPanel = new SplitPane();

        // Visualização do grid
        VBox gridBox = new VBox(10);
        gridBox.setPadding(new Insets(10));
        Label gridLabel = new Label("Visualização do GridWorld");
        gridLabel.setFont(new Font(14));
        gridVisualization = new GridPane();
        gridVisualization.setHgap(5);
        gridVisualization.setVgap(5);
        gridBox.getChildren().addAll(gridLabel, gridVisualization);

        // Área de logs
        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        Label logLabel = new Label("Progresso do Treinamento");
        logLabel.setFont(new Font(14));
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logBox.getChildren().addAll(logLabel, logArea);

        centerPanel.getItems().addAll(gridBox, logBox);
        root.setCenter(centerPanel);

        // 3. Painel inferior - Q-Table e Caminho Ótimo
        SplitPane bottomPanel = new SplitPane();

        // Q-Table
        VBox qTableBox = new VBox(10);
        qTableBox.setPadding(new Insets(10));
        Label qTableLabel = new Label("Q-Table");
        qTableLabel.setFont(new Font(14));
        qTableArea = new TextArea();
        qTableArea.setEditable(false);
        qTableArea.setFont(Font.font("Monospaced", 12));
        qTableBox.getChildren().addAll(qTableLabel, qTableArea);

        // Caminho Ótimo
        VBox pathBox = new VBox(10);
        pathBox.setPadding(new Insets(10));
        Label pathLabel = new Label("Caminho Ótimo");
        pathLabel.setFont(new Font(14));
        pathArea = new TextArea();
        pathArea.setEditable(false);
        pathArea.setFont(Font.font("Monospaced", 12));
        pathBox.getChildren().addAll(pathLabel, pathArea);

        bottomPanel.getItems().addAll(qTableBox, pathBox);
        root.setBottom(bottomPanel);

        // Configuração da cena
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("QLearning - GridWorld");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createControlsPanel(Stage stage) {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        startButton = new Button("Iniciar Treinamento");
        startButton.setOnAction(e -> startTraining());

        Button resetButton = new Button("Reiniciar");
        resetButton.setOnAction(e -> resetUI());

        controls.getChildren().addAll(startButton, resetButton);
        return controls;
    }

    private void startTraining() {
        // Desabilita o botão durante o treinamento
        startButton.setDisable(true);

        new Thread(() -> {
            int tamanhoGrade = 5;
            int totalEpisodios = 1000;
            int reportInterval = 100;

            GridWorld ambiente = new GridWorld(tamanhoGrade, tamanhoGrade);
            Agent agente = new Agent(0.1, 0.9, 0.1);
            List<Double> recompensasPorEpisodio = new ArrayList<>();

            // Atualização inicial do grid
            updateGridVisualization(ambiente);

            for (int i = 0; i < totalEpisodios; i++) {
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

                recompensasPorEpisodio.add(recompensaAcumulada);

                if ((i + 1) % reportInterval == 0) {
                    final int episode = i + 1;
                    final double reward = recompensaAcumulada;

                    Platform.runLater(() -> {
                        appendToLog(String.format("Episódio %d - Recompensa: %s",
                                episode, df.format(reward)));
                        updateGridVisualization(ambiente); // Atualiza a cada X episódios
                    });
                }
            }

            Platform.runLater(() -> {
                appendToLog("\nTreinamento concluído!\n");
                showStatistics(recompensasPorEpisodio);
                showQTable(agente.getTabelaQ(), tamanhoGrade);
                showOptimalPath(agente.getTabelaQ(), new GridWorld(tamanhoGrade, tamanhoGrade));
                startButton.setDisable(false); // Reabilita o botão
            });
        }).start();
    }

    private void updateGridVisualization(GridWorld grid) {
        Platform.runLater(() -> {
            gridVisualization.getChildren().clear();

            int cellSize = 50;
            for (int y = 0; y < grid.getAltura(); y++) {
                for (int x = 0; x < grid.getLargura(); x++) {
                    Rectangle rect = new Rectangle(cellSize, cellSize);

                    if (x == grid.getxAgente() && y == grid.getyAgente()) {
                        rect.setFill(Color.BLUE);
                    } else if (x == grid.getxObjetivo() && y == grid.getyObjetivo()) {
                        rect.setFill(Color.GREEN);
                    } else if (grid.isArmadilha(x, y)) {
                        rect.setFill(Color.RED);
                    } else {
                        rect.setFill(Color.WHITE);
                    }

                    rect.setStroke(Color.BLACK);
                    gridVisualization.add(rect, x, y);

                    Label coord = new Label(x + "," + y);
                    coord.setStyle("-fx-font-size: 8pt;");
                    gridVisualization.add(coord, x, y);
                }
            }
        });
    }

    private void appendToLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }

    private void showStatistics(List<Double> recompensas) {
        double media = recompensas.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        appendToLog("\nEstatísticas de Treinamento:");
        appendToLog(String.format("Média de recompensa por episódio: %s", df.format(media)));
    }

    private void showQTable(Map<String, double[]> qTable, int gridSize) {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                String state = x + ":" + y;
                if (qTable.containsKey(state)) {
                    double[] actions = qTable.get(state);
                    sb.append(String.format("Estado (%d,%d): ↑%s ↓%s ←%s →%s%n",
                            x, y,
                            df.format(actions[0]),
                            df.format(actions[1]),
                            df.format(actions[2]),
                            df.format(actions[3])));
                }
            }
        }

        qTableArea.setText(sb.toString());
    }

    private void showOptimalPath(Map<String, double[]> qTable, GridWorld grid) {
        StringBuilder sb = new StringBuilder();

        grid.reiniciar();
        String currentState = grid.getEstadoAtual();
        boolean finalizado = false;
        int maxSteps = 20;
        int step = 0;

        while (!finalizado && step < maxSteps) {
            step++;
            int bestAction = getBestAction(qTable, currentState);
            String actionSymbol = getActionSymbol(bestAction);
            sb.append(String.format("Estado %s → %s ", currentState, actionSymbol));

            Object[] result = grid.executarPasso(bestAction);
            currentState = (String) result[1];
            finalizado = (boolean) result[2];

            sb.append(String.format("→ %s (Recompensa: %s)%n",
                    currentState, df.format((double) result[0])));
        }

        pathArea.setText(sb.toString());
    }

    private int getBestAction(Map<String, double[]> qTable, String state) {
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

    private String getActionSymbol(int action) {
        switch (action) {
            case 0: return "↑";
            case 1: return "↓";
            case 2: return "←";
            case 3: return "→";
            default: return "?";
        }
    }

    private void resetUI() {
        logArea.clear();
        qTableArea.clear();
        pathArea.clear();
        gridVisualization.getChildren().clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}