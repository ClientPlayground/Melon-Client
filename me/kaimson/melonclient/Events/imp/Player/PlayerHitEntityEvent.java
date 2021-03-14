package me.kaimson.melonclient.Events.imp.Player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerHitEntityEvent extends PlayerEvent {
  Entity entity;
  
  public PlayerHitEntityEvent(EntityPlayer player, Entity entity) {
    super(player);
    this.entity = entity;
  }
  
  public Entity getEntity() {
    return this.entity;
  }
}
