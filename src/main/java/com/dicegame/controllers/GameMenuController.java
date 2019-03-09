package com.dicegame.controllers;

import com.dicegame.enums.ConnectionStatus;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class GameMenuController implements Initializable {

  @FXML
  private VBox messageLog;

  @FXML
  private TextArea messageArea;

  @FXML
  private Button closeButton;

  @FXML
  private Button rollButton;

  @FXML
  private Label statusLabel;

  private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

  public void load() {

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @FXML
  public void stop() {
    closeButton.getScene().getWindow().hide();

    // TODO: Implement stop
  }

}
