package com.replaymod.lib.de.johni0702.minecraft.gui.function;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public interface Draggable extends Clickable {
  boolean mouseDrag(ReadablePoint paramReadablePoint, int paramInt, @Deprecated long paramLong);
  
  boolean mouseRelease(ReadablePoint paramReadablePoint, int paramInt);
}
