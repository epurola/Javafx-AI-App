package com.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import com.example.EmotionDetector;
import com.example.ModelManager;
import com.example.Utils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class EmotionController {

    @FXML
    private Button secondaryButton;
    @FXML
    private ImageView currentFrame; 
    @FXML
    private Button button;
    private Stage stage;
    private Scene scene;
    private Parent root;
    private ModelManager modelManager = new ModelManager();
    private EmotionDetector detectEmotion = new EmotionDetector(modelManager);

    @FXML
    private FontAwesomeIconView icon;
    @FXML
    private HBox topBar;

    private double xOffSet = 0;
    private double yOffSet = 0;

    @FXML
    private void initialize() {
        setupDragListeners();
    }

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
                }
            });
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

    @FXML
    void switchToPrimary(ActionEvent event) {
        try {
            System.out.println("Switching to Primary View...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            
            // Get the current stage using the event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Set the stage and root in the controller
            controller.setStage(stage);
            controller.setRoot(root);
            
            // Create a new scene with the loaded root node
            Scene scene = new Scene(root);
            
            // Set the new scene on the stage
            stage.setScene(scene);
            stage.show();
            
            // Stop acquisition if necessary
            if (this.capture.isOpened()) {
                this.stopAcquisition();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception as needed
        }
    }
    


    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;

    /**
     * The action triggered by pushing the button on the GUI
     *
     * @param event the push button event
     */
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(cameraId);
            icon.setVisible(false);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();
                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.button.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
    }


    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
            
                    detectEmotion.detectEmotion(frame);
                }

            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

  
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stopAcquisition();
    }
}
