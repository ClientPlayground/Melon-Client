package me.kaimson.melonclient.gui.buttons;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiButton extends GuiButton {
  private Consumer<GuiButton> onPress;
  
  public GuiButton onPress(Consumer<GuiButton> onPress) {
    this.onPress = onPress;
    return this;
  }
  
  public Consumer<GuiButton> onPress() {
    return this.onPress;
  }
  
  public GuiButton(String text) {
    super(-1, 0, 0, text);
  }
  
  public GuiButton(int x, int y, int width, int height, String text) {
    super(-1, x, y, width, height, text);
  }
  
  public GuiButton(int id, int x, int y, int width, int height, String text, Consumer<GuiButton> onPress) {
    super(id, x, y, width, height, text);
    this.xPosition = x;
    this.yPosition = y;
    this.onPress = onPress;
  }
  
  public GuiButton setText(String text) {
    this.displayString = text;
    return this;
  }
  
  public GuiButton set(int x, int y) {
    this.xPosition = x;
    this.yPosition = y;
    return this;
  }
  
  public GuiButton set(int x, int y, int width, int height) {
    this.xPosition = x;
    this.yPosition = y;
    this.width = width;
    this.height = height;
    return this;
  }
  
  public GuiButton setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }
  
  public boolean isEnabled() {
    return this.enabled;
  }
  
  protected void hoverCheck(int mouseX, int mouseY) {
    this.hovered = (mouseX >= this.xPosition && mouseX <= this.xPosition + this.width && mouseY >= this.yPosition && mouseY <= this.yPosition + this.height);
  }
  
  protected void hoverCheck(int x, int y, int mouseX, int mouseY) {
    this.hovered = (mouseX >= x && mouseX <= x + this.width && mouseY >= y && mouseY <= y + this.height);
  }
  
  public boolean isHovered() {
    return this.hovered;
  }
  
  public int getHeight() {
    return this.height;
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (this.enabled && this.hovered && this.visible && this.onPress != null)
      this.onPress.accept(this); 
    return (this.enabled && this.hovered && this.visible);
  }
}
