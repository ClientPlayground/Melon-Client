package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public class GuiResourceLoadingList<U extends GuiElement<U> & Comparable<U>> extends AbstractGuiResourceLoadingList<GuiResourceLoadingList<U>, U> {
  public GuiResourceLoadingList() {}
  
  public GuiResourceLoadingList(GuiContainer container) {
    super(container);
  }
  
  protected GuiResourceLoadingList<U> getThis() {
    return this;
  }
}
