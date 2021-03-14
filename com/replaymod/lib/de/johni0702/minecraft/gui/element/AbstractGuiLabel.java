package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public abstract class AbstractGuiLabel<T extends AbstractGuiLabel<T>> extends AbstractGuiElement<T> implements IGuiLabel<T> {
  private String text = "";
  
  public String getText() {
    return this.text;
  }
  
  private ReadableColor color = ReadableColor.WHITE, disabledColor = ReadableColor.GREY;
  
  public ReadableColor getColor() {
    return this.color;
  }
  
  public ReadableColor getDisabledColor() {
    return this.disabledColor;
  }
  
  public AbstractGuiLabel(GuiContainer container) {
    super(container);
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    List<String> lines = fontRenderer.func_78271_c(this.text, size.getWidth());
    int y = 0;
    for (String line : lines) {
      renderer.drawString(0, y, isEnabled() ? this.color : this.disabledColor, line);
      y += fontRenderer.field_78288_b;
    } 
  }
  
  public ReadableDimension calcMinSize() {
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    return (ReadableDimension)new Dimension(fontRenderer.func_78256_a(this.text), fontRenderer.field_78288_b);
  }
  
  public ReadableDimension getMaxSize() {
    return getMinSize();
  }
  
  public T setText(String text) {
    this.text = text;
    return getThis();
  }
  
  public T setI18nText(String text, Object... args) {
    return setText(I18n.func_135052_a(text, args));
  }
  
  public T setColor(ReadableColor color) {
    this.color = color;
    return getThis();
  }
  
  public T setDisabledColor(ReadableColor disabledColor) {
    this.disabledColor = disabledColor;
    return getThis();
  }
  
  public AbstractGuiLabel() {}
}
