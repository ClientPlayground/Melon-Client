package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiCheckbox<T extends IGuiCheckbox<T>> extends IGuiClickable<T> {
  T setLabel(String paramString);
  
  T setI18nLabel(String paramString, Object... paramVarArgs);
  
  T setChecked(boolean paramBoolean);
  
  String getLabel();
  
  boolean isChecked();
}
