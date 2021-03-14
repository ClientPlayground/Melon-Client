package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.Minecraft;

public interface GuiElement<T extends GuiElement<T>> {
  Minecraft getMinecraft();
  
  GuiContainer getContainer();
  
  T setContainer(GuiContainer paramGuiContainer);
  
  void layout(ReadableDimension paramReadableDimension, RenderInfo paramRenderInfo);
  
  void draw(GuiRenderer paramGuiRenderer, ReadableDimension paramReadableDimension, RenderInfo paramRenderInfo);
  
  ReadableDimension getMinSize();
  
  ReadableDimension getMaxSize();
  
  T setMaxSize(ReadableDimension paramReadableDimension);
  
  boolean isEnabled();
  
  T setEnabled(boolean paramBoolean);
  
  T setEnabled();
  
  T setDisabled();
  
  GuiElement getTooltip(RenderInfo paramRenderInfo);
  
  T setTooltip(GuiElement paramGuiElement);
  
  int getLayer();
}
