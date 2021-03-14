package me.kaimson.melonclient.Events;

import me.kaimson.melonclient.Events.imp.TickEvent;
import me.kaimson.melonclient.Events.imp.WorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;

public class EventListener {
  private Minecraft mc;
  
  private WorldClient lastWorld;
  
  public void init() {
    EventHandler.register(this);
    this.mc = Minecraft.getMinecraft();
  }
  
  @TypeEvent
  public void onTick(TickEvent.ClientTick e) {
    if (this.lastWorld != (Minecraft.getMinecraft()).theWorld) {
      EventHandler.call((Event)new WorldEvent.Change((World)this.lastWorld, (World)(Minecraft.getMinecraft()).theWorld));
      this.lastWorld = (Minecraft.getMinecraft()).theWorld;
    } 
  }
}
