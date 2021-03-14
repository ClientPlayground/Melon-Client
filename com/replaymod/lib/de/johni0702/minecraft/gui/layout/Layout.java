package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.org.apache.commons.lang3.tuple.Pair;
import java.util.Map;

public interface Layout {
  Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> paramGuiContainer, ReadableDimension paramReadableDimension);
  
  ReadableDimension calcMinSize(GuiContainer<?> paramGuiContainer);
}
