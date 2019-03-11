package com.dicegame.utils;

import java.util.regex.Pattern;
import javafx.scene.control.TextField;

public class Validations {

  private static final Pattern IPPATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

  public static boolean isValidIpAddress(final String ip) {
    return ip.length() < 16 && (ip.equals("localhost") || IPPATTERN.matcher(ip).matches());
  }

  public static void filterNumberField(TextField f, int maxSize, String newValue) {
    if (!newValue.matches("\\d*")) {
      f.setText(newValue.replaceAll("\\D+", ""));
    }

    if (!f.getText().equals("")) {
      int num = Integer.parseInt(f.getText());

      if (num > maxSize) {
        f.setText("" + maxSize);
      }
    }
  }
}
