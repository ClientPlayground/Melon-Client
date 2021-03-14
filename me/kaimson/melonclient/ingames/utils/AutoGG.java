package me.kaimson.melonclient.ingames.utils;

import java.util.Arrays;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

public class AutoGG {
  public static final AutoGG INSTANCE = new AutoGG();
  
  private long lastTrigger = 0L;
  
  private final Minecraft mc = Minecraft.getMinecraft();
  
  public void onChat(IChatComponent message) {
    if (IngameDisplay.AUTO_GG.isEnabled() && 
      this.mc.getCurrentServerData() != null && (this.mc.getCurrentServerData()).serverIP != null && 
      System.currentTimeMillis() > this.lastTrigger + 1000L && Arrays.<String>stream(getHypixelTrigger().split("\n")).anyMatch(match -> message.getUnformattedText().contains(match))) {
      this.mc.thePlayer.sendChatMessage("gg");
      this.mc.ingameGUI.getChatGUI().addToSentMessages("gg");
      this.lastTrigger = System.currentTimeMillis();
    } 
  }
  
  public static String getHypixelTrigger() {
    return "1st Killer - \n1st Place - \nWinner: \n - Damage Dealt - \nWinning Team -\n1st - \nWinners: \nWinner: \nWinning Team: \n won the game!\nTop Seeker: \n1st Place: \nLast team standing!\nWinner #1 (\nTop Survivors\nWinners - \nSumo Duel - \n";
  }
}
