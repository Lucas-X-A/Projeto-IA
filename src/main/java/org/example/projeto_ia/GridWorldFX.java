package org.example.projeto_ia;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
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
import org.example.qlearning.QTableRow;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GridWorldFX extends Application {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private TextArea logArea;
    private GridPane gridVisualization;
    private TableView<QTableRow> qTableView;
    private TextArea pathArea;
    private Button startButton;
    private TextField gridSizeField;
    private TextField alphaField;
    private TextField gammaField;
    private TextField epsilonField;
    private TextField episodesField;
    private TextArea trapsArea;
    private CheckBox stepByStepCheckBox;
    private Button nextStepButton;
    private Agent agente;
    private GridWorld ambiente;
    private int totalEpisodios;
    private int episodioAtual;
    private List<Double> recompensasPorEpisodio;
    private double recompensaAcumuladaEpisodio;
    private StringBuilder firstEpisodePathLogBuilder;

    // Área para mostrar o caminho do primeiro episódio
    private TextArea firstEpisodePathArea;

    private List<String> caminhoOtimo = new ArrayList<>();

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

        // Q-Table
        VBox qTableBox = new VBox(10);
        qTableBox.setPadding(new Insets(10));
        Label qTableLabel = new Label("Q-Table");
        qTableLabel.setFont(new Font(14));
        qTableView = new TableView<>();
        qTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        qTableView.setPrefHeight(300); // altura inicial

        TableColumn<QTableRow, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEstado()));

        TableColumn<QTableRow, String> upCol = new TableColumn<>("↑");
        upCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getUp()));

        TableColumn<QTableRow, String> downCol = new TableColumn<>("↓");
        downCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDown()));

        TableColumn<QTableRow, String> leftCol = new TableColumn<>("←");
        leftCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLeft()));

        TableColumn<QTableRow, String> rightCol = new TableColumn<>("→");
        rightCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRight()));

        qTableView.setRowFactory(tv -> new TableRow<QTableRow>() {
            @Override
            protected void updateItem(QTableRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (item.isHighlighted()) {
                        setStyle("-fx-background-color: lightblue; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        qTableView.getColumns().addAll(estadoCol, upCol, downCol, leftCol, rightCol);

        qTableBox.getChildren().addAll(qTableLabel, qTableView);

        // 3. Painel inferior - Q-Table e Caminhos
        SplitPane bottomPanel = new SplitPane();

        // Área de logs
        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        Label logLabel = new Label("Progresso do Treinamento");
        logLabel.setFont(new Font(14));
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logBox.getChildren().addAll(logLabel, logArea);

        centerPanel.getItems().addAll(gridBox, qTableBox);
        root.setCenter(centerPanel);

        // Caminhos (agora em um VBox para ter os dois)
        VBox pathsContainer = new VBox(10);
        pathsContainer.setPadding(new Insets(10));

        // Caminho Ótimo
        Label pathLabel = new Label("Caminho Ótimo (Final)");
        pathLabel.setFont(new Font(14));
        pathArea = new TextArea();
        pathArea.setEditable(false);
        pathArea.setWrapText(true);
        pathArea.setFocusTraversable(false);
        pathArea.setPrefRowCount(3); // Definido para 3 linhas
        pathArea.setFont(Font.font("Monospaced", 12));

        //Caminho do Primeiro Episódio
        Label firstEpisodePathLabel = new Label("Caminho do 1º Episódio (Aleatório)");
        firstEpisodePathLabel.setFont(new Font(14));
        firstEpisodePathArea = new TextArea();
        firstEpisodePathArea.setEditable(false);
        firstEpisodePathArea.setWrapText(true);
        firstEpisodePathArea.setFocusTraversable(false);
        firstEpisodePathArea.setPrefRowCount(3); // Definido para 3 linhas
        firstEpisodePathArea.setFont(Font.font("Monospaced", 12));

        pathsContainer.getChildren().addAll(firstEpisodePathLabel, firstEpisodePathArea, pathLabel, pathArea);

        bottomPanel.getItems().addAll(logBox, pathsContainer);
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

        // Desenha o grid inicial com base nos valores padrão dos controles
        resetUI();
    }

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

        // Armadilhas
        VBox trapsBox = new VBox(5);
        trapsBox.setPadding(new Insets(0, 10, 0, 0));
        Label trapsLabel = new Label("Armadilhas (x,y):");
        trapsArea = new TextArea(
                "1,1\n" +
                        "1,3\n" +
                        "3,1\n" +
                        "5,2\n" +
                        "3,5\n" +
                        "7,4\n" +
                        "3,8\n" +
                        "0,6\n" +
                        "8,6\n" +
                        "8,0\n" +
                        "6,7\n" +
                        "11,3\n" +
                        "2,11\n" +
                        "5,13\n" +
                        "10,14\n" +
                        "9,10\n" +
                        "12,8\n" +
                        "14,5\n" +
                        "14,12"
        );
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

        // Passo a Passo
        VBox stepBox = new VBox(5);
        stepBox.setPadding(new Insets(18, 10, 0, 0)); // Padding para alinhar
        stepByStepCheckBox = new CheckBox("Executar Passo a Passo");
        nextStepButton = new Button("Próximo Passo");
        nextStepButton.setOnAction(e -> runNextStep());
        nextStepButton.setDisable(true); // Desabilitado por padrão
        stepBox.getChildren().addAll(stepByStepCheckBox, nextStepButton);

        // Listeners para atualização dinâmica do grid
        gridSizeField.textProperty().addListener((obs, oldVal, newVal) -> updateGridFromControls());
        trapsArea.textProperty().addListener((obs, oldVal, newVal) -> updateGridFromControls());

        // Adiciona todos os componentes ao painel principal
        mainControls.getChildren().addAll(
                gridBox,
                paramsBox,
                episodesBox,
                trapsBox,
                buttonsBox,
                stepBox
        );

        return mainControls;
    }


    private void startTraining() {
        setControlsDisabled(true);
        resetUI(); // Limpa a UI antes de começar

        try {
            int tamanhoGrade = Integer.parseInt(gridSizeField.getText());
            double alpha = Double.parseDouble(alphaField.getText());
            double gamma = Double.parseDouble(gammaField.getText());
            double epsilon = Double.parseDouble(epsilonField.getText());
            this.totalEpisodios = Integer.parseInt(episodesField.getText());
            List<Point> armadilhas = parseTraps();

            // Inicializa agente e ambiente para ambos os modos
            this.ambiente = new GridWorld(tamanhoGrade, tamanhoGrade, armadilhas);
            this.agente = new Agent(alpha, gamma, epsilon);
            this.recompensasPorEpisodio = new ArrayList<>();
            this.episodioAtual = 0;
            this.firstEpisodePathLogBuilder = new StringBuilder();

            if (stepByStepCheckBox.isSelected()) {
                // Modo passo a passo
                nextStepButton.setDisable(false);
                appendToLog("Modo Passo a Passo iniciado. Clique em 'Próximo Passo'.");
                prepareForNextEpisode();
            } else {
                // Modo automático
                runTrainingAutomatically();
            }
        } catch (Exception e) {
            appendToLog("Erro: Verifique os valores inseridos. " + e.getMessage());
            setControlsDisabled(false);
        }
    }

    private void runTrainingAutomatically() {
        // Lógica de treinamento automático que já existia
        new Thread(() -> {
            int reportInterval = Math.max(1, totalEpisodios / 20); // Atualiza 20x durante o treino
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
                    if (i == 0) {
                        firstEpisodePathLog.append(String.format(" %s (%s)", getActionSymbol(acao), proximoEstado));
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
                        appendToLog(String.format("Episódio %d - Recompensa: %s", episode, df.format(reward)));
                        updateGridVisualization(ambiente, currentQTable);
                    });
                    try { Thread.sleep(50); } catch (InterruptedException e) {}
                }
            }
            Platform.runLater(() -> {
                appendToLog("\nTreinamento automático concluído!\n");
                double altura = calcularAlturaTexto(firstEpisodePathArea, firstEpisodePathLog.toString());
                firstEpisodePathArea.setPrefHeight(altura);
                firstEpisodePathArea.setText(firstEpisodePathLog.toString());
                finishTraining();
            });
        }).start();
    }

    private void runNextStep() {
        // Se o episódio anterior terminou, prepara o próximo
        if (ambiente.isFinalState()) {
            if (episodioAtual == 0) {
                firstEpisodePathLogBuilder.append(" FIM!");
                firstEpisodePathArea.setText(firstEpisodePathLogBuilder.toString());
            }
            recompensasPorEpisodio.add(recompensaAcumuladaEpisodio);
            episodioAtual++;
            prepareForNextEpisode();
            return;
        }

        // Executa um único passo do agente
        String estado = ambiente.getEstadoAtual();
        int acao = agente.escolherAcao(estado);
        Object[] resultado = ambiente.executarPasso(acao);
        double recompensa = (double) resultado[0];
        String proximoEstado = (String) resultado[1];

        agente.atualizarTabelaQ(estado, acao, recompensa, proximoEstado);
        recompensaAcumuladaEpisodio += recompensa;

        // Adiciona ao log do caminho do primeiro episódio
        if (episodioAtual == 0) {
            firstEpisodePathLogBuilder.append(String.format(" %s (%s)", getActionSymbol(acao), proximoEstado));
            firstEpisodePathArea.setText(firstEpisodePathLogBuilder.toString());
        }

        // Atualiza a interface
        appendToLog(String.format("Ep.%d: %s %s ( %s )| R: %.2f",
                episodioAtual + 1, estado, getActionSymbol(acao), proximoEstado, recompensa));

        // Atualiza tanto a visualização do grid quanto a QTable
        updateGridVisualization(ambiente, agente.getTabelaQ());
        updateQTableView(agente.getTabelaQ(), ambiente.getLargura());

        if (ambiente.isFinalState()) {
            appendToLog(String.format("--- Fim do Episódio %d --- Recompensa Total: %.2f",
                    episodioAtual + 1, recompensaAcumuladaEpisodio));
            // O próximo clique irá para o próximo episódio
        }
    }

    // Novo método para atualizar a TableView da QTable
    private void updateQTableView(Map<String, double[]> qTable, int gridSize) {
        Platform.runLater(() -> {
            qTableView.getItems().clear();
            String currentState = ambiente.getxAgente() + ":" + ambiente.getyAgente();

            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    String state = x + ":" + y;
                    if (qTable.containsKey(state)) {
                        double[] actions = qTable.get(state);
                        QTableRow row = new QTableRow(state, actions);
                        row.setHighlighted(state.equals(currentState));
                        qTableView.getItems().add(row);
                    }
                }
            }

            // Auto-scroll to current state
            if (!currentState.equals("0:0")) {
                for (QTableRow row : qTableView.getItems()) {
                    if (row.getEstado().equals(currentState)) {
                        qTableView.scrollTo(row);
                        break;
                    }
                }
            }
        });
    }

    private void prepareForNextEpisode() {
        if (episodioAtual >= totalEpisodios) {
            appendToLog("\nTreinamento passo a passo concluído!\n");
            if (episodioAtual > 0 && firstEpisodePathArea.getText().isEmpty() && firstEpisodePathLogBuilder.length() > 0) {
                firstEpisodePathArea.setText(firstEpisodePathLogBuilder.toString());
            }
            finishTraining();
            return;
        }
        ambiente.reiniciar();
        recompensaAcumuladaEpisodio = 0;
        appendToLog(String.format("\n--- Iniciando Episódio %d de %d ---", episodioAtual + 1, totalEpisodios));
        // Inicia o log do caminho para o primeiro episódio
        if (episodioAtual == 0) {
            firstEpisodePathLogBuilder.append("Início: ").append(ambiente.getEstadoAtual());
            firstEpisodePathArea.setText(firstEpisodePathLogBuilder.toString());
        }
        updateGridVisualization(ambiente, agente.getTabelaQ());
        updateQTableView(agente.getTabelaQ(), ambiente.getLargura()); // Adicionado esta linha
    }

    private void finishTraining() {
        showStatistics(recompensasPorEpisodio);
        showQTable(agente.getTabelaQ(), ambiente.getLargura());
        showOptimalPath(agente.getTabelaQ(), ambiente);
        setControlsDisabled(false);
        // Desabilita o botão "Próximo Passo".
        nextStepButton.setDisable(true);
    }

    private void setControlsDisabled(boolean disabled) {
        startButton.setDisable(disabled);
        gridSizeField.setDisable(disabled);
        alphaField.setDisable(disabled);
        gammaField.setDisable(disabled);
        epsilonField.setDisable(disabled);
        episodesField.setDisable(disabled);
        trapsArea.setDisable(disabled);
        stepByStepCheckBox.setDisable(disabled);

        // Lida com o botão "Próximo Passo" separadamente
        if (disabled) {
            nextStepButton.setDisable(true);
        }
    }

    private double calcularAlturaTexto(TextArea area, String texto) {
        Text text = new Text(texto);
        text.setFont(area.getFont());
        text.setWrappingWidth(area.getPrefColumnCount() * 7); // estimativa da largura
        new Scene(new Group(text)); // necessário para aplicar CSS
        text.applyCss();
        return text.getLayoutBounds().getHeight() + 20; // margem extra
    }

    private void updateGridVisualization(GridWorld grid, Map<String, double[]> qTable) {
        Platform.runLater(() -> {
            gridVisualization.getChildren().clear();
            int cellSize = 60;

            for (int y = 0; y < grid.getAltura(); y++) {
                for (int x = 0; x < grid.getLargura(); x++) {
                    StackPane cellPane = new StackPane();
                    Rectangle rect = new Rectangle(cellSize, cellSize);
                    String state = x + ":" + y;

                    // Verifica se está no caminho ótimo (exceto início e objetivo)
                    boolean isPath = caminhoOtimo.contains(state) &&
                            !(x == 0 && y == 0) &&
                            !(x == grid.getxObjetivo() && y == grid.getyObjetivo());

                    // Pinta o fundo da célula
                    if (x == 0 && y == 0) {
                        rect.setFill(Color.YELLOW); // Ponto de partida
                    } else if (x == grid.getxObjetivo() && y == grid.getyObjetivo()) {
                        rect.setFill(Color.GREEN); // Objetivo
                    } else if (grid.isArmadilha(x, y)) {
                        rect.setFill(Color.RED); // Armadilha
                    } else if (isPath) {
                        rect.setFill(Color.LIGHTGREEN.deriveColor(1, 1, 1, 0.7)); // Caminho ótimo
                    } else {
                        rect.setFill(Color.WHITE); // Célula normal
                    }

                    // Posição atual do agente (durante treinamento)
                    if (!qTable.isEmpty() && x == grid.getxAgente() && y == grid.getyAgente()) {
                        rect.setFill(Color.BLUE.deriveColor(1, 1, 1, 0.7));
                    }

                    rect.setStroke(Color.BLACK);
                    cellPane.getChildren().add(rect);

                    // Adiciona a seta e o valor-Q (se aplicável)
                    if (qTable.containsKey(state) && !grid.isArmadilha(x, y) &&
                            !(x == grid.getxObjetivo() && y == grid.getyObjetivo())) {

                        double[] actions = qTable.get(state);
                        int bestAction = getBestAction(qTable, state);
                        double bestValue = actions[bestAction];

                        Text arrow = new Text(getActionSymbol(bestAction));
                        arrow.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                        arrow.setFill(Color.BLACK);

                        Label qValueLabel = new Label(df.format(bestValue));
                        qValueLabel.setFont(Font.font("Arial", 10));
                        qValueLabel.setTextFill(Color.DARKSLATEGRAY);

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

    private List<Point> parseTraps() {
        List<Point> armadilhas = new ArrayList<>();
        String[] linhas = trapsArea.getText().split("\n");
        for (String linha : linhas) {
            if (!linha.trim().isEmpty()) {
                try {
                    String[] coords = linha.trim().split(",");
                    if (coords.length == 2) {
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        armadilhas.add(new Point(x, y));
                    }
                } catch (NumberFormatException e) {
                    // Ignora linhas com formato inválido
                    System.err.println("Formato de armadilha inválido: " + linha);
                }
            }
        }
        return armadilhas;
    }

    private void updateGridFromControls() {
        try {
            int gridSize = Integer.parseInt(gridSizeField.getText());
            if (gridSize <= 0) return; // Evita grid com tamanho inválido

            List<Point> traps = parseTraps();
            GridWorld initialGrid = new GridWorld(gridSize, gridSize, traps);
            updateGridVisualization(initialGrid, new java.util.HashMap<>());
        } catch (NumberFormatException e) {
            // Ignora se o campo de tamanho do grid for inválido
            gridVisualization.getChildren().clear();
        }
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
        qTableView.getItems().clear();

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                String state = x + ":" + y;
                if (qTable.containsKey(state)) {
                    double[] actions = qTable.get(state);
                    qTableView.getItems().add(new QTableRow(state, actions));
                }
            }
        }
    }

    private void showOptimalPath(Map<String, double[]> qTable, GridWorld grid) {
        caminhoOtimo.clear(); // Limpa o caminho anterior
        StringBuilder sb = new StringBuilder();
        grid.reiniciar();
        String currentState = grid.getEstadoAtual();
        boolean finalizado = false;
        int maxSteps = grid.getLargura() * grid.getAltura() * 2;
        int step = 0;
        java.util.Set<String> visitados = new java.util.HashSet<>();

        sb.append("Início: ").append(currentState);
        caminhoOtimo.add(currentState); // Adiciona o estado inicial

        while (!finalizado && step < maxSteps) {
            step++;
            if (visitados.contains(currentState)) {
                sb.append(" [CICLO DETECTADO]");
                break;
            }
            visitados.add(currentState);

            int bestAction = getBestAction(qTable, currentState);
            String actionSymbol = getActionSymbol(bestAction);

            Object[] result = grid.executarPasso(bestAction);
            currentState = (String) result[1];
            finalizado = (boolean) result[2];

            sb.append(String.format(" %s (%s)", actionSymbol, currentState));
            caminhoOtimo.add(currentState); // Adiciona cada estado do caminho
        }

        if (finalizado) {
            sb.append(" FIM!");
        } else {
            sb.append(" [CAMINHO NÃO ENCONTRADO]");
        }

        double altura = calcularAlturaTexto(pathArea, sb.toString());
        pathArea.setPrefHeight(altura);
        pathArea.setText(sb.toString());

        // Atualiza a visualização para mostrar o caminho
        updateGridVisualization(ambiente, agente.getTabelaQ());
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
        qTableView.getItems().clear();
        pathArea.clear();
        if(firstEpisodePathArea != null) firstEpisodePathArea.clear();
        gridVisualization.getChildren().clear();
        caminhoOtimo.clear(); // Limpa o caminho ótimo

        // Reseta o estado do treinamento e reabilita os controles
        setControlsDisabled(false);
        this.agente = null;
        this.ambiente = null;

        // Desenha o grid com base nos valores atuais dos controles
        updateGridFromControls();
    }

    public static void main(String[] args) {
        launch(args);
    }
}