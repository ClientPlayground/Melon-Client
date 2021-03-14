package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import java.util.Map;
import java.util.function.Function;

public interface IGuiDropdownMenu<V, T extends IGuiDropdownMenu<V, T>> extends GuiElement<T> {
  T setValues(V... paramVarArgs);
  
  T setSelected(int paramInt);
  
  T setSelected(V paramV);
  
  V getSelectedValue();
  
  T setOpened(boolean paramBoolean);
  
  int getSelected();
  
  V[] getValues();
  
  boolean isOpened();
  
  T onSelection(Consumer<Integer> paramConsumer);
  
  Map<V, IGuiClickable> getDropdownEntries();
  
  T setToString(Function<V, String> paramFunction);
}
