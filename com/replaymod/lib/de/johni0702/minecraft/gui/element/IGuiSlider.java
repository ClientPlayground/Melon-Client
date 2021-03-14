package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiSlider<T extends IGuiSlider<T>> extends GuiElement<T> {
  T setText(String paramString);
  
  T setI18nText(String paramString, Object... paramVarArgs);
  
  T setValue(int paramInt);
  
  int getValue();
  
  int getSteps();
  
  T setSteps(int paramInt);
  
  T onValueChanged(Runnable paramRunnable);
}
