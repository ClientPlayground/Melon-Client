package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiPasswordField extends AbstractGuiPasswordField<GuiPasswordField> {
  public GuiPasswordField() {}
  
  public GuiPasswordField(GuiContainer container) {
    super(container);
  }
  
  protected GuiPasswordField getThis() {
    return this;
  }
}
