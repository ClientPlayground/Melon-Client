package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiAnimationSettingsOF;
import net.optifine.gui.GuiDetailSettingsOF;
import net.optifine.gui.GuiOptionButtonOF;
import net.optifine.gui.GuiOptionSliderOF;
import net.optifine.gui.GuiOtherSettingsOF;
import net.optifine.gui.GuiPerformanceSettingsOF;
import net.optifine.gui.GuiQualitySettingsOF;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProvider;
import net.optifine.gui.TooltipProviderOptions;
import net.optifine.shaders.gui.GuiShaders;

public class GuiVideoSettings extends GuiScreenOF {
  private GuiScreen parentGuiScreen;
  
  protected String screenTitle = "Video Settings";
  
  private GameSettings guiGameSettings;
  
  private static GameSettings.Options[] videoOptions = new GameSettings.Options[] { 
      GameSettings.Options.GRAPHICS, GameSettings.Options.RENDER_DISTANCE, GameSettings.Options.AMBIENT_OCCLUSION, GameSettings.Options.FRAMERATE_LIMIT, GameSettings.Options.AO_LEVEL, GameSettings.Options.VIEW_BOBBING, GameSettings.Options.GUI_SCALE, GameSettings.Options.USE_VBO, GameSettings.Options.GAMMA, GameSettings.Options.BLOCK_ALTERNATIVES, 
      GameSettings.Options.DYNAMIC_LIGHTS, GameSettings.Options.DYNAMIC_FOV };
  
  private TooltipManager tooltipManager = new TooltipManager((GuiScreen)this, (TooltipProvider)new TooltipProviderOptions());
  
  public GuiVideoSettings(GuiScreen parentScreenIn, GameSettings gameSettingsIn) {
    this.parentGuiScreen = parentScreenIn;
    this.guiGameSettings = gameSettingsIn;
  }
  
  public void initGui() {
    this.screenTitle = I18n.format("options.videoTitle", new Object[0]);
    this.buttonList.clear();
    for (int i = 0; i < videoOptions.length; i++) {
      GameSettings.Options gamesettings$options = videoOptions[i];
      if (gamesettings$options != null) {
        int j = this.width / 2 - 155 + i % 2 * 160;
        int k = this.height / 6 + 21 * i / 2 - 12;
        if (gamesettings$options.getEnumFloat()) {
          this.buttonList.add(new GuiOptionSliderOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
        } else {
          this.buttonList.add(new GuiOptionButtonOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, this.guiGameSettings.getKeyBinding(gamesettings$options)));
        } 
      } 
    } 
    int l = this.height / 6 + 21 * videoOptions.length / 2 - 12;
    int i1 = 0;
    i1 = this.width / 2 - 155 + 0;
    this.buttonList.add(new GuiOptionButton(231, i1, l, Lang.get("of.options.shaders")));
    i1 = this.width / 2 - 155 + 160;
    this.buttonList.add(new GuiOptionButton(202, i1, l, Lang.get("of.options.quality")));
    l += 21;
    i1 = this.width / 2 - 155 + 0;
    this.buttonList.add(new GuiOptionButton(201, i1, l, Lang.get("of.options.details")));
    i1 = this.width / 2 - 155 + 160;
    this.buttonList.add(new GuiOptionButton(212, i1, l, Lang.get("of.options.performance")));
    l += 21;
    i1 = this.width / 2 - 155 + 0;
    this.buttonList.add(new GuiOptionButton(211, i1, l, Lang.get("of.options.animations")));
    i1 = this.width / 2 - 155 + 160;
    this.buttonList.add(new GuiOptionButton(222, i1, l, Lang.get("of.options.other")));
    l += 21;
    this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    actionPerformed(button, 1);
  }
  
  protected void actionPerformedRightClick(GuiButton p_actionPerformedRightClick_1_) {
    if (p_actionPerformedRightClick_1_.id == GameSettings.Options.GUI_SCALE.ordinal())
      actionPerformed(p_actionPerformedRightClick_1_, -1); 
  }
  
