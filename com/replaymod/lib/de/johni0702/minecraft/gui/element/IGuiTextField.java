package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import lombok.NonNull;

public interface IGuiTextField<T extends IGuiTextField<T>> extends GuiElement<T>, Focusable<T> {
  @NonNull
  T setText(String paramString);
  
  @NonNull
  T setI18nText(String paramString, Object... paramVarArgs);
  
  @NonNull
  String getText();
  
  int getMaxLength();
  
  T setMaxLength(int paramInt);
  
  @NonNull
  String deleteText(int paramInt1, int paramInt2);
  
  int getSelectionFrom();
  
  int getSelectionTo();
  
  @NonNull
  String getSelectedText();
  
  @NonNull
  String deleteSelectedText();
  
  @NonNull
  T writeText(String paramString);
  
  @NonNull
  T writeChar(char paramChar);
  
  T deleteNextChar();
  
  String deleteNextWord();
  
  @NonNull
  T deletePreviousChar();
  
  @NonNull
  String deletePreviousWord();
  
  @NonNull
  T setCursorPosition(int paramInt);
  
  T onEnter(Runnable paramRunnable);
  
  T onTextChanged(Consumer<String> paramConsumer);
  
  String getHint();
  
  T setHint(String paramString);
  
  T setI18nHint(String paramString, Object... paramVarArgs);
  
  ReadableColor getTextColor();
  
  T setTextColor(ReadableColor paramReadableColor);
  
  ReadableColor getTextColorDisabled();
  
  T setTextColorDisabled(ReadableColor paramReadableColor);
}
