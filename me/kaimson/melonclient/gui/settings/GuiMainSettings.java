package me.kaimson.melonclient.gui.settings;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiScreen;
import me.kaimson.melonclient.gui.GuiScrolling;
import me.kaimson.melonclient.gui.buttons.ButtonModule;
import me.kaimson.melonclient.gui.buttons.ButtonSettings;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.ingames.annotations.SettingAll;
import me.kaimson.melonclient.ingames.render.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class GuiMainSettings extends SettingBase {
  private int row = 1;
  
  private int column = 1;
  
  private int i = 0;
  
  private final List<IngameDisplay> settings = Lists.newArrayList();
  
  public GuiMainSettings(GuiScreen parentScreen) {
    super(parentScreen);
  }
  
  public void initGui() {
    super.initGui();
    this.row = 1;
    this.column = 1;
    this.i = 0;
    this.settings.clear();
    Client.blurShader.onGuiOpen();
    addSettingButtons();
    Arrays.<IngameDisplay>stream(IngameDisplay.values()).filter(display -> (display.isDisplayItem() || display.isEventItem())).forEach(display -> this.i++);
    this.scroll = new Scroll(this.settings, this.width, this.height, 95, this.height - 90, 58, this.width / 2 + getMaxWidth(66, 5) / 2 + 2, 5);
    this.scroll.registerScrollButtons(7, 8);
  }
  
  private void addSettingButtons() {
    AtomicInteger i = new AtomicInteger();
    Arrays.<IngameDisplay>stream(IngameDisplay.values()).filter(display -> (display.isDisplayItem() || display.isEventItem())).forEach(display -> {
          this.settings.add(display);
          addButton(display, i.incrementAndGet());
        });
  }
  
  private List<IngameDisplay> getSettings(IngameDisplay display) {
    List<IngameDisplay> settings = Lists.newArrayList();
    for (IngameDisplay i : IngameDisplay.values()) {
      if (i.isAllSetting()) {
        SettingAll settingAll = (SettingAll)i.getAnnotation();
        if ((settingAll.target() == SettingAll.Target.DISPLAY_ITEM_RENDERTYPE_TEXT && display.isDisplayItem() && display.getRenderType() == RenderType.TEXT) || (settingAll.target() == SettingAll.Target.DISPLAY_ITEM && display.isDisplayItem()))
          settings.add(i); 
      } 
      if (i != display && 
        i.isSetting() && 
        i.name().startsWith(display.name()))
        settings.add(i); 
    } 
    return settings;
  }
  
  private void addButton(IngameDisplay display, int index) {
    String text = display.getID();
    int boxWidth = (getWidth() - 20) / 5;
    int boxHeight = 60;
    int currentColumn = (index - 1) / 5;
    int x = this.width / 2 - boxWidth / 2 * 5 + (boxWidth + 5) * (index - 1) - currentColumn * getMaxWidth(boxWidth, 5) - 10;
    int y = (int)getRowHeight(this.row);
    if (getSettings(display).size() > 0)
      this.buttonList.add(new ButtonSettings(x + 66 - 9, y + boxHeight - 20, display, button -> this.mc.displayGuiScreen((GuiScreen)new GuiModuleSettings(this, ((ButtonSettings)button).getDisplay(), getSettings(((ButtonSettings)button).getDisplay()))))); 
    this.buttonList.add(new ButtonModule(-1, x + 1, y + 1, boxWidth, text, display, button -> {
            if (this.mc.currentScreen instanceof GuiMainSettings)
              Client.config.setEnabled(((ButtonModule)button).getDisplay()); 
          }));
    this.column++;
    if (this.column > 5) {
      this.column = 1;
      this.row++;
    } 
  }
  
  private double getRowHeight(double row) {
    row--;
    return 95.0D + row * 60.0D;
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawBackground(this.i, 22, this.width, this.height, this.scroll);
    int x = this.width / 2 - getMaxWidth(66, 5) / 2 - 5;
    int x2 = this.width / 2 + getMaxWidth(66, 5) / 2 + 2 + 5;
    GL11.glEnable(3089);
    GL11.glScissor(x * this.sr
        .getScaleFactor(), 90 * this.sr.getScaleFactor(), x2 * this.sr
        .getScaleFactor() - x * this.sr.getScaleFactor(), (this.sr.getScaledHeight() - 90) * this.sr.getScaleFactor() - 90 * this.sr.getScaleFactor());
    for (GuiButton button : this.buttonList) {
      int buttonY = button.yPosition - (int)this.scroll.amountScrolled;
      if (!(button instanceof ButtonModule))
        continue; 
      GuiButton guiButton = (GuiButton)button;
      if (buttonY + guiButton.getHeight() > 89 && buttonY < this.sr.getScaledHeight() - 90 - 1)
        ((ButtonModule)button).render(this.mc, button.xPosition, buttonY, mouseX, mouseY); 
    } 
    for (GuiButton button : this.buttonList) {
      int buttonY = button.yPosition - (int)this.scroll.amountScrolled;
      if (!(button instanceof ButtonSettings))
        continue; 
      GuiButton guiButton = (GuiButton)button;
      if (buttonY + guiButton.getHeight() > 89 && buttonY < this.sr.getScaledHeight() - 90 - 1)
        ((ButtonSettings)button).render(this.mc, button.xPosition, button.yPosition - (int)this.scroll.amountScrolled, mouseX, mouseY); 
    } 
    GL11.glDisable(3089);
    for (GuiButton button : this.buttonList) {
      if (button instanceof ButtonModule || button instanceof ButtonSettings)
        continue; 
      button.drawButton(this.mc, mouseX, mouseY);
    } 
    this.scroll.drawScroll(this.scroll.getScrollBarX(), this.scroll.getScrollBarX() + 3);
    this.scroll.handleDragging(mouseX, mouseY);
    this.scroll.drawScreen(mouseX, mouseY, partialTicks);
  }
  
  public void onGuiClosed() {
    Client.blurShader.onGuiClose();
    Client.config.saveConfig();
  }
  
  static class Scroll extends GuiScrolling {
    private final List<IngameDisplay> list;
    
    private final int scrollbarX;
    
    private final int columns;
    
    public Scroll(List<IngameDisplay> list, int width, int height, int topIn, int bottomIn, int slotHeightIn, int scrollbarX, int columns) {
      super(Minecraft.getMinecraft(), width, height, topIn, bottomIn, slotHeightIn);
      this.list = list;
      this.scrollbarX = scrollbarX;
      this.columns = columns;
    }
    
    public int getScrollBarX() {
      return this.scrollbarX;
    }
    
    public void drawScroll(int i, int j) {
      int j1 = func_148135_f();
      if (j1 > 0) {
        drawBackground();
        int height = (this.bottom - this.top) * (this.bottom - this.top) / getContentHeight();
        height = MathHelper.clamp_int(height, 32, this.bottom - this.top - 8);
        height -= (int)Math.min((this.amountScrolled < 0.0D) ? (int)-this.amountScrolled : ((this.amountScrolled > func_148135_f()) ? ((int)this.amountScrolled - func_148135_f()) : false), height * 0.75D);
        int minY = Math.min(Math.max(getAmountScrolled() * (this.bottom - this.top - height) / func_148135_f() + this.top, this.top), this.bottom - height);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(7425);
        GlStateManager.disableTexture2D();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(i, (minY + height - 1), 0.0D).tex(0.0D, 1.0D).color(0, 255, 255, 2005).endVertex();
        buffer.pos((j - 1), (minY + height - 1), 0.0D).tex(1.0D, 1.0D).color(0, 255, 255, 200).endVertex();
        buffer.pos((j - 1), minY, 0.0D).tex(1.0D, 0.0D).color(0, 255, 255, 200).endVertex();
        buffer.pos(i, minY, 0.0D).tex(0.0D, 0.0D).color(0, 255, 255, 200).endVertex();
        tessellator.draw();
      } 
    }
    
    public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_) {}
    
    protected int getSize() {
      if (this.list.size() % this.columns != 0)
        return (int)Math.ceil(this.list.size() / this.columns); 
      return this.list.size() / this.columns;
    }
    
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {}
    
    protected boolean isSelected(int slotIndex) {
      return false;
    }
    
    protected void drawBackground() {}
    
    protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {}
  }
}
