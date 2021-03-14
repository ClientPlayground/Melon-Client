package me.kaimson.melonclient.Events.imp.Player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class PlayerDamagedEvent extends PlayerEvent {
  DamageSource damageSrc;
  
  float damage;
  
  public PlayerDamagedEvent(EntityPlayer player, DamageSource damageSrc, float damage) {
    super(player);
    this.damageSrc = damageSrc;
    this.damage = damage;
  }
  
  public DamageSource getDamageSource() {
    return this.damageSrc;
  }
  
  public float getDamage() {
    return this.damage;
  }
}
