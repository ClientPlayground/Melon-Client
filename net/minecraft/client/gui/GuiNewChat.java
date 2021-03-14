package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Iterator;
import java.util.List;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.ingames.utils.AutoGG;
import me.kaimson.melonclient.ingames.utils.AutoGLHF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiNewChat extends Gui {
  private static final Logger logger = LogManager.getLogger();
  
  private final Minecraft mc;
  
  private final List<String> sentMessages = Lists.newArrayList();
  
  private final List<ChatLine> chatLines = Lists.newArrayList();
  
  private final List<ChatLine> drawnChatLines = Lists.newArrayList();
  
  private int scrollPos;
  
  private boolean isScrolled;
  
  private long animationShift = 0L;
  
  private String lastMessage;
  
  private int amount;
  
  public void drawChat(int updateCounter) {
    if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
      int i = getLineCount();
      boolean flag = false;
      int j = 0;
      int k = this.drawnChatLines.size();
      float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
      if (k > 0) {
        if (getChatOpen())
          flag = true; 
        float f1 = getChatScale();
        int l = MathHelper.ceiling_float_int(getChatWidth() / f1);
        GlStateManager.pushMatrix();
        float f2 = 10.0F * f1;
        double d1 = 20.0D;
        double d2 = 0.0D;
        if (IngameDisplay.CHAT_SMOOTH.isEnabled()) {
          d2 = (System.currentTimeMillis() - f2 * d1 - this.animationShift) / d1;
          if (d2 > 0.0D)
            d2 = 0.0D; 
        } 
        GlStateManager.translate(2.0D, 20.0D - d2, 0.0D);
        GlStateManager.scale(f1, f1, 1.0F);
        if (!getChatOpen())
          this.scrollPos = 0; 
        int a1 = -this.scrollPos;
        for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; i1++) {
          ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);
          if (chatline != null) {
            boolean flag1 = (a1 == -this.scrollPos);
            a1++;
            boolean flag2 = (a1 == getLineCount());
            int j1 = updateCounter - chatline.getUpdatedCounter();
            if (j1 < 200 || flag) {
              int l1 = 255;
              if (!flag) {
                double d0 = j1 / 200.0D;
                d0 = 1.0D - d0;
                d0 *= 10.0D;
                d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                d0 *= d0;
                l1 = (int)(255.0D * d0);
              } 
              if (d2 != 0.0D && flag1) {
                double d14 = 25.5D * -d2;
                l1 = (int)(255.0D - d14);
              } 
              if (d2 != 0.0D && flag2) {
                double d15 = 25.5D * -d2;
                l1 = (int)d15;
              } 
              l1 = (int)(l1 * f);
              j++;
              if (l1 > 3) {
                int i2 = 0;
                int j2 = -i1 * 9;
                drawRect(i2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
                String s = chatline.getChatComponent().getFormattedText();
                GlStateManager.enableBlend();
                GuiUtils.drawString(s, i2, (j2 - 8), 16777215 + (l1 << 24), IngameDisplay.CHAT_SHADOW.isEnabled());
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
              } 
            } 
          } 
        } 
        if (flag) {
          int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
          GlStateManager.translate(-3.0F, 0.0F, 0.0F);
          int l2 = k * k2 + k;
          int i3 = j * k2 + j;
          int j3 = this.scrollPos * i3 / k;
          int k1 = i3 * i3 / l2;
          if (l2 != i3) {
            int k3 = (j3 > 0) ? 170 : 96;
            int l3 = this.isScrolled ? 13382451 : 3355562;
            drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
            drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
          } 
        } 
        GlStateManager.popMatrix();
      } 
    } 
  }
  
  public void clearChatMessages() {
    this.drawnChatLines.clear();
    this.chatLines.clear();
    this.sentMessages.clear();
  }
  
  public GuiNewChat(Minecraft mcIn) {
    this.lastMessage = "";
    this.mc = mcIn;
  }
  
  public void printChatMessage(IChatComponent chatComponent) {
    this.animationShift = System.currentTimeMillis();
    AutoGLHF.INSTANCE.onChat(chatComponent);
    AutoGG.INSTANCE.onChat(chatComponent);
    if (IngameDisplay.CHAT_COMPACT.isEnabled() && 
      !(this.mc.ingameGUI.getChatGUI()).drawnChatLines.isEmpty())
      if (this.lastMessage.equals(chatComponent.getUnformattedText())) {
        (this.mc.ingameGUI.getChatGUI()).drawnChatLines.remove(0);
        this.amount++;
        this.lastMessage = chatComponent.getUnformattedText();
        chatComponent = chatComponent.appendText(ChatFormatting.GRAY + " (" + this.amount + ")");
      } else {
        this.amount = 1;
        this.lastMessage = chatComponent.getUnformattedText();
      }  
    printChatMessageWithOptionalDeletion(chatComponent, 0);
  }
  
  public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {
    setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
    logger.info("[CHAT] " + chatComponent.getUnformattedText());
  }
  
  private void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
    if (chatLineId != 0)
      deleteChatLine(chatLineId); 
    int i = MathHelper.floor_float(getChatWidth() / getChatScale());
    List<IChatComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRendererObj, false, false);
    boolean flag = getChatOpen();
    for (IChatComponent ichatcomponent : list) {
      if (flag && this.scrollPos > 0) {
        this.isScrolled = true;
        scroll(1);
      } 
      this.drawnChatLines.add(0, new ChatLine(updateCounter, ichatcomponent, chatLineId));
    } 
    while (this.drawnChatLines.size() > 100)
      this.drawnChatLines.remove(this.drawnChatLines.size() - 1); 
    if (!displayOnly) {
      this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));
      while (this.chatLines.size() > 100)
        this.chatLines.remove(this.chatLines.size() - 1); 
    } 
  }
  
  public void refreshChat() {
    this.drawnChatLines.clear();
    resetScroll();
    for (int i = this.chatLines.size() - 1; i >= 0; i--) {
      ChatLine chatline = this.chatLines.get(i);
      setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
    } 
  }
  
  public List<String> getSentMessages() {
    return this.sentMessages;
  }
  
  public void addToSentMessages(String message) {
    if (this.sentMessages.isEmpty() || !((String)this.sentMessages.get(this.sentMessages.size() - 1)).equals(message))
      this.sentMessages.add(message); 
  }
  
  public void resetScroll() {
    this.scrollPos = 0;
    this.isScrolled = false;
  }
  
  public void scroll(int amount) {
    this.scrollPos += amount;
    int i = this.drawnChatLines.size();
    if (this.scrollPos > i - getLineCount())
      this.scrollPos = i - getLineCount(); 
    if (this.scrollPos <= 0) {
      this.scrollPos = 0;
      this.isScrolled = false;
    } 
  }
  
  public IChatComponent getChatComponent(int mouseX, int mouseY) {
    if (!getChatOpen())
      return null; 
    ScaledResolution scaledresolution = new ScaledResolution(this.mc);
    int i = scaledresolution.getScaleFactor();
    float f = getChatScale();
    int j = mouseX / i - 3;
    int k = mouseY / i - 27;
    j = MathHelper.floor_float(j / f);
    k = MathHelper.floor_float(k / f);
    if (j >= 0 && k >= 0) {
      int l = Math.min(getLineCount(), this.drawnChatLines.size());
      if (j <= MathHelper.floor_float(getChatWidth() / getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l) {
        int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;
        if (i1 >= 0 && i1 < this.drawnChatLines.size()) {
          ChatLine chatline = this.drawnChatLines.get(i1);
          int j1 = 0;
          for (IChatComponent ichatcomponent : chatline.getChatComponent()) {
            if (ichatcomponent instanceof ChatComponentText) {
              j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));
              if (j1 > j)
                return ichatcomponent; 
            } 
          } 
        } 
        return null;
      } 
      return null;
    } 
    return null;
  }
  
  public boolean getChatOpen() {
    return this.mc.currentScreen instanceof GuiChat;
  }
  
  public void deleteChatLine(int id) {
    Iterator<ChatLine> iterator = this.drawnChatLines.iterator();
    while (iterator.hasNext()) {
      ChatLine chatline = iterator.next();
      if (chatline.getChatLineID() == id)
        iterator.remove(); 
    } 
    iterator = this.chatLines.iterator();
    while (iterator.hasNext()) {
      ChatLine chatline1 = iterator.next();
      if (chatline1.getChatLineID() == id) {
        iterator.remove();
        break;
      } 
    } 
  }
  
  public int getChatWidth() {
    return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
  }
  
  public int getChatHeight() {
    return calculateChatboxHeight(getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
  }
  
  public float getChatScale() {
    return this.mc.gameSettings.chatScale;
  }
  
  public static int calculateChatboxWidth(float scale) {
    int i = 320;
    int j = 40;
    return MathHelper.floor_float(scale * (i - j) + j);
  }
  
  public static int calculateChatboxHeight(float scale) {
    int i = 180;
    int j = 20;
    return MathHelper.floor_float(scale * (i - j) + j);
  }
  
  public int getLineCount() {
    return getChatHeight() / 9;
  }
}
