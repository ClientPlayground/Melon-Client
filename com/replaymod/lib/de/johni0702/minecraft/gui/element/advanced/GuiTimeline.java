package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiTimeline extends AbstractGuiTimeline<GuiTimeline> {
  public GuiTimeline() {}
  
  public GuiTimeline(GuiContainer container) {
    super(container);
  }
  
  protected GuiTimeline getThis() {
    return this;
  }
}
