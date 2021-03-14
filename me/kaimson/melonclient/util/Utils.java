package me.kaimson.melonclient.util;

public class Utils {
  public String capitalize(String input) {
    String capitalized = input.toLowerCase().replace("_", " ");
    StringBuilder builder = new StringBuilder();
    for (String words : capitalized.split(" ")) {
      if (words.length() <= 3) {
        builder.append(words.toUpperCase());
      } else {
        builder.append(words.substring(0, 1).toUpperCase());
        builder.append(words.substring(1));
      } 
    } 
    return builder.toString();
  }
}
