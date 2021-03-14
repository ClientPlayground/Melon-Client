package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiButton extends AbstractGuiButton<GuiButton> {
  public GuiButton() {}
  
  public GuiButton(GuiContainer container) {
    super(container);
  }
  
  protected GuiButton getThis() {
    return this;
  }
}
