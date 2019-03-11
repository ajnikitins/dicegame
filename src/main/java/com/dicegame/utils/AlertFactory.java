package com.dicegame.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class AlertFactory {

  static public void showAlert(AlertType type, String contentText) {
    Alert alert = new Alert(type);
    alert.setHeaderText(null);
    alert.setContentText(contentText);
    alert.showAndWait();
  }

  static public void showAlert(AlertType type, String contentText, Runnable onDismiss) {
    Alert alert = new Alert(type);
    alert.setHeaderText(null);
    alert.setContentText(contentText);
    alert.showAndWait();

    if (alert.getResult() == ButtonType.OK) {
      onDismiss.run();
    }

  }
}
