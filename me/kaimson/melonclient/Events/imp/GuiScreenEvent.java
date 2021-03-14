package me.kaimson.melonclient.Events.imp;

import java.util.List;
import me.kaimson.melonclient.Events.Cancellable;
import me.kaimson.melonclient.Events.Event;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenEvent extends Cancellable {
  public GuiScreen screen;
  
  public GuiScreenEvent(GuiScreen screen) {
    this.screen = screen;
  }
  
  public static class Open extends GuiScreenEvent {
    public Open(GuiScreen screen) {
      super(screen);
    }
  }
  
  public static class Close extends Event {}
  
  public static class DrawScreen extends GuiScreenEvent {
    public int mouseX;
    
    public int mouseY;
    
    public float partialTicks;
    
    public DrawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks) {
      super(screen);
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.partialTicks = partialTicks;
    }
    
    public static class Pre extends DrawScreen {
      public Pre(GuiScreen screen, int mouseX, int mouseY, float partialTicks) {
        super(screen, mouseX, mouseY, partialTicks);
      }
    }
    
    public static class Post extends DrawScreen {
      public Post(GuiScreen screen, int mouseX, int mouseY, float partialTicks) {
        super(screen, mouseX, mouseY, partialTicks);
      }
    }
  }
  
  public static class Render extends GuiScreenEvent {
    public Render(GuiScreen screen) {
      super(screen);
    }
    
    public static class WorldBackground extends Render {
      public int tint;
      
      public WorldBackground(GuiScreen screen, int tint) {
        super(screen);
        this.tint = tint;
      }
    }
  }
  
  public static class Init extends GuiScreenEvent {
    public final List<GuiButton> buttonList;
    
    public Init(GuiScreen screen, List<GuiButton> buttonList) {
      super(screen);
      this.buttonList = buttonList;
    }
    
    public static class PreInit extends Init {
      public PreInit(GuiScreen screen, List<GuiButton> buttonList) {
        super(screen, buttonList);
      }
    }
    
    public static class Post extends Init {
      public Post(GuiScreen screen, List<GuiButton> buttonList) {
        super(screen, buttonList);
      }
    }
  }
  
  public static class ActionPerformed extends GuiScreenEvent {
    public GuiButton button;
    
    public ActionPerformed(GuiScreen screen, GuiButton button) {
      super(screen);
      this.button = button;
    }
    
    public static class Pre extends ActionPerformed {
      public Pre(GuiScreen screen, GuiButton button) {
        super(screen, button);
      }
    }
  }
}
