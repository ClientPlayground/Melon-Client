package net.minecraft.entity;

import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public interface IEntityMultiPart {
  World getWorld();
  
  boolean attackEntityFromPart(EntityDragonPart paramEntityDragonPart, DamageSource paramDamageSource, float paramFloat);
}
