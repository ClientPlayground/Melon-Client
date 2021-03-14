package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiNumberField extends AbstractGuiNumberField<GuiNumberField> {
  public GuiNumberField() {}
  
  public GuiNumberField(GuiContainer container) {
    super(container);
  }
  
  protected GuiNumberField getThis() {
    return this;
  }
}
