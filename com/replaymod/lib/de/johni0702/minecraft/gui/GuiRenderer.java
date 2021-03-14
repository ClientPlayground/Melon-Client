package com.replaymod.lib.de.johni0702.minecraft.gui;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.util.ResourceLocation;

public interface GuiRenderer {
  ReadablePoint getOpenGlOffset();
  
  ReadableDimension getSize();
  
  void setDrawingArea(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  void bindTexture(ResourceLocation paramResourceLocation);
  
  void bindTexture(int paramInt);
  
  void drawTexturedRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);
  
  void drawTexturedRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, int paramInt9, int paramInt10);
  
  void drawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);
  
  void drawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ReadableColor paramReadableColor);
  
  void drawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8);
  
  void drawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ReadableColor paramReadableColor1, ReadableColor paramReadableColor2, ReadableColor paramReadableColor3, ReadableColor paramReadableColor4);
  
  int drawString(int paramInt1, int paramInt2, int paramInt3, String paramString);
  
  int drawString(int paramInt1, int paramInt2, ReadableColor paramReadableColor, String paramString);
  
  int drawCenteredString(int paramInt1, int paramInt2, int paramInt3, String paramString);
  
  int drawCenteredString(int paramInt1, int paramInt2, ReadableColor paramReadableColor, String paramString);
  
  int drawString(int paramInt1, int paramInt2, int paramInt3, String paramString, boolean paramBoolean);
  
  int drawString(int paramInt1, int paramInt2, ReadableColor paramReadableColor, String paramString, boolean paramBoolean);
  
  int drawCenteredString(int paramInt1, int paramInt2, int paramInt3, String paramString, boolean paramBoolean);
  
  int drawCenteredString(int paramInt1, int paramInt2, ReadableColor paramReadableColor, String paramString, boolean paramBoolean);
}
