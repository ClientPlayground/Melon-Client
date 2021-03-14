package me.kaimson.melonclient.ingames.utils;

import java.awt.event.ActionEvent;
import javax.swing.Timer;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

public class AutoGLHF {
  public static final AutoGLHF INSTANCE = new AutoGLHF();
  
  private long lastTrigger = 0L;
  
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private boolean shouldSend = true;
  
  public void onChat(IChatComponent message) {
    if (IngameDisplay.AUTO_GLHF.isEnabled() && 
      this.mc.getCurrentServerData() != null && (this.mc.getCurrentServerData()).serverIP != null && 
      System.currentTimeMillis() > this.lastTrigger + 1000L && message.getUnformattedText().contains(getHypixelTrigger()) && !message.getUnformattedText().contains(":") && this.shouldSend) {
      int timeleft = Integer.parseInt(message.getUnformattedText().replaceAll("\\D+", ""));
      if (timeleft <= 10) {
        this.shouldSend = false;
        Timer t = new Timer((timeleft + 1) * 1000, event -> {
              this.mc.thePlayer.sendChatMessage("/achat GL HF!");
              this.mc.ingameGUI.getChatGUI().addToSentMessages("/achat GL HF!");
              this.lastTrigger = System.currentTimeMillis();
              this.shouldSend = true;
            });
        t.setRepeats(false);
        t.start();
      } 
    } 
  }
  
  public static String getHypixelTrigger() {
    return "The game starts in ";
  }
}
