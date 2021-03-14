package me.kaimson.melonclient.Events.imp.Player;

import me.kaimson.melonclient.Events.Cancellable;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerEvent extends Cancellable {
  EntityPlayer player;
  
  public PlayerEvent(EntityPlayer player) {
    this.player = player;
  }
  
  public EntityPlayer getPlayer() {
    return this.player;
  }
  
  public static class Drop extends PlayerEvent {
    public Drop(EntityPlayer player) {
      super(player);
    }
  }
}
