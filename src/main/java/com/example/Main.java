package com.example;

import nu.pattern.OpenCV;

import com.example.controllers.MainController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load OpenCV and CSS
        OpenCV.loadLocally();
        primaryStage.initStyle(StageStyle.UNDECORATED);
        String cssPath = getClass().getResource("/styles.css").toExternalForm();

        // Create content
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setStage(primaryStage);
        controller.setRoot(root);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(cssPath); // Apply CSS to the scene

        // Configure stage and show
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
