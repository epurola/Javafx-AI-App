package com.example.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private void launchAgeDetection(ActionEvent event) {
        try {
            System.out.println("Launching Age Detection...");
            root = FXMLLoader.load(getClass().getResource("/com/example/views/secondary.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an alert dialog to the user
        }
    }

    @FXML
    private void launchEmotionDetection(ActionEvent event) throws IOException {
        System.out.println("Launching Age Detection...");
        root = FXMLLoader.load(getClass().getResource("/com/example/views/emotion.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

