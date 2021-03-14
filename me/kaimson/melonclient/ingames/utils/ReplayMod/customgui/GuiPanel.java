package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuiPanel extends AbstractGuiContainer<GuiPanel> {
  public GuiPanel() {}
  
  public GuiPanel(GuiContainer container) {
    super(container);
  }
  
  GuiPanel(Layout layout, int width, int height, Map<GuiElement, LayoutData> withElements) {
    setLayout(layout);
    if (width != 0 || height != 0)
      setSize(width, height); 
    Iterator<Map.Entry<GuiElement, LayoutData>> var5 = withElements.entrySet().iterator();
    while (var5.hasNext()) {
      Map.Entry<GuiElement, LayoutData> e = var5.next();
      addElements(e.getValue(), new GuiElement[] { e.getKey() });
    } 
  }
  
  protected GuiPanel getThis() {
    return this;
  }
  
  public static GuiPanelBuilder builder() {
    return new GuiPanelBuilder();
  }
  
  public static class GuiPanelBuilder {
    private Layout layout;
    
    private int width;
    
    private int height;
    
    private ArrayList<GuiElement<?>> withElements$key;
    
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
    
    public GuiPanelBuilder with(GuiElement<?> withKey, LayoutData withValue) {
      if (this.withElements$key == null) {
        this.withElements$key = new ArrayList<>();
        this.withElements$value = new ArrayList<>();
      } 
      this.withElements$key.add(withKey);
      this.withElements$value.add(withValue);
      return this;
    }
    
    public GuiPanelBuilder withElements(Map<? extends GuiElement<?>, ? extends LayoutData> withElements) {
      if (withElements == null)
        throw new NullPointerException("withElements cannot be null"); 
      if (this.withElements$key == null) {
        this.withElements$key = new ArrayList<>();
        this.withElements$value = new ArrayList<>();
      } 
      Iterator<Map.Entry<? extends GuiElement, ? extends LayoutData>> var2 = withElements.entrySet().iterator();
      while (var2.hasNext()) {
        Map.Entry<? extends GuiElement, ? extends LayoutData> $lombokEntry = var2.next();
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
      Map<?, ?> map2, map1;
      switch ((this.withElements$key == null) ? 0 : this.withElements$key.size()) {
        case false:
          map2 = Collections.emptyMap();
          return new GuiPanel(this.layout, this.width, this.height, (Map)map2);
        case true:
          map1 = Collections.singletonMap(this.withElements$key.get(0), this.withElements$value.get(0));
          return new GuiPanel(this.layout, this.width, this.height, (Map)map1);
      } 
      Map<GuiElement, LayoutData> withElements2 = new LinkedHashMap<>((this.withElements$key.size() < 1073741824) ? (1 + this.withElements$key.size() + (this.withElements$key.size() - 3) / 3) : Integer.MAX_VALUE);
      for (int $i = 0; $i < this.withElements$key.size(); $i++)
        withElements2.put(this.withElements$key.get($i), this.withElements$value.get($i)); 
      Map<GuiElement, LayoutData> withElements = Collections.unmodifiableMap(withElements2);
      return new GuiPanel(this.layout, this.width, this.height, withElements);
    }
    
    public String toString() {
      return "GuiPanel.GuiPanelBuilder(layout=" + this.layout + ", width=" + this.width + ", height=" + this.height + ", withElements$key=" + this.withElements$key + ", withElements$value=" + this.withElements$value + ")";
    }
  }
}
