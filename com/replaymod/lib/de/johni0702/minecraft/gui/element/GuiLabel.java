package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiLabel extends AbstractGuiLabel<GuiLabel> {
  public GuiLabel() {}
  
  public GuiLabel(GuiContainer container) {
    super(container);
  }
  
  protected GuiLabel getThis() {
    return this;
  }
}
