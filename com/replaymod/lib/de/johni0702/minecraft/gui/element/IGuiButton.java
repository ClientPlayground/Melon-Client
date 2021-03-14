package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiButton<T extends IGuiButton<T>> extends IGuiClickable<T> {
  T setLabel(String paramString);
  
  T setI18nLabel(String paramString, Object... paramVarArgs);
  
  String getLabel();
}
