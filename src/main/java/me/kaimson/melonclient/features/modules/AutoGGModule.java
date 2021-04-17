package me.kaimson.melonclient.features.modules;

import me.kaimson.melonclient.features.*;
import net.minecraft.util.*;
import me.kaimson.melonclient.config.*;
import net.minecraft.client.*;
import java.util.*;

public class AutoGGModule extends Module
{
    public static AutoGGModule INSTANCE;
    private long lastTrigger;
    
    public AutoGGModule() {
        super("AutoGG");
        this.lastTrigger = 0L;
        AutoGGModule.INSTANCE = this;
    }
    
    public void onChat(final IChatComponent message) {
        if (ModuleConfig.INSTANCE.isEnabled(this) && Minecraft.getMinecraft().getCurrentServerData() != null && Minecraft.getMinecraft().getCurrentServerData().serverIP != null && System.currentTimeMillis() > this.lastTrigger + 1000L && Arrays.stream(this.getHypixelTrigger().split("\n")).anyMatch(match -> message.getUnformattedText().contains(match))) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/achat gg");
            Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages("/achat gg");
            this.lastTrigger = System.currentTimeMillis();
        }
    }
    
    private String getHypixelTrigger() {
        return "1st Killer - \n1st Place - \nWinner: \n - Damage Dealt - \nWinning Team -\n1st - \nWinners: \nWinner: \nWinning Team: \n won the game!\nTop Seeker: \n1st Place: \nLast team standing!\nWinner #1 (\nTop Survivors\nWinners - \nSumo Duel - \n";
    }
}
