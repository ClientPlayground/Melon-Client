package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiScreen extends AbstractGuiScreen<GuiScreen> {
  public static AbstractGuiScreen from(net.minecraft.client.gui.GuiScreen minecraft) {
    if (!(minecraft instanceof AbstractGuiScreen.MinecraftGuiScreen))
      return null; 
    return (AbstractGuiScreen)((AbstractGuiScreen.MinecraftGuiScreen)minecraft).getWrapper();
  }
  
  public static GuiScreen wrap(final net.minecraft.client.gui.GuiScreen minecraft) {
    return new GuiScreen() {
        public net.minecraft.client.gui.GuiScreen toMinecraft() {
          return minecraft;
        }
      };
  }
  
  protected GuiScreen getThis() {
    return this;
  }
}
