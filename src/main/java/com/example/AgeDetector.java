package com.example;
import java.util.HashMap;
import java.util.Map;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;


public class AgeDetector implements imageProcessingInterface {
   private final FaceDetector faceDetector;
   private ModelManager modelManager;

    public AgeDetector( ModelManager modelManager) {
        String pathToXml = "src/main/resources/haarcascade_frontalface_alt2.xml"; 
        this.faceDetector = new FaceDetector(pathToXml);
        this.modelManager = modelManager;
    }



public void getPrediction(Mat frame) throws OrtException {
      
         Rect box = faceDetector.detectFaces(frame);
         Mat Cropped = cropFace(frame, box);
         Mat frame2 =  processFrame(Cropped);
         int i = predictAge(frame2);

         String ageString = Integer.toString(i);
         Scalar color = new Scalar(0, 255, 0); 
         int thickness = 2;
         int fontFace = Imgproc.FONT_ITALIC;
         double scale = 1.0;
         Point textOrg = new Point(350, 350);
         
         Imgproc.putText(frame, ageString, textOrg, fontFace, scale, color, thickness);
       }
   

   private int predictAge(Mat frame) throws OrtException {
    OnnxTensor tensor = Utils.matToTensor(frame);
    Map<String, OnnxTensor> inputs = new HashMap<>();
    inputs.put("input", tensor);
    OrtSession.Result outputs = modelManager.getAgeDetectionSession().run(inputs);

    // Assuming the output tensor is the first result
    OnnxValue outputValue = outputs.get(0);
   
    float[][] probabilities = (float[][]) outputValue.getValue();

    // Find the index with the maximum probability
    int maxIndex = getMaxIndex(probabilities[0]);
    return maxIndex;
}
private int getMaxIndex(float[] array) {
    int maxIndex = 0;
    float maxProbability = Float.MIN_VALUE;
    for (int i = 0; i < array.length; i++) {
        if (array[i] > maxProbability) {
            maxIndex = i;
            maxProbability = array[i];
        }
    }
    return maxIndex;
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

    // Ensure the color space is preserved
    if (image.channels() == 3) {
        Imgproc.cvtColor(croppedImage, croppedImage, Imgproc.COLOR_BGR2RGB);
    }

    // Ensure the data type is consistent
    croppedImage.convertTo(croppedImage, CvType.CV_8UC3); // Assuming 3 channels

    return croppedImage;
}

    public Mat processFrame(Mat frame) {
    Mat resizedImage = Utils.resizeImage(frame, 224, 224, Imgproc.INTER_AREA);
    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2BGR);
    
    frame.convertTo(frame, CvType.CV_32FC3);
   

    return resizedImage;
    }
    
}

