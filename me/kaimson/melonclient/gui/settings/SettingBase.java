package me.kaimson.melonclient.gui.settings;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiScreen;
import me.kaimson.melonclient.gui.GuiScrolling;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.gui.buttons.ButtonBack;
import me.kaimson.melonclient.gui.buttons.ButtonClose;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public abstract class SettingBase extends GuiScreen {
  protected ScaledResolution sr;
  
  protected final int relativeHeight = 90;
  
  protected final int gap = 5;
  
  protected final int columns = 5;
  
  protected final GuiScreen parentScreen;
  
  public SettingBase(GuiScreen parentScreen) {
    this.parentScreen = parentScreen;
  }
  
  public void initGui() {
    this.sr = new ScaledResolution(this.mc);
    createButtons();
  }
  
  public void createButtons() {
    this.buttonList.add(new ButtonClose(this.width / 2 + getMaxWidth(66, 5) / 2 + 2 + 5 - 19, 73, 14, 14));
    this.buttonList.add(new ButtonBack(this.width / 2 - getMaxWidth(66, 5) / 2, 73, 15, 14, button -> this.mc.displayGuiScreen((GuiScreen)this.parentScreen)));
  }
  
  public void drawBackground(int i, int size, int width, int height, GuiScrolling scroll) {
    int y2 = this.sr.getScaledHeight() - 90;
    int maxWidth = getMaxWidth(66, 5);
    int x = width / 2 - maxWidth / 2 - 5;
    int x2 = width / 2 + maxWidth / 2 + 2 + 5 + 2;
    drawRect(0, 0, width, height, (new Color(0, 0, 0, 110)).getRGB());
    GuiUtils.drawRoundedRect(x, 70, x2, Math.max(y2, this.sr.getScaledHeight() - 90), 5.0F, -2147483648);
    drawRect(x, 89, x2, 90, 2147483647);
    GuiUtils.drawScaledString("Settings", width / 2 - getMaxWidth(66, 5) / 2 + 20, 76, false, 1.5F);
  }
  
  protected int getWidth() {
    return 355;
  }
  
  protected int getMaxWidth(int boxWidth, int gap) {
    return (boxWidth + gap) * 5;
  }
}
