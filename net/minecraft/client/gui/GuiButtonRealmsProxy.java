package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsButton;

public class GuiButtonRealmsProxy extends GuiButton {
  private RealmsButton realmsButton;
  
  public GuiButtonRealmsProxy(RealmsButton realmsButtonIn, int buttonId, int x, int y, String text) {
    super(buttonId, x, y, text);
    this.realmsButton = realmsButtonIn;
  }
  
  public GuiButtonRealmsProxy(RealmsButton realmsButtonIn, int buttonId, int x, int y, String text, int widthIn, int heightIn) {
    super(buttonId, x, y, widthIn, heightIn, text);
    this.realmsButton = realmsButtonIn;
  }
  
  public int getId() {
    return this.id;
  }
  
  public boolean getEnabled() {
    return this.enabled;
  }
  
  public void setEnabled(boolean isEnabled) {
    this.enabled = isEnabled;
  }
  
  public void setText(String text) {
    this.displayString = text;
  }
  
  public int getButtonWidth() {
    return super.getButtonWidth();
  }
  
  public int getPositionY() {
    return this.yPosition;
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (super.mousePressed(mc, mouseX, mouseY))
      this.realmsButton.clicked(mouseX, mouseY); 
    return super.mousePressed(mc, mouseX, mouseY);
  }
  
  public void mouseReleased(int mouseX, int mouseY) {
    this.realmsButton.released(mouseX, mouseY);
  }
  
  public void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    this.realmsButton.renderBg(mouseX, mouseY);
  }
  
  public RealmsButton getRealmsButton() {
    return this.realmsButton;
  }
  
  public int getHoverState(boolean mouseOver) {
    return this.realmsButton.getYImage(mouseOver);
  }
  
  public int func_154312_c(boolean p_154312_1_) {
    return super.getHoverState(p_154312_1_);
  }
  
  public int getHeight() {
    return this.height;
  }
}
