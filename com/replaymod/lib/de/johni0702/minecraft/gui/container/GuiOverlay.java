package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import net.minecraft.client.gui.GuiScreen;

public class GuiOverlay extends AbstractGuiOverlay<GuiOverlay> {
  public static AbstractGuiOverlay from(GuiScreen minecraft) {
    if (!(minecraft instanceof AbstractGuiOverlay.UserInputGuiScreen))
      return null; 
    return ((AbstractGuiOverlay.UserInputGuiScreen)minecraft).getOverlay();
  }
  
  protected GuiOverlay getThis() {
    return this;
  }
}
