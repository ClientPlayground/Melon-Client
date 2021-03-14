package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiHorizontalScrollbar<T extends IGuiHorizontalScrollbar<T>> extends GuiElement<T> {
  T setPosition(double paramDouble);
  
  double getPosition();
  
  T setZoom(double paramDouble);
  
  double getZoom();
  
  T onValueChanged(Runnable paramRunnable);
}
