package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuiPanel extends AbstractGuiContainer<GuiPanel> {
  public GuiPanel() {}
  
  public GuiPanel(GuiContainer container) {
    super(container);
  }
  
  public static GuiPanelBuilder builder() {
    return new GuiPanelBuilder();
  }
  
  public static class GuiPanelBuilder {
    private Layout layout;
    
    private int width;
    
    private int height;
    
    private ArrayList<GuiElement> withElements$key;
    
    private ArrayList<LayoutData> withElements$value;
    
    public GuiPanelBuilder layout(Layout layout) {
      this.layout = layout;
      return this;
    }
    
    public GuiPanelBuilder width(int width) {
      this.width = width;
      return this;
    }
    
    public GuiPanelBuilder height(int height) {
      this.height = height;
      return this;
    }
    
    public GuiPanelBuilder with(GuiElement withKey, LayoutData withValue) {
      if (this.withElements$key == null) {
        this.withElements$key = new ArrayList<>();
        this.withElements$value = new ArrayList<>();
      } 
      this.withElements$key.add(withKey);
      this.withElements$value.add(withValue);
      return this;
    }
    
    public GuiPanelBuilder withElements(Map<? extends GuiElement, ? extends LayoutData> withElements) {
      if (withElements == null)
        throw new NullPointerException("withElements cannot be null"); 
      if (this.withElements$key == null) {
        this.withElements$key = new ArrayList<>();
        this.withElements$value = new ArrayList<>();
      } 
      for (Map.Entry<? extends GuiElement, ? extends LayoutData> $lombokEntry : withElements.entrySet()) {
        this.withElements$key.add($lombokEntry.getKey());
        this.withElements$value.add($lombokEntry.getValue());
      } 
      return this;
    }
    
    public GuiPanelBuilder clearWithElements() {
      if (this.withElements$key != null) {
        this.withElements$key.clear();
        this.withElements$value.clear();
      } 
      return this;
    }
    
    public GuiPanel build() {
      switch ((this.withElements$key == null) ? 0 : this.withElements$key.size()) {
        case false:
          withElements = Collections.emptyMap();
          return new GuiPanel(this.layout, this.width, this.height, withElements);
        case true:
          withElements = Collections.singletonMap(this.withElements$key.get(0), this.withElements$value.get(0));
          return new GuiPanel(this.layout, this.width, this.height, withElements);
      } 
      Map<GuiElement, LayoutData> withElements;
      int $i;
      for (withElements = new LinkedHashMap<>((this.withElements$key.size() < 1073741824) ? (1 + this.withElements$key.size() + (this.withElements$key.size() - 3) / 3) : Integer.MAX_VALUE), $i = 0; $i < this.withElements$key.size(); ) {
        withElements.put(this.withElements$key.get($i), this.withElements$value.get($i));
        $i++;
      } 
      withElements = Collections.unmodifiableMap(withElements);
      return new GuiPanel(this.layout, this.width, this.height, withElements);
    }
    
    public String toString() {
      return "GuiPanel.GuiPanelBuilder(layout=" + this.layout + ", width=" + this.width + ", height=" + this.height + ", withElements$key=" + this.withElements$key + ", withElements$value=" + this.withElements$value + ")";
    }
  }
  
  GuiPanel(Layout layout, int width, int height, Map<GuiElement, LayoutData> withElements) {
    setLayout(layout);
    if (width != 0 || height != 0)
      setSize(width, height); 
    for (Map.Entry<GuiElement, LayoutData> e : withElements.entrySet()) {
      addElements(e.getValue(), new GuiElement[] { e.getKey() });
    } 
  }
  
  protected GuiPanel getThis() {
    return this;
  }
}
