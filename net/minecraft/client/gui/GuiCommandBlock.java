package net.minecraft.client.gui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class GuiCommandBlock extends GuiScreen {
  private static final Logger field_146488_a = LogManager.getLogger();
  
  private GuiTextField commandTextField;
  
  private GuiTextField previousOutputTextField;
  
  private final CommandBlockLogic localCommandBlock;
  
  private GuiButton doneBtn;
  
  private GuiButton cancelBtn;
  
  private GuiButton field_175390_s;
  
  private boolean field_175389_t;
  
  public GuiCommandBlock(CommandBlockLogic p_i45032_1_) {
    this.localCommandBlock = p_i45032_1_;
  }
  
  public void updateScreen() {
    this.commandTextField.updateCursorCounter();
  }
  
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    this.buttonList.clear();
    this.buttonList.add(this.doneBtn = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.done", new Object[0])));
    this.buttonList.add(this.cancelBtn = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel", new Object[0])));
    this.buttonList.add(this.field_175390_s = new GuiButton(4, this.width / 2 + 150 - 20, 150, 20, 20, "O"));
    this.commandTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 150, 50, 300, 20);
    this.commandTextField.setMaxStringLength(32767);
    this.commandTextField.setFocused(true);
    this.commandTextField.setText(this.localCommandBlock.getCommand());
    this.previousOutputTextField = new GuiTextField(3, this.fontRendererObj, this.width / 2 - 150, 150, 276, 20);
    this.previousOutputTextField.setMaxStringLength(32767);
    this.previousOutputTextField.setEnabled(false);
    this.previousOutputTextField.setText("-");
    this.field_175389_t = this.localCommandBlock.shouldTrackOutput();
    func_175388_a();
    this.doneBtn.enabled = (this.commandTextField.getText().trim().length() > 0);
  }
  
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button.enabled)
      if (button.id == 1) {
        this.localCommandBlock.setTrackOutput(this.field_175389_t);
        this.mc.displayGuiScreen((GuiScreen)null);
      } else if (button.id == 0) {
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeByte(this.localCommandBlock.func_145751_f());
        this.localCommandBlock.func_145757_a((ByteBuf)packetbuffer);
        packetbuffer.writeString(this.commandTextField.getText());
        packetbuffer.writeBoolean(this.localCommandBlock.shouldTrackOutput());
        this.mc.getNetHandler().addToSendQueue((Packet)new C17PacketCustomPayload("MC|AdvCdm", packetbuffer));
        if (!this.localCommandBlock.shouldTrackOutput())
          this.localCommandBlock.setLastOutput((IChatComponent)null); 
        this.mc.displayGuiScreen((GuiScreen)null);
      } else if (button.id == 4) {
        this.localCommandBlock.setTrackOutput(!this.localCommandBlock.shouldTrackOutput());
        func_175388_a();
      }  
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    this.commandTextField.textboxKeyTyped(typedChar, keyCode);
    this.previousOutputTextField.textboxKeyTyped(typedChar, keyCode);
    this.doneBtn.enabled = (this.commandTextField.getText().trim().length() > 0);
    if (keyCode != 28 && keyCode != 156) {
      if (keyCode == 1)
        actionPerformed(this.cancelBtn); 
    } else {
      actionPerformed(this.doneBtn);
    } 
  }
  
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    this.commandTextField.mouseClicked(mouseX, mouseY, mouseButton);
    this.previousOutputTextField.mouseClicked(mouseX, mouseY, mouseButton);
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(this.fontRendererObj, I18n.format("advMode.setCommand", new Object[0]), this.width / 2, 20, 16777215);
    drawString(this.fontRendererObj, I18n.format("advMode.command", new Object[0]), this.width / 2 - 150, 37, 10526880);
    this.commandTextField.drawTextBox();
    int i = 75;
    int j = 0;
    drawString(this.fontRendererObj, I18n.format("advMode.nearestPlayer", new Object[0]), this.width / 2 - 150, i + j++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
    drawString(this.fontRendererObj, I18n.format("advMode.randomPlayer", new Object[0]), this.width / 2 - 150, i + j++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
    drawString(this.fontRendererObj, I18n.format("advMode.allPlayers", new Object[0]), this.width / 2 - 150, i + j++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
    drawString(this.fontRendererObj, I18n.format("advMode.allEntities", new Object[0]), this.width / 2 - 150, i + j++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
    drawString(this.fontRendererObj, "", this.width / 2 - 150, i + j++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
    if (this.previousOutputTextField.getText().length() > 0) {
      i = i + j * this.fontRendererObj.FONT_HEIGHT + 16;
      drawString(this.fontRendererObj, I18n.format("advMode.previousOutput", new Object[0]), this.width / 2 - 150, i, 10526880);
      this.previousOutputTextField.drawTextBox();
    } 
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
  
  private void func_175388_a() {
    if (this.localCommandBlock.shouldTrackOutput()) {
      this.field_175390_s.displayString = "O";
      if (this.localCommandBlock.getLastOutput() != null)
        this.previousOutputTextField.setText(this.localCommandBlock.getLastOutput().getUnformattedText()); 
    } else {
      this.field_175390_s.displayString = "X";
      this.previousOutputTextField.setText("-");
    } 
  }
}
