package me.kaimson.melonclient.gui;

import java.io.IOException;
import me.kaimson.melonclient.gui.buttons.ButtonText;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreen extends GuiScreen {
  protected GuiScrolling scroll;
  
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    if (this.scroll != null)
      this.scroll.handleMouseInput(); 
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    for (GuiButton button : this.buttonList) {
      if (button instanceof ButtonText)
        ((ButtonText)button).getTextField().textboxKeyTyped(typedChar, keyCode); 
    } 
  }
  
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    for (GuiButton button : this.buttonList) {
      if (button instanceof ButtonText)
        ((ButtonText)button).getTextField().mouseClicked(mouseX, mouseY, mouseButton); 
    } 
  }
}
