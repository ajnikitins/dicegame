package com.dicegame.controllers;

import com.dicegame.enums.ConnectionState;
import com.dicegame.enums.ConnectionStatus;
import com.dicegame.utilities.Validations;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class FXMLController implements Initializable {

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

  private ConnectionState connectionState = ConnectionState.HOST;

  private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    connectionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {

      if (portField.getText().equals("")) {
        portField.setText("35555");
      }

      if (connectionState == ConnectionState.HOST) {
        connectionState = ConnectionState.GUEST;

        ipField.setDisable(false);

        roomSizeField.setDisable(true);

        hostButton.setText("Connect");
      } else {
        connectionState = ConnectionState.HOST;

        ipField.setText("localhost");
        ipField.setDisable(true);

        roomSizeField.setDisable(false);
        roomSizeField.setText("5");

        hostButton.setText("Start server");
      }
    });

    portField.textProperty().addListener((observable, oldValue, newValue) -> {

      if (!newValue.matches("\\d*")) {
        portField.setText(newValue.replaceAll("\\D+", ""));
      }

      if (!portField.getText().equals("")) {
        int num = Integer.parseInt(portField.getText());

        if (num > 65535) {
          portField.setText("65535");
        }
      }
    });

    roomSizeField.textProperty().addListener((observable, oldValue, newValue) -> {

      if (!newValue.matches("\\d*")) {
        roomSizeField.setText(newValue.replaceAll("\\D+", ""));
      }

      if (!roomSizeField.getText().equals("")) {
        int num = Integer.parseInt(roomSizeField.getText());

        if (num > 100) {
          roomSizeField.setText("100");
        }
      }
    });
  }

  @FXML
  private void onHostButtonClick() {
//    TODO: Figure out how to colour fields and still display error message
//    Border errorBorder = new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

    String errorMessage = "";

    Alert errorAlert = new Alert(AlertType.ERROR);
    errorAlert.setHeaderText(null);

    if (displayNameField.getText().equals("")) {
      errorMessage += "Missing display name.\n";
    }

    if (connectionState == ConnectionState.GUEST) {

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

    if (connectionState == ConnectionState.HOST && Integer.parseInt(roomSizeField.getText()) < 2) {
      errorMessage += "Room cannot be smaller than 2.\n";
    }

    if (!errorMessage.equals("")) {
      errorAlert.setContentText(errorMessage);
      errorAlert.showAndWait();
      return;
    }

    if (connectionState == ConnectionState.HOST) {

    } else {


    }
  }

}
