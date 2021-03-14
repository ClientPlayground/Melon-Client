package me.kaimson.melonclient.util;

import me.kaimson.melonclient.config.AnchorPoint;
import me.kaimson.melonclient.ingames.IngameDisplay;

public class BoxUtils {
  public static boolean checkHovered(int boxX, int boxY, int boxX2, int boxY2, int mouseX, int mouseY, float scale) {
    return (mouseX >= boxX * scale && mouseY >= boxY * scale && mouseX <= boxX2 * scale && mouseY <= boxY2 * scale);
  }
  
  public static int getBoxOffX(IngameDisplay display, int x, int width) {
    switch (display.getAnchorPoint()) {
      case CENTER:
      case TOP_CENTER:
      case BOTTOM_CENTER:
        return x - width / 2;
      case CENTER_RIGHT:
      case BOTTOM_RIGHT:
      case TOP_RIGHT:
        return x - width;
    } 
    return x;
  }
  
  public static int getBoxOffY(IngameDisplay display, int y, int height) {
    switch (display.getAnchorPoint()) {
      case CENTER:
      case CENTER_RIGHT:
      case CENTER_LEFT:
        return y - height / 2;
      case BOTTOM_CENTER:
      case BOTTOM_RIGHT:
      case BOTTOM_LEFT:
        return y - height;
    } 
    return y;
  }
}
