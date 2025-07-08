package com.example.qlearning;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GridWorldFX extends Application {
    private GridWorld grid;
    private Agent agent;

    @Override
    public void start(Stage stage) throws Exception {
        grid = new GridWorld(5, 5);
        agent = new Agent(0.1, 0.9, 0.1);

        GridPane gridPane = new GridPane();
        updateGrid(gridPane);

        Scene scene = new Scene(gridPane, 400, 400);
        stage.setTitle("GridWorld Q-Learning");
        stage.setScene(scene);
        stage.show();
    }

    private void updateGrid(GridPane gridPane) {
        gridPane.getChildren().clear();

        for (int y = 0; y < grid.getAltura(); y++) {
            for (int x = 0; x < grid.getLargura(); x++) {
                Rectangle rect = new Rectangle(60, 60);

                if (x == grid.getxAgente() && y == grid.getyAgente()) {
                    rect.setFill(javafx.scene.paint.Color.BLUE);
                } else if (x == grid.getxObjetivo() && y == grid.getyObjetivo()) {
                    rect.setFill(Color.GREEN);
                } else if (grid.isArmadilha(x, y)) {
                    rect.setFill(Color.RED);
                } else {
                    rect.setFill(Color.WHITE);
                }

                rect.setStroke(Color.BLACK);
                gridPane.add(rect, x, y);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);  // Este método inicia a aplicação JavaFX
    }
}