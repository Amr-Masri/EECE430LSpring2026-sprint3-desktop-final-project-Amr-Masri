package com.amr.exchange;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("parent.fxml"));
        BorderPane root = fxmlLoader.load();
        Parent controller = fxmlLoader.getController();
        controller.setBorderPane(root);  //pass the root to the controller
        controller.initializeContent();  //now load the first screen

        Scene scene = new Scene(root, 630, 475);
        stage.setTitle("Currency Exchange");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}