package me.kaimson.melonclient.features.modules;

import me.kaimson.melonclient.features.*;
import net.minecraft.util.*;
import me.kaimson.melonclient.config.*;
import net.minecraft.client.*;
import java.util.regex.*;

public class AutoFriendModule extends Module
{
    public static AutoFriendModule INSTANCE;
    private final Pattern pattern;
    
    public AutoFriendModule() {
        super("Auto Friend");
        this.pattern = Pattern.compile("§m----------------------------------------------------Friend request from (?<name>.+)\\[ACCEPT\\] - \\[DENY\\] - \\[IGNORE\\].*");
        AutoFriendModule.INSTANCE = this;
    }
    
    public void onChat(final IChatComponent chatComponent) {
        if (ModuleConfig.INSTANCE.isEnabled(this) && Minecraft.getMinecraft().getCurrentServerData() != null && Minecraft.getMinecraft().getCurrentServerData().serverIP != null) {
            final Matcher matcher = this.pattern.matcher(chatComponent.getUnformattedText().replace("\n", ""));
            if (matcher.matches()) {
                String name = matcher.group("name");
                if (name.startsWith("[")) {
                    name = name.substring(name.indexOf("] ") + 2);
                }
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/friend accept " + name);
            }
        }
    }
}
