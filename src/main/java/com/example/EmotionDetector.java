package com.example;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.example.controllers.CustomAlertController;

import java.util.HashMap;
import java.util.Map;

public class EmotionDetector implements imageProcessingInterface {
  private final FaceDetector faceDetector;
   private ModelManager modelManager;
   private int predictedEmotionIndex;

   public EmotionDetector(ModelManager modelManager) {
     String pathToXml = "src/main/resources/haarcascade_frontalface_alt2.xml"; 
    this.faceDetector = new FaceDetector(pathToXml);
    this.modelManager = modelManager;
    }


public void detectEmotion(Mat frame) {
    try {
        Rect box = faceDetector.detectFaces(frame);
        Mat Cropped = cropFace(frame, box); // Detect faces in the frame
        Mat processedFrame = processFrame(Cropped);
            // Resize and normalize the cropped face image
            Mat resizedFace = Utils.resizeImage(processedFrame, 64, 64, Imgproc.INTER_AREA);
            float[] mean = {0f, 0f, 0f};
            Mat normalizedFace = Utils.normalizeImage(resizedFace, mean);

            // Convert the normalized face image to tensor
            OnnxTensor tensor = Utils.matToTensor4(normalizedFace);

            // Prepare input map for the ONNX session
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("Input3", tensor);

            // Run the ONNX session for emotion detection
            OrtSession.Result outputs = modelManager.getEmotionDetectionSession().run(inputs);

            // Get the output value 
            OnnxValue outputValue = outputs.get(0);
            float[][] scores = (float[][]) outputValue.getValue();
           
            // Find the predicted emotion index
             predictedEmotionIndex = findMaxIndex(scores);
            
            // Get the emotion label for the predicted index
            String predictedEmotion = getEmotionLabel(predictedEmotionIndex);

            // Add the emotion text to the image
            Scalar color = new Scalar(0, 255, 0);
            int thickness = 1;
            int fontFace = Imgproc.FONT_HERSHEY_PLAIN;
            double scale = 1.0;
            org.opencv.core.Point textOrg = new org.opencv.core.Point(box.x, box.y - 10); // Above the face rectangle
            Imgproc.putText(frame, predictedEmotion, textOrg, fontFace, scale, color, thickness);
    } catch (OrtException | CvException e) {
        System.out.print("Wrong image format");
        Utils util = new Utils();
        Platform.runLater(() -> {
        util.displayCustomAlert("Warning!", "Wrong file format, \n We currently only support JPG" );
        });
    }
}

public static Mat cropFace(Mat image, Rect faceBox) {
    // Check for empty rectangle (no face detected)
    if (faceBox.empty()) {
        System.out.println("No face detected for cropping!");
        return image; // Return original image if no face detected
    }

    // Define crop region based on face bounding box and margin
    int margin = 20; // Adjust margin as needed
    int cropX = Math.max(0, faceBox.x - margin);
    int cropY = Math.max(0, faceBox.y - margin);
    int cropWidth = faceBox.width + 2 * margin;
    int cropHeight = faceBox.height + 2 * margin;

    // Ensure crop region stays within image boundaries
    cropX = Math.min(cropX, image.cols() - cropWidth); // Limit X to avoid exceeding image width
    cropY = Math.min(cropY, image.rows() - cropHeight); // Limit Y to avoid exceeding image height

    // Extract the cropped image
    Mat croppedImage = new Mat(image, new Rect(cropX, cropY, cropWidth, cropHeight));

  
    // Ensure the data type is consistent
    croppedImage.convertTo(croppedImage, CvType.CV_8UC3); // Assuming 3 channels

    return croppedImage;
}

    private int findMaxIndex(float[][] array) {
        int maxIndex = 0;
        float maxScore = Float.MIN_VALUE;
        for (int i = 0; i < array[0].length; i++) {
            if (array[0][i] > maxScore) {
                maxIndex = i;
                maxScore = array[0][i];
            }
        }
        return maxIndex;
    }

     private static final Map<Integer, String> emotionTable = Map.of(
            0, "Neutral",
            1, "Happiness",
            2, "Surprise",
            3, "Sadness",
            4, "Anger",
            5, "Disgust",
            6, "Fear",
            7, "Contempt"
    );

    public String getEmotionLabel(int index) {
        if (emotionTable.containsKey(index)) {
            return emotionTable.get(index);
        } else {
            return "Invalid emotion index";
        }
    }
  
    public Mat processFrame(Mat frame) {
        Mat resizedImage = Utils.resizeImage(frame, 64, 64, Imgproc.INTER_AREA);
        return resizedImage;
   }

   public int getPredictedEmotionIndex() {
       return predictedEmotionIndex;
   }
 
   
}
