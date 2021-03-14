package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.MinecraftGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Loadable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.MouseUtils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public abstract class AbstractGuiScreen<T extends AbstractGuiScreen<T>> extends AbstractGuiContainer<T> {
  private final MinecraftGuiScreen wrapped = new MinecraftGuiScreen();
  
  private Dimension screenSize;
  
  private Background background = Background.DEFAULT;
  
  public Background getBackground() {
    return this.background;
  }
  
  public void setBackground(Background background) {
    this.background = background;
  }
  
  private boolean enabledRepeatedKeyEvents = true;
  
  private GuiLabel title;
  
  protected boolean suppressVanillaKeys;
  
  public boolean isEnabledRepeatedKeyEvents() {
    return this.enabledRepeatedKeyEvents;
  }
  
  public GuiLabel getTitle() {
    return this.title;
  }
  
  public void setTitle(GuiLabel title) {
    this.title = title;
  }
  
  public GuiScreen toMinecraft() {
    return this.wrapped;
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    Dimension dimension;
    if (size == null)
      dimension = this.screenSize; 
    if (renderInfo.layer == 0 && 
      this.title != null)
      this.title.layout(this.title.getMinSize(), renderInfo); 
    super.layout((ReadableDimension)dimension, renderInfo);
    if (renderInfo.layer == getMaxLayer()) {
      GuiElement tooltip = ((GuiElement)forEach(GuiElement.class)).getTooltip(renderInfo);
      if (tooltip != null)
        tooltip.layout(tooltip.getMinSize(), renderInfo); 
    } 
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    if (renderInfo.layer == 0) {
      int top;
      int bottom;
      switch (this.background) {
        case DEFAULT:
          this.wrapped.func_146276_q_();
          break;
        case TRANSPARENT:
          top = -1072689136;
          bottom = -804253680;
          renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), top, top, bottom, bottom);
          break;
        case DIRT:
          this.wrapped.func_146278_c(0);
          break;
      } 
      if (this.title != null) {
        ReadableDimension titleSize = this.title.getMinSize();
        int x = this.screenSize.getWidth() / 2 - titleSize.getWidth() / 2;
        OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, (ReadablePoint)new Point(x, 10), (ReadableDimension)new Dimension(0, 0));
        this.title.draw((GuiRenderer)eRenderer, titleSize, renderInfo);
      } 
    } 
    super.draw(renderer, size, renderInfo);
    if (renderInfo.layer == getMaxLayer()) {
      GuiElement tooltip = ((GuiElement)forEach(GuiElement.class)).getTooltip(renderInfo);
      if (tooltip != null) {
        int x, y;
        ReadableDimension tooltipSize = tooltip.getMinSize();
        if (renderInfo.mouseX + 8 + tooltipSize.getWidth() < this.screenSize.getWidth()) {
          x = renderInfo.mouseX + 8;
        } else {
          x = this.screenSize.getWidth() - tooltipSize.getWidth() - 1;
        } 
        if (renderInfo.mouseY + 8 + tooltipSize.getHeight() < this.screenSize.getHeight()) {
          y = renderInfo.mouseY + 8;
        } else {
          y = this.screenSize.getHeight() - tooltipSize.getHeight() - 1;
        } 
        Point point = new Point(x, y);
        try {
          OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, (ReadablePoint)point, tooltipSize);
          tooltip.draw((GuiRenderer)eRenderer, tooltipSize, renderInfo);
        } catch (Exception ex) {
          CrashReport crashReport = CrashReport.func_85055_a(ex, "Rendering Gui Tooltip");
          renderInfo.addTo(crashReport);
          CrashReportCategory category = crashReport.func_85058_a("Gui container details");
          MCVer.addDetail(category, "Container", this::toString);
          MCVer.addDetail(category, "Width", () -> "" + size.getWidth());
          MCVer.addDetail(category, "Height", () -> "" + size.getHeight());
          category = crashReport.func_85058_a("Tooltip details");
          MCVer.addDetail(category, "Element", tooltip::toString);
          MCVer.addDetail(category, "Position", point::toString);
          MCVer.addDetail(category, "Size", tooltipSize::toString);
          throw new ReportedException(crashReport);
        } 
      } 
    } 
  }
  
  public ReadableDimension getMinSize() {
    return (ReadableDimension)this.screenSize;
  }
  
  public ReadableDimension getMaxSize() {
    return (ReadableDimension)this.screenSize;
  }
  
  public void setEnabledRepeatedKeyEvents(boolean enableRepeatKeyEvents) {
    this.enabledRepeatedKeyEvents = enableRepeatKeyEvents;
    if (this.wrapped.active)
      Keyboard.enableRepeatEvents(enableRepeatKeyEvents); 
  }
  
  public void display() {
    getMinecraft().func_147108_a(toMinecraft());
  }
  
  protected class MinecraftGuiScreen extends GuiScreen {
    private MinecraftGuiRenderer renderer;
    
    private boolean active;
    
    public void func_73863_a(int mouseX, int mouseY, float partialTicks) {
      GlStateManager.func_179090_x();
      GlStateManager.func_179098_w();
      int layers = AbstractGuiScreen.this.getMaxLayer();
      RenderInfo renderInfo = new RenderInfo(partialTicks, mouseX, mouseY, 0);
      int layer;
      for (layer = 0; layer <= layers; layer++)
        AbstractGuiScreen.this.layout((ReadableDimension)AbstractGuiScreen.this.screenSize, renderInfo.layer(layer)); 
      for (layer = 0; layer <= layers; layer++)
        AbstractGuiScreen.this.draw((GuiRenderer)this.renderer, (ReadableDimension)AbstractGuiScreen.this.screenSize, renderInfo.layer(layer)); 
    }
    
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
      if (!((Typeable)AbstractGuiScreen.this.forEach(Typeable.class)).typeKey(
          (ReadablePoint)MouseUtils.getMousePos(), keyCode, typedChar, func_146271_m(), func_146272_n())) {
        if (AbstractGuiScreen.this.suppressVanillaKeys)
          return; 
        super.func_73869_a(typedChar, keyCode);
      } 
    }
    
    protected void func_73864_a(int mouseX, int mouseY, int mouseButton) throws IOException {
      ((Clickable)AbstractGuiScreen.this.forEach(Clickable.class)).mouseClick((ReadablePoint)new Point(mouseX, mouseY), mouseButton);
    }
    
    protected void func_146286_b(int mouseX, int mouseY, int mouseButton) {
      ((Draggable)AbstractGuiScreen.this.forEach(Draggable.class)).mouseRelease((ReadablePoint)new Point(mouseX, mouseY), mouseButton);
    }
    
    protected void func_146273_a(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
      ((Draggable)AbstractGuiScreen.this.forEach(Draggable.class)).mouseDrag((ReadablePoint)new Point(mouseX, mouseY), mouseButton, timeSinceLastClick);
    }
    
    public void func_73876_c() {
      ((Tickable)AbstractGuiScreen.this.forEach(Tickable.class)).tick();
    }
    
    public void func_146274_d() throws IOException {
      super.func_146274_d();
      if (Mouse.hasWheel() && Mouse.getEventDWheel() != 0)
        ((Scrollable)AbstractGuiScreen.this.forEach(Scrollable.class)).scroll((ReadablePoint)MouseUtils.getMousePos(), Mouse.getEventDWheel()); 
    }
    
    public void func_146281_b() {
      ((Closeable)AbstractGuiScreen.this.forEach(Closeable.class)).close();
      this.active = false;
      if (AbstractGuiScreen.this.enabledRepeatedKeyEvents)
        Keyboard.enableRepeatEvents(false); 
    }
    
    public void func_73866_w_() {
      this.active = false;
      if (AbstractGuiScreen.this.enabledRepeatedKeyEvents)
        Keyboard.enableRepeatEvents(true); 
      AbstractGuiScreen.this.screenSize = new Dimension(this.field_146294_l, this.field_146295_m);
      this.renderer = new MinecraftGuiRenderer(MCVer.newScaledResolution(this.field_146297_k));
      ((Loadable)AbstractGuiScreen.this.forEach(Loadable.class)).load();
    }
    
    public T getWrapper() {
      return (T)AbstractGuiScreen.this.getThis();
    }
  }
  
  public enum Background {
    NONE, DEFAULT, TRANSPARENT, DIRT;
  }
}
