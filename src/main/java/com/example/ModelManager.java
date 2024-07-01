package com.example;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;

public class ModelManager {
    private OrtSession ageDetectionSession;
    private OrtSession emotionDetectionSession;
    // Add similar properties for other models

    public ModelManager() {
        initializeModels();
    }

    private void initializeModels() {
        try {
            // Initialize age detection model and other models
            ageDetectionSession = loadModel("C:\\Users\\eelip\\demo\\src\\main\\resources\\vgg_ilsvrc_16_age_imdb_wiki.onnx");
            emotionDetectionSession = loadModel("C:\\Users\\eelip\\demo\\src\\main\\resources\\emotion-ferplus-8.onnx");
            // Load other models
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }

    private OrtSession loadModel(String modelPath) throws OrtException {
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        opts.setOptimizationLevel(OptLevel.BASIC_OPT);
        return env.createSession(modelPath, opts);
    }

    public OrtSession getAgeDetectionSession() {
        return ageDetectionSession;
    }
    
    public OrtSession getEmotionDetectionSession() {
        return emotionDetectionSession;
    }
    // Add similar methods for other models
}



