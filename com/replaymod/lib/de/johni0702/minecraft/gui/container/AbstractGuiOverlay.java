package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.MinecraftGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Loadable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.MouseUtils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public abstract class AbstractGuiOverlay<T extends AbstractGuiOverlay<T>> extends AbstractGuiContainer<T> {
  private final UserInputGuiScreen userInputGuiScreen = new UserInputGuiScreen();
  
  private final EventHandler eventHandler = new EventHandler();
  
  private boolean visible;
  
  private Dimension screenSize;
  
  private boolean mouseVisible;
  
  private boolean closeable = true;
  
  public boolean isVisible() {
    return this.visible;
  }
  
  public void setVisible(boolean visible) {
    if (this.visible != visible) {
      if (visible) {
        ((Loadable)forEach(Loadable.class)).load();
        this.eventHandler.register();
      } else {
        ((Closeable)forEach(Closeable.class)).close();
        this.eventHandler.unregister();
      } 
      updateUserInputGui();
    } 
    this.visible = visible;
  }
  
  public boolean isMouseVisible() {
    return this.mouseVisible;
  }
  
  public void setMouseVisible(boolean mouseVisible) {
    this.mouseVisible = mouseVisible;
    updateUserInputGui();
  }
  
  public boolean isCloseable() {
    return this.closeable;
  }
  
  public void setCloseable(boolean closeable) {
    this.closeable = closeable;
  }
  
  public boolean isAllowUserInput() {
    return this.userInputGuiScreen.field_146291_p;
  }
  
  public void setAllowUserInput(boolean allowUserInput) {
    this.userInputGuiScreen.field_146291_p = allowUserInput;
  }
  
  private void updateUserInputGui() {
    Minecraft mc = getMinecraft();
    if (this.visible)
      if (this.mouseVisible) {
        if (mc.field_71462_r != this.userInputGuiScreen)
          mc.func_147108_a(this.userInputGuiScreen); 
      } else if (mc.field_71462_r == this.userInputGuiScreen) {
        mc.func_147108_a(null);
      }  
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    Dimension dimension;
    if (size == null)
      dimension = this.screenSize; 
    super.layout((ReadableDimension)dimension, renderInfo);
    if (this.mouseVisible && renderInfo.layer == getMaxLayer()) {
      GuiElement tooltip = ((GuiElement)forEach(GuiElement.class)).getTooltip(renderInfo);
      if (tooltip != null)
        tooltip.layout(tooltip.getMinSize(), renderInfo); 
    } 
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
    if (this.mouseVisible && renderInfo.layer == getMaxLayer()) {
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
  
  private class EventHandler extends EventRegistrations {
    private MinecraftGuiRenderer renderer;
    
    private EventHandler() {}
    
    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Text event) {
      float partialTicks = MCVer.getPartialTicks((RenderGameOverlayEvent)event);
      updateRenderer();
      int layers = AbstractGuiOverlay.this.getMaxLayer();
      int mouseX = -1, mouseY = -1;
      if (AbstractGuiOverlay.this.mouseVisible) {
        Point mouse = MouseUtils.getMousePos();
        mouseX = mouse.getX();
        mouseY = mouse.getY();
      } 
      RenderInfo renderInfo = new RenderInfo(partialTicks, mouseX, mouseY, 0);
      int layer;
      for (layer = 0; layer <= layers; layer++)
        AbstractGuiOverlay.this.layout((ReadableDimension)AbstractGuiOverlay.this.screenSize, renderInfo.layer(layer)); 
      for (layer = 0; layer <= layers; layer++)
        AbstractGuiOverlay.this.draw((GuiRenderer)this.renderer, (ReadableDimension)AbstractGuiOverlay.this.screenSize, renderInfo.layer(layer)); 
    }
    
    @SubscribeEvent
    public void tickOverlay(TickEvent.ClientTickEvent event) {
      if (event.phase == TickEvent.Phase.START)
        ((Tickable)AbstractGuiOverlay.this.forEach(Tickable.class)).tick(); 
    }
    
    private void updateRenderer() {
      Minecraft mc = AbstractGuiOverlay.this.getMinecraft();
      ScaledResolution res = MCVer.newScaledResolution(mc);
      if (AbstractGuiOverlay.this.screenSize == null || AbstractGuiOverlay.this
        .screenSize.getWidth() != res.func_78326_a() || AbstractGuiOverlay.this
        .screenSize.getHeight() != res.func_78328_b()) {
        AbstractGuiOverlay.this.screenSize = new Dimension(res.func_78326_a(), res.func_78328_b());
        this.renderer = new MinecraftGuiRenderer(res);
      } 
    }
  }
  
  protected class UserInputGuiScreen extends GuiScreen {
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
      ((Typeable)AbstractGuiOverlay.this.forEach(Typeable.class)).typeKey((ReadablePoint)MouseUtils.getMousePos(), keyCode, typedChar, func_146271_m(), func_146272_n());
      if (AbstractGuiOverlay.this.closeable)
        super.func_73869_a(typedChar, keyCode); 
    }
    
    protected void func_73864_a(int mouseX, int mouseY, int mouseButton) throws IOException {
      ((Clickable)AbstractGuiOverlay.this.forEach(Clickable.class)).mouseClick((ReadablePoint)new Point(mouseX, mouseY), mouseButton);
    }
    
    protected void func_146286_b(int mouseX, int mouseY, int mouseButton) {
      ((Draggable)AbstractGuiOverlay.this.forEach(Draggable.class)).mouseRelease((ReadablePoint)new Point(mouseX, mouseY), mouseButton);
    }
    
    protected void func_146273_a(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
      ((Draggable)AbstractGuiOverlay.this.forEach(Draggable.class)).mouseDrag((ReadablePoint)new Point(mouseX, mouseY), mouseButton, timeSinceLastClick);
    }
    
    public void func_73876_c() {
      ((Tickable)AbstractGuiOverlay.this.forEach(Tickable.class)).tick();
    }
    
    public void func_146274_d() throws IOException {
      super.func_146274_d();
      if (Mouse.hasWheel() && Mouse.getEventDWheel() != 0)
        ((Scrollable)AbstractGuiOverlay.this.forEach(Scrollable.class)).scroll((ReadablePoint)MouseUtils.getMousePos(), Mouse.getEventDWheel()); 
    }
    
    public void func_146281_b() {
      AbstractGuiOverlay.this.mouseVisible = false;
    }
    
    public AbstractGuiOverlay<T> getOverlay() {
      return AbstractGuiOverlay.this;
    }
  }
}
