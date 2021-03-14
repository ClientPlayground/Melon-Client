package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

public class ButtonText extends GuiButton {
  private final int width;
  
  private final int height;
  
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final GuiTextField textField;
  
  public GuiTextField getTextField() {
    return this.textField;
  }
  
  public ButtonText(int x, int y, int width, int height) {
    super(-1, x, y, width, height, "");
    this.width = width;
    this.height = height;
    this.textField = new GuiTextField(-1, (Minecraft.getMinecraft()).fontRendererObj, x, y, width, height);
    this.textField.setMaxStringLength(100);
  }
  
  public void drawTextBox(int yPosition, int mouseX, int mouseY) {
    if (this.visible) {
      GuiUtils.drawRoundedRect(this.xPosition, yPosition, this.xPosition + this.width, yPosition + this.height, 2.0F, (new Color(255, 255, 255, 100)).getRGB());
      int i = getTextField().isEnabled() ? (new Color(255, 255, 255, 100)).getRGB() : (new Color(255, 255, 255, 50)).getRGB();
      int j = getTextField().getCursorPosition() - getTextField().getLineScrollOffset();
      int k = getTextField().getSelectionEnd() - getTextField().getLineScrollOffset();
      String s = this.mc.fontRendererObj.trimStringToWidth(getTextField().getText().substring(getTextField().getLineScrollOffset()), getTextField().getWidth());
      boolean flag = (j >= 0 && j <= s.length());
      int l = getTextField().getEnableBackgroundDrawing() ? ((getTextField()).xPosition + 4) : (getTextField()).xPosition;
      int i1 = getTextField().getEnableBackgroundDrawing() ? ((getTextField()).yPosition + (this.height - 8) / 2) : (getTextField()).yPosition;
      int j1 = l;
      if (s.length() > 0) {
        String s1 = flag ? s.substring(0, j) : s;
        GuiUtils.drawString(s1, l, i1, i, true);
      } 
      if (s.length() > 0 && flag && j < s.length())
        j1 = GuiUtils.drawString(s.substring(j), j1, i1, i); 
    } 
  }
}
