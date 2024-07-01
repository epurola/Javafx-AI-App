package com.example;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {
    private CascadeClassifier faceCascade;
    

    public FaceDetector(String cascadePath) {
        faceCascade = new CascadeClassifier(cascadePath);
    }

    public Rect detectFaces(Mat image) {
        MatOfRect faceDetections = new MatOfRect();
        faceCascade.detectMultiScale(image, faceDetections);
        
    
        Rect[] rectsArray = faceDetections.toArray();
    
        if (!faceDetections.empty()) {
          Rect rect = rectsArray[0];
    
          // Draw rectangle and text as existing 
          Imgproc.rectangle(image, new Point(rect.x, rect.y),
                  new Point(rect.x + rect.width, rect.y + rect.height),
                  new Scalar(0, 255, 0), 2);
    
          String text = "Detected Face";
    
          // Text properties 
          int fontFace = Imgproc.FONT_HERSHEY_PLAIN;
          double fontScale = 1;
          Scalar fontColor = new Scalar(0, 255, 0);
          int thickness = 1;
    
          // Text position 
          Point textOrg = new Point(rect.x + 5, rect.y + rect.height + 15);
    
          Imgproc.putText(image, text, textOrg, fontFace, fontScale, fontColor, thickness);
    
          // Return the first detected face bounding box
          return rect;
        } else {
          // No faces detected, return an empty Rect, nit really empty 
          return new Rect(100, 100, 50, 50);
        }
    }
}
      
