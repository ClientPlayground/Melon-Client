package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiTextField extends AbstractGuiTextField<GuiTextField> {
  public GuiTextField() {}
  
  public GuiTextField(GuiContainer container) {
    super(container);
  }
  
  protected GuiTextField getThis() {
    return this;
  }
}
