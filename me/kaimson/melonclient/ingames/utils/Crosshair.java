package me.kaimson.melonclient.ingames.utils;

import java.awt.Color;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.ChromaUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class Crosshair {
  public static final Crosshair INSTANCE = new Crosshair();
  
  public void render(IngameDisplay display, int x, int y) {
    int mode = ((Integer)Client.config.getCustoms().getOrDefault(IngameDisplay.CUSTOM_CROSSHAIR_MODE, Integer.valueOf(0))).intValue();
    float gap = ((Float)Client.config.getCustoms().getOrDefault(IngameDisplay.CUSTOM_CROSSHAIR_RENDERGAP, Float.valueOf(1.0F))).floatValue();
    float thickness = ((Float)Client.config.getCustoms().getOrDefault(IngameDisplay.CUSTOM_CROSSHAIR_THICKNESS, Float.valueOf(1.0F))).floatValue();
    float width = ((Float)Client.config.getCustoms().getOrDefault(IngameDisplay.CUSTOM_CROSSHAIR_WIDTH, Float.valueOf(0.6F))).floatValue();
    float height = ((Float)Client.config.getCustoms().getOrDefault(IngameDisplay.CUSTOM_CROSSHAIR_HEIGHT, Float.valueOf(0.6F))).floatValue();
    GlStateManager.pushMatrix();
    switch (mode) {
      case 0:
        GuiUtils.drawCircle(x, y, gap, thickness, new Color(getColor(x, (int)thickness), true), true);
        break;
      case 1:
        GuiUtils.drawFilledRect(x - gap - thickness, y - gap - thickness, x + gap + thickness, y - gap, getColor(x, (int)thickness), true);
        GuiUtils.drawFilledRect(x - gap - thickness, y + gap, x + gap + thickness, y + gap + thickness, getColor(x, (int)thickness), true);
        GuiUtils.drawFilledRect(x - gap - thickness, y - gap, x - gap, y + gap, getColor(x, (int)thickness), true);
        GuiUtils.drawFilledRect(x + gap, y - gap, x + gap + thickness, y + gap, getColor(x, (int)thickness), true);
        break;
      case 2:
        GuiUtils.drawVerticalLine(x, y - gap - height, y - gap, thickness, getColor(x, (int)thickness), false);
        GuiUtils.drawVerticalLine(x, y + gap, y + gap + height, thickness, getColor(x, (int)thickness), false);
        GuiUtils.drawLine(x - gap - width, x - gap, y, thickness, getColor(x, (int)thickness), false);
        GuiUtils.drawLine(x + gap, x + gap + width, y, thickness, getColor(x, (int)thickness), false);
        break;
    } 
    GlStateManager.popMatrix();
  }
  
  private int getColor(double offset, int width) {
    int color = ((Integer)Client.config.getCustoms().getOrDefault(IngameDisplay.CUSTOM_CROSSHAIR_COLOR, Integer.valueOf((new Color(255, 255, 255, 255)).getRGB()))).intValue();
    if (IngameDisplay.CUSTOM_CROSSHAIR_RAINBOW.isEnabled())
      color = ChromaUtil.getColor(offset, width); 
    return color;
  }
  
  public void render(IngameDisplay display, ScaledResolution sr) {
    render(display, sr.getScaledWidth() / 2, sr.getScaledHeight() / 2);
  }
}
