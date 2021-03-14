package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractGuiTexturedButton<T extends AbstractGuiTexturedButton<T>> extends AbstractGuiClickable<T> implements Clickable, IGuiTexturedButton<T> {
  private ResourceLocation texture;
  
  public ResourceLocation getTexture() {
    return this.texture;
  }
  
  private ReadableDimension textureSize = new ReadableDimension() {
      public int getWidth() {
        return AbstractGuiTexturedButton.this.getMaxSize().getWidth();
      }
      
      public int getHeight() {
        return AbstractGuiTexturedButton.this.getMaxSize().getHeight();
      }
      
      public void getSize(WritableDimension dest) {
        AbstractGuiTexturedButton.this.getMaxSize().getSize(dest);
      }
    };
  
  private ReadableDimension textureTotalSize;
  
  private ReadablePoint textureNormal;
  
  private ReadablePoint textureHover;
  
  private ReadablePoint textureDisabled;
  
  public ReadableDimension getTextureSize() {
    return this.textureSize;
  }
  
  public ReadableDimension getTextureTotalSize() {
    return this.textureTotalSize;
  }
  
  public ReadablePoint getTextureNormal() {
    return this.textureNormal;
  }
  
  public ReadablePoint getTextureHover() {
    return this.textureHover;
  }
  
  public ReadablePoint getTextureDisabled() {
    return this.textureDisabled;
  }
  
  public AbstractGuiTexturedButton(GuiContainer container) {
    super(container);
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    renderer.bindTexture(this.texture);
    ReadablePoint texture = this.textureNormal;
    if (!isEnabled()) {
      texture = this.textureDisabled;
    } else if (isMouseHovering((ReadablePoint)new Point(renderInfo.mouseX, renderInfo.mouseY))) {
      texture = this.textureHover;
    } 
    if (texture == null) {
      GlStateManager.func_179131_c(0.5F, 0.5F, 0.5F, 1.0F);
      texture = this.textureNormal;
    } 
    GlStateManager.func_179147_l();
    GlStateManager.func_179120_a(770, 771, 1, 0);
    GlStateManager.func_179112_b(770, 771);
    renderer.drawTexturedRect(0, 0, texture.getX(), texture.getY(), size.getWidth(), size.getHeight(), this.textureSize
        .getWidth(), this.textureSize.getHeight(), this.textureTotalSize
        .getWidth(), this.textureTotalSize.getHeight());
  }
  
  public ReadableDimension calcMinSize() {
    return (ReadableDimension)new Dimension(0, 0);
  }
  
  public void onClick() {
    AbstractGuiButton.playClickSound(getMinecraft());
    super.onClick();
  }
  
  public T setTexture(ResourceLocation resourceLocation, int size) {
    return setTexture(resourceLocation, size, size);
  }
  
  public T setTexture(ResourceLocation resourceLocation, int width, int height) {
    this.texture = resourceLocation;
    this.textureTotalSize = (ReadableDimension)new Dimension(width, height);
    return getThis();
  }
  
  public T setTextureSize(int size) {
    return setTextureSize(size, size);
  }
  
  public T setTextureSize(int width, int height) {
    this.textureSize = (ReadableDimension)new Dimension(width, height);
    return getThis();
  }
  
  public T setTexturePosH(int x, int y) {
    return setTexturePosH((ReadablePoint)new Point(x, y));
  }
  
  public T setTexturePosV(int x, int y) {
    return setTexturePosV((ReadablePoint)new Point(x, y));
  }
  
  public T setTexturePosH(final ReadablePoint pos) {
    this.textureNormal = pos;
    this.textureHover = new ReadablePoint() {
        public int getX() {
          return pos.getX() + AbstractGuiTexturedButton.this.textureSize.getWidth();
        }
        
        public int getY() {
          return pos.getY();
        }
        
        public void getLocation(WritablePoint dest) {
          dest.setLocation(getX(), getY());
        }
      };
    return getThis();
  }
  
  public T setTexturePosV(final ReadablePoint pos) {
    this.textureNormal = pos;
    this.textureHover = new ReadablePoint() {
        public int getX() {
          return pos.getX();
        }
        
        public int getY() {
          return pos.getY() + AbstractGuiTexturedButton.this.textureSize.getHeight();
        }
        
        public void getLocation(WritablePoint dest) {
          dest.setLocation(getX(), getY());
        }
      };
    return getThis();
  }
  
  public T setTexturePos(int normalX, int normalY, int hoverX, int hoverY) {
    return setTexturePos((ReadablePoint)new Point(normalX, normalY), (ReadablePoint)new Point(hoverX, hoverY));
  }
  
  public T setTexturePos(ReadablePoint normal, ReadablePoint hover) {
    this.textureNormal = normal;
    this.textureHover = hover;
    return getThis();
  }
  
  public T setTexturePos(int normalX, int normalY, int hoverX, int hoverY, int disabledX, int disabledY) {
    return setTexturePos((ReadablePoint)new Point(normalX, normalY), (ReadablePoint)new Point(hoverX, hoverY), (ReadablePoint)new Point(disabledX, disabledY));
  }
  
  public T setTexturePos(ReadablePoint normal, ReadablePoint hover, ReadablePoint disabled) {
    this.textureDisabled = disabled;
    return setTexturePos(normal, hover);
  }
  
  public AbstractGuiTexturedButton() {}
}
