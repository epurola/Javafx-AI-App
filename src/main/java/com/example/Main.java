package com.example;

import nu.pattern.OpenCV;

import java.io.File;
import java.io.InputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load OpenCV and CSS
        OpenCV.loadLocally();
        String cssPath = getClass().getResource("/styles.css").toExternalForm();

        // Create content
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/views/primary.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(cssPath); // Apply CSS to the scene

        // Configure stage and show
        primaryStage.setTitle("JavaFX Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
    
        launch(args);
    }
}


