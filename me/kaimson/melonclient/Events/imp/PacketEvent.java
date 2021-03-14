package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Event;
import net.minecraft.network.Packet;

public class PacketEvent extends Event {
  public Packet packet;
  
  public PacketEvent(Packet packet) {
    this.packet = packet;
  }
  
  public static class Receive extends PacketEvent {
    public Receive(Packet packet) {
      super(packet);
    }
  }
}
