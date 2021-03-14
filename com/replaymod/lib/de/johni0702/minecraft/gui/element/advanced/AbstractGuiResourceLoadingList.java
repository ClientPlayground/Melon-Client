package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.google.common.base.Supplier;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Loadable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractGuiResourceLoadingList<T extends AbstractGuiResourceLoadingList<T, U>, U extends GuiElement<U> & Comparable<U>> extends AbstractGuiVerticalList<T> implements Tickable, Loadable, Closeable {
  private static final String[] LOADING_TEXT = new String[] { "Ooo", "oOo", "ooO", "oOo" };
  
  private final GuiLabel loadingElement = new GuiLabel();
  
  private final GuiPanel resourcesPanel = (GuiPanel)(new GuiPanel((GuiContainer)getListPanel())).setLayout((Layout)new VerticalLayout());
  
  private final Queue<Runnable> resourcesQueue = new ConcurrentLinkedQueue<>();
  
  private Consumer<Consumer<Supplier<U>>> onLoad;
  
  private Runnable onSelectionChanged;
  
  private Runnable onSelectionDoubleClicked;
  
  private Thread loaderThread;
  
  private int tick;
  
  private Element selected;
  
  private long selectedLastClickTime;
  
  public AbstractGuiResourceLoadingList(GuiContainer container) {
    super(container);
  }
  
  public void tick() {
    this.loadingElement.setText(LOADING_TEXT[this.tick++ / 5 % LOADING_TEXT.length]);
    Runnable resource;
    while ((resource = this.resourcesQueue.poll()) != null)
      resource.run(); 
  }
  
  public void load() {
    if (this.loaderThread != null) {
      this.loaderThread.interrupt();
      try {
        this.loaderThread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } 
    } 
    this.resourcesQueue.clear();
    for (GuiElement element : new ArrayList(this.resourcesPanel.getChildren()))
      this.resourcesPanel.removeElement(element); 
    this.selected = null;
    onSelectionChanged();
    this.loaderThread = new Thread(new Runnable() {
          public void run() {
            try {
              AbstractGuiResourceLoadingList.this.onLoad.consume(new Consumer<Supplier<U>>() {
                    public void consume(final Supplier<U> obj) {
                      AbstractGuiResourceLoadingList.this.resourcesQueue.offer(new Runnable() {
                            public void run() {
                              AbstractGuiResourceLoadingList.this.resourcesPanel.addElements(null, new GuiElement[] { (GuiElement)new AbstractGuiResourceLoadingList.Element((U)this.val$obj.get()) });
                              AbstractGuiResourceLoadingList.this.resourcesPanel.sortElements();
                            }
                          });
                    }
                  });
            } finally {
              AbstractGuiResourceLoadingList.this.resourcesQueue.offer(new Runnable() {
                    public void run() {
                      AbstractGuiResourceLoadingList.this.getListPanel().removeElement((GuiElement)AbstractGuiResourceLoadingList.this.loadingElement);
                    }
                  });
            } 
          }
        });
    getListPanel().addElements((LayoutData)new VerticalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.loadingElement });
    this.loaderThread.start();
  }
  
  public void close() {
    this.loaderThread.interrupt();
  }
  
  public T onLoad(Consumer<Consumer<Supplier<U>>> function) {
    this.onLoad = function;
    return (T)getThis();
  }
  
  public void onSelectionChanged() {
    if (this.onSelectionChanged != null)
      this.onSelectionChanged.run(); 
  }
  
  public void onSelectionDoubleClicked() {
    if (this.onSelectionDoubleClicked != null)
      this.onSelectionDoubleClicked.run(); 
  }
  
  public T onSelectionChanged(Runnable onSelectionChanged) {
    this.onSelectionChanged = onSelectionChanged;
    return (T)getThis();
  }
  
  public T onSelectionDoubleClicked(Runnable onSelectionDoubleClicked) {
    this.onSelectionDoubleClicked = onSelectionDoubleClicked;
    return (T)getThis();
  }
  
  public U getSelected() {
    return (this.selected == null) ? null : this.selected.resource;
  }
  
  public AbstractGuiResourceLoadingList() {}
  
  private class Element extends GuiPanel implements Clickable, Comparable<Element> {
    private final U resource;
    
    public Element(final U resource) {
      this.resource = resource;
      addElements(null, new GuiElement[] { (GuiElement)resource });
      setLayout((Layout)new CustomLayout<GuiPanel>() {
            protected void layout(GuiPanel container, int width, int height) {
              pos(resource, 2, 2);
            }
            
            public ReadableDimension calcMinSize(GuiContainer<?> container) {
              ReadableDimension size = resource.getMinSize();
              return (ReadableDimension)new Dimension(size.getWidth() + 4, size.getHeight() + 4);
            }
          });
    }
    
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
      if (renderInfo.layer == 0 && AbstractGuiResourceLoadingList.this.selected == this) {
        int w = size.getWidth();
        int h = size.getHeight();
        renderer.drawRect(0, 0, w, h, Colors.BLACK);
        renderer.drawRect(0, 0, w, 1, Colors.LIGHT_GRAY);
        renderer.drawRect(0, h - 1, w, 1, Colors.LIGHT_GRAY);
        renderer.drawRect(0, 0, 1, h, Colors.LIGHT_GRAY);
        renderer.drawRect(w - 1, 0, 1, h, Colors.LIGHT_GRAY);
      } 
      super.draw(renderer, size, renderInfo);
    }
    
    public boolean mouseClick(ReadablePoint position, int button) {
      Point point = new Point(position);
      getContainer().convertFor((GuiElement)this, point);
      if (point.getX() > 0 && point.getX() < getLastSize().getWidth() && point
        .getY() > 0 && point.getY() < getLastSize().getHeight()) {
        if (AbstractGuiResourceLoadingList.this.selected != this) {
          AbstractGuiResourceLoadingList.this.selected = this;
          AbstractGuiResourceLoadingList.this.onSelectionChanged();
        } else if (System.currentTimeMillis() - AbstractGuiResourceLoadingList.this.selectedLastClickTime < 250L) {
          AbstractGuiResourceLoadingList.this.onSelectionDoubleClicked();
        } 
        AbstractGuiResourceLoadingList.this.selectedLastClickTime = System.currentTimeMillis();
        return true;
      } 
      return false;
    }
    
    public int compareTo(Element o) {
      return ((Comparable<U>)this.resource).compareTo(o.resource);
    }
  }
}
