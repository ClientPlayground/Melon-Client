package me.kaimson.melonclient.ingames.render;

import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.config.AnchorPoint;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.ingames.utils.ArmorStatus;
import me.kaimson.melonclient.ingames.utils.Clock;
import me.kaimson.melonclient.ingames.utils.Combo;
import me.kaimson.melonclient.ingames.utils.PotionEffects;
import me.kaimson.melonclient.ingames.utils.Reach;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import me.kaimson.melonclient.ingames.utils.Scoreboard;
import me.kaimson.melonclient.ingames.utils.keystrokes.Keystrokes;
import me.kaimson.melonclient.util.BoxUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class RenderManager {
  private final ArmorStatus armorStatus = new ArmorStatus();
  
  private final Scoreboard scoreboard = new Scoreboard();
  
  private final PotionEffects potionEffects = new PotionEffects();
  
  private final Keystrokes keystrokes = new Keystrokes();
  
  private final Clock clock = new Clock();
  
  private final Reach reach = new Reach();
  
  private final Combo combo = new Combo();
  
  public void onRenderTick() {
    if ((Minecraft.getMinecraft()).currentScreen instanceof me.kaimson.melonclient.gui.GuiHudEditor || (Minecraft.getMinecraft()).gameSettings.showDebugInfo || ReplayModReplay.getInstance().getReplayHandler() != null)
      return; 
    for (IngameDisplay display : Client.config.getEnabledIngame()) {
      if (display.isDisplayItem())
        display.render((int)Client.config.getActualX(display), (int)Client.config.getActualY(display)); 
    } 
  }
  
  public void renderIngame(IngameDisplay display, int x, int y) {
    renderIngame(display.getMessage().getTranslated(new String[] { display.getMessage().getMemberName() }, ), display, x, y);
  }
  
  public void renderIngame(String text, IngameDisplay display, int x, int y) {
    float scale = display.getScale();
    x = (int)(x / scale);
    y = (int)(y / scale);
    int width = (Minecraft.getMinecraft()).fontRendererObj.getStringWidth(text);
    int height = (Minecraft.getMinecraft()).fontRendererObj.FONT_HEIGHT;
    String[] lines = text.split("\n");
    if (lines.length > 1) {
      width = 0;
      height = 0;
      for (int i = 0; i < lines.length; i++) {
        width = Math.max(width, (Minecraft.getMinecraft()).fontRendererObj.getStringWidth(lines[i]));
        height = i * ((Minecraft.getMinecraft()).fontRendererObj.FONT_HEIGHT + 6);
      } 
    } 
    display.setWidth(width);
    display.setHeight(height);
    int coordX = x;
    int coordY = y;
    switch (display.getAnchorPoint()) {
      case SCOREBOARD:
        coordX = x - width / 2;
        break;
      case KEYSTROKES:
        coordX = x - width;
        break;
      case ARMOR_STATUS:
        coordY = y - height / 2;
        break;
      case TOGGLE_SPRINT:
        coordX = x - width / 2;
        coordY = y - height / 2;
        break;
      case CLOCK:
        coordX = x - width;
        coordY = y - height / 2;
        break;
      case REACH:
        coordY = y - height;
        break;
      case COMBO:
        coordX = x - width / 2;
        coordY = y - height;
        break;
      case null:
        coordX = x - width;
        coordY = y - height;
        break;
    } 
    GlStateManager.pushMatrix();
    GlStateManager.scale(scale, scale, 1.0F);
    if (display.isChroma()) {
      GuiUtils.drawChromaString(text, coordX, coordY, display.isShadow());
    } else {
      GuiUtils.drawString(text, coordX, coordY, display.isShadow());
    } 
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    GlStateManager.popMatrix();
  }
  
  public void renderOther(IngameDisplay display, int x, int y, boolean directive) {
    if (directive) {
      float scale = display.getScale();
      x = BoxUtils.getBoxOffX(display, (int)(x / scale), display.getWidth());
      y = BoxUtils.getBoxOffY(display, (int)(y / scale), display.getHeight());
    } 
    switch (display) {
      case POTION_EFFECTS:
        this.potionEffects.render(display, x, y);
        break;
      case SCOREBOARD:
        this.scoreboard.render(display, x, y);
        break;
      case KEYSTROKES:
        this.keystrokes.render(display, x, y);
        break;
      case ARMOR_STATUS:
        this.armorStatus.render(display, x, y);
        break;
      case TOGGLE_SPRINT:
        Client.toggleSprint.render(display, x, y);
        break;
      case CLOCK:
        this.clock.render(display, x, y);
        break;
      case REACH:
        this.reach.render(display, x, y);
        break;
      case COMBO:
        this.combo.render(display, x, y);
        break;
    } 
  }
  
  public void renderBackground(IngameDisplay display, int x, int y, boolean directive) {
    float scale = display.getScale();
    int width = display.getWidth();
    int height = display.getHeight();
    int color = display.getBackground_color();
    x = (int)(x / scale);
    y = (int)(y / scale);
    int coordX = x;
    int coordY = y;
    switch (display.getAnchorPoint()) {
      case SCOREBOARD:
        coordX = x - width / 2;
        break;
      case KEYSTROKES:
        coordX = x - width;
        break;
      case ARMOR_STATUS:
        coordY = y - height / 2;
        break;
      case TOGGLE_SPRINT:
        coordX = x - width / 2;
        coordY = y - height / 2;
        break;
      case CLOCK:
        coordX = x - width;
        coordY = y - height / 2;
        break;
      case REACH:
        coordY = y - height;
        break;
      case COMBO:
        coordX = x - width / 2;
        coordY = y - height;
        break;
      case null:
        coordX = x - width;
        coordY = y - height;
        break;
    } 
    GlStateManager.scale(scale, scale, 1.0F);
    GuiUtils.drawRoundedRect(coordX - 1, coordY - 1, coordX + width + 1, coordY + height + 1, 1.0F, color);
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
  }
}
