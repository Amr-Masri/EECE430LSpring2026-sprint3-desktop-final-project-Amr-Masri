package com.amr.exchange;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 475);
        stage.setTitle("Currency Exchange");
        stage.setScene(scene);
        stage.show();
    }
}
