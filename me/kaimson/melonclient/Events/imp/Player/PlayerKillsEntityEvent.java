package me.kaimson.melonclient.Events.imp.Player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerKillsEntityEvent extends PlayerEvent {
  Entity entity;
  
  public PlayerKillsEntityEvent(EntityPlayer player, Entity entity) {
    super(player);
    this.entity = entity;
  }
  
  public Entity getEntity() {
    return this.entity;
  }
}
