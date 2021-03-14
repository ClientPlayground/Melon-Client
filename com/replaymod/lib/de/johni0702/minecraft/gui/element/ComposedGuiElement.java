package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import java.util.Collection;

public interface ComposedGuiElement<T extends ComposedGuiElement<T>> extends GuiElement<T> {
  Collection<GuiElement> getChildren();
  
  <C> C forEach(Class<C> paramClass);
  
  <C> C forEach(int paramInt, Class<C> paramClass);
  
  int getMaxLayer();
}
