package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public abstract class AbstractGuiLabel<T extends AbstractGuiLabel<T>> extends AbstractGuiElement<T> implements IGuiLabel<T> {
  private String text = "";
  
  private ReadableColor color;
  
  private ReadableColor disabledColor;
  
  public AbstractGuiLabel() {
    this.color = ReadableColor.WHITE;
    this.disabledColor = ReadableColor.GREY;
  }
  
  public AbstractGuiLabel(GuiContainer container) {
    super(container);
    this.color = ReadableColor.WHITE;
    this.disabledColor = ReadableColor.GREY;
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    FontRenderer fontRenderer = (Minecraft.getMinecraft()).fontRendererObj;
    List<String> lines = fontRenderer.listFormattedStringToWidth(this.text, size.getWidth());
    int y = 0;
    for (Iterator<String> var7 = lines.iterator(); var7.hasNext(); y += fontRenderer.FONT_HEIGHT) {
      String line = var7.next();
      renderer.drawString(0, y, isEnabled() ? this.color : this.disabledColor, line);
    } 
  }
  
  public ReadableDimension calcMinSize() {
    FontRenderer fontRenderer = (Minecraft.getMinecraft()).fontRendererObj;
    return (ReadableDimension)new Dimension(fontRenderer.getStringWidth(this.text), fontRenderer.FONT_HEIGHT);
  }
  
  public ReadableDimension getMaxSize() {
    return getMinSize();
  }
  
  public T setText(String text) {
    this.text = text;
    return getThis();
  }
  
  public T setI18nText(String text, Object... args) {
    return setText(I18n.format(text, args));
  }
  
  public T setColor(ReadableColor color) {
    this.color = color;
    return getThis();
  }
  
  public T setDisabledColor(ReadableColor disabledColor) {
    this.disabledColor = disabledColor;
    return getThis();
  }
  
  public String getText() {
    return this.text;
  }
  
  public ReadableColor getColor() {
    return this.color;
  }
  
  public ReadableColor getDisabledColor() {
    return this.disabledColor;
  }
}
