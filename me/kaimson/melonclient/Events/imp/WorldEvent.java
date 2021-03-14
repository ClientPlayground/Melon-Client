package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Event;
import net.minecraft.world.World;

public class WorldEvent extends Event {
  public static class Change extends WorldEvent {
    World currentWorld;
    
    World newWorld;
    
    public Change(World currentWorld, World newWorld) {
      this.currentWorld = currentWorld;
      this.newWorld = newWorld;
    }
    
    public World getCurrentWorld() {
      return this.currentWorld;
    }
    
    public World getWorld() {
      return this.newWorld;
    }
  }
}
