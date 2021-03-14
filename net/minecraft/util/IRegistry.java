package net.minecraft.util;

public interface IRegistry<K, V> extends Iterable<V> {
  V getObject(K paramK);
  
  void putObject(K paramK, V paramV);
}
