package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiCheckbox extends AbstractGuiCheckbox<GuiCheckbox> {
  public GuiCheckbox() {}
  
  public GuiCheckbox(GuiContainer container) {
    super(container);
  }
  
  protected GuiCheckbox getThis() {
    return this;
  }
}
