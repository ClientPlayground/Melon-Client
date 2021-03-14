package net.minecraft.entity.ai.attributes;

import java.util.Collection;
import java.util.UUID;

public interface IAttributeInstance {
  IAttribute getAttribute();
  
  double getBaseValue();
  
  void setBaseValue(double paramDouble);
  
  Collection<AttributeModifier> getModifiersByOperation(int paramInt);
  
  Collection<AttributeModifier> func_111122_c();
  
  boolean hasModifier(AttributeModifier paramAttributeModifier);
  
  AttributeModifier getModifier(UUID paramUUID);
  
  void applyModifier(AttributeModifier paramAttributeModifier);
  
  void removeModifier(AttributeModifier paramAttributeModifier);
  
  void removeAllModifiers();
  
  double getAttributeValue();
}
