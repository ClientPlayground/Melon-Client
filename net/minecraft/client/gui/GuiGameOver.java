package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

public class GuiGameOver extends GuiScreen implements GuiYesNoCallback {
  private int enableButtonsTimer;
  
  private boolean field_146346_f = false;
  
  public void initGui() {
    this.buttonList.clear();
    if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
      if (this.mc.isIntegratedServerRunning()) {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.deleteWorld", new Object[0])));
      } else {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.leaveServer", new Object[0])));
      } 
    } else {
      this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn", new Object[0])));
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen", new Object[0])));
      if (this.mc.getSession() == null)
        ((GuiButton)this.buttonList.get(1)).enabled = false; 
    } 
    for (GuiButton guibutton : this.buttonList)
      guibutton.enabled = false; 
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {}
  
  protected void actionPerformed(GuiButton button) throws IOException {
    GuiYesNo guiyesno;
    switch (button.id) {
      case 0:
        this.mc.thePlayer.respawnPlayer();
        this.mc.displayGuiScreen((GuiScreen)null);
        break;
      case 1:
        if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
          this.mc.displayGuiScreen(new GuiMainMenu());
          break;
        } 
        guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm", new Object[0]), "", I18n.format("deathScreen.titleScreen", new Object[0]), I18n.format("deathScreen.respawn", new Object[0]), 0);
        this.mc.displayGuiScreen(guiyesno);
        guiyesno.setButtonDelay(20);
        break;
    } 
  }
  
  public void confirmClicked(boolean result, int id) {
    if (result) {
      this.mc.theWorld.sendQuittingDisconnectingPacket();
      this.mc.loadWorld((WorldClient)null);
      this.mc.displayGuiScreen(new GuiMainMenu());
    } else {
      this.mc.thePlayer.respawnPlayer();
      this.mc.displayGuiScreen((GuiScreen)null);
    } 
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
    GlStateManager.pushMatrix();
    GlStateManager.scale(2.0F, 2.0F, 2.0F);
    boolean flag = this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled();
    String s = flag ? I18n.format("deathScreen.title.hardcore", new Object[0]) : I18n.format("deathScreen.title", new Object[0]);
    drawCenteredString(this.fontRendererObj, s, this.width / 2 / 2, 30, 16777215);
    GlStateManager.popMatrix();
    if (flag)
      drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.hardcoreInfo", new Object[0]), this.width / 2, 144, 16777215); 
    drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.score", new Object[0]) + ": " + EnumChatFormatting.YELLOW + this.mc.thePlayer.getScore(), this.width / 2, 100, 16777215);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
  
  public boolean doesGuiPauseGame() {
    return false;
  }
  
  public void updateScreen() {
    super.updateScreen();
    this.enableButtonsTimer++;
    if (this.enableButtonsTimer == 20)
      for (GuiButton guibutton : this.buttonList)
        guibutton.enabled = true;  
  }
}
