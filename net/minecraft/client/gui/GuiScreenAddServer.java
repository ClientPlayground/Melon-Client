package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import java.io.IOException;
import java.net.IDN;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

public class GuiScreenAddServer extends GuiScreen {
  private final GuiScreen parentScreen;
  
  private final ServerData serverData;
  
  private GuiTextField serverIPField;
  
  private GuiTextField serverNameField;
  
  private GuiButton serverResourcePacks;
  
  private Predicate<String> field_181032_r = new Predicate<String>() {
      public boolean apply(String p_apply_1_) {
        if (p_apply_1_.length() == 0)
          return true; 
        String[] astring = p_apply_1_.split(":");
        if (astring.length == 0)
          return true; 
        try {
          String s = IDN.toASCII(astring[0]);
          return true;
        } catch (IllegalArgumentException var4) {
          return false;
        } 
      }
    };
  
  public GuiScreenAddServer(GuiScreen p_i1033_1_, ServerData p_i1033_2_) {
    this.parentScreen = p_i1033_1_;
    this.serverData = p_i1033_2_;
  }
  
  public void updateScreen() {
    this.serverNameField.updateCursorCounter();
    this.serverIPField.updateCursorCounter();
  }
  
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    this.buttonList.clear();
    this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 18, I18n.format("addServer.add", new Object[0])));
    this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 18, I18n.format("gui.cancel", new Object[0])));
    this.buttonList.add(this.serverResourcePacks = new GuiButton(2, this.width / 2 - 100, this.height / 4 + 72, I18n.format("addServer.resourcePack", new Object[0]) + ": " + this.serverData.getResourceMode().getMotd().getFormattedText()));
    this.serverNameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 66, 200, 20);
    this.serverNameField.setFocused(true);
    this.serverNameField.setText(this.serverData.serverName);
    this.serverIPField = new GuiTextField(1, this.fontRendererObj, this.width / 2 - 100, 106, 200, 20);
    this.serverIPField.setMaxStringLength(128);
    this.serverIPField.setText(this.serverData.serverIP);
    this.serverIPField.setValidator(this.field_181032_r);
    ((GuiButton)this.buttonList.get(0)).enabled = (this.serverIPField.getText().length() > 0 && (this.serverIPField.getText().split(":")).length > 0 && this.serverNameField.getText().length() > 0);
  }
  
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button.enabled)
      if (button.id == 2) {
        this.serverData.setResourceMode(ServerData.ServerResourceMode.values()[(this.serverData.getResourceMode().ordinal() + 1) % (ServerData.ServerResourceMode.values()).length]);
        this.serverResourcePacks.displayString = I18n.format("addServer.resourcePack", new Object[0]) + ": " + this.serverData.getResourceMode().getMotd().getFormattedText();
      } else if (button.id == 1) {
        this.parentScreen.confirmClicked(false, 0);
      } else if (button.id == 0) {
        this.serverData.serverName = this.serverNameField.getText();
        this.serverData.serverIP = this.serverIPField.getText();
        this.parentScreen.confirmClicked(true, 0);
      }  
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    this.serverNameField.textboxKeyTyped(typedChar, keyCode);
    this.serverIPField.textboxKeyTyped(typedChar, keyCode);
    if (keyCode == 15) {
      this.serverNameField.setFocused(!this.serverNameField.isFocused());
      this.serverIPField.setFocused(!this.serverIPField.isFocused());
    } 
    if (keyCode == 28 || keyCode == 156)
      actionPerformed(this.buttonList.get(0)); 
    ((GuiButton)this.buttonList.get(0)).enabled = (this.serverIPField.getText().length() > 0 && (this.serverIPField.getText().split(":")).length > 0 && this.serverNameField.getText().length() > 0);
  }
  
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    this.serverIPField.mouseClicked(mouseX, mouseY, mouseButton);
    this.serverNameField.mouseClicked(mouseX, mouseY, mouseButton);
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(this.fontRendererObj, I18n.format("addServer.title", new Object[0]), this.width / 2, 17, 16777215);
    drawString(this.fontRendererObj, I18n.format("addServer.enterName", new Object[0]), this.width / 2 - 100, 53, 10526880);
    drawString(this.fontRendererObj, I18n.format("addServer.enterIp", new Object[0]), this.width / 2 - 100, 94, 10526880);
    this.serverNameField.drawTextBox();
    this.serverIPField.drawTextBox();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
}
