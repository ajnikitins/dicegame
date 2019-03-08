package com.dicegame.utilities;

import java.util.regex.Pattern;

public class Validations {

  private static final Pattern IPPATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

  public static boolean isValidIpAddress(final String ip) {
    return ip.length() < 16 && (ip.equals("localhost") || IPPATTERN.matcher(ip).matches());
  }
}
