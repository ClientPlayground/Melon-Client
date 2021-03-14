package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public interface IGuiProgressBar<T extends IGuiProgressBar<T>> extends GuiElement<T> {
  T setProgress(float paramFloat);
  
  T setLabel(String paramString);
  
  T setI18nLabel(String paramString, Object... paramVarArgs);
  
  float getProgress();
  
  String getLabel();
}
