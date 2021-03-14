package me.kaimson.melonclient.ingames.utils;

import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.Player.PlayerHitEntityEvent;
import me.kaimson.melonclient.ingames.Ingame;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public class MoreParticles extends Ingame {
  public void init() {
    super.init();
    EventHandler.register(this);
  }
  
  @TypeEvent
  private void onPlayerHit(PlayerHitEntityEvent e) {
    if (IngameDisplay.MORE_PARTICLES.isEnabled() && 
      e.getEntity() instanceof net.minecraft.client.entity.EntityOtherPlayerMP) {
      EntityPlayerSP p = this.mc.thePlayer;
      boolean critical = (p.fallDistance > 0.0F && !p.onGround && !p.isOnLadder() && !p.isInWater() && !p.isPotionActive(Potion.blindness) && p.ridingEntity == null);
      float enchantment = EnchantmentHelper.getModifierForCreature(p.getHeldItem(), ((EntityLivingBase)e.getEntity()).getCreatureAttribute());
      double multiplier = ((Float)IngameDisplay.MORE_PARTICLES_MULTIPLIER.getOrDefault(Double.valueOf(1.0D))).floatValue();
      for (int i = 1; i < multiplier; i++) {
        if (critical)
          this.mc.thePlayer.onCriticalHit(e.getEntity()); 
        if (enchantment > 0.0F)
          this.mc.thePlayer.onEnchantmentCritical(e.getEntity()); 
      } 
    } 
  }
}
