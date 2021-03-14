package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl;

public interface WritableRectangle extends WritablePoint, WritableDimension {
  void setBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  void setBounds(ReadablePoint paramReadablePoint, ReadableDimension paramReadableDimension);
  
  void setBounds(ReadableRectangle paramReadableRectangle);
}
