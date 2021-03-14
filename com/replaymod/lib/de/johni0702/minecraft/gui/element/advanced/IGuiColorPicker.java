package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiColorPicker<T extends IGuiColorPicker<T>> extends GuiElement<T> {
  T setColor(ReadableColor paramReadableColor);
  
  ReadableColor getColor();
  
  T setOpened(boolean paramBoolean);
  
  boolean isOpened();
  
  T onSelection(Consumer<ReadableColor> paramConsumer);
}
