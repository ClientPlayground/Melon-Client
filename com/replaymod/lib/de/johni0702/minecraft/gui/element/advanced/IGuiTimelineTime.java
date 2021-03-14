package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public interface IGuiTimelineTime<T extends IGuiTimelineTime<T, U>, U extends IGuiTimeline<U>> extends GuiElement<T> {
  U getTimeline();
  
  T setTimeline(U paramU);
}
