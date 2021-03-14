package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import net.minecraft.util.ResourceLocation;

public interface IGuiImage<T extends IGuiImage<T>> extends GuiElement<T> {
  T setTexture(Image paramImage);
  
  T setTexture(ResourceLocation paramResourceLocation);
  
  T setTexture(ResourceLocation paramResourceLocation, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  T setU(int paramInt);
  
  T setV(int paramInt);
  
  T setUV(int paramInt1, int paramInt2);
  
  T setUWidth(int paramInt);
  
  T setVHeight(int paramInt);
  
  T setUVSize(int paramInt1, int paramInt2);
}
