package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.StringUtils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public abstract class AbstractGuiTooltip<T extends AbstractGuiTooltip<T>> extends AbstractGuiElement<T> {
  private static final int LINE_SPACING = 3;
  
  private static final ReadableColor BACKGROUND_COLOR = (ReadableColor)new Color(16, 0, 16, 240);
  
  private static final ReadableColor BORDER_LIGHT = (ReadableColor)new Color(80, 0, 255, 80);
  
  private static final ReadableColor BORDER_DARK = (ReadableColor)new Color(40, 0, 127, 80);
  
  private String[] text = new String[0];
  
  public String[] getText() {
    return this.text;
  }
  
  private ReadableColor color = ReadableColor.WHITE;
  
  public ReadableColor getColor() {
    return this.color;
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    int width = size.getWidth();
    int height = size.getHeight();
    renderer.drawRect(1, 0, width - 2, height, BACKGROUND_COLOR);
    renderer.drawRect(0, 1, 1, height - 2, BACKGROUND_COLOR);
    renderer.drawRect(width - 1, 1, 1, height - 2, BACKGROUND_COLOR);
    renderer.drawRect(1, 1, width - 2, 1, BORDER_LIGHT);
    renderer.drawRect(1, height - 2, width - 2, 1, BORDER_DARK);
    renderer.drawRect(1, 2, 1, height - 4, BORDER_LIGHT, BORDER_LIGHT, BORDER_DARK, BORDER_DARK);
    renderer.drawRect(width - 2, 2, 1, height - 4, BORDER_LIGHT, BORDER_LIGHT, BORDER_DARK, BORDER_DARK);
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int y = 4;
    for (String line : this.text) {
      renderer.drawString(4, y, this.color, line, true);
      y += fontRenderer.field_78288_b + 3;
    } 
  }
  
  public ReadableDimension calcMinSize() {
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int height = 4 + this.text.length * (fontRenderer.field_78288_b + 3);
    int width = 0;
    for (String line : this.text) {
      int w = fontRenderer.func_78256_a(line);
      if (w > width)
        width = w; 
    } 
    width += 8;
    return (ReadableDimension)new Dimension(width, height);
  }
  
  public ReadableDimension getMaxSize() {
    return getMinSize();
  }
  
  public T setText(String[] text) {
    this.text = text;
    return getThis();
  }
  
  public T setText(String text) {
    return setText(StringUtils.splitStringInMultipleRows(text, 250));
  }
  
  public T setI18nText(String text, Object... args) {
    return setText(I18n.func_135052_a(text, args));
  }
  
  public T setColor(ReadableColor color) {
    this.color = color;
    return getThis();
  }
}
