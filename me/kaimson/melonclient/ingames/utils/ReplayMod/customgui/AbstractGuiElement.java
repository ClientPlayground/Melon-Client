package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractGuiElement<T extends AbstractGuiElement<T>> implements GuiElement<T> {
  protected static final ResourceLocation TEXTURE = new ResourceLocation("melonclient/replaymod/gui.png");
  
  private final Minecraft minecraft = Minecraft.getMinecraft();
  
  private GuiContainer container;
  
  private GuiElement tooltip;
  
  private boolean enabled = true;
  
  protected Dimension minSize;
  
  protected Dimension maxSize;
  
  private ReadableDimension lastSize;
  
  public AbstractGuiElement(GuiContainer container) {
    container.addElements(null, new GuiElement[] { this });
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    if (size == null) {
      if (getContainer() == null)
        throw new RuntimeException("Any top containers must implement layout(null, ...) themselves!"); 
      getContainer().layout(size, renderInfo.layer(renderInfo.layer + getLayer()));
    } else if (renderInfo.layer == 0) {
      this.lastSize = size;
    } 
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {}
  
  public T setEnabled(boolean enabled) {
    this.enabled = enabled;
    return getThis();
  }
  
  public T setEnabled() {
    return setEnabled(true);
  }
  
  public T setDisabled() {
    return setEnabled(false);
  }
  
  public GuiElement getTooltip(RenderInfo renderInfo) {
    if (this.tooltip != null && this.lastSize != null) {
      Point mouse = new Point(renderInfo.mouseX, renderInfo.mouseY);
      if (this.container != null)
        this.container.convertFor(this, mouse); 
      if (mouse.getX() > 0 && mouse.getY() > 0 && mouse.getX() < this.lastSize.getWidth() && mouse.getY() < this.lastSize.getHeight())
        return this.tooltip; 
    } 
    return null;
  }
  
  public T setTooltip(GuiElement tooltip) {
    this.tooltip = tooltip;
    return getThis();
  }
  
  public T setContainer(GuiContainer container) {
    this.container = container;
    return getThis();
  }
  
  public T setMinSize(ReadableDimension minSize) {
    this.minSize = new Dimension(minSize);
    return getThis();
  }
  
  public T setMaxSize(ReadableDimension maxSize) {
    this.maxSize = new Dimension(maxSize);
    return getThis();
  }
  
  public T setSize(ReadableDimension size) {
    setMinSize(size);
    return setMaxSize(size);
  }
  
  public T setSize(int width, int height) {
    return setSize((ReadableDimension)new Dimension(width, height));
  }
  
  public T setWidth(int width) {
    if (this.minSize == null) {
      this.minSize = new Dimension(width, 0);
    } else {
      this.minSize.setWidth(width);
    } 
    if (this.maxSize == null) {
      this.maxSize = new Dimension(width, 2147483647);
    } else {
      this.maxSize.setWidth(width);
    } 
    return getThis();
  }
  
  public T setHeight(int height) {
    if (this.minSize == null) {
      this.minSize = new Dimension(0, height);
    } else {
      this.minSize.setHeight(height);
    } 
    if (this.maxSize == null) {
      this.maxSize = new Dimension(2147483647, height);
    } else {
      this.maxSize.setHeight(height);
    } 
    return getThis();
  }
  
  public int getLayer() {
    return 0;
  }
  
  public ReadableDimension getMinSize() {
    ReadableDimension calcSize = calcMinSize();
    if (this.minSize == null)
      return calcSize; 
    return (this.minSize.getWidth() >= calcSize.getWidth() && this.minSize.getHeight() >= calcSize.getHeight()) ? (ReadableDimension)this.minSize : (ReadableDimension)new Dimension(Math.max(calcSize.getWidth(), this.minSize.getWidth()), Math.max(calcSize.getHeight(), this.minSize.getHeight()));
  }
  
  public ReadableDimension getMaxSize() {
    return (this.maxSize == null) ? (ReadableDimension)new Dimension(2147483647, 2147483647) : (ReadableDimension)this.maxSize;
  }
  
  public Minecraft getMinecraft() {
    return this.minecraft;
  }
  
  public GuiContainer getContainer() {
    return this.container;
  }
  
  public boolean isEnabled() {
    return this.enabled;
  }
  
  protected ReadableDimension getLastSize() {
    return this.lastSize;
  }
  
  public AbstractGuiElement() {}
  
  protected abstract T getThis();
  
  protected abstract ReadableDimension calcMinSize();
}
