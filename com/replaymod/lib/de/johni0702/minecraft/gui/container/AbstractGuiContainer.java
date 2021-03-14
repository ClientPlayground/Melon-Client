package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.ComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import com.replaymod.lib.org.apache.commons.lang3.tuple.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public abstract class AbstractGuiContainer<T extends AbstractGuiContainer<T>> extends AbstractComposedGuiElement<T> implements GuiContainer<T> {
  private static final Layout DEFAULT_LAYOUT = (Layout)new HorizontalLayout();
  
  private final Map<GuiElement, LayoutData> elements = new LinkedHashMap<>();
  
  private Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layedOutElements;
  
  private Layout layout = DEFAULT_LAYOUT;
  
  private ReadableColor backgroundColor;
  
  public AbstractGuiContainer(GuiContainer container) {
    super(container);
  }
  
  public T setLayout(Layout layout) {
    this.layout = layout;
    return (T)getThis();
  }
  
  public Layout getLayout() {
    return this.layout;
  }
  
  public void convertFor(GuiElement element, Point point) {
    convertFor(element, point, element.getLayer());
  }
  
  public void convertFor(GuiElement element, Point point, int relativeLayer) {
    if (this.layedOutElements == null || !this.layedOutElements.containsKey(element))
      layout((ReadableDimension)null, new RenderInfo(0.0F, 0, 0, relativeLayer)); 
    Preconditions.checkState((this.layedOutElements != null), "Cannot convert position unless rendered at least once.");
    Pair<ReadablePoint, ReadableDimension> pair = this.layedOutElements.get(element);
    Preconditions.checkState((pair != null), "Element " + element + " not part of " + this);
    ReadablePoint pos = (ReadablePoint)pair.getKey();
    if (getContainer() != null)
      getContainer().convertFor((GuiElement)this, point, relativeLayer + getLayer()); 
    point.translate(-pos.getX(), -pos.getY());
  }
  
  public Collection<GuiElement> getChildren() {
    return Collections.unmodifiableCollection(this.elements.keySet());
  }
  
  public Map<GuiElement, LayoutData> getElements() {
    return Collections.unmodifiableMap(this.elements);
  }
  
  public T addElements(LayoutData layoutData, GuiElement... elements) {
    if (layoutData == null)
      layoutData = LayoutData.NONE; 
    for (GuiElement element : elements) {
      this.elements.put(element, layoutData);
      element.setContainer(this);
    } 
    return (T)getThis();
  }
  
  public T removeElement(GuiElement element) {
    if (this.elements.remove(element) != null) {
      element.setContainer(null);
      if (this.layedOutElements != null)
        this.layedOutElements.remove(element); 
    } 
    return (T)getThis();
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    super.layout(size, renderInfo);
    if (size == null)
      return; 
    try {
      this.layedOutElements = this.layout.layOut(this, size);
    } catch (Exception ex) {
      CrashReport crashReport = CrashReport.func_85055_a(ex, "Gui Layout");
      renderInfo.addTo(crashReport);
      CrashReportCategory category = crashReport.func_85058_a("Gui container details");
      MCVer.addDetail(category, "Container", this::toString);
      MCVer.addDetail(category, "Layout", this.layout::toString);
      throw new ReportedException(crashReport);
    } 
    for (Map.Entry<GuiElement, Pair<ReadablePoint, ReadableDimension>> e : this.layedOutElements.entrySet()) {
      GuiElement element = e.getKey();
      if ((element instanceof ComposedGuiElement) ? ((
        (ComposedGuiElement)element).getMaxLayer() < renderInfo.layer) : (
        
        element.getLayer() != renderInfo.layer))
        continue; 
      ReadablePoint ePosition = (ReadablePoint)((Pair)e.getValue()).getLeft();
      ReadableDimension eSize = (ReadableDimension)((Pair)e.getValue()).getRight();
      element.layout(eSize, renderInfo.offsetMouse(ePosition.getX(), ePosition.getY())
          .layer(renderInfo.getLayer() - element.getLayer()));
    } 
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    if (this.backgroundColor != null && renderInfo.getLayer() == 0)
      renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), this.backgroundColor); 
    for (Map.Entry<GuiElement, Pair<ReadablePoint, ReadableDimension>> e : this.layedOutElements.entrySet()) {
      boolean strict;
      GuiElement element = e.getKey();
      if (element instanceof ComposedGuiElement) {
        if (((ComposedGuiElement)element).getMaxLayer() < renderInfo.layer)
          continue; 
        strict = (renderInfo.layer == 0);
      } else {
        if (element.getLayer() != renderInfo.layer)
          continue; 
        strict = true;
      } 
      ReadablePoint ePosition = (ReadablePoint)((Pair)e.getValue()).getLeft();
      ReadableDimension eSize = (ReadableDimension)((Pair)e.getValue()).getRight();
      try {
        OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, ePosition, eSize, strict);
        eRenderer.startUsing();
        ((GuiElement)e.getKey()).draw((GuiRenderer)eRenderer, eSize, renderInfo.offsetMouse(ePosition.getX(), ePosition.getY())
            .layer(renderInfo.getLayer() - ((GuiElement)e.getKey()).getLayer()));
        eRenderer.stopUsing();
      } catch (Exception ex) {
        CrashReport crashReport = CrashReport.func_85055_a(ex, "Rendering Gui");
        renderInfo.addTo(crashReport);
        CrashReportCategory category = crashReport.func_85058_a("Gui container details");
        MCVer.addDetail(category, "Container", this::toString);
        MCVer.addDetail(category, "Width", () -> "" + size.getWidth());
        MCVer.addDetail(category, "Height", () -> "" + size.getHeight());
        MCVer.addDetail(category, "Layout", this.layout::toString);
        category = crashReport.func_85058_a("Gui element details");
        MCVer.addDetail(category, "Element", () -> ((GuiElement)e.getKey()).toString());
        MCVer.addDetail(category, "Position", ePosition::toString);
        MCVer.addDetail(category, "Size", eSize::toString);
        if (e.getKey() instanceof GuiContainer)
          MCVer.addDetail(category, "Layout", () -> ((GuiContainer)e.getKey()).getLayout().toString()); 
        throw new ReportedException(crashReport);
      } 
    } 
  }
  
  public ReadableDimension calcMinSize() {
    return this.layout.calcMinSize(this);
  }
  
  public T sortElements() {
    sortElements(new Comparator<GuiElement>() {
          public int compare(GuiElement o1, GuiElement o2) {
            if (o1 instanceof Comparable && o2 instanceof Comparable)
              return ((Comparable<GuiElement>)o1).compareTo(o2); 
            return o1.hashCode() - o2.hashCode();
          }
        });
    return (T)getThis();
  }
  
  public T sortElements(final Comparator<GuiElement> comparator) {
    Ordering<Map.Entry<GuiElement, LayoutData>> ordering = new Ordering<Map.Entry<GuiElement, LayoutData>>() {
        public int compare(Map.Entry<GuiElement, LayoutData> left, Map.Entry<GuiElement, LayoutData> right) {
          return comparator.compare(left.getKey(), right.getKey());
        }
      };
    if (!ordering.isOrdered(this.elements.entrySet())) {
      ImmutableList<Map.Entry<GuiElement, LayoutData>> sorted = ordering.immutableSortedCopy(this.elements.entrySet());
      this.elements.clear();
      for (UnmodifiableIterator<Map.Entry<GuiElement, LayoutData>> unmodifiableIterator = sorted.iterator(); unmodifiableIterator.hasNext(); ) {
        Map.Entry<GuiElement, LayoutData> entry = unmodifiableIterator.next();
        this.elements.put(entry.getKey(), entry.getValue());
      } 
    } 
    return (T)getThis();
  }
  
  public ReadableColor getBackgroundColor() {
    return this.backgroundColor;
  }
  
  public T setBackgroundColor(ReadableColor backgroundColor) {
    this.backgroundColor = backgroundColor;
    return (T)getThis();
  }
  
  public AbstractGuiContainer() {}
}
