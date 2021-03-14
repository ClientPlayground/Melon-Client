package net.minecraft.client.gui.inventory;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;

public class GuiEditSign extends GuiScreen {
  private TileEntitySign tileSign;
  
  private int updateCounter;
  
  private int editLine;
  
  private GuiButton doneBtn;
  
  public GuiEditSign(TileEntitySign teSign) {
    this.tileSign = teSign;
  }
  
  public void initGui() {
    this.buttonList.clear();
    Keyboard.enableRepeatEvents(true);
    this.buttonList.add(this.doneBtn = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, I18n.format("gui.done", new Object[0])));
    this.tileSign.setEditable(false);
  }
  
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
    NetHandlerPlayClient nethandlerplayclient = this.mc.getNetHandler();
    if (nethandlerplayclient != null)
      nethandlerplayclient.addToSendQueue((Packet)new C12PacketUpdateSign(this.tileSign.getPos(), this.tileSign.signText)); 
    this.tileSign.setEditable(true);
  }
  
  public void updateScreen() {
    this.updateCounter++;
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button.enabled)
      if (button.id == 0) {
        this.tileSign.markDirty();
        this.mc.displayGuiScreen((GuiScreen)null);
      }  
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (keyCode == 200)
      this.editLine = this.editLine - 1 & 0x3; 
    if (keyCode == 208 || keyCode == 28 || keyCode == 156)
      this.editLine = this.editLine + 1 & 0x3; 
    String s = this.tileSign.signText[this.editLine].getUnformattedText();
    if (keyCode == 14 && s.length() > 0)
      s = s.substring(0, s.length() - 1); 
    if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && this.fontRendererObj.getStringWidth(s + typedChar) <= 90)
      s = s + typedChar; 
    this.tileSign.signText[this.editLine] = (IChatComponent)new ChatComponentText(s);
    if (keyCode == 1)
      actionPerformed(this.doneBtn); 
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(this.fontRendererObj, I18n.format("sign.edit", new Object[0]), this.width / 2, 40, 16777215);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.pushMatrix();
    GlStateManager.translate((this.width / 2), 0.0F, 50.0F);
    float f = 93.75F;
    GlStateManager.scale(-f, -f, -f);
    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
    Block block = this.tileSign.getBlockType();
    if (block == Blocks.standing_sign) {
      float f1 = (this.tileSign.getBlockMetadata() * 360) / 16.0F;
      GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, -1.0625F, 0.0F);
    } else {
      int i = this.tileSign.getBlockMetadata();
      float f2 = 0.0F;
      if (i == 2)
        f2 = 180.0F; 
      if (i == 4)
        f2 = 90.0F; 
      if (i == 5)
        f2 = -90.0F; 
      GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, -1.0625F, 0.0F);
    } 
    if (this.updateCounter / 6 % 2 == 0)
      this.tileSign.lineBeingEdited = this.editLine; 
    TileEntityRendererDispatcher.instance.renderTileEntityAt((TileEntity)this.tileSign, -0.5D, -0.75D, -0.5D, 0.0F);
    this.tileSign.lineBeingEdited = -1;
    GlStateManager.popMatrix();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
}
