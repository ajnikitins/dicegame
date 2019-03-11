package com.dicegame.controllers;

import com.dicegame.endpoints.Server;
import com.dicegame.enums.ConnectionRole;
import com.dicegame.enums.ConnectionStatus;
import com.dicegame.utils.Validations;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class MainMenuController implements Initializable {

  @FXML
  public ToggleGroup connectionGroup;

  @FXML
  private TextField displayNameField;

  @FXML
  private TextField ipField;

  @FXML
  private TextField portField;

  @FXML
  private TextField roomSizeField;

  @FXML
  private Button hostButton;

  private ConnectionRole connectionRole = ConnectionRole.HOST;

  private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

  private Stage serverStage;

  private Stage clientStage;

  private Server server;

  private void setConnectionStatus(ConnectionStatus newStatus) {
    boolean isConnecting = newStatus == ConnectionStatus.CONNECTED;

    portField.setDisable(isConnecting);
    connectionGroup.getToggles().forEach(t -> ((RadioButton) t).setDisable(isConnecting));
    displayNameField.setDisable(isConnecting);

    if (connectionRole == ConnectionRole.HOST) {
      hostButton.setText(isConnecting ? "Stop server" : "Start server");

      roomSizeField.setDisable(isConnecting);
    } else if (connectionRole == ConnectionRole.GUEST) {
      hostButton.setText(isConnecting ? "Disconnect" : "Connect");

      ipField.setDisable(isConnecting);
    }

    connectionStatus = newStatus;
  }

  private void switchConnectionRole() {
    if (portField.getText().equals("")) {
      portField.setText("35555");
    }

    boolean isHost = connectionRole == ConnectionRole.HOST;

    connectionRole = isHost ? ConnectionRole.GUEST : ConnectionRole.HOST;

    ipField.setDisable(!isHost);
    roomSizeField.setDisable(isHost);
    roomSizeField.setText("5");

    if (!isHost) {
      ipField.setText("localhost");
    }

    hostButton.setText(isHost ? "Connect" : "Start server");
  }

  private String checkInputs() {
    String errorMessage = "";

    if (displayNameField.getText().equals("")) {
      errorMessage += "Missing display name.\n";
    }

    if (connectionRole == ConnectionRole.GUEST) {

      if (ipField.getText().equals("")) {
        errorMessage += "Missing host IP address.\n";
      }

      if (!Validations.isValidIpAddress(ipField.getText())) {
        errorMessage += "Malformed IP address.\n";
      }
    }

    if (portField.getText().equals("")) {
      errorMessage += "Missing port.\n";
    }

    if (connectionRole == ConnectionRole.HOST
        && Integer.parseInt(roomSizeField.getText()) < 2) {
      errorMessage += "Room cannot be smaller than 2.\n";
    }

    return errorMessage;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    connectionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
        switchConnectionRole());

    portField.textProperty().addListener((observable, oldValue, newValue) ->
        Validations.filterNumberField(portField, 65535, newValue));

    roomSizeField.textProperty().addListener((observable, oldValue, newValue) ->
        Validations.filterNumberField(roomSizeField, 100, newValue));
  }

  @FXML
  private void onHostButtonClick() {
    if (connectionStatus == ConnectionStatus.DISCONNECTED) {
//    TODO: Figure out how to colour fields and still display error message
//    Border errorBorder = new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

      String errorMessage = checkInputs();
      if (!errorMessage.equals("")) {
        Alert errorAlert = new Alert(AlertType.WARNING);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(errorMessage);
        errorAlert.showAndWait();
        return;
      }

      setConnectionStatus(ConnectionStatus.CONNECTED);

      if (connectionRole == ConnectionRole.HOST) {

        FXMLLoader gameFxmlLoader = new FXMLLoader(getClass().getResource("/com/dicegame/views/serverMenu.fxml"));
        Parent serverRoot;

        try {
          serverRoot = gameFxmlLoader.load();
        } catch (IOException e) {
          System.out.println("Failed to read serverMenu.fxml");
          return;
        }

        serverStage = new Stage();

        ServerMenuController controller = gameFxmlLoader.getController();
        serverStage.setOnHiding(e -> {
          controller.stop();
          disconnect();
        });

        serverStage.setScene(new Scene(serverRoot));
        serverStage.setResizable(false);
        serverStage.show();

        controller.createServer(
            Integer.parseInt(portField.getText()),
            Integer.parseInt(roomSizeField.getText())
        );

      }

      FXMLLoader gameFxmlLoader = new FXMLLoader(getClass().getResource("/com/dicegame/views/gameMenu.fxml"));
      Parent clientRoot;

      try {
        clientRoot = gameFxmlLoader.load();
      } catch (IOException e) {
        System.out.println("Failed to read gameMenu.fxml");
        return;
      }

      clientStage = new Stage();

      ClientMenuController controller = gameFxmlLoader.getController();
      clientStage.setOnHiding(e -> {
        controller.stop();
        disconnect();
      });

      clientStage.setScene(new Scene(clientRoot));
      clientStage.setResizable(false);
      clientStage.show();

      controller.createClient(
          ipField.getText(),
          Integer.parseInt(portField.getText()),
          displayNameField.getText()
      );
    } else {
      disconnect();
    }
  }

  private void disconnect() {
    setConnectionStatus(ConnectionStatus.DISCONNECTED);
    clientStage.hide();
    if (connectionRole == ConnectionRole.HOST) {
      serverStage.hide();
    }
  }
}
