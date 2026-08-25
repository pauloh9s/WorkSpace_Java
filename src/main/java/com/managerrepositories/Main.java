package com.managerrepositories;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Ponto de entrada da demonstração DEVHUB. */
public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/Main.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 760);
        scene.getStylesheets().add(Main.class.getResource("/css/application.css").toExternalForm());

        stage.setTitle("DEVHUB — Gerenciador de Projetos");
        stage.setMinWidth(960);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
