package com.replaymod.lib.de.johni0702.minecraft.gui.popup;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiOverlay;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public abstract class AbstractGuiPopup<T extends AbstractGuiPopup<T>> extends AbstractGuiContainer<T> {
  private final GuiPanel popupContainer = (GuiPanel)(new GuiPanel((GuiContainer)this) {
      private final int u0 = 0;
      
      private final int v0 = 39;
      
      public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
          renderer.bindTexture(TEXTURE);
          int w = size.getWidth();
          int h = size.getHeight();
          renderer.drawTexturedRect(0, 0, 0, 39, 5, 5);
          renderer.drawTexturedRect(w - 5, 0, 12, 39, 5, 5);
          renderer.drawTexturedRect(0, h - 5, 0, 51, 5, 5);
          renderer.drawTexturedRect(w - 5, h - 5, 12, 51, 5, 5);
          for (int i = 5; i < w - 5; i += 5) {
            int rx = Math.min(5, w - 5 - i);
            renderer.drawTexturedRect(i, 0, 6, 39, rx, 5);
            renderer.drawTexturedRect(i, h - 5, 6, 51, rx, 5);
          } 
          for (int y = 5; y < h - 5; y += 5) {
            int ry = Math.min(5, h - 5 - y);
            renderer.drawTexturedRect(0, y, 0, 45, 5, ry);
            renderer.drawTexturedRect(w - 5, y, 12, 45, 5, ry);
          } 
          for (int x = 5; x < w - 5; x += 5) {
            for (int j = 5; j < h - 5; j += 5) {
              int rx = Math.min(5, w - 5 - x);
              int ry = Math.min(5, h - 5 - j);
              renderer.drawTexturedRect(x, j, 6, 45, rx, ry);
            } 
          } 
        } 
        super.draw(renderer, size, renderInfo);
      }
    }).setLayout((Layout)new CustomLayout<GuiPanel>() {
        protected void layout(GuiPanel container, int width, int height) {
          pos((GuiElement)AbstractGuiPopup.this.popup, 10, 10);
        }
        
        public ReadableDimension calcMinSize(GuiContainer<?> container) {
          ReadableDimension size = AbstractGuiPopup.this.popup.calcMinSize();
          return (ReadableDimension)new Dimension(size.getWidth() + 20, size.getHeight() + 20);
        }
      });
  
  protected final GuiPanel popup = new GuiPanel((GuiContainer)this.popupContainer);
  
  private int layer;
  
  private Layout originalLayout;
  
  private boolean wasAllowUserInput;
  
  private boolean wasMouseVisible;
  
  private final GuiContainer container;
  
  public AbstractGuiPopup(GuiContainer container) {
    setLayout((Layout)new CustomLayout<T>() {
          protected void layout(T container, int width, int height) {
            pos((GuiElement)AbstractGuiPopup.this.popupContainer, width / 2 - width((GuiElement)AbstractGuiPopup.this.popupContainer) / 2, height / 2 - height((GuiElement)AbstractGuiPopup.this.popupContainer) / 2);
          }
        });
    while (container.getContainer() != null)
      container = container.getContainer(); 
    this.container = container;
  }
  
  protected void open() {
    setLayer(this.container.getMaxLayer() + 1);
    this.container.addElements(null, new GuiElement[] { (GuiElement)this });
    this.container.setLayout((Layout)new CustomLayout(this.originalLayout = this.container.getLayout()) {
          protected void layout(GuiContainer container, int width, int height) {
            pos((GuiElement)AbstractGuiPopup.this, 0, 0);
            size((GuiElement)AbstractGuiPopup.this, width, height);
          }
        });
    if (this.container instanceof AbstractGuiOverlay) {
      AbstractGuiOverlay overlay = (AbstractGuiOverlay)this.container;
      this.wasAllowUserInput = overlay.isAllowUserInput();
      overlay.setAllowUserInput(false);
      this.wasMouseVisible = overlay.isMouseVisible();
      overlay.setMouseVisible(true);
    } 
  }
  
  protected void close() {
    getContainer().setLayout(this.originalLayout);
    getContainer().removeElement((GuiElement)this);
    if (this.container instanceof AbstractGuiOverlay) {
      AbstractGuiOverlay overlay = (AbstractGuiOverlay)this.container;
      overlay.setAllowUserInput(this.wasAllowUserInput);
      overlay.setMouseVisible(this.wasMouseVisible);
    } 
  }
  
  public T setLayer(int layer) {
    this.layer = layer;
    return (T)getThis();
  }
  
  public int getLayer() {
    return this.layer;
  }
  
  public <C> C forEach(int layer, Class<C> ofType) {
    C realProxy = (C)super.forEach(layer, ofType);
    if (layer >= 0)
      return (C)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ofType }, (proxy, method, args) -> {
            try {
              if (method.getReturnType().equals(boolean.class)) {
                method.invoke(forEachChild(ofType), args);
                return Boolean.valueOf(true);
              } 
              return method.invoke(realProxy, args);
            } catch (Throwable e) {
              if (e instanceof java.lang.reflect.InvocationTargetException)
                e = e.getCause(); 
              CrashReport crash = CrashReport.func_85055_a(e, "Calling Gui method");
              CrashReportCategory category = crash.func_85058_a("Gui");
              MCVer.addDetail(category, "Method", method::toString);
              MCVer.addDetail(category, "Layer", ());
              MCVer.addDetail(category, "ComposedElement", this::toString);
              throw new ReportedException(crash);
            } 
          }); 
    return realProxy;
  }
  
  private <C> C forEachChild(Class<C> ofType) {
    int maxLayer = getMaxLayer();
    final List<C> layers = new ArrayList<>(maxLayer + 1);
    for (int i = maxLayer; i >= 0; i--)
      layers.add((C)super.forEach(i, ofType)); 
    return (C)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ofType }, new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isGetter = method.getName().startsWith("get");
            Object handled = method.getReturnType().equals(boolean.class) ? Boolean.valueOf(false) : null;
            for (C layer : layers) {
              try {
                handled = method.invoke(layer, args);
              } catch (Throwable e) {
                if (e instanceof java.lang.reflect.InvocationTargetException)
                  e = e.getCause(); 
                CrashReport crash = CrashReport.func_85055_a(e, "Calling Gui method");
                CrashReportCategory category = crash.func_85058_a("Gui");
                MCVer.addDetail(category, "Method", method::toString);
                MCVer.addDetail(category, "Layer", () -> "" + layer);
                MCVer.addDetail(category, "ComposedElement", this::toString);
                throw new ReportedException(crash);
              } 
              if (handled != null) {
                if (handled instanceof Boolean) {
                  if (Boolean.TRUE.equals(handled))
                    break; 
                  continue;
                } 
                if (isGetter)
                  return handled; 
              } 
            } 
            return handled;
          }
        });
  }
}