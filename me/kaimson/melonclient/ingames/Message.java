package me.kaimson.melonclient.ingames;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import me.kaimson.melonclient.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public enum Message {
  FPS(MessageObject.PREFIX, "fps"),
  COORDINATES(MessageObject.PREFIX, "coordinates"),
  PING(MessageObject.PREFIX, "ping"),
  MEMORY_USAGE(MessageObject.PREFIX, "memoryUsage"),
  CPS(MessageObject.PREFIX, "cps"),
  NONE(MessageObject.PREFIX, "none"),
  EDITABLE(MessageObject.PREFIX, "none");
  
  Message(MessageObject messageObject, String memberName) {
    this.messageObject = messageObject;
    this.memberName = memberName;
  }
  
  private final MessageObject messageObject;
  
  private final String memberName;
  
  public MessageObject getMessageObject() {
    return this.messageObject;
  }
  
  public String getMemberName() {
    return this.memberName;
  }
  
  public String getTranslated(String... variables) {
    String text = null;
    try {
      List<String> path = getMessageObject().getPath();
      JsonObject jsonObject = Client.config.getLanguageConfig();
      for (String part : path) {
        if (!part.isEmpty())
          jsonObject = jsonObject.getAsJsonObject(part); 
      } 
      text = jsonObject.get(getMemberName()).getAsString();
      if (text != null) {
        int lcps;
        int rcps;
        int ping;
        BlockPos pos;
        long i;
        long j;
        long k;
        long l;
        switch (this) {
          case FPS:
            text = insert(text, "fps", String.valueOf(Minecraft.getDebugFPS()));
            break;
          case CPS:
            lcps = Client.cps.leftCounter.getCPS();
            rcps = Client.cps.rightCounter.getCPS();
            text = insert(text, "lcps", Integer.valueOf(lcps));
            text = insert(text, "rcps", Integer.valueOf(rcps));
            break;
          case PING:
            ping = 0;
            if (Minecraft.getMinecraft().getNetHandler().getPlayerInfo((Minecraft.getMinecraft()).thePlayer.getUniqueID()) != null)
              ping = Minecraft.getMinecraft().getNetHandler().getPlayerInfo((Minecraft.getMinecraft()).thePlayer.getUniqueID()).getResponseTime(); 
            text = insert(text, "ping", Integer.valueOf(ping));
            break;
          case COORDINATES:
            pos = (Minecraft.getMinecraft()).thePlayer.getPosition();
            text = insert(text, "x", Integer.valueOf(pos.getX()));
            text = insert(text, "y", Integer.valueOf(pos.getY()));
            text = insert(text, "z", Integer.valueOf(pos.getZ()));
            break;
          case MEMORY_USAGE:
            i = Runtime.getRuntime().maxMemory();
            j = Runtime.getRuntime().totalMemory();
            k = Runtime.getRuntime().freeMemory();
            l = j - k;
            text = insert(text, "usage", String.format("% 2d%% %03d/%03dMB", new Object[] { Long.valueOf(l * 100L / i), Long.valueOf(l / 1024L / 1024L), Long.valueOf(i / 1024L / 1024L) }));
            break;
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return text;
  }
  
  private String insert(String target, String placeholder, Object value) {
    return target.replace("%" + placeholder + "%", String.valueOf(value));
  }
  
  private enum MessageObject {
    PREFIX("prefixes"),
    SETTING("settings");
    
    private final List<String> path;
    
    public List<String> getPath() {
      return this.path;
    }
    
    MessageObject(String path) {
      this.path = Lists.newLinkedList(Arrays.asList(path.split(Pattern.quote("."))));
    }
  }
}
