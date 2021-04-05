package com.network_storage.client.controllers;

import com.network_storage.client.handlers.ClientHandler;
import com.network_storage.client.network.Network;
import com.network_storage.client_server.Commands;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Network network = new Network(this);
    private ClientHandler clientHandler = new ClientHandler(network);
    private RegAuthController authController;

    @javafx.fxml.FXML
    private ListView<String> filesList;

    @javafx.fxml.FXML
    private ListView<String> serverFilesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread t = new Thread(() -> {
            try {
                network.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
        showAuth();
    }

    public ClientHandler getClientFileMethods() {
        return clientHandler;
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String serverFileName = serverFilesList.getSelectionModel().getSelectedItem();
        if (serverFileName != null && !serverFileName.equals("") ){
            clientHandler.sendCommand(Commands.DOWNLOAD, serverFileName);
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent){
        String clientFileName = filesList.getSelectionModel().getSelectedItem();
        if (clientFileName != null && !clientFileName.equals("")){
            clientHandler.sendCommand(Commands.UPLOAD, clientFileName);
            clientHandler.writeFile(clientFileName);
            updateLocalFilesList();
            clientHandler.sendShortCommand(Commands.LIST);
        }
    }

    public void pressOnUpdateBtn(ActionEvent actionEvent) {
        updateLocalFilesList();
        clientHandler.sendShortCommand(Commands.LIST);
    }

    public void pressOnDltBtn(ActionEvent actionEvent) {
        String serverFileName = serverFilesList.getSelectionModel().getSelectedItem();
        if (serverFileName != null && !serverFileName.equals("")) clientHandler.sendCommand(Commands.DELETE, serverFileName);

        String clientFileName = filesList.getSelectionModel().getSelectedItem();
        if (clientFileName != null && !clientFileName.equals("")){
            try {
                Files.deleteIfExists(Paths.get(clientHandler.getCLIENT_PATH() + clientFileName));
                updateLocalFilesList();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void updateLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get(clientHandler.getCLIENT_PATH())).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.getItems().clear();
                    Files.list(Paths.get(clientHandler.getCLIENT_PATH())).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void refreshServerFilesList(String[] serverList) {

        if (Platform.isFxApplicationThread()) {
            serverFilesList.getItems().clear();
            for (String o : serverList) serverFilesList.getItems().add(o);
        } else {
            Platform.runLater(() -> {
                serverFilesList.getItems().clear();
                for (String o : serverList) serverFilesList.getItems().add(o);
            });
        }
    }

    private void showAuth(){
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginWindow.fxml"));
            Parent root = loader.load();
            authController = (RegAuthController) loader.getController();
            authController.backController = this;
            stage.setTitle("My Network Storage Authorization");
            stage.setScene(new Scene(root, 400, 400));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeAuth(){
        authController.close();
    }

    public void pressOnAddBtn(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addFolderWindow.fxml"));
            Parent root = loader.load();
            AddFolderController afc = (AddFolderController) loader.getController();
            afc.backController = this;
            stage.setTitle("My Network Storage Adding Folder");
            stage.setScene(new Scene(root, 400, 400));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pressOnForwardBtn(ActionEvent actionEvent) {
        String serverFileName = serverFilesList.getSelectionModel().getSelectedItem();
        if (serverFileName != null && !serverFileName.equals("")) clientHandler.sendCommand(Commands.FORWARD, serverFileName + "/");
    }

    public void pressOnBckBtn(ActionEvent actionEvent) {
        clientHandler.sendShortCommand(Commands.BACK);
    }

    public void menuExit(ActionEvent actionEvent) {
        clientHandler.sendShortCommand(Commands.EXIT);
        System.exit(0);
    }

    public void showWarning(String warning) {
        Alert alert = new Alert(Alert.AlertType.WARNING, warning);
        alert.showAndWait();
    }
}