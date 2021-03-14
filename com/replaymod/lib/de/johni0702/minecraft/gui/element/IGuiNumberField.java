package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiNumberField<T extends IGuiNumberField<T>> extends IGuiTextField<T> {
  byte getByte();
  
  short getShort();
  
  int getInteger();
  
  long getLong();
  
  float getFloat();
  
  double getDouble();
  
  T setValue(int paramInt);
  
  T setValue(double paramDouble);
  
  T setMinValue(Double paramDouble);
  
  T setMaxValue(Double paramDouble);
  
  T setMinValue(int paramInt);
  
  T setMaxValue(int paramInt);
  
  T setValidateOnFocusChange(boolean paramBoolean);
  
  T setPrecision(int paramInt);
}
