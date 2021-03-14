package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiTexturedButton extends AbstractGuiTexturedButton<GuiTexturedButton> {
  public GuiTexturedButton() {}
  
  public GuiTexturedButton(GuiContainer container) {
    super(container);
  }
  
  protected GuiTexturedButton getThis() {
    return this;
  }
}
