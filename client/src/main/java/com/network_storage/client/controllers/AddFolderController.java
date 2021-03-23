package com.network_storage.client.controllers;

import com.network_storage.client_server.Commands;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AddFolderController {

    @javafx.fxml.FXML
    TextField folder;
    @javafx.fxml.FXML
    VBox globParent;

    public Controller backController;

    public void create(ActionEvent actionEvent) {
        if (!folder.getText().trim().equals("")) {
            backController.getClientFileMethods().sendCommand(Commands.CREATE, folder.getText() + "/");
            backController.updateLocalFilesList();
        }
        globParent.getScene().getWindow().hide();
    }
}
