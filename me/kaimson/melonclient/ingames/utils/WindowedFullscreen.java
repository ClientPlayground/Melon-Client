package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.GuiScreenEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import me.kaimson.melonclient.ingames.Ingame;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class WindowedFullscreen extends Ingame {
  private boolean lastFullscreen = false;
  
  public void init() {
    super.init();
    EventHandler.register(this);
  }
  
  @TypeEvent
  private void onTick(TickEvent.ClientTick e) {
    boolean fullscreen = Minecraft.getMinecraft().isFullScreen();
    if (IngameDisplay.WINDOWED_FULLSCREEN.isEnabled()) {
      if (this.lastFullscreen != fullscreen) {
        if (fullscreen) {
          System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
          try {
            Display.setFullscreen(false);
          } catch (LWJGLException lwjglException) {
            lwjglException.printStackTrace();
          } 
        } else {
          System.clearProperty("org.lwjgl.opengl.Window.undecorated");
          try {
            Display.setDisplayMode(new DisplayMode((Minecraft.getMinecraft()).displayWidth, (Minecraft.getMinecraft()).displayHeight));
          } catch (LWJGLException lwjglException) {
            lwjglException.printStackTrace();
          } 
        } 
        this.lastFullscreen = fullscreen;
      } 
    } else if (this.lastFullscreen) {
      System.clearProperty("org.lwjgl.opengl.Window.undecorated");
      try {
        Display.setFullscreen(true);
      } catch (LWJGLException lwjglException) {
        lwjglException.printStackTrace();
      } 
      this.lastFullscreen = fullscreen;
    } 
  }
  
  @TypeEvent
  private void onGuiOpen(GuiScreenEvent.Open e) {}
}
