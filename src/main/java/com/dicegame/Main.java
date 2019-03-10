package com.dicegame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("views/mainMenu.fxml"));

    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

    primaryStage.setTitle("Poker Dice");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();

    primaryStage.onCloseRequestProperty().setValue(e -> Platform.exit());
  }

  public static void main(String[] args) {
    launch(args);
  }
}
