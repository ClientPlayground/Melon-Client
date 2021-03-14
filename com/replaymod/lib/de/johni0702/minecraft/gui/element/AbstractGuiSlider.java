package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.client.resources.I18n;

public abstract class AbstractGuiSlider<T extends AbstractGuiSlider<T>> extends AbstractGuiElement<T> implements Clickable, Draggable, IGuiSlider<T> {
  private Runnable onValueChanged;
  
  private int value;
  
  private int steps;
  
  private String text = "";
  
  private boolean dragging;
  
  public AbstractGuiSlider(GuiContainer container) {
    super(container);
  }
  
  protected ReadableDimension calcMinSize() {
    return (ReadableDimension)new Dimension(0, 0);
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    Point pos = new Point(position);
    if (getContainer() != null)
      getContainer().convertFor(this, pos); 
    if (isMouseHovering((ReadablePoint)pos) && isEnabled()) {
      updateValue((ReadablePoint)pos);
      this.dragging = true;
      return true;
    } 
    return false;
  }
  
  public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
    if (this.dragging) {
      Point pos = new Point(position);
      if (getContainer() != null)
        getContainer().convertFor(this, pos); 
      updateValue((ReadablePoint)pos);
    } 
    return this.dragging;
  }
  
  public boolean mouseRelease(ReadablePoint position, int button) {
    if (this.dragging) {
      this.dragging = false;
      Point pos = new Point(position);
      if (getContainer() != null)
        getContainer().convertFor(this, pos); 
      updateValue((ReadablePoint)pos);
      return true;
    } 
    return false;
  }
  
  protected boolean isMouseHovering(ReadablePoint pos) {
    return (pos.getX() > 0 && pos.getY() > 0 && pos
      .getX() < getLastSize().getWidth() && pos.getY() < getLastSize().getHeight());
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    int width = size.getWidth();
    int height = size.getHeight();
    renderer.bindTexture(GuiButton.WIDGETS_TEXTURE);
    renderer.drawTexturedRect(0, 0, 0, 46, width / 2, height);
    renderer.drawTexturedRect(width / 2, 0, 200 - width / 2, 46, width / 2, height);
    int sliderX = (width - 8) * this.value / this.steps;
    renderer.drawTexturedRect(sliderX, 0, 0, 66, 4, 20);
    renderer.drawTexturedRect(sliderX + 4, 0, 196, 66, 4, 20);
    int color = 14737632;
    if (!isEnabled()) {
      color = 10526880;
    } else if (isMouseHovering((ReadablePoint)new Point(renderInfo.mouseX, renderInfo.mouseY))) {
      color = 16777120;
    } 
    renderer.drawCenteredString(width / 2, height / 2 - 4, color, this.text);
  }
  
  protected void updateValue(ReadablePoint position) {
    if (getLastSize() == null)
      return; 
    int width = getLastSize().getWidth() - 8;
    int pos = Math.max(0, Math.min(width, position.getX() - 4));
    setValue(this.steps * pos / width);
  }
  
  public void onValueChanged() {
    if (this.onValueChanged != null)
      this.onValueChanged.run(); 
  }
  
  public T setText(String text) {
    this.text = text;
    return getThis();
  }
  
  public T setI18nText(String text, Object... args) {
    return setText(I18n.func_135052_a(text, args));
  }
  
  public T setValue(int value) {
    this.value = value;
    onValueChanged();
    return getThis();
  }
  
  public int getValue() {
    return this.value;
  }
  
  public int getSteps() {
    return this.steps;
  }
  
  public T setSteps(int steps) {
    this.steps = steps;
    return getThis();
  }
  
  public T onValueChanged(Runnable runnable) {
    this.onValueChanged = runnable;
    return getThis();
  }
  
  public AbstractGuiSlider() {}
}
