package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.util.ResourceLocation;

public interface IGuiTexturedButton<T extends IGuiTexturedButton<T>> extends IGuiClickable<T> {
  ResourceLocation getTexture();
  
  ReadableDimension getTextureTotalSize();
  
  T setTexture(ResourceLocation paramResourceLocation, int paramInt);
  
  T setTexture(ResourceLocation paramResourceLocation, int paramInt1, int paramInt2);
  
  ReadableDimension getTextureSize();
  
  T setTextureSize(int paramInt);
  
  T setTextureSize(int paramInt1, int paramInt2);
  
  ReadablePoint getTextureNormal();
  
  ReadablePoint getTextureHover();
  
  ReadablePoint getTextureDisabled();
  
  T setTexturePosH(int paramInt1, int paramInt2);
  
  T setTexturePosV(int paramInt1, int paramInt2);
  
  T setTexturePosH(ReadablePoint paramReadablePoint);
  
  T setTexturePosV(ReadablePoint paramReadablePoint);
  
  T setTexturePos(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  T setTexturePos(ReadablePoint paramReadablePoint1, ReadablePoint paramReadablePoint2);
  
  T setTexturePos(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);
  
  T setTexturePos(ReadablePoint paramReadablePoint1, ReadablePoint paramReadablePoint2, ReadablePoint paramReadablePoint3);
}
