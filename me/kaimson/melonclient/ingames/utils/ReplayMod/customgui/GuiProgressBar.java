package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiProgressBar extends AbstractGuiProgressBar<GuiProgressBar> {
  public GuiProgressBar() {}
  
  public GuiProgressBar(GuiContainer container) {
    super(container);
  }
  
  protected GuiProgressBar getThis() {
    return this;
  }
}
