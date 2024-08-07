package com.example;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.example.controllers.CustomAlertController;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {



    public static Mat normalizeImage(Mat image, float[] mean) {
        // Create a new Mat object to store the normalized image
        Mat normalizedImage = new Mat();
    
        // Normalize each channel (BGR) using the provided mean values
        Imgproc.cvtColor(image, normalizedImage, Imgproc.COLOR_BGR2RGB);
        normalizedImage.convertTo(normalizedImage, CvType.CV_32F); // Convert to float for normalization
        Core.subtract(normalizedImage, new Scalar(mean[0], mean[1], mean[2]), normalizedImage);
    
        return normalizedImage;
    }

     public static Mat resizeImage(Mat image, int targetWidth, int targetHeight, int interpolation) {
      // Create a new Mat object to store the resized image
      Mat resizedImage = new Mat();

      // Resize the image using the specified interpolation method
      Imgproc.resize(image, resizedImage, new Size(targetWidth, targetHeight), 0.0, 0.0, interpolation);

      return resizedImage;
  }

  public static BufferedImage matToBufferedImage(Mat mat) {
    // Get the dimensions of the Mat
    int width = mat.cols();
    int height = mat.rows();
    int channels = mat.channels();

    // Create a BufferedImage with the same dimensions and type as the Mat
    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

    // Get the byte array of the Mat's pixel data
    byte[] data = new byte[width * height * channels];
    mat.get(0, 0, data);

    // Populate the BufferedImage with pixel data
    byte[] targetPixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
    System.arraycopy(data, 0, targetPixels, 0, data.length);

    return bufferedImage;
}

    public static Mat loadImage(String imagePath) {
        return Imgcodecs.imread(imagePath);
    }

    public static void saveImage(Mat image, String outputPath) {
        Imgcodecs.imwrite(outputPath, image);
    }

     public static OnnxTensor matToTensor(Mat image) throws OrtException {
      // Get the dimensions of the Mat
      int height = image.rows();
      int width = image.cols();
      int channels = image.channels();
  
      // Create a 4D float array to hold the data
      float[][][][] sourceArray = new float[1][channels][height][width];
  
      // Populate the array with data from the Mat, assuming BGR channel order
      for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
              for (int k = 0; k < channels; k++) {
                  sourceArray[0][k][i][j] = (float) image.get(i, j)[k];
              }
          }
      }
  
      // Create an OrtEnvironment 
      OrtEnvironment env = OrtEnvironment.getEnvironment();
  
      // Create the ONNX tensor from the array
      OnnxTensor tensor = OnnxTensor.createTensor(env, sourceArray);
  
      return tensor;
  }
  public static OnnxTensor matToTensor4(Mat image) throws OrtException {
    // Get the dimensions of the Mat
    int height = image.rows();
    int width = image.cols();

    // Create a 4D float array to hold the data
    float[][][][] sourceArray = new float[1][1][height][width];

    // Populate the array with data from the Mat, assuming BGR channel order
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            for (int k = 0; k < 1; k++) {
                sourceArray[0][k][i][j] = (float) image.get(i, j)[k];
            }
        }
    }

    // Create an OrtEnvironment
    OrtEnvironment env = OrtEnvironment.getEnvironment();

    // Create the ONNX tensor from the array
    OnnxTensor tensor = OnnxTensor.createTensor(env, sourceArray);

    return tensor;
}

	public static Image mat2Image(Mat frame)
	{
		try
		{
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		}
		catch (Exception e)
		{
			System.err.println("Cannot convert the Mat obejct: " + e);
			return null;
		}
	}
	

	public static <T> void onFXThread(final ObjectProperty<T> property, final T value)
	{
		Platform.runLater(() -> {
			property.set(value);
		});
	}

    public static Mat imageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        Mat mat = new Mat(height, width, CvType.CV_8UC3);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getPixelReader().getArgb(x, y);
                // Extract the individual color channels
                byte[] bgr = new byte[3];
                bgr[0] = (byte) (argb & 0xFF);        // Blue
                bgr[1] = (byte) ((argb >> 8) & 0xFF); // Green
                bgr[2] = (byte) ((argb >> 16) & 0xFF);// Red
                mat.put(y, x, bgr);
            }
        }
        return mat;
    }
    public void displayCustomAlert(String header, String content) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/alert.fxml"));
            Parent root = loader.load();
            
            CustomAlertController controller = loader.getController();
            controller.setAlertHeader(header);
            controller.setAlertContent(content);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Wrong image format");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

}



