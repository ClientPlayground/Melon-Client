package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiTextArea extends AbstractGuiTextArea<GuiTextArea> {
  public GuiTextArea() {}
  
  public GuiTextArea(GuiContainer container) {
    super(container);
  }
  
  protected GuiTextArea getThis() {
    return this;
  }
}
