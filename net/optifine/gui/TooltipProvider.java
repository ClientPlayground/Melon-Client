package net.optifine.gui;

import java.awt.Rectangle;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public interface TooltipProvider {
  Rectangle getTooltipBounds(GuiScreen paramGuiScreen, int paramInt1, int paramInt2);
  
  String[] getTooltipLines(GuiButton paramGuiButton, int paramInt);
  
  boolean isRenderBorder();
}
