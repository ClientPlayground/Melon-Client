package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiImage extends AbstractGuiImage<GuiImage> {
  public GuiImage() {}
  
  public GuiImage(GuiContainer container) {
    super(container);
  }
  
  public GuiImage(GuiImage copyOf) {
    super(copyOf);
  }
  
  protected GuiImage getThis() {
    return this;
  }
}
