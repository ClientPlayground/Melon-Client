package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiTextArea<T extends IGuiTextArea<T>> extends GuiElement<T>, Focusable<T> {
  T setText(String[] paramArrayOfString);
  
  String[] getText();
  
  String getText(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  int getSelectionFromX();
  
  int getSelectionToX();
  
  int getSelectionFromY();
  
  int getSelectionToY();
  
  String getSelectedText();
  
  void deleteSelectedText();
  
  String cutSelectedText();
  
  void writeText(String paramString);
  
  void writeChar(char paramChar);
  
  T setCursorPosition(int paramInt1, int paramInt2);
  
  T setMaxTextWidth(int paramInt);
  
  T setMaxTextHeight(int paramInt);
  
  T setMaxCharCount(int paramInt);
  
  T setTextColor(ReadableColor paramReadableColor);
  
  T setTextColorDisabled(ReadableColor paramReadableColor);
  
  int getMaxTextWidth();
  
  int getMaxTextHeight();
  
  int getMaxCharCount();
  
  String[] getHint();
  
  T setHint(String... paramVarArgs);
  
  T setI18nHint(String paramString, Object... paramVarArgs);
}
