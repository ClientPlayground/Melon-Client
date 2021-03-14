package me.kaimson.melonclient.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

public class ButtonKeybind extends GuiButton {
  private int keycode;
  
  public int getKeycode() {
    return this.keycode;
  }
  
  public ButtonKeybind(int x, int y, int width, int height) {
    super(-1, x, y, width, height, "");
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    super.drawButton(mc, mouseX, mouseY);
    this.hovered = (mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height);
  }
  
  public boolean keyTyped(char c, int keycode) {
    if (this.displayString.equals("> <")) {
      this.displayString = "";
      this.keycode = keycode;
      this.displayString = Keyboard.getKeyName(keycode);
      return false;
    } 
    return true;
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (this.hovered && 
      !this.displayString.equals("> <"))
      this.displayString = "> <"; 
    return this.hovered;
  }
}
