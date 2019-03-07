package com.dicegame.controller;

import com.dicegame.enums.ConnectionState;
import java.net.URL;
import java.util.ResourceBundle;
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
  private Button hostButton;

  private ConnectionState connectionState = ConnectionState.HOST;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @FXML
  private void onButtonClick() {
    System.out.println(1);
  }

}
