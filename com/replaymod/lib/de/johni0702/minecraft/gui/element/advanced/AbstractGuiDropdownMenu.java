package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.FontRenderer;

public abstract class AbstractGuiDropdownMenu<V, T extends AbstractGuiDropdownMenu<V, T>> extends AbstractComposedGuiElement<T> implements IGuiDropdownMenu<V, T>, Clickable {
  private static final ReadableColor OUTLINE_COLOR = (ReadableColor)new Color(160, 160, 160);
  
  private int selected;
  
  private V[] values;
  
  private boolean opened;
  
  private Consumer<Integer> onSelection;
  
  private GuiPanel dropdown;
  
  private Map<V, IGuiClickable> unmodifiableDropdownEntries;
  
  public int getSelected() {
    return this.selected;
  }
  
  public V[] getValues() {
    return this.values;
  }
  
  public boolean isOpened() {
    return this.opened;
  }
  
  private Function<V, String> toString = Object::toString;
  
  public AbstractGuiDropdownMenu(GuiContainer container) {
    super(container);
  }
  
  public int getMaxLayer() {
    return this.opened ? 1 : 0;
  }
  
  protected ReadableDimension calcMinSize() {
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int maxWidth = 0;
    for (V value : this.values) {
      int width = fontRenderer.func_78256_a(this.toString.apply(value));
      if (width > maxWidth)
        maxWidth = width; 
    } 
    return (ReadableDimension)new Dimension(11 + maxWidth + fontRenderer.field_78288_b, fontRenderer.field_78288_b + 4);
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    super.layout(size, renderInfo);
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    if (renderInfo.layer == 1) {
      Point point = new Point(0, size.getHeight());
      Dimension dimension = new Dimension(size.getWidth(), (fontRenderer.field_78288_b + 5) * this.values.length);
      this.dropdown.layout((ReadableDimension)dimension, renderInfo.offsetMouse(0, point.getY()).layer(0));
    } 
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    if (renderInfo.layer == 0) {
      int width = size.getWidth();
      int height = size.getHeight();
      renderer.drawRect(0, 0, width, height, OUTLINE_COLOR);
      renderer.drawRect(1, 1, width - 2, height - 2, ReadableColor.BLACK);
      renderer.drawRect(width - height, 0, 1, height, OUTLINE_COLOR);
      int base = height - 6;
      int tHeight = base / 2;
      int x = width - 3 - base / 2;
      int y = height / 2 - 2;
      for (int layer = tHeight; layer > 0; layer--)
        renderer.drawRect(x - layer, y + tHeight - layer, layer * 2 - 1, 1, OUTLINE_COLOR); 
      renderer.drawString(3, height / 2 - fontRenderer.field_78288_b / 2, ReadableColor.WHITE, this.toString.apply(getSelectedValue()));
    } else if (renderInfo.layer == 1) {
      Point point = new Point(0, size.getHeight());
      Dimension dimension = new Dimension(size.getWidth(), (fontRenderer.field_78288_b + 5) * this.values.length);
      OffsetGuiRenderer offsetRenderer = new OffsetGuiRenderer(renderer, (ReadablePoint)point, (ReadableDimension)dimension);
      offsetRenderer.startUsing();
      try {
        this.dropdown.draw((GuiRenderer)offsetRenderer, (ReadableDimension)dimension, renderInfo.offsetMouse(0, point.getY()).layer(0));
      } finally {
        offsetRenderer.stopUsing();
      } 
    } 
  }
  
  public T setValues(V... values) {
    this.values = values;
    this
      
      .dropdown = (GuiPanel)(new GuiPanel() {
        public void convertFor(GuiElement element, Point point, int relativeLayer) {
          AbstractGuiDropdownMenu parent = AbstractGuiDropdownMenu.this;
          if (parent.getContainer() != null)
            parent.getContainer().convertFor(parent, point, relativeLayer + 1); 
          point.translate(0, -AbstractGuiDropdownMenu.this.getLastSize().getHeight());
          super.convertFor(element, point, relativeLayer);
        }
      }).setLayout((Layout)new VerticalLayout());
    Map<V, IGuiClickable> dropdownEntries = new LinkedHashMap<>();
    for (V value : values) {
      DropdownEntry entry = new DropdownEntry(value);
      dropdownEntries.put(value, entry);
      this.dropdown.addElements(null, new GuiElement[] { (GuiElement)entry });
    } 
    this.unmodifiableDropdownEntries = Collections.unmodifiableMap(dropdownEntries);
    return (T)getThis();
  }
  
  public T setSelected(int selected) {
    this.selected = selected;
    onSelection(Integer.valueOf(selected));
    return (T)getThis();
  }
  
  public T setSelected(V value) {
    for (int i = 0; i < this.values.length; i++) {
      if (this.values[i].equals(value))
        return setSelected(i); 
    } 
    throw new IllegalArgumentException("The value " + value + " is not in this dropdown menu.");
  }
  
  public V getSelectedValue() {
    return this.values[this.selected];
  }
  
  public T setOpened(boolean opened) {
    this.opened = opened;
    return (T)getThis();
  }
  
  public Collection<GuiElement> getChildren() {
    return this.opened ? (Collection)Collections.singletonList(this.dropdown) : Collections.<GuiElement>emptyList();
  }
  
  public T onSelection(Consumer<Integer> consumer) {
    this.onSelection = consumer;
    return (T)getThis();
  }
  
  public void onSelection(Integer value) {
    if (this.onSelection != null)
      this.onSelection.consume(value); 
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    Point pos = new Point(position);
    if (getContainer() != null)
      getContainer().convertFor(this, pos); 
    if (isEnabled() && 
      isMouseHovering((ReadablePoint)pos)) {
      setOpened(!isOpened());
      return true;
    } 
    return false;
  }
  
  protected boolean isMouseHovering(ReadablePoint pos) {
    return (pos.getX() > 0 && pos.getY() > 0 && pos
      .getX() < getLastSize().getWidth() && pos.getY() < getLastSize().getHeight());
  }
  
  public Map<V, IGuiClickable> getDropdownEntries() {
    return this.unmodifiableDropdownEntries;
  }
  
  public T setToString(Function<V, String> toString) {
    this.toString = toString;
    return (T)getThis();
  }
  
  public AbstractGuiDropdownMenu() {}
  
  private class DropdownEntry extends AbstractGuiClickable<DropdownEntry> {
    private final V value;
    
    public DropdownEntry(V value) {
      this.value = value;
    }
    
    protected DropdownEntry getThis() {
      return this;
    }
    
    protected ReadableDimension calcMinSize() {
      return (ReadableDimension)new Dimension(0, (MCVer.getFontRenderer()).field_78288_b + 5);
    }
    
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
      super.draw(renderer, size, renderInfo);
      int width = size.getWidth();
      int height = size.getHeight();
      renderer.drawRect(0, 0, width, height, AbstractGuiDropdownMenu.OUTLINE_COLOR);
      renderer.drawRect(1, 0, width - 2, height - 1, ReadableColor.BLACK);
      renderer.drawString(3, 2, ReadableColor.WHITE, AbstractGuiDropdownMenu.this.toString.apply(this.value));
    }
    
    public boolean mouseClick(ReadablePoint position, int button) {
      boolean result = super.mouseClick(position, button);
      AbstractGuiDropdownMenu.this.setOpened(false);
      return result;
    }
    
    protected void onClick() {
      AbstractGuiDropdownMenu.this.setSelected(this.value);
    }
  }
}