package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
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
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import java.io.IOException;
import me.kaimson.melonclient.Events.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.lwjgl.input.Mouse;

public abstract class AbstractGuiOverlay<T extends AbstractGuiOverlay<T>> extends AbstractGuiContainer<T> {
  private final UserInputGuiScreen userInputGuiScreen = new UserInputGuiScreen();
  
  private AbstractGuiEventHandler eventHandler;
  
  private boolean visible;
  
  protected Dimension screenSize;
  
  protected boolean mouseVisible;
  
  private boolean closeable = true;
  
  public boolean isVisible() {
    return this.visible;
  }
  
  public void setVisible(boolean visible) {
    if (this.visible != visible) {
      if (visible) {
        ((Loadable)forEach(Loadable.class)).load();
        EventHandler.register(this.eventHandler = new AbstractGuiEventHandler(this));
      } else {
        ((Closeable)forEach(Closeable.class)).close();
        EventHandler.unregister(this.eventHandler);
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
    return this.userInputGuiScreen.allowUserInput;
  }
  
  public void setAllowUserInput(boolean allowUserInput) {
    this.userInputGuiScreen.allowUserInput = allowUserInput;
  }
  
  private void updateUserInputGui() {
    Minecraft mc = getMinecraft();
    if (this.visible)
      if (this.mouseVisible) {
        if (mc.currentScreen != this.userInputGuiScreen)
          mc.displayGuiScreen(this.userInputGuiScreen); 
      } else if (mc.currentScreen == this.userInputGuiScreen) {
        mc.displayGuiScreen((GuiScreen)null);
      }  
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    Dimension dimension;
    if (size == null)
      dimension = this.screenSize; 
    super.layout((ReadableDimension)dimension, renderInfo);
    if (this.mouseVisible && renderInfo.layer == getMaxLayer()) {
      GuiElement<?> tooltip = ((GuiElement)forEach(GuiElement.class)).getTooltip(renderInfo);
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
        Point position = new Point(x, y);
        try {
          OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, (ReadablePoint)position, tooltipSize);
          tooltip.draw((GuiRenderer)eRenderer, tooltipSize, renderInfo);
        } catch (Exception var12) {
          CrashReport crashReport = CrashReport.makeCrashReport(var12, "Rendering Gui Tooltip");
          renderInfo.addTo(crashReport);
          CrashReportCategory category = crashReport.makeCategory("Gui container details");
          category.addCrashSectionCallable("Container", this::toString);
          category.addCrashSectionCallable("Width", () -> "" + size.getWidth());
          category.addCrashSectionCallable("Height", () -> "" + size.getHeight());
          category = crashReport.makeCategory("Tooltip details");
          category.addCrashSectionCallable("Element", tooltip::toString);
          category.addCrashSectionCallable("Position", position::toString);
          category.addCrashSectionCallable("Size", tooltipSize::toString);
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
  
  public class UserInputGuiScreen extends GuiScreen {
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
      ((Typeable)AbstractGuiOverlay.this.forEach(Typeable.class)).typeKey((ReadablePoint)MouseUtils.getMousePos(), keyCode, typedChar, isCtrlKeyDown(), isShiftKeyDown());
      if (AbstractGuiOverlay.this.closeable)
        super.keyTyped(typedChar, keyCode); 
    }
    
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      ((Clickable)AbstractGuiOverlay.this.forEach(Clickable.class)).mouseClick((ReadablePoint)new Point(mouseX, mouseY), mouseButton);
    }
    
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      ((Draggable)AbstractGuiOverlay.this.forEach(Draggable.class)).mouseRelease((ReadablePoint)new Point(mouseX, mouseY), mouseButton);
    }
    
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
      ((Draggable)AbstractGuiOverlay.this.forEach(Draggable.class)).mouseDrag((ReadablePoint)new Point(mouseX, mouseY), mouseButton, timeSinceLastClick);
    }
    
    public void updateScreen() {
      ((Tickable)AbstractGuiOverlay.this.forEach(Tickable.class)).tick();
    }
    
    public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      if (Mouse.hasWheel() && Mouse.getEventDWheel() != 0)
        ((Scrollable)AbstractGuiOverlay.this.forEach(Scrollable.class)).scroll((ReadablePoint)MouseUtils.getMousePos(), Mouse.getEventDWheel()); 
    }
    
    public void onGuiClosed() {
      AbstractGuiOverlay.this.mouseVisible = false;
    }
    
    public AbstractGuiOverlay<T> getOverlay() {
      return AbstractGuiOverlay.this;
    }
  }
}
