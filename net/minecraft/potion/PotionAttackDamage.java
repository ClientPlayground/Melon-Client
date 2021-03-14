package net.minecraft.potion;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.ResourceLocation;

public class PotionAttackDamage extends Potion {
  protected PotionAttackDamage(int potionID, ResourceLocation location, boolean badEffect, int potionColor) {
    super(potionID, location, badEffect, potionColor);
  }
  
  public double getAttributeModifierAmount(int p_111183_1_, AttributeModifier modifier) {
    return (this.id == Potion.weakness.id) ? (-0.5F * (p_111183_1_ + 1)) : (1.3D * (p_111183_1_ + 1));
  }
}
