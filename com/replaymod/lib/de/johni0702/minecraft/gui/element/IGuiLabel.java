package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiLabel<T extends IGuiLabel<T>> extends GuiElement<T> {
  T setText(String paramString);
  
  T setI18nText(String paramString, Object... paramVarArgs);
  
  T setColor(ReadableColor paramReadableColor);
  
  T setDisabledColor(ReadableColor paramReadableColor);
  
  String getText();
  
  ReadableColor getColor();
  
  ReadableColor getDisabledColor();
}
