package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.ComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.org.apache.commons.lang3.tuple.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public abstract class AbstractGuiContainer<T extends AbstractGuiContainer<T>> extends AbstractComposedGuiElement<T> implements GuiContainer<T> {
  private static final Layout DEFAULT_LAYOUT = (Layout)new HorizontalLayout();
  
  private final Map<GuiElement, LayoutData> elements = Maps.newLinkedHashMap();
  
  private Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layedOutElements;
  
  private Layout layout;
  
  private ReadableColor backgroundColor;
  
  public AbstractGuiContainer() {
    this.layout = DEFAULT_LAYOUT;
  }
  
  public AbstractGuiContainer(GuiContainer container) {
    super(container);
    this.layout = DEFAULT_LAYOUT;
  }
  
  public T setLayout(Layout layout) {
    this.layout = layout;
    return getThis();
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
      getContainer().convertFor(this, point, relativeLayer + getLayer()); 
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
    GuiElement[] var3 = elements;
    int var4 = elements.length;
    for (int var5 = 0; var5 < var4; var5++) {
      GuiElement element = var3[var5];
      this.elements.put(element, layoutData);
      element.setContainer(this);
    } 
    return getThis();
  }
  
  public T removeElement(GuiElement element) {
    if (this.elements.remove(element) != null) {
      element.setContainer(null);
      if (this.layedOutElements != null)
        this.layedOutElements.remove(element); 
    } 
    return getThis();
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    super.layout(size, renderInfo);
    if (size != null) {
      try {
        this.layedOutElements = this.layout.layOut(this, size);
      } catch (Exception var8) {
        CrashReport crashReport = CrashReport.makeCrashReport(var8, "Gui Layout");
        renderInfo.addTo(crashReport);
        CrashReportCategory category = crashReport.makeCategory("Gui container details");
        category.addCrashSectionCallable("Container", this::toString);
        Layout var10002 = this.layout;
        category.addCrashSectionCallable("Layout", var10002::toString);
        throw new ReportedException(crashReport);
      } 
      Iterator<Map.Entry> var3 = this.layedOutElements.entrySet().iterator();
      while (true) {
        if (!var3.hasNext())
          return; 
        Map.Entry e = var3.next();
        GuiElement element = (GuiElement)e.getKey();
        if ((element instanceof ComposedGuiElement) ? ((
          (ComposedGuiElement)element).getMaxLayer() < renderInfo.layer) : (
          
          element.getLayer() != renderInfo.layer))
          continue; 
        ReadablePoint ePosition = (ReadablePoint)((Pair)e.getValue()).getLeft();
        ReadableDimension eSize = (ReadableDimension)((Pair)e.getValue()).getRight();
        element.layout(eSize, renderInfo.offsetMouse(ePosition.getX(), ePosition.getY()).layer(renderInfo.getLayer() - element.getLayer()));
      } 
    } 
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    if (this.backgroundColor != null && renderInfo.getLayer() == 0)
      renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), this.backgroundColor); 
    Iterator<Map.Entry> var4 = this.layedOutElements.entrySet().iterator();
    while (true) {
      boolean strict;
      if (!var4.hasNext())
        return; 
      Map.Entry e = var4.next();
      GuiElement element = (GuiElement)e.getKey();
      if (element instanceof ComposedGuiElement) {
        if (((ComposedGuiElement)element).getMaxLayer() >= renderInfo.layer) {
          strict = (renderInfo.layer == 0);
        } else {
          continue;
        } 
      } else if (element.getLayer() == renderInfo.layer) {
        strict = true;
      } else {
        continue;
      } 
      ReadablePoint ePosition = (ReadablePoint)((Pair)e.getValue()).getLeft();
      ReadableDimension eSize = (ReadableDimension)((Pair)e.getValue()).getRight();
      try {
        OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, ePosition, eSize, strict);
        eRenderer.startUsing();
        ((GuiElement)e.getKey()).draw((GuiRenderer)eRenderer, eSize, renderInfo.offsetMouse(ePosition.getX(), ePosition.getY()).layer(renderInfo.getLayer() - ((GuiElement)e.getKey()).getLayer()));
        eRenderer.stopUsing();
      } catch (Exception var13) {
        CrashReport crashReport = CrashReport.makeCrashReport(var13, "Rendering Gui");
        renderInfo.addTo(crashReport);
        CrashReportCategory category = crashReport.makeCategory("Gui container details");
        category.addCrashSectionCallable("Container", this::toString);
        category.addCrashSectionCallable("Width", () -> "" + size.getWidth());
        category.addCrashSectionCallable("Height", () -> "" + size.getHeight());
        Layout var10002 = this.layout;
        category.addCrashSectionCallable("Layout", var10002::toString);
        category = crashReport.makeCategory("Gui element details");
        Map.Entry finalE = e;
        category.addCrashSectionCallable("Element", () -> finalE.getKey().toString());
        category.addCrashSectionCallable("Position", ePosition::toString);
        category.addCrashSectionCallable("Size", eSize::toString);
        if (e.getKey() instanceof GuiContainer) {
          Map.Entry finalE1 = e;
          category.addCrashSectionCallable("Layout", () -> ((GuiContainer)finalE1.getKey()).getLayout().toString());
        } 
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
            return (o1 instanceof Comparable && o2 instanceof Comparable) ? ((Comparable<GuiElement>)o1).compareTo(o2) : (o1.hashCode() - o2.hashCode());
          }
        });
    return getThis();
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
      UnmodifiableIterator var4 = sorted.iterator();
      while (var4.hasNext()) {
        Map.Entry<GuiElement, LayoutData> entry = (Map.Entry<GuiElement, LayoutData>)var4.next();
        this.elements.put(entry.getKey(), entry.getValue());
      } 
    } 
    return getThis();
  }
  
  public ReadableColor getBackgroundColor() {
    return this.backgroundColor;
  }
  
  public T setBackgroundColor(ReadableColor backgroundColor) {
    this.backgroundColor = backgroundColor;
    return getThis();
  }
}
