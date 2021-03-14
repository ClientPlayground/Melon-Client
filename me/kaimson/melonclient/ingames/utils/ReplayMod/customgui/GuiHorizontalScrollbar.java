package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiHorizontalScrollbar extends AbstractGuiHorizontalScrollbar<GuiHorizontalScrollbar> {
  public GuiHorizontalScrollbar() {}
  
  public GuiHorizontalScrollbar(GuiContainer container) {
    super(container);
  }
  
  protected GuiHorizontalScrollbar getThis() {
    return this;
  }
}
