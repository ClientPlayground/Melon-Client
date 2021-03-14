package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public interface IGuiTimeline<T extends IGuiTimeline<T>> extends GuiElement<T> {
  T setLength(int paramInt);
  
  int getLength();
  
  T setCursorPosition(int paramInt);
  
  int getCursorPosition();
  
  T setZoom(double paramDouble);
  
  double getZoom();
  
  T setOffset(int paramInt);
  
  int getOffset();
  
  T setMarkers();
  
  T setMarkers(boolean paramBoolean);
  
  boolean getMarkers();
  
  int getMarkerInterval();
  
  T setCursor(boolean paramBoolean);
  
  boolean getCursor();
  
  T onClick(OnClick paramOnClick);
  
  public static interface OnClick {
    void run(int param1Int);
  }
}
