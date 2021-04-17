package me.kaimson.melonclient.features.modules;

import me.kaimson.melonclient.features.*;
import net.minecraft.util.*;
import me.kaimson.melonclient.config.*;
import net.minecraft.client.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.*;

public class AutoGLHFModule extends Module
{
    public static AutoGLHFModule INSTANCE;
    private long lastTrigger;
    private boolean shouldSend;
    
    public AutoGLHFModule() {
        super("AutoGLHF");
        this.lastTrigger = 0L;
        this.shouldSend = true;
        AutoGLHFModule.INSTANCE = this;
    }
    
    public void onChat(final IChatComponent message) {
        if (ModuleConfig.INSTANCE.isEnabled(this) && Minecraft.getMinecraft().getCurrentServerData() != null && Minecraft.getMinecraft().getCurrentServerData().serverIP != null && System.currentTimeMillis() > this.lastTrigger + 1000L && message.getUnformattedText().contains("The game starts in ") && !message.getUnformattedText().contains(":") && this.shouldSend) {
            final int timeLeft = Integer.parseInt(message.getUnformattedText().replaceAll("\\D+", ""));
            if (timeLeft <= 10) {
                this.shouldSend = false;
                final Timer t = new Timer((timeLeft + 1) * 1000, event -> {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/achat GL HF!");
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages("/achat GL HF!");
                    this.lastTrigger = System.currentTimeMillis();
                    this.shouldSend = true;
                });
                t.setRepeats(false);
                t.start();
            }
        }
    }
}
