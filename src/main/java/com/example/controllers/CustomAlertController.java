package com.example.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public class CustomAlertController {
    @FXML
    private Label alertHeader;
    
    @FXML
    private Label alertContent;

    public void setAlertHeader(String header) {
        this.alertHeader.setText(header);
    }

    public void setAlertContent(String content) {
        this.alertContent.setText(content);
    }

    @FXML
    private void closeAlert() {
        Stage stage = (Stage) alertHeader.getScene().getWindow();
        stage.close();
    }
}

