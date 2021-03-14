package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.IGuiProgressBar;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public abstract class AbstractGuiProgressBar<T extends AbstractGuiProgressBar<T>> extends AbstractGuiElement<T> implements IGuiProgressBar<T> {
  private static final int BORDER = 2;
  
  private float progress;
  
  private String label = "%d%%";
  
  public AbstractGuiProgressBar(GuiContainer container) {
    super(container);
  }
  
  public T setProgress(float progress) {
    this.progress = progress;
    return getThis();
  }
  
  public T setLabel(String label) {
    this.label = label;
    return getThis();
  }
  
  public T setI18nLabel(String label, Object... args) {
    return setLabel(I18n.format(label, args));
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    FontRenderer fontRenderer = (Minecraft.getMinecraft()).fontRendererObj;
    int width = size.getWidth();
    int height = size.getHeight();
    int barTotalWidth = width - 4;
    int barDoneWidth = (int)(barTotalWidth * this.progress);
    renderer.drawRect(0, 0, width, height, ReadableColor.BLACK);
    renderer.drawRect(2, 2, barTotalWidth, height - 4, ReadableColor.WHITE);
    renderer.drawRect(2, 2, barDoneWidth, height - 4, ReadableColor.GREY);
    String text = String.format(this.label, new Object[] { Integer.valueOf((int)(this.progress * 100.0F)) });
    renderer.drawCenteredString(width / 2, size.getHeight() / 2 - fontRenderer.FONT_HEIGHT / 2, ReadableColor.BLACK, text);
  }
  
  public ReadableDimension calcMinSize() {
    return (ReadableDimension)new Dimension(0, 0);
  }
  
  public float getProgress() {
    return this.progress;
  }
  
  public String getLabel() {
    return this.label;
  }
  
  public AbstractGuiProgressBar() {}
}
