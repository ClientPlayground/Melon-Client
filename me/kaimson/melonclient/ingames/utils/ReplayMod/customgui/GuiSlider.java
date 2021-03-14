package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiSlider extends AbstractGuiSlider<GuiSlider> {
  public GuiSlider() {}
  
  public GuiSlider(GuiContainer container) {
    super(container);
  }
  
  protected GuiSlider getThis() {
    return this;
  }
}
