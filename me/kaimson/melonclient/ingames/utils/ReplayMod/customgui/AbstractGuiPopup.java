package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public abstract class AbstractGuiPopup<T extends AbstractGuiPopup<T>> extends AbstractGuiContainer<T> {
  private final GuiPanel popupContainer = (new GuiPanel(this) {
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
          int x;
          for (x = 5; x < w - 5; x += 5) {
            int y = Math.min(5, w - 5 - x);
            renderer.drawTexturedRect(x, 0, 6, 39, y, 5);
            renderer.drawTexturedRect(x, h - 5, 6, 51, y, 5);
          } 
          for (x = 5; x < h - 5; x += 5) {
            int y = Math.min(5, h - 5 - x);
            renderer.drawTexturedRect(0, x, 0, 45, 5, y);
            renderer.drawTexturedRect(w - 5, x, 12, 45, 5, y);
          } 
          for (x = 5; x < w - 5; x += 5) {
            for (int y = 5; y < h - 5; y += 5) {
              int rx = Math.min(5, w - 5 - x);
              int ry = Math.min(5, h - 5 - y);
              renderer.drawTexturedRect(x, y, 6, 45, rx, ry);
            } 
          } 
        } 
        super.draw(renderer, size, renderInfo);
      }
    }).setLayout((Layout)new CustomLayout<GuiPanel>() {
        protected void layout(GuiPanel container, int width, int height) {
          pos(AbstractGuiPopup.this.popup, 10, 10);
        }
        
        public ReadableDimension calcMinSize(GuiContainer<?> container) {
          ReadableDimension size = AbstractGuiPopup.this.popup.calcMinSize();
          return (ReadableDimension)new Dimension(size.getWidth() + 20, size.getHeight() + 20);
        }
      });
  
  protected final GuiPanel popup;
  
  private int layer;
  
  private Layout originalLayout;
  
  private boolean wasAllowUserInput;
  
  private boolean wasMouseVisible;
  
  private final GuiContainer container;
  
  public AbstractGuiPopup(GuiContainer container) {
    this.popup = new GuiPanel(this.popupContainer);
    setLayout((Layout)new CustomLayout<T>() {
          protected void layout(T container, int width, int height) {
            pos(AbstractGuiPopup.this.popupContainer, width / 2 - width(AbstractGuiPopup.this.popupContainer) / 2, (Minecraft.getMinecraft()).displayHeight / (Minecraft.getMinecraft()).gameSettings.guiScale / 2 - height(AbstractGuiPopup.this.popupContainer) / 2);
          }
        });
    while (container.getContainer() != null)
      container = container.getContainer(); 
    this.container = container;
  }
  
  protected void open() {
    setLayer(this.container.getMaxLayer() + 1);
    this.container.addElements(null, new GuiElement[] { this });
    this.container.setLayout((Layout)new CustomLayout(this.originalLayout = this.container.getLayout()) {
          protected void layout(GuiContainer container, int width, int height) {
            pos(AbstractGuiPopup.this, 0, 0);
            size(AbstractGuiPopup.this, width, height);
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
    getContainer().removeElement(this);
    if (this.container instanceof AbstractGuiOverlay) {
      AbstractGuiOverlay overlay = (AbstractGuiOverlay)this.container;
      overlay.setAllowUserInput(this.wasAllowUserInput);
      overlay.setMouseVisible(this.wasMouseVisible);
    } 
  }
  
  public T setLayer(int layer) {
    this.layer = layer;
    return getThis();
  }
  
  public int getLayer() {
    return this.layer;
  }
  
  public <C> C forEach(int layer, Class<C> ofType) {
    C realProxy = (C)super.forEach(layer, ofType);
    return (layer >= 0) ? (C)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ofType }, (proxy, method, args) -> {
          try {
            if (method.getReturnType().equals(boolean.class)) {
              method.invoke(forEachChild(ofType), args);
              return Boolean.valueOf(true);
            } 
            return method.invoke(realProxy, args);
          } catch (Throwable var10) {
            Throwable e = var10;
            if (var10 instanceof java.lang.reflect.InvocationTargetException)
              e = var10.getCause(); 
            CrashReport crash = CrashReport.makeCrashReport(e, "Calling Gui method");
            CrashReportCategory category = crash.makeCategory("Gui");
            category.addCrashSectionCallable("Method", method::toString);
            category.addCrashSectionCallable("Layer", ());
            category.addCrashSectionCallable("ComposedElement", this::toString);
            throw new ReportedException(crash);
          } 
        }) : realProxy;
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
            Iterator var6 = layers.iterator();
            while (var6.hasNext()) {
              Object layer = var6.next();
              try {
                handled = method.invoke(layer, args);
              } catch (Throwable var11) {
                Throwable e = var11;
                if (var11 instanceof java.lang.reflect.InvocationTargetException)
                  e = var11.getCause(); 
                CrashReport crash = CrashReport.makeCrashReport(e, "Calling Gui method");
                CrashReportCategory category = crash.makeCategory("Gui");
                category.addCrashSectionCallable("Method", method::toString);
                category.addCrashSectionCallable("Layer", () -> "" + layer);
                category.addCrashSectionCallable("ComposedElement", this::toString);
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
