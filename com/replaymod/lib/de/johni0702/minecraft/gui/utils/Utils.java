package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.google.common.base.Preconditions;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import java.util.Arrays;
import java.util.HashSet;

public class Utils {
  public static final int DOUBLE_CLICK_INTERVAL = 250;
  
  public static void link(Focusable... focusables) {
    Preconditions.checkArgument(((new HashSet(Arrays.asList((Object[])focusables))).size() == focusables.length), "focusables must be unique and not null");
    for (int i = 0; i < focusables.length; i++) {
      Focusable next = focusables[(i + 1) % focusables.length];
      focusables[i].setNext(next);
      next.setPrevious(focusables[i]);
    } 
  }
  
  public static void drawDynamicRect(GuiRenderer renderer, int width, int height, int u, int v, int uWidth, int vHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
    int textureBodyHeight = vHeight - topBorder - bottomBorder;
    int textureBodyWidth = uWidth - leftBorder - rightBorder;
    for (int pass = 0; pass < 2; pass++) {
      int i = (pass == 0) ? 0 : (width - rightBorder);
      int textureX = (pass == 0) ? u : (u + uWidth - rightBorder);
      int y;
      for (y = topBorder; y < height - bottomBorder; y += textureBodyHeight) {
        int segmentHeight = Math.min(textureBodyHeight, height - bottomBorder - y);
        renderer.drawTexturedRect(i, y, textureX, v + topBorder, leftBorder, segmentHeight);
      } 
      renderer.drawTexturedRect(i, 0, textureX, v, leftBorder, topBorder);
      renderer.drawTexturedRect(i, height - bottomBorder, textureX, v + vHeight - bottomBorder, leftBorder, bottomBorder);
    } 
    int x;
    for (x = leftBorder; x < width - rightBorder; x += textureBodyWidth) {
      int segmentWidth = Math.min(textureBodyWidth, width - rightBorder - x);
      int textureX = u + leftBorder;
      int y;
      for (y = topBorder; y < height - bottomBorder; y += textureBodyHeight) {
        int segmentHeight = Math.min(textureBodyHeight, height - bottomBorder - y);
        renderer.drawTexturedRect(x, y, textureX, v + topBorder, segmentWidth, segmentHeight);
      } 
      renderer.drawTexturedRect(x, 0, textureX, v, segmentWidth, topBorder);
      renderer.drawTexturedRect(x, height - bottomBorder, textureX, v + vHeight - bottomBorder, segmentWidth, bottomBorder);
    } 
  }
  
  public static int clamp(int val, int min, int max) {
    return (val < min) ? min : ((val > max) ? max : val);
  }
}
