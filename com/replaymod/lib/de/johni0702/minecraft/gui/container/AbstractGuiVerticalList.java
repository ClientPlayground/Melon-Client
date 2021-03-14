package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;

public abstract class AbstractGuiVerticalList<T extends AbstractGuiVerticalList<T>> extends AbstractGuiScrollable<T> implements Draggable {
  public static final ReadableColor BACKGROUND = (ReadableColor)new Color(0, 0, 0, 150);
  
  public VerticalLayout getListLayout() {
    return this.listLayout;
  }
  
  private final VerticalLayout listLayout = (new VerticalLayout())
    .setSpacing(3);
  
  public GuiPanel getListPanel() {
    return this.listPanel;
  }
  
  private final GuiPanel listPanel = (new GuiPanel(this))
    .setLayout((Layout)this.listLayout);
  
  private boolean drawShadow;
  
  private boolean drawSlider;
  
  private ReadablePoint lastMousePos;
  
  private boolean draggingSlider;
  
  public AbstractGuiVerticalList() {
    setLayout((Layout)new CustomLayout<T>() {
          protected void layout(T container, int width, int height) {
            pos((GuiElement)AbstractGuiVerticalList.this.listPanel, width / 2 - width((GuiElement)AbstractGuiVerticalList.this.listPanel) / 2, 5);
          }
          
          public ReadableDimension calcMinSize(GuiContainer<?> container) {
            final ReadableDimension panelSize = AbstractGuiVerticalList.this.listPanel.getMinSize();
            return new ReadableDimension() {
                public int getWidth() {
                  return panelSize.getWidth();
                }
                
                public int getHeight() {
                  return panelSize.getHeight() + 10;
                }
                
                public void getSize(WritableDimension dest) {
                  dest.setSize(getWidth(), getHeight());
                }
              };
          }
        });
  }
  
  public AbstractGuiVerticalList(GuiContainer container) {
    super(container);
    setLayout((Layout)new CustomLayout<T>() {
          protected void layout(T container, int width, int height) {
            pos((GuiElement)AbstractGuiVerticalList.this.listPanel, width / 2 - width((GuiElement)AbstractGuiVerticalList.this.listPanel) / 2, 5);
          }
          
          public ReadableDimension calcMinSize(GuiContainer<?> container) {
            final ReadableDimension panelSize = AbstractGuiVerticalList.this.listPanel.getMinSize();
            return new ReadableDimension() {
                public int getWidth() {
                  return panelSize.getWidth();
                }
                
                public int getHeight() {
                  return panelSize.getHeight() + 10;
                }
                
                public void getSize(WritableDimension dest) {
                  dest.setSize(getWidth(), getHeight());
                }
              };
          }
        });
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    int width = size.getWidth();
    int height = size.getHeight();
    if (this.drawShadow) {
      renderer.drawRect(0, 0, width, height, BACKGROUND);
      super.draw(renderer, size, renderInfo);
      renderer.drawRect(0, 0, width, 4, ReadableColor.BLACK, ReadableColor.BLACK, Colors.TRANSPARENT, Colors.TRANSPARENT);
      renderer.drawRect(0, height - 4, width, 4, Colors.TRANSPARENT, Colors.TRANSPARENT, ReadableColor.BLACK, ReadableColor.BLACK);
    } else {
      super.draw(renderer, size, renderInfo);
    } 
    if (this.drawSlider) {
      ReadableDimension contentSize = this.listPanel.calcMinSize();
      int contentHeight = contentSize.getHeight() + 10;
      if (contentHeight > height) {
        int sliderX = width / 2 + contentSize.getWidth() / 2 + 3;
        renderer.drawRect(sliderX, 0, 6, height, ReadableColor.BLACK);
        int sliderY = getOffsetY() * height / contentHeight;
        int sliderSize = height * height / contentHeight;
        renderer.drawRect(sliderX, sliderY, 6, sliderSize, Color.LTGREY);
        renderer.drawRect(sliderX + 5, sliderY, 1, sliderSize, Color.GREY);
        renderer.drawRect(sliderX, sliderY + sliderSize - 1, 6, 1, Color.GREY);
      } 
    } 
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    position = convert(position);
    if (isOnThis(position)) {
      if (isOnSliderBar(position))
        this.draggingSlider = true; 
      this.lastMousePos = position;
    } 
    return false;
  }
  
  public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
    position = convert(position);
    if (this.lastMousePos != null) {
      int dPixel = this.lastMousePos.getY() - position.getY();
      if (this.draggingSlider) {
        int contentHeight = this.listPanel.calcMinSize().getHeight();
        int renderHeight = this.lastRenderSize.getHeight();
        scrollY(dPixel * (contentHeight + renderHeight) / renderHeight);
      } else {
        scrollY(-dPixel);
      } 
      this.lastMousePos = position;
    } 
    return false;
  }
  
  public boolean mouseRelease(ReadablePoint position, int button) {
    if (this.lastMousePos != null) {
      this.lastMousePos = null;
      this.draggingSlider = false;
    } 
    return false;
  }
  
  private ReadablePoint convert(ReadablePoint readablePoint) {
    if (getContainer() != null) {
      Point point = new Point(readablePoint);
      getContainer().convertFor((GuiElement)this, point);
      return (ReadablePoint)point;
    } 
    return readablePoint;
  }
  
  private boolean isOnThis(ReadablePoint point) {
    return (point.getX() > 0 && point.getY() > 0 && point
      .getX() < this.lastRenderSize.getWidth() && point.getY() < this.lastRenderSize.getHeight());
  }
  
  private boolean isOnSliderBar(ReadablePoint point) {
    if (!this.drawSlider)
      return false; 
    int sliderX = this.lastRenderSize.getWidth() / 2 + this.listPanel.calcMinSize().getWidth() / 2 + 3;
    return (sliderX <= point.getX() && point.getX() < sliderX + 6);
  }
  
  private boolean isOnBackground(ReadablePoint point) {
    int width = this.lastRenderSize.getWidth();
    int listPanelWidth = this.listPanel.calcMinSize().getWidth();
    return (point.getX() < width / 2 - listPanelWidth / 2 || width / 2 + listPanelWidth / 2 + (this.drawSlider ? 6 : 0) < point
      .getX());
  }
  
  public boolean doesDrawSlider() {
    return this.drawSlider;
  }
  
  public T setDrawSlider(boolean drawSlider) {
    this.drawSlider = drawSlider;
    return (T)getThis();
  }
  
  public boolean doesDrawShadow() {
    return this.drawShadow;
  }
  
  public T setDrawShadow(boolean drawShadow) {
    this.drawShadow = drawShadow;
    return (T)getThis();
  }
}
