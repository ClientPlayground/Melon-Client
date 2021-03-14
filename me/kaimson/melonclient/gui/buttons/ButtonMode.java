package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import java.util.Arrays;
import java.util.function.Consumer;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonMode extends GuiButton {
  private final IngameDisplay display;
  
  private final IngameDisplay setting;
  
  private final ChangeButton prev;
  
  private final ChangeButton next;
  
  private int currentMode;
  
  private final int maxMode;
  
  public ButtonMode(int x, int y, int width, int height, IngameDisplay display, IngameDisplay setting) {
    super(x, y, width, height, "");
    this.currentMode = ((Integer)Client.config.getCustoms().getOrDefault(setting, Integer.valueOf(0))).intValue();
    this.maxMode = (int)Arrays.<IngameDisplay>stream(IngameDisplay.values()).filter(s -> (s != setting && s.name().startsWith(setting.name()))).count();
    this.display = display;
    this.setting = setting;
    this.prev = new ChangeButton(x, y, 10, 10, "<", button -> {
          if (button.enabled) {
            newMode(button, false);
            Client.config.getCustoms().put(setting, Integer.valueOf(this.currentMode));
          } 
        });
    this.next = new ChangeButton(x + width - 10, y, 10, 10, ">", button -> {
          if (button.enabled) {
            newMode(button, true);
            Client.config.getCustoms().put(setting, Integer.valueOf(this.currentMode));
          } 
        });
    lockAll();
    updateMode();
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {}
  
  public void render(Minecraft mc, int yPosition, int mouseX, int mouseY) {
    if (this.visible) {
      GlStateManager.pushMatrix();
      hoverCheck(this.xPosition, yPosition, mouseX, mouseY);
      drawRect(this.xPosition, yPosition, this.xPosition + this.width, yPosition + this.height, 2147483647);
      GuiUtils.drawCenteredString(translateModeID(this.currentMode), this.xPosition + this.width / 2, yPosition + (this.height - 8) / 2);
      this.prev.render(mc, yPosition, mouseX, mouseY);
      this.next.render(mc, yPosition, mouseX, mouseY);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
    } 
  }
  
  private String translateModeID(int currentMode) {
    int i = 0;
    for (IngameDisplay mode : this.setting.getSettingMode().modes()) {
      String name = mode.name().replace(this.setting.name(), "").replace("_", "");
      if (i == currentMode)
        return Client.utils.capitalize(name); 
      i++;
    } 
    return null;
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (this.prev.isHovered() && this.prev.enabled) {
      this.prev.onPress().accept(this.prev);
    } else if (this.next.isHovered() && this.next.enabled) {
      this.next.onPress().accept(this.next);
    } 
    return ((this.prev.isHovered() && this.prev.enabled) || (this.next.isHovered() && this.next.enabled));
  }
  
  private static class ChangeButton extends GuiButton {
    public ChangeButton(int x, int y, int width, int height, String text, Consumer<GuiButton> onPress) {
      super(-1, x, y, width, height, text, onPress);
    }
    
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {}
    
    public void render(Minecraft mc, int yPosition, int mouseX, int mouseY) {
      if (this.visible) {
        hoverCheck(this.xPosition, yPosition, mouseX, mouseY);
        GuiUtils.drawCenteredString(this.displayString, this.xPosition + this.width / 2, yPosition + (this.height - 8) / 2, this.enabled ? (new Color(0, 255, 255, 150)).getRGB() : (new Color(50, 50, 50, 150)).getRGB());
      } 
    }
  }
  
  private void newMode(GuiButton button, boolean next) {
    if (next) {
      this.currentMode++;
    } else {
      this.currentMode--;
    } 
    lockAll();
  }
  
  private void reset() {
    this.prev.enabled = true;
    this.next.enabled = true;
  }
  
  private void lockAll() {
    reset();
    this.prev.enabled = (this.currentMode > 0);
    this.next.enabled = (this.currentMode + 1 < this.maxMode);
  }
  
  private void updateMode() {
    this.currentMode = ((Integer)Client.config.getCustoms().getOrDefault(this.setting, Integer.valueOf(0))).intValue();
  }
}
