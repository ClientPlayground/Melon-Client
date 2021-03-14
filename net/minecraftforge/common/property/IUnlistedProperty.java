package net.minecraftforge.common.property;

public interface IUnlistedProperty<V> {
  String getName();
  
  boolean isValid(V paramV);
  
  Class<V> getType();
  
  String valueToString(V paramV);
}
