package me.kaimson.melonclient.gui.settings;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiColorPicker;
import me.kaimson.melonclient.gui.GuiColorPickerReflections;
import me.kaimson.melonclient.gui.GuiScreen;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.gui.buttons.ButtonCheckbox;
import me.kaimson.melonclient.gui.buttons.ButtonColor;
import me.kaimson.melonclient.gui.buttons.ButtonKeybind;
import me.kaimson.melonclient.gui.buttons.ButtonMode;
import me.kaimson.melonclient.gui.buttons.ButtonScale;
import me.kaimson.melonclient.gui.buttons.ButtonText;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import me.kaimson.melonclient.gui.buttons.reflection.ButtonCheckboxReflection;
import me.kaimson.melonclient.gui.buttons.reflection.ButtonColorReflection;
import me.kaimson.melonclient.gui.buttons.slider.ButtonSlider;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.ingames.annotations.Setting;
import me.kaimson.melonclient.ingames.annotations.SettingAll;
import me.kaimson.melonclient.ingames.annotations.SettingSlider;
import me.kaimson.melonclient.util.LoopUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiModuleSettings extends SettingBase {
  private final IngameDisplay display;
  
  private final List<IngameDisplay> settings;
  
  public GuiModuleSettings(GuiScreen parentScreen, IngameDisplay display, List<IngameDisplay> settings) {
    super(parentScreen);
    this.display = display;
    this.settings = settings;
  }
  
  public void initGui() {
    super.initGui();
    Client.blurShader.onGuiOpen();
    int i2 = 0;
    for (IngameDisplay setting : this.settings) {
      if (setting.isAllSetting()) {
        SettingAll settingAll = (SettingAll)setting.getAnnotation();
        int x = this.width / 2 - 50;
        int y = 98 + i2 * 15 - 1;
        if (settingAll.wrap())
          if (settingAll.type() == Setting.Type.CHECKBOX) {
            this.buttonList.add(new ButtonCheckboxReflection(x, y, 10, 10, this.display, setting));
          } else if (settingAll.type() == Setting.Type.COLOR) {
            this.buttonList.add(new ButtonColorReflection(x, y, 100, 10, (GuiScreen)new GuiColorPickerReflections(this, this.display, setting), this.display, setting));
          }  
        if (settingAll.type() == Setting.Type.SCALE)
          this.buttonList.add(new ButtonScale(-1, x, y, 140, 4, 0.1F, 5.0F, 0.1F, 1.0F, this.display)); 
        i2++;
      } 
    } 
    LoopUtil.streamToIndex(this.settings, (i, setting) -> addButton(setting, this.width / 2 - 50, 98 + i.intValue() * 15 - 1, setting.getType()));
    this.scroll = new GuiMainSettings.Scroll(this.settings, this.width, this.height, 95, this.height - 90, 15, this.width / 2 + getMaxWidth(66, 5) / 2 + 2 + 5, 1);
    this.scroll.registerScrollButtons(7, 8);
  }
  
  public void addButton(IngameDisplay setting, int x, int y, Setting.Type type) {
    if (type == Setting.Type.SLIDER) {
      SettingSlider slider = (SettingSlider)setting.getAnnotation();
      if (slider == null)
        return; 
      this.buttonList.add(new ButtonSlider(-1, x, y, 140, 4, slider.min(), slider.max(), slider.step(), ((Float)Client.config.getCustoms().getOrDefault(setting, Float.valueOf(slider.current()))).floatValue(), setting));
    } else if (type == Setting.Type.CHECKBOX) {
      this.buttonList.add(new ButtonCheckbox(-1, x, y, 10, 10, setting));
    } else if (type == Setting.Type.COLOR) {
      this.buttonList.add(new ButtonColor(x, y, 100, 10, "", (GuiScreen)new GuiColorPicker(this, setting), setting));
    } else if (type == Setting.Type.MODE) {
      this.buttonList.add(new ButtonMode(x, y, 100, 10, this.display, setting));
    } 
  }
  
  public void createButtons() {
    super.createButtons();
    this.buttonList.add(new GuiButton(-1, this.width / 2 + getMaxWidth(66, 5) / 2 + 2 - 15 - 2 - 100, 73, 100, 14, "Reset to defaults", button -> {
            Client.config.putDefault(this.display);
            this.mc.displayGuiScreen((GuiScreen)this);
          }));
  }
  
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    this.scroll.handleMouseInput();
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    for (GuiButton button : this.buttonList) {
      if (button instanceof ButtonKeybind)
        ((ButtonKeybind)button).keyTyped(typedChar, keyCode); 
    } 
    super.keyTyped(typedChar, keyCode);
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawBackground(this.settings.size(), 10, this.width, this.height, this.scroll);
    int i2 = 0;
    for (int i = 0; i < this.settings.size(); i++) {
      GuiUtils.drawString(Client.utils.capitalize(((IngameDisplay)this.settings.get(i)).name().replace(this.display.name(), "")), this.width / 2 - getMaxWidth(66, 5) / 2 + 10, 98 + (i + i2) * 15 - (int)this.scroll.amountScrolled);
      if (i != this.settings.size() - 1)
        drawRect(this.width / 2 - getMaxWidth(66, 5) / 2, 100 + (i + i2) * 15 + 9, this.width / 2 + 132 - 2, 100 + (i + i2) * 15 + 10, (new Color(120, 120, 120, 70)).getRGB()); 
    } 
    for (GuiButton button : this.buttonList) {
      if (button instanceof ButtonSlider) {
        ((ButtonSlider)button).render(this.mc, button.yPosition - (int)this.scroll.amountScrolled, mouseX, mouseY);
        continue;
      } 
      if (button instanceof ButtonCheckbox) {
        ((ButtonCheckbox)button).render(button.yPosition - (int)this.scroll.amountScrolled, mouseX, mouseY);
        continue;
      } 
      if (button instanceof ButtonText) {
        ((ButtonText)button).drawTextBox(button.yPosition, mouseX, mouseY);
        continue;
      } 
      if (button instanceof ButtonScale) {
        ((ButtonScale)button).render(this.mc, button.yPosition - (int)this.scroll.amountScrolled, mouseX, mouseY);
        continue;
      } 
      if (button instanceof ButtonMode) {
        ((ButtonMode)button).render(this.mc, button.yPosition - (int)this.scroll.amountScrolled, mouseX, mouseY);
        continue;
      } 
      button.drawButton(this.mc, mouseX, mouseY);
    } 
    this.scroll.handleDragging(mouseX, mouseY);
  }
  
  public void onGuiClosed() {
    Client.blurShader.onGuiClose();
    Client.config.saveConfig();
  }
}
