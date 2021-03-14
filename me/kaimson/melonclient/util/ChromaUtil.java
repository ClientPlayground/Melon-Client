package me.kaimson.melonclient.util;

import java.awt.Color;

public class ChromaUtil {
  public static int getColor(double offset, int width) {
    long systemTime = 2000L;
    float speed = 2000.0F;
    float hue = (float)(System.currentTimeMillis() % systemTime) / speed;
    hue = (float)(hue - offset / width * 0.3D);
    int color = Color.HSBtoRGB(hue, 1.0F, 1.0F);
    return color;
  }
}
