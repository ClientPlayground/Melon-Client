package net.minecraft.entity.ai.attributes;

public interface IAttribute {
  String getAttributeUnlocalizedName();
  
  double clampValue(double paramDouble);
  
  double getDefaultValue();
  
  boolean getShouldWatch();
  
  IAttribute func_180372_d();
}
