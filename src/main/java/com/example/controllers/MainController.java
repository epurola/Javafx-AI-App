package com.example.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import org.opencv.core.Mat;

import com.example.AgeDetector;
import com.example.EmotionDetector;
import com.example.ModelManager;
import com.example.Utils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
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
    private HBox topBar;
    @FXML
    private FontAwesomeIconView icon;
    @FXML
    private Label dragLabel;
    private boolean showText = true;
    private int count;
    @FXML
    private MediaView mediaView;
    @FXML
    private Button button;
    MediaPlayer player;

    


    @FXML
    private void initialize(URL url, ResourceBundle rb) {
        setupDragListeners();
    }

    private double xOffSet=0;
    private double yOffSet=0;

    public void setRoot(Parent root) {
        this.root = root;
        setupDragListeners();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupDragListeners() {
        if (topBar != null) {
            topBar.setOnMousePressed(event -> {
                xOffSet = event.getSceneX();
                yOffSet = event.getSceneY();
            });

            topBar.setOnMouseDragged(event -> {
                if (stage != null) {
                    stage.setX(event.getScreenX() - xOffSet);
                    stage.setY(event.getScreenY() - yOffSet);
                    if(showText)
                    {
                        dragLabel.setVisible(true);
                        icon.setVisible(false);
                    }
                    
                }
            });
            if(showText)
            {
                topBar.setOnMouseReleased(event -> {
                    dragLabel.setVisible(false); 
                    icon.setVisible(true); // Hide the drag label when dragging stops
                });
            }
           
        }
        
    }

    @FXML
    private void launchAgeDetection(ActionEvent event) {
        try {
            
            System.out.println("Launching Age Detection...");
            
            // Load secondary.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/secondary.fxml"));
            Parent root = loader.load();
            
            // Get the controller instance
            AgeController controller = loader.getController();
            
            // Set the stage and scene from the event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            controller.setStage(stage);
            
            // Create a new scene with the loaded root node
            Scene scene = new Scene(root);
            
            // Set the scene on the stage
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an alert dialog to the user
        }
    }
    

    @FXML
    private void launchEmotionDetection(ActionEvent event) throws IOException {
    
        try {
            System.out.println("Launching Emotion Detection...");
            
            // Load secondary.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/emotion.fxml"));
            Parent root = loader.load();
            
            // Get the controller instance
            EmotionController controller = loader.getController();
            
            // Set the stage and scene from the event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            controller.setStage(stage);
            
            // Create a new scene with the loaded root node
            Scene scene = new Scene(root);
            
            // Set the scene on the stage
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an alert dialog to the user
        }
    
    }
    
     //Causes some thred issue with the alert box, fix later
     @FXML
    private void selectImage(ActionEvent event) {
        showText = false;
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
        icon.setVisible(false);
        loadingIndicator.setProgress(show ? ProgressBar.INDETERMINATE_PROGRESS : 0);
    }
    @FXML
    private void closeVideo()
    {
        player.stop();
        mediaView.setVisible(false);
        icon.setVisible(true);
        button.setVisible(false); 
        imageView.setVisible(true);
    }
    @FXML
    private void selectAgeImage(ActionEvent event) {
        showText = false;
        count++;
        if (count == 3) {
            imageView.setVisible(false);
            File file = new File("C:\\Users\\eelip\\demo\\src\\main\\resources\\sample.mp4");
            Media media = new Media(file.toURI().toString());
            player = new MediaPlayer(media);
            mediaView.setMediaPlayer(player);
            mediaView.setVisible(true);
            player.play();
            button.setVisible(true); 
            return;
          }
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
                        detectAge.getPrediction(frame);
                        Image processedImage = Utils.mat2Image(frame);

                        // Update UI on JavaFX Application Thread
                        Platform.runLater(() -> {
                            emotion.setText(detectAge.getAgeString());
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

    @FXML
    private void closeApp(ActionEvent event)
    {
        Platform.exit();
    }
    @FXML
    private void minimizeApp(ActionEvent event) {
        if (stage != null) {
            stage.setIconified(true); // Minimize the stage
        }
    }

}