package com.example.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.opencv.core.Mat;

import com.example.AgeDetector;
import com.example.EmotionDetector;
import com.example.ModelManager;
import com.example.Utils;

import ai.onnxruntime.OrtException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

public class MainController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private ImageView imageView;
    private ModelManager modelManager = new ModelManager();
    private EmotionDetector detectEmotion = new EmotionDetector(modelManager);
    private AgeDetector detectAge = new AgeDetector(modelManager);

    @FXML
    private Label emotion;
    @FXML
    private ProgressIndicator loadingIndicator;

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
    

     @FXML
    private void selectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            // File selected, start background task
            imageView.setImage(null);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Show loading indicator
                    Platform.runLater(() -> showLoadingIndicator(true));

                    try {
                        Image image = new Image(new FileInputStream(selectedFile));
                        Mat frame = Utils.imageToMat(image);
                        detectEmotion.detectEmotion(frame);
                        Image processedImage = Utils.mat2Image(frame);

                        // Update UI on JavaFX Application Thread
                        Platform.runLater(() -> {
                            int index = detectEmotion.getPredictedEmotionIndex();
                            emotion.setText(detectEmotion.getEmotionLabel(index));
                            imageView.setImage(processedImage);
                            showLoadingIndicator(false); // Hide loading indicator
                        });
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };

            new Thread(task).start(); // Start the background task
        }
    }

    private void showLoadingIndicator(boolean show) {
        loadingIndicator.setVisible(show);
        loadingIndicator.setProgress(show ? ProgressBar.INDETERMINATE_PROGRESS : 0);
    }

    @FXML
    private void selectAgeImage(ActionEvent event) throws OrtException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        // Set extension filter if needed
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedFile));
                Mat frame=Utils.imageToMat(image);
                detectAge.getPrediction(frame);
                Image i=Utils.mat2Image(frame);
                System.out.print(detectAge.getAgeString());
                emotion.setText(detectAge.getAgeString());
                imageView.setImage(i);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                // Optionally, show an alert dialog to the user
            }
        }
    }
}

