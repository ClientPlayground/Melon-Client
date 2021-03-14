package net.minecraft.client.gui;

import java.io.IOException;
import java.net.URI;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiScreenDemo extends GuiScreen {
  private static final Logger logger = LogManager.getLogger();
  
  private static final ResourceLocation field_146348_f = new ResourceLocation("textures/gui/demo_background.png");
  
  public void initGui() {
    this.buttonList.clear();
    int i = -16;
    this.buttonList.add(new GuiButton(1, this.width / 2 - 116, this.height / 2 + 62 + i, 114, 20, I18n.format("demo.help.buy", new Object[0])));
    this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height / 2 + 62 + i, 114, 20, I18n.format("demo.help.later", new Object[0])));
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    switch (button.id) {
      case 1:
        button.enabled = false;
        try {
          Class<?> oclass = Class.forName("java.awt.Desktop");
          Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
          oclass.getMethod("browse", new Class[] { URI.class }).invoke(object, new Object[] { new URI("http://www.minecraft.net/store?source=demo") });
        } catch (Throwable throwable) {
          logger.error("Couldn't open link", throwable);
        } 
        break;
      case 2:
        this.mc.displayGuiScreen((GuiScreen)null);
        this.mc.setIngameFocus();
        break;
    } 
  }
  
  public void updateScreen() {
    super.updateScreen();
  }
  
  public void drawDefaultBackground() {
    super.drawDefaultBackground();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(field_146348_f);
    int i = (this.width - 248) / 2;
    int j = (this.height - 166) / 2;
    drawTexturedModalRect(i, j, 0, 0, 248, 166);
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    int i = (this.width - 248) / 2 + 10;
    int j = (this.height - 166) / 2 + 8;
    this.fontRendererObj.drawString(I18n.format("demo.help.title", new Object[0]), i, j, 2039583);
    j += 12;
    GameSettings gamesettings = this.mc.gameSettings;
    this.fontRendererObj.drawString(I18n.format("demo.help.movementShort", new Object[] { GameSettings.getKeyDisplayString(gamesettings.keyBindForward.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindLeft.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindBack.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindRight.getKeyCode()) }), i, j, 5197647);
    this.fontRendererObj.drawString(I18n.format("demo.help.movementMouse", new Object[0]), i, j + 12, 5197647);
    this.fontRendererObj.drawString(I18n.format("demo.help.jump", new Object[] { GameSettings.getKeyDisplayString(gamesettings.keyBindJump.getKeyCode()) }), i, j + 24, 5197647);
    this.fontRendererObj.drawString(I18n.format("demo.help.inventory", new Object[] { GameSettings.getKeyDisplayString(gamesettings.keyBindInventory.getKeyCode()) }), i, j + 36, 5197647);
    this.fontRendererObj.drawSplitString(I18n.format("demo.help.fullWrapped", new Object[0]), i, j + 68, 218, 2039583);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
}
