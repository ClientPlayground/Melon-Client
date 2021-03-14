package me.kaimson.melonclient.util;

import java.awt.Color;
import me.kaimson.melonclient.Client;
import org.lwjgl.opengl.GL11;

public class GLColor {
  public static void setGlColor(int color) {
    float alpha = (color >> 24 & 0xFF) / 255.0F;
    float red = (color >> 16 & 0xFF) / 255.0F;
    float green = (color >> 8 & 0xFF) / 255.0F;
    float blue = (color & 0xFF) / 255.0F;
    GL11.glColor4f(red, green, blue, alpha);
  }
  
  public static int convertPercentToValue(float percent) {
    return (int)(percent * 255.0F);
  }
  
  public static int getIntermediateColor(int a, int b, float percent) {
    float avgRed = (a >> 16 & 0xFF) * percent + (b >> 16 & 0xFF) * (1.0F - percent);
    float avgGreen = (a >> 8 & 0xFF) * percent + (b >> 8 & 0xFF) * (1.0F - percent);
    float avgBlue = (a >> 0 & 0xFF) * percent + (b >> 0 & 0xFF) * (1.0F - percent);
    float avgAlpha = (a >> 24 & 0xFF) * percent + (b >> 24 & 0xFF) * (1.0F - percent);
    try {
      return (new Color(avgRed / 255.0F, avgGreen / 255.0F, avgBlue / 255.0F, avgAlpha / 255.0F)).getRGB();
    } catch (IllegalArgumentException e) {
      Client.error("Color parameter outside of expected range!", new Object[0]);
      return Integer.MIN_VALUE;
    } 
  }
}
