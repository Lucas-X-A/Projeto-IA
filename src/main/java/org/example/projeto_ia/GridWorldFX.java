package org.example.projeto_ia;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.qlearning.Point;
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
    private TextField gridSizeField;
    private TextField alphaField;
    private TextField gammaField;
    private TextField epsilonField;
    private TextField episodesField;
    private TextArea trapsArea;

    // Área para mostrar o caminho do primeiro episódio
    private TextArea firstEpisodePathArea;

    @Override
    public void start(Stage primaryStage) {
        // Layout principal dentro de um ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 1. Painel superior - Controles e Legenda
        VBox topContainer = new VBox(10);
        HBox controlsPanel = createControlsPanel(primaryStage);
        HBox legendPanel = createLegendPanel();
        topContainer.getChildren().addAll(controlsPanel, legendPanel);
        root.setTop(topContainer);

        // 2. Painel central - Visualização do grid e logs
        SplitPane centerPanel = new SplitPane();

        // Visualização do grid
        VBox gridBox = new VBox(10);
        gridBox.setPadding(new Insets(10));
        Label gridLabel = new Label("Visualização do GridWorld (com Política Aprendida)");
        gridLabel.setFont(new Font(14));
        gridVisualization = new GridPane();
        gridVisualization.setHgap(2); // Diminuído para melhor visualização
        gridVisualization.setVgap(2); // Diminuído para melhor visualização
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

        // 3. Painel inferior - Q-Table e Caminhos
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

        // Caminhos (agora em um VBox para ter os dois)
        VBox pathsContainer = new VBox(10);
        pathsContainer.setPadding(new Insets(10));

        // Caminho Ótimo
        Label pathLabel = new Label("Caminho Ótimo (Final)");
        pathLabel.setFont(new Font(14));
        pathArea = new TextArea();
        pathArea.setEditable(false);
        pathArea.setFont(Font.font("Monospaced", 12));

        //Caminho do Primeiro Episódio
        Label firstEpisodePathLabel = new Label("Caminho do 1º Episódio (Aleatório)");
        firstEpisodePathLabel.setFont(new Font(14));
        firstEpisodePathArea = new TextArea();
        firstEpisodePathArea.setEditable(false);
        firstEpisodePathArea.setFont(Font.font("Monospaced", 12));

        pathsContainer.getChildren().addAll(firstEpisodePathLabel, firstEpisodePathArea, pathLabel, pathArea);

        bottomPanel.getItems().addAll(qTableBox, pathsContainer);
        root.setBottom(bottomPanel);

        scrollPane.setContent(root);

        // Pega os limites visuais da tela principal
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        // Define o tamanho da cena para ser uma porcentagem da tela.
        double sceneWidth = Math.min(1200, visualBounds.getWidth() * 0.96);
        double sceneHeight = Math.min(900, visualBounds.getHeight() * 0.96);

        // Configuração da cena
        Scene scene = new Scene(scrollPane, sceneWidth, sceneHeight); // Aumentado o tamanho da tela
        primaryStage.setTitle("QLearning - GridWorld");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ... (createControlsPanel continua o mesmo, sem alterações)
    private HBox createControlsPanel(Stage stage) {
        HBox mainControls = new HBox(10);
        mainControls.setAlignment(Pos.CENTER_LEFT);
        mainControls.setPadding(new Insets(10));

        // Grid - Tamanho
        VBox gridBox = new VBox(5);
        gridBox.setPadding(new Insets(0, 10, 0, 0));
        Label gridLabel = new Label("Tamanho do Grid:");
        gridSizeField = new TextField("5");
        gridSizeField.setPrefWidth(40);
        gridBox.getChildren().addAll(gridLabel, gridSizeField);

        // Parâmetros do Algoritmo
        VBox paramsBox = new VBox(5);
        paramsBox.setPadding(new Insets(0, 10, 0, 0));
        Label paramsLabel = new Label("Parâmetros:");
        HBox paramsFields = new HBox(5);
        alphaField = new TextField("0.1");
        gammaField = new TextField("0.9");
        epsilonField = new TextField("0.1");
        alphaField.setPrefWidth(40);
        gammaField.setPrefWidth(40);
        epsilonField.setPrefWidth(40);
        paramsFields.getChildren().addAll(
                new Label("α:"), alphaField,
                new Label("γ:"), gammaField,
                new Label("ε:"), epsilonField
        );
        paramsBox.getChildren().addAll(paramsLabel, paramsFields);

        // Episódios
        VBox episodesBox = new VBox(5);
        episodesBox.setPadding(new Insets(0, 10, 0, 0));
        Label episodesLabel = new Label("Episódios:");
        episodesField = new TextField("1000");
        episodesField.setPrefWidth(60);
        episodesBox.getChildren().addAll(episodesLabel, episodesField);

        // Armadilhas (mais compacto)
        VBox trapsBox = new VBox(5);
        trapsBox.setPadding(new Insets(0, 10, 0, 0));
        Label trapsLabel = new Label("Armadilhas (x,y):");
        trapsArea = new TextArea("1,1\n1,3\n3,1");
        trapsArea.setPrefRowCount(3);
        trapsArea.setPrefWidth(100);
        trapsBox.getChildren().addAll(trapsLabel, trapsArea);

        // Botões
        VBox buttonsBox = new VBox(5);
        buttonsBox.setPadding(new Insets(0, 10, 0, 0));
        startButton = new Button("Iniciar");
        startButton.setOnAction(e -> startTraining());

        Button resetButton = new Button("Reiniciar");
        resetButton.setOnAction(e -> resetUI());

        buttonsBox.getChildren().addAll(startButton, resetButton);

        // Adiciona todos os componentes ao painel principal
        mainControls.getChildren().addAll(
                gridBox,
                paramsBox,
                episodesBox,
                trapsBox,
                buttonsBox
        );

        return mainControls;
    }


    private void startTraining() {
        startButton.setDisable(true);
        resetUI(); // Limpa a UI antes de começar

        try {
            int tamanhoGrade = Integer.parseInt(gridSizeField.getText());
            double alpha = Double.parseDouble(alphaField.getText());
            double gamma = Double.parseDouble(gammaField.getText());
            double epsilon = Double.parseDouble(epsilonField.getText());
            int totalEpisodios = Integer.parseInt(episodesField.getText());

            List<Point> armadilhas = new ArrayList<>();
            String[] linhas = trapsArea.getText().split("\n");
            for (String linha : linhas) {
                if (!linha.trim().isEmpty()) {
                    String[] coords = linha.trim().split(",");
                    if (coords.length == 2) {
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        armadilhas.add(new Point(x, y));
                    }
                }
            }

            new Thread(() -> {
                int reportInterval = Math.max(1, totalEpisodios / 20); // Atualiza 20x durante o treino

                GridWorld ambiente = new GridWorld(tamanhoGrade, tamanhoGrade, armadilhas);
                Agent agente = new Agent(alpha, gamma, epsilon);
                List<Double> recompensasPorEpisodio = new ArrayList<>();

                // Variável para guardar o caminho do primeiro episódio
                final StringBuilder firstEpisodePathLog = new StringBuilder();

                // Atualização inicial do grid
                updateGridVisualization(ambiente, agente.getTabelaQ());

                for (int i = 0; i < totalEpisodios; i++) {
                    ambiente.reiniciar();
                    String estado = ambiente.getEstadoAtual();
                    boolean finalizado = false;
                    double recompensaAcumulada = 0;

                    if (i == 0) firstEpisodePathLog.append("Início: ").append(estado);

                    while (!finalizado) {
                        int acao = agente.escolherAcao(estado);

                        Object[] resultado = ambiente.executarPasso(acao);
                        double recompensa = (double) resultado[0];
                        String proximoEstado = (String) resultado[1];
                        finalizado = (boolean) resultado[2];

                        agente.atualizarTabelaQ(estado, acao, recompensa, proximoEstado);

                        // Loga o caminho do primeiro episódio
                        if (i == 0) {
                            firstEpisodePathLog.append(String.format(" → %s (%s)", getActionSymbol(acao), proximoEstado));
                        }

                        estado = proximoEstado;
                        recompensaAcumulada += recompensa;
                    }

                    recompensasPorEpisodio.add(recompensaAcumulada);

                    if (i == 0) firstEpisodePathLog.append(" FIM!");

                    if ((i + 1) % reportInterval == 0 || i == 0) {
                        final int episode = i + 1;
                        final double reward = recompensaAcumulada;
                        final Map<String, double[]> currentQTable = agente.getTabelaQ();

                        Platform.runLater(() -> {
                            appendToLog(String.format("Episódio %d - Recompensa: %s",
                                    episode, df.format(reward)));
                            // ALTERADO: Passa a Q-Table para a visualização
                            updateGridVisualization(ambiente, currentQTable);
                        });

                        // Pequena pausa para a GUI conseguir se atualizar visualmente
                        try { Thread.sleep(50); } catch (InterruptedException e) {}
                    }
                }

                Platform.runLater(() -> {
                    appendToLog("\nTreinamento concluído!\n");
                    showStatistics(recompensasPorEpisodio);
                    showQTable(agente.getTabelaQ(), tamanhoGrade);
                    //Exibe o caminho do primeiro episódio
                    firstEpisodePathArea.setText(firstEpisodePathLog.toString());
                    showOptimalPath(agente.getTabelaQ(), new GridWorld(tamanhoGrade, tamanhoGrade, armadilhas));
                    startButton.setDisable(false);
                });
            }).start();
        } catch (Exception e) {
            appendToLog("Erro: Verifique os valores inseridos. " + e.getMessage());
            startButton.setDisable(false);
        }
    }
    
    private void updateGridVisualization(GridWorld grid, Map<String, double[]> qTable) {
        Platform.runLater(() -> {
            gridVisualization.getChildren().clear();
            int cellSize = 60; // Aumentado para caber texto e setas

            for (int y = 0; y < grid.getAltura(); y++) {
                for (int x = 0; x < grid.getLargura(); x++) {
                    StackPane cellPane = new StackPane();
                    Rectangle rect = new Rectangle(cellSize, cellSize);

                    // Pinta o fundo da célula
                    if (x == 0 && y == 0) {
                        rect.setFill(Color.YELLOW); // Ponto de partida
                    } else if (x == grid.getxObjetivo() && y == grid.getyObjetivo()) {
                        rect.setFill(Color.GREEN);
                    } else if (grid.isArmadilha(x, y)) {
                        rect.setFill(Color.RED);
                    } else {
                        rect.setFill(Color.WHITE);
                    }

                    // Posição atual do agente
                    if (x == grid.getxAgente() && y == grid.getyAgente()) {
                        rect.setFill(Color.BLUE.deriveColor(1, 1, 1, 0.7)); // Semi-transparente
                    }

                    rect.setStroke(Color.BLACK);
                    cellPane.getChildren().add(rect);

                    // Adiciona a seta e o valor-Q
                    String state = x + ":" + y;
                    if (qTable.containsKey(state) && !grid.isArmadilha(x, y) && !(x == grid.getxObjetivo() && y == grid.getyObjetivo())) {
                        double[] actions = qTable.get(state);
                        int bestAction = getBestAction(qTable, state);
                        double bestValue = actions[bestAction];

                        // Seta indicando a melhor ação
                        Text arrow = new Text(getActionSymbol(bestAction));
                        arrow.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                        arrow.setFill(Color.BLACK);

                        // Texto com o valor Q máximo
                        Label qValueLabel = new Label(df.format(bestValue));
                        qValueLabel.setFont(Font.font("Arial", 10));
                        qValueLabel.setTextFill(Color.DARKSLATEGRAY);

                        // Posiciona os elementos dentro da célula
                        VBox content = new VBox(2);
                        content.setAlignment(Pos.CENTER);
                        content.getChildren().addAll(arrow, qValueLabel);

                        cellPane.getChildren().add(content);
                    }

                    gridVisualization.add(cellPane, x, y);
                }
            }
        });
    }

    private HBox createLegendPanel() {
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(0, 10, 0, 10));

        legend.getChildren().addAll(
                createLegendItem(Color.YELLOW, "Início"),
                createLegendItem(Color.GREEN, "Objetivo"),
                createLegendItem(Color.RED, "Armadilha"),
                createLegendItem(Color.BLUE.deriveColor(1, 1, 1, 0.7), "Agente")
        );
        return legend;
    }

    private HBox createLegendItem(Color color, String text) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        Rectangle colorRect = new Rectangle(15, 15, color);
        colorRect.setStroke(Color.BLACK);
        item.getChildren().addAll(colorRect, new Label(text));
        return item;
    }


    private void appendToLog(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    private void showStatistics(List<Double> recompensas) {
        double media = recompensas.stream().mapToDouble(Double::doubleValue).average().orElse(0);
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
                            x, y, df.format(actions[0]), df.format(actions[1]),
                            df.format(actions[2]), df.format(actions[3])));
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
        int maxSteps = grid.getLargura() * grid.getAltura();
        int step = 0;

        sb.append("Início: ").append(currentState);

        while (!finalizado && step < maxSteps) {
            step++;
            int bestAction = getBestAction(qTable, currentState);
            String actionSymbol = getActionSymbol(bestAction);

            Object[] result = grid.executarPasso(bestAction);
            currentState = (String) result[1];
            finalizado = (boolean) result[2];

            sb.append(String.format(" → %s (%s)", actionSymbol, currentState));
        }
        sb.append(" FIM!");
        pathArea.setText(sb.toString());
    }

    private int getBestAction(Map<String, double[]> qTable, String state) {
        if (!qTable.containsKey(state)) return new java.util.Random().nextInt(4); // Ação aleatória se estado é desconhecido
        double[] actions = qTable.get(state);
        int bestAction = 0;
        double maxQ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] > maxQ) {
                maxQ = actions[i];
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
        if(firstEpisodePathArea != null) firstEpisodePathArea.clear();
        gridVisualization.getChildren().clear();
        // Desenha um grid vazio inicial
        try {
            int gridSize = Integer.parseInt(gridSizeField.getText());
            updateGridVisualization(new GridWorld(gridSize, gridSize, new ArrayList<>()), new java.util.HashMap<>());
        } catch (NumberFormatException e) {
            // ignora se o campo estiver inválido
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}