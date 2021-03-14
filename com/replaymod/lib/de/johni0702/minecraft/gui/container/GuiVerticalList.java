package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiVerticalList extends AbstractGuiVerticalList<GuiVerticalList> {
  public GuiVerticalList() {}
  
  public GuiVerticalList(GuiContainer container) {
    super(container);
  }
  
  protected GuiVerticalList getThis() {
    return this;
  }
}
