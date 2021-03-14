package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.ComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import java.util.Comparator;
import java.util.Map;

public interface GuiContainer<T extends GuiContainer<T>> extends ComposedGuiElement<T> {
  T setLayout(Layout paramLayout);
  
  Layout getLayout();
  
  void convertFor(GuiElement paramGuiElement, Point paramPoint);
  
  void convertFor(GuiElement paramGuiElement, Point paramPoint, int paramInt);
  
  Map<GuiElement, LayoutData> getElements();
  
  T addElements(LayoutData paramLayoutData, GuiElement... paramVarArgs);
  
  T removeElement(GuiElement paramGuiElement);
  
  T sortElements();
  
  T sortElements(Comparator<GuiElement> paramComparator);
  
  ReadableColor getBackgroundColor();
  
  T setBackgroundColor(ReadableColor paramReadableColor);
}