  private void actionPerformed(GuiButton p_actionPerformed_1_, int p_actionPerformed_2_) {
    if (p_actionPerformed_1_.enabled) {
      int i = this.guiGameSettings.guiScale;
      if (p_actionPerformed_1_.id < 200 && p_actionPerformed_1_ instanceof GuiOptionButton) {
        this.guiGameSettings.setOptionValue(((GuiOptionButton)p_actionPerformed_1_).returnEnumOptions(), p_actionPerformed_2_);
        p_actionPerformed_1_.displayString = this.guiGameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(p_actionPerformed_1_.id));
      } 
      if (p_actionPerformed_1_.id == 200) {
        this.mc.gameSettings.saveOptions();
        this.mc.displayGuiScreen(this.parentGuiScreen);
      } 
      if (this.guiGameSettings.guiScale != i) {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int j = scaledresolution.getScaledWidth();
        int k = scaledresolution.getScaledHeight();
        setWorldAndResolution(this.mc, j, k);
      } 
      if (p_actionPerformed_1_.id == 201) {
        this.mc.gameSettings.saveOptions();
        GuiDetailSettingsOF guidetailsettingsof = new GuiDetailSettingsOF((GuiScreen)this, this.guiGameSettings);
        this.mc.displayGuiScreen((GuiScreen)guidetailsettingsof);
      } 
      if (p_actionPerformed_1_.id == 202) {
        this.mc.gameSettings.saveOptions();
        GuiQualitySettingsOF guiqualitysettingsof = new GuiQualitySettingsOF((GuiScreen)this, this.guiGameSettings);
        this.mc.displayGuiScreen((GuiScreen)guiqualitysettingsof);
      } 
      if (p_actionPerformed_1_.id == 211) {
        this.mc.gameSettings.saveOptions();
        GuiAnimationSettingsOF guianimationsettingsof = new GuiAnimationSettingsOF((GuiScreen)this, this.guiGameSettings);
        this.mc.displayGuiScreen((GuiScreen)guianimationsettingsof);
      } 
      if (p_actionPerformed_1_.id == 212) {
        this.mc.gameSettings.saveOptions();
        GuiPerformanceSettingsOF guiperformancesettingsof = new GuiPerformanceSettingsOF((GuiScreen)this, this.guiGameSettings);
        this.mc.displayGuiScreen((GuiScreen)guiperformancesettingsof);
      } 
      if (p_actionPerformed_1_.id == 222) {
        this.mc.gameSettings.saveOptions();
        GuiOtherSettingsOF guiothersettingsof = new GuiOtherSettingsOF((GuiScreen)this, this.guiGameSettings);
        this.mc.displayGuiScreen((GuiScreen)guiothersettingsof);
      } 
      if (p_actionPerformed_1_.id == 231) {
        if (Config.isAntialiasing() || Config.isAntialiasingConfigured()) {
          Config.showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
          return;
        } 
        if (Config.isAnisotropicFiltering()) {
          Config.showGuiMessage(Lang.get("of.message.shaders.af1"), Lang.get("of.message.shaders.af2"));
          return;
        } 
        if (Config.isFastRender()) {
          Config.showGuiMessage(Lang.get("of.message.shaders.fr1"), Lang.get("of.message.shaders.fr2"));
          return;
        } 
        if ((Config.getGameSettings()).anaglyph) {
          Config.showGuiMessage(Lang.get("of.message.shaders.an1"), Lang.get("of.message.shaders.an2"));
          return;
        } 
        this.mc.gameSettings.saveOptions();
        GuiShaders guishaders = new GuiShaders((GuiScreen)this, this.guiGameSettings);
        this.mc.displayGuiScreen((GuiScreen)guishaders);
      } 
    } 
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
    String s = Config.getVersion();
    String s1 = "HD_U";
    if (s1.equals("HD"))
      s = "OptiFine HD L5"; 
    if (s1.equals("HD_U"))
      s = "OptiFine HD L5 Ultra"; 
    if (s1.equals("L"))
      s = "OptiFine L5 Light"; 
    drawString(this.fontRendererObj, s, 2, this.height - 10, 8421504);
    String s2 = "Minecraft 1.8.9";
    int i = this.fontRendererObj.getStringWidth(s2);
    drawString(this.fontRendererObj, s2, this.width - i - 2, this.height - 10, 8421504);
    super.drawScreen(mouseX, mouseY, partialTicks);
    this.tooltipManager.drawTooltips(mouseX, mouseY, this.buttonList);
  }
  
  public static int getButtonWidth(GuiButton p_getButtonWidth_0_) {
    return p_getButtonWidth_0_.width;
  }
  
  public static int getButtonHeight(GuiButton p_getButtonHeight_0_) {
    return p_getButtonHeight_0_.height;
  }
  
  public static void drawGradientRect(GuiScreen p_drawGradientRect_0_, int p_drawGradientRect_1_, int p_drawGradientRect_2_, int p_drawGradientRect_3_, int p_drawGradientRect_4_, int p_drawGradientRect_5_, int p_drawGradientRect_6_) {
    p_drawGradientRect_0_.drawGradientRect(p_drawGradientRect_1_, p_drawGradientRect_2_, p_drawGradientRect_3_, p_drawGradientRect_4_, p_drawGradientRect_5_, p_drawGradientRect_6_);
  }
  
  public static String getGuiChatText(GuiChat p_getGuiChatText_0_) {
    return p_getGuiChatText_0_.inputField.getText();
  }
}
