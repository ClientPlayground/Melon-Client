package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractGuiCheckbox<T extends AbstractGuiCheckbox<T>> extends AbstractGuiClickable<T> implements IGuiCheckbox<T> {
  protected static final ResourceLocation BUTTON_SOUND = new ResourceLocation("gui.button.press");
  
  protected static final ReadableColor BOX_BACKGROUND_COLOR = (ReadableColor)new Color(46, 46, 46);
  
  private String label;
  
  private boolean checked;
  
  public String getLabel() {
    return this.label;
  }
  
  public boolean isChecked() {
    return this.checked;
  }
  
  public AbstractGuiCheckbox() {}
  
  public AbstractGuiCheckbox(GuiContainer container) {
    super(container);
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    int color = 14737632;
    if (!isEnabled())
      color = 10526880; 
    int boxSize = size.getHeight();
    renderer.drawRect(0, 0, boxSize, boxSize, ReadableColor.BLACK);
    renderer.drawRect(1, 1, boxSize - 2, boxSize - 2, BOX_BACKGROUND_COLOR);
    if (isChecked())
      renderer.drawCenteredString(boxSize / 2 + 1, 1, color, "x", true); 
    renderer.drawString(boxSize + 2, 2, color, this.label);
  }
  
  public ReadableDimension calcMinSize() {
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int height = fontRenderer.field_78288_b + 2;
    int width = height + 2 + fontRenderer.func_78256_a(this.label);
    return (ReadableDimension)new Dimension(width, height);
  }
  
  public ReadableDimension getMaxSize() {
    return getMinSize();
  }
  
  public void onClick() {
    AbstractGuiButton.playClickSound(getMinecraft());
    setChecked(!isChecked());
    super.onClick();
  }
  
  public T setLabel(String label) {
    this.label = label;
    return getThis();
  }
  
  public T setI18nLabel(String label, Object... args) {
    return setLabel(I18n.func_135052_a(label, args));
  }
  
  public T setChecked(boolean checked) {
    this.checked = checked;
    return getThis();
  }
}
