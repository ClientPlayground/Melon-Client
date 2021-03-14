package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiColorPicker extends AbstractGuiColorPicker<GuiColorPicker> {
  public GuiColorPicker() {}
  
  public GuiColorPicker(GuiContainer container) {
    super(container);
  }
  
  protected GuiColorPicker getThis() {
    return this;
  }
}
