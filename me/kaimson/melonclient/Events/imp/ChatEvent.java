package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Event;
import net.minecraft.util.IChatComponent;

public class ChatEvent extends Event {
  public static class Receive extends ChatEvent {
    public IChatComponent message;
    
    public Receive(IChatComponent message) {
      this.message = message;
    }
  }
  
  public static class Send extends ChatEvent {
    public String message;
    
    public Send(String message) {
      this.message = message;
    }
  }
}
