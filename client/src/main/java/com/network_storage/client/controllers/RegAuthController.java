package com.network_storage.client.controllers;

import com.network_storage.client_server.Commands;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegAuthController {

    @javafx.fxml.FXML
    private TextField loginField;
    @javafx.fxml.FXML
    private PasswordField passwordField;
    @javafx.fxml.FXML
    private VBox globParent;
    public Controller backController;

    public void auth(ActionEvent actionEvent) {
        if (!loginField.getText().trim().equals("") && !passwordField.getText().trim().equals("")) {
            backController.getClientFileMethods().sendCommand(Commands.AUTH,
                    (loginField.getText().trim() + " " + passwordField.getText().trim()));
            backController.updateLocalFilesList();
        }
    }

    public void reg(ActionEvent actionEvent) {
        if (!loginField.getText().trim().equals("") && !passwordField.getText().trim().equals("")) {
            backController.getClientFileMethods().sendCommand(Commands.REG,
                    (loginField.getText().trim() + " " + passwordField.getText().trim()));
        }
    }

    public void close() {
        globParent.getScene().getWindow().hide();
    }
}
