package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class VanillaGuiScreen extends GuiScreen implements Draggable, Typeable, Scrollable {
  private final GuiScreen mcScreen;
  
  public static VanillaGuiScreen setup(GuiScreen originalGuiScreen) {
    VanillaGuiScreen gui = new VanillaGuiScreen(originalGuiScreen);
    gui.register();
    return gui;
  }
  
  private final EventHandler eventHandler = new EventHandler();
  
  public VanillaGuiScreen(GuiScreen mcScreen) {
    this.mcScreen = mcScreen;
    this.suppressVanillaKeys = true;
    super.setBackground(AbstractGuiScreen.Background.NONE);
  }
  
  public void register() {
    if (!this.eventHandler.active) {
      this.eventHandler.active = true;
      this.eventHandler.register();
      getSuperMcGui().func_146280_a(MCVer.getMinecraft(), this.mcScreen.field_146294_l, this.mcScreen.field_146295_m);
    } 
  }
  
  public void display() {
    getMinecraft().func_147108_a(this.mcScreen);
    register();
  }
  
  public GuiScreen toMinecraft() {
    return this.mcScreen;
  }
  
  public void setBackground(AbstractGuiScreen.Background background) {
    throw new UnsupportedOperationException("Cannot set background of vanilla gui screen.");
  }
  
  private GuiScreen getSuperMcGui() {
    return super.toMinecraft();
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    this.eventHandler.handled = false;
    return false;
  }
  
  public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
    this.eventHandler.handled = false;
    return false;
  }
  
  public boolean mouseRelease(ReadablePoint position, int button) {
    this.eventHandler.handled = false;
    return false;
  }
  
  public boolean scroll(ReadablePoint mousePosition, int dWheel) {
    this.eventHandler.handled = false;
    return false;
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    this.eventHandler.handled = false;
    return false;
  }
  
  private class EventHandler extends EventRegistrations {
    private boolean active;
    
    private boolean handled;
    
    private EventHandler() {}
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiClosed(GuiOpenEvent event) {
      unregister();
      if (this.active) {
        this.active = false;
        VanillaGuiScreen.this.getSuperMcGui().func_146281_b();
      } 
    }
    
    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {
      VanillaGuiScreen.this.getSuperMcGui().func_73863_a(MCVer.getMouseX(event), MCVer.getMouseY(event), MCVer.getPartialTicks(event));
    }
    
    @SubscribeEvent
    public void tickOverlay(TickEvent.ClientTickEvent event) {
      if (event.phase != TickEvent.Phase.START)
        return; 
      VanillaGuiScreen.this.getSuperMcGui().func_73876_c();
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) throws IOException {
      this.handled = true;
      VanillaGuiScreen.this.getSuperMcGui().func_146274_d();
      if (this.handled)
        event.setCanceled(true); 
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) throws IOException {
      this.handled = true;
      VanillaGuiScreen.this.getSuperMcGui().func_146282_l();
      if (this.handled)
        event.setCanceled(true); 
    }
  }
}
