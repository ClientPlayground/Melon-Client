package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;

public class GuiLanguage extends GuiScreen {
  protected GuiScreen parentScreen;
  
  private List list;
  
  private final GameSettings game_settings_3;
  
  private final LanguageManager languageManager;
  
  private GuiOptionButton forceUnicodeFontBtn;
  
  private GuiOptionButton confirmSettingsBtn;
  
  public GuiLanguage(GuiScreen screen, GameSettings gameSettingsObj, LanguageManager manager) {
    this.parentScreen = screen;
    this.game_settings_3 = gameSettingsObj;
    this.languageManager = manager;
  }
  
  public void initGui() {
    this.buttonList.add(this.forceUnicodeFontBtn = new GuiOptionButton(100, this.width / 2 - 155, this.height - 38, GameSettings.Options.FORCE_UNICODE_FONT, this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
    this.buttonList.add(this.confirmSettingsBtn = new GuiOptionButton(6, this.width / 2 - 155 + 160, this.height - 38, I18n.format("gui.done", new Object[0])));
    this.list = new List(this.mc);
    this.list.registerScrollButtons(7, 8);
  }
  
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    this.list.handleMouseInput();
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button.enabled) {
      switch (button.id) {
        case 5:
          return;
        case 6:
          this.mc.displayGuiScreen(this.parentScreen);
        case 100:
          if (button instanceof GuiOptionButton) {
            this.game_settings_3.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
            button.displayString = this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            setWorldAndResolution(this.mc, i, j);
          } 
      } 
      this.list.actionPerformed(button);
    } 
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    this.list.drawScreen(mouseX, mouseY, partialTicks);
    drawCenteredString(this.fontRendererObj, I18n.format("options.language", new Object[0]), this.width / 2, 16, 16777215);
    drawCenteredString(this.fontRendererObj, "(" + I18n.format("options.languageWarning", new Object[0]) + ")", this.width / 2, this.height - 56, 8421504);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
  
  class List extends GuiSlot {
    private final java.util.List<String> langCodeList = Lists.newArrayList();
    
    private final Map<String, Language> languageMap = Maps.newHashMap();
    
    public List(Minecraft mcIn) {
      super(mcIn, GuiLanguage.this.width, GuiLanguage.this.height, 32, GuiLanguage.this.height - 65 + 4, 18);
      for (Language language : GuiLanguage.this.languageManager.getLanguages()) {
        this.languageMap.put(language.getLanguageCode(), language);
        this.langCodeList.add(language.getLanguageCode());
      } 
    }
    
    protected int getSize() {
      return this.langCodeList.size();
    }
    
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
      Language language = this.languageMap.get(this.langCodeList.get(slotIndex));
      GuiLanguage.this.languageManager.setCurrentLanguage(language);
      GuiLanguage.this.game_settings_3.language = language.getLanguageCode();
      this.mc.refreshResources();
      GuiLanguage.this.fontRendererObj.setUnicodeFlag((GuiLanguage.this.languageManager.isCurrentLocaleUnicode() || GuiLanguage.this.game_settings_3.forceUnicodeFont));
      GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.isCurrentLanguageBidirectional());
      GuiLanguage.this.confirmSettingsBtn.displayString = I18n.format("gui.done", new Object[0]);
      GuiLanguage.this.forceUnicodeFontBtn.displayString = GuiLanguage.this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
      GuiLanguage.this.game_settings_3.saveOptions();
    }
    
    protected boolean isSelected(int slotIndex) {
      return ((String)this.langCodeList.get(slotIndex)).equals(GuiLanguage.this.languageManager.getCurrentLanguage().getLanguageCode());
    }
    
    protected int getContentHeight() {
      return getSize() * 18;
    }
    
    protected void drawBackground() {
      GuiLanguage.this.drawDefaultBackground();
    }
    
    protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
      GuiLanguage.this.fontRendererObj.setBidiFlag(true);
      GuiLanguage.this.drawCenteredString(GuiLanguage.this.fontRendererObj, ((Language)this.languageMap.get(this.langCodeList.get(entryID))).toString(), this.width / 2, p_180791_3_ + 1, 16777215);
      GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.getCurrentLanguage().isBidirectional());
    }
  }
}
