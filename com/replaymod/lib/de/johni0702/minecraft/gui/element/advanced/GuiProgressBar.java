package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiProgressBar extends AbstractGuiProgressBar<GuiProgressBar> {
  public GuiProgressBar() {}
  
  public GuiProgressBar(GuiContainer container) {
    super(container);
  }
  
  protected GuiProgressBar getThis() {
    return this;
  }
}
