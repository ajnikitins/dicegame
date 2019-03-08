package com.dicegame.controller;

import com.dicegame.enums.ConnectionState;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
        hostButton.setText("Host");
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
  private void onButtonClick() {

  }

}
