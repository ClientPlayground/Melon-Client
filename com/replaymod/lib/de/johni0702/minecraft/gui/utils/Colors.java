package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface Colors extends ReadableColor {
  public static final ReadableColor TRANSPARENT = (ReadableColor)new Color(0, 0, 0, 0);
  
  public static final ReadableColor LIGHT_TRANSPARENT = (ReadableColor)new Color(0, 0, 0, 64);
  
  public static final ReadableColor HALF_TRANSPARENT = (ReadableColor)new Color(0, 0, 0, 128);
  
  public static final ReadableColor DARK_TRANSPARENT = (ReadableColor)new Color(0, 0, 0, 192);
  
  public static final ReadableColor LIGHT_GRAY = (ReadableColor)new Color(192, 192, 192);
  
  public static final ReadableColor DARK_RED = (ReadableColor)new Color(170, 0, 0);
}
