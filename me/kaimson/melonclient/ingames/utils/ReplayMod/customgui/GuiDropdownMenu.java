package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiDropdownMenu<V> extends AbstractGuiDropdownMenu<V, GuiDropdownMenu<V>> {
  public GuiDropdownMenu() {}
  
  public GuiDropdownMenu(GuiContainer container) {
    super(container);
  }
  
  protected GuiDropdownMenu<V> getThis() {
    return this;
  }
}
