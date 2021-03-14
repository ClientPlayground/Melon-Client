package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Event;
import net.minecraft.client.multiplayer.ServerData;

public class ServerEvent extends Event {
  public ServerData serverData;
  
  public ServerEvent(ServerData serverData) {
    this.serverData = serverData;
  }
  
  public static class Join extends Event {
    public String ip;
    
    public Join(String ip) {
      this.ip = ip;
    }
  }
  
  public static class Leave extends Event {}
}
