package me.kaimson.melonclient.gui;

import java.awt.Color;
import java.io.IOException;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.config.AnchorPoint;
import me.kaimson.melonclient.gui.buttons.ButtonFade;
import me.kaimson.melonclient.gui.buttons.ButtonLocation;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import me.kaimson.melonclient.gui.settings.GuiMainSettings;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.BoxUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class GuiHudEditor extends GuiScreen {
  private IngameDisplay dragging;
  
  private IngameDisplay lastHovered;
  
  private ScaledResolution sr;
  
  private int xOffset;
  
  private int yOffset;
  
  private GuiButton fade;
  
  public void setLastHovered(IngameDisplay lastHovered) {
    this.lastHovered = lastHovered;
  }
  
  public void initGui() {
    this.sr = new ScaledResolution(Minecraft.getMinecraft());
    Client.blurShader.onGuiOpen();
    this.buttonList.add(this
        
        .fade = (GuiButton)(new ButtonFade("Settings")).set(this.width / 2 - 50, this.height / 2 - 10, 100, 20).onPress(button -> this.mc.displayGuiScreen((GuiScreen)new GuiMainSettings(this))));
    for (IngameDisplay display : IngameDisplay.values()) {
      if (display.isDisplayItem() && display.isEnabled())
        this.buttonList.add(new ButtonLocation(display)); 
    } 
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    int startColor = (new Color(0, 0, 0, 107)).getRGB();
    int endColor = (new Color(0, 0, 0, 130)).getRGB();
    drawGradientRect(0, 0, this.width, this.height, startColor, endColor);
    if (this.dragging != null && Mouse.isButtonDown(0)) {
      int x = mouseX - BoxUtils.getBoxOffX(this.dragging, this.dragging.getAnchorPoint().getX(this.sr.getScaledWidth()), this.dragging.getWidth());
      int y = mouseY - BoxUtils.getBoxOffY(this.dragging, this.dragging.getAnchorPoint().getY(this.sr.getScaledHeight()), this.dragging.getHeight());
      Client.config.setPosition(this.dragging, (x + this.xOffset), (y + this.yOffset));
      Client.config.setClosestAnchorPoint(this.dragging);
    } 
    this.fade.visible = true;
    for (GuiButton button : this.buttonList) {
      if (button instanceof ButtonLocation && ((ButtonLocation)button).isHovered() && ((ButtonFade)this.fade).isHovered())
        this.fade.visible = false; 
    } 
    super.drawScreen(mouseX, mouseY, partialTicks);
    for (GuiButton button : this.buttonList) {
      if (button instanceof ButtonFade)
        ((ButtonFade)button).render(mouseX, mouseY); 
    } 
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button instanceof ButtonLocation) {
      ButtonLocation loc = (ButtonLocation)button;
      this.dragging = loc.getDisplay();
      this.xOffset = loc.getLastMouseX() - (int)Client.config.getActualX(this.dragging);
      this.yOffset = loc.getLastMouseY() - (int)Client.config.getActualY(this.dragging);
      this.xOffset *= -1;
      this.yOffset *= -1;
      switch (this.dragging.getAnchorPoint()) {
        case TOP_RIGHT:
          this.xOffset += -this.dragging.getWidth();
          break;
        case TOP_CENTER:
          this.xOffset += -this.dragging.getWidth() / 2;
          break;
        case CENTER_LEFT:
          this.yOffset += -this.dragging.getHeight() / 2;
          break;
        case CENTER:
          this.xOffset += -this.dragging.getWidth() / 2;
          this.yOffset += -this.dragging.getHeight() / 2;
          break;
        case CENTER_RIGHT:
          this.xOffset += -this.dragging.getWidth();
          this.yOffset += -this.dragging.getHeight() / 2;
          break;
        case BOTTOM_LEFT:
          this.yOffset += -this.dragging.getHeight();
          break;
        case BOTTOM_CENTER:
          this.xOffset += -this.dragging.getWidth() / 2;
          this.yOffset += -this.dragging.getHeight();
          break;
        case BOTTOM_RIGHT:
          this.xOffset += -this.dragging.getWidth();
          this.yOffset += -this.dragging.getHeight();
          break;
      } 
    } 
  }
  
  protected void mouseReleased(int mouseX, int mouseY, int state) {
    this.dragging = null;
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    if (this.lastHovered != null) {
      int xOffset = 0;
      int yOffset = 0;
      switch (keyCode) {
        case 203:
          xOffset--;
          break;
        case 205:
          xOffset++;
          break;
        case 200:
          yOffset--;
          break;
        case 208:
          yOffset++;
          break;
        case 30:
          xOffset -= 10;
          break;
        case 32:
          xOffset += 10;
          break;
        case 17:
          yOffset -= 10;
          break;
        case 31:
          yOffset += 10;
          break;
      } 
      Client.config.setPosition(this.lastHovered, Client.config.getRelativePosition(this.lastHovered).getX() + xOffset, Client.config.getRelativePosition(this.lastHovered).getY() + yOffset);
    } 
  }
  
  public void onGuiClosed() {
    Client.blurShader.onGuiClose();
    Client.config.saveConfig();
  }
}
