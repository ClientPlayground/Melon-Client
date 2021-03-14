package com.github.steveice10.netty.handler.codec;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Headers<K, V, T extends Headers<K, V, T>> extends Iterable<Map.Entry<K, V>> {
  V get(K paramK);
  
  V get(K paramK, V paramV);
  
  V getAndRemove(K paramK);
  
  V getAndRemove(K paramK, V paramV);
  
  List<V> getAll(K paramK);
  
  List<V> getAllAndRemove(K paramK);
  
  Boolean getBoolean(K paramK);
  
  boolean getBoolean(K paramK, boolean paramBoolean);
  
  Byte getByte(K paramK);
  
  byte getByte(K paramK, byte paramByte);
  
  Character getChar(K paramK);
  
  char getChar(K paramK, char paramChar);
  
  Short getShort(K paramK);
  
  short getShort(K paramK, short paramShort);
  
  Integer getInt(K paramK);
  
  int getInt(K paramK, int paramInt);
  
  Long getLong(K paramK);
  
  long getLong(K paramK, long paramLong);
  
  Float getFloat(K paramK);
  
  float getFloat(K paramK, float paramFloat);
  
  Double getDouble(K paramK);
  
  double getDouble(K paramK, double paramDouble);
  
  Long getTimeMillis(K paramK);
  
  long getTimeMillis(K paramK, long paramLong);
  
  Boolean getBooleanAndRemove(K paramK);
  
  boolean getBooleanAndRemove(K paramK, boolean paramBoolean);
  
  Byte getByteAndRemove(K paramK);
  
  byte getByteAndRemove(K paramK, byte paramByte);
  
  Character getCharAndRemove(K paramK);
  
  char getCharAndRemove(K paramK, char paramChar);
  
  Short getShortAndRemove(K paramK);
  
  short getShortAndRemove(K paramK, short paramShort);
  
  Integer getIntAndRemove(K paramK);
  
  int getIntAndRemove(K paramK, int paramInt);
  
  Long getLongAndRemove(K paramK);
  
  long getLongAndRemove(K paramK, long paramLong);
  
  Float getFloatAndRemove(K paramK);
  
  float getFloatAndRemove(K paramK, float paramFloat);
  
  Double getDoubleAndRemove(K paramK);
  
  double getDoubleAndRemove(K paramK, double paramDouble);
  
  Long getTimeMillisAndRemove(K paramK);
  
  long getTimeMillisAndRemove(K paramK, long paramLong);
  
  boolean contains(K paramK);
  
  boolean contains(K paramK, V paramV);
  
  boolean containsObject(K paramK, Object paramObject);
  
  boolean containsBoolean(K paramK, boolean paramBoolean);
  
  boolean containsByte(K paramK, byte paramByte);
  
  boolean containsChar(K paramK, char paramChar);
  
  boolean containsShort(K paramK, short paramShort);
  
  boolean containsInt(K paramK, int paramInt);
  
  boolean containsLong(K paramK, long paramLong);
  
  boolean containsFloat(K paramK, float paramFloat);
  
  boolean containsDouble(K paramK, double paramDouble);
  
  boolean containsTimeMillis(K paramK, long paramLong);
  
  int size();
  
  boolean isEmpty();
  
  Set<K> names();
  
  T add(K paramK, V paramV);
  
  T add(K paramK, Iterable<? extends V> paramIterable);
  
  T add(K paramK, V... paramVarArgs);
  
  T addObject(K paramK, Object paramObject);
  
  T addObject(K paramK, Iterable<?> paramIterable);
  
  T addObject(K paramK, Object... paramVarArgs);
  
  T addBoolean(K paramK, boolean paramBoolean);
  
  T addByte(K paramK, byte paramByte);
  
  T addChar(K paramK, char paramChar);
  
  T addShort(K paramK, short paramShort);
  
  T addInt(K paramK, int paramInt);
  
  T addLong(K paramK, long paramLong);
  
  T addFloat(K paramK, float paramFloat);
  
  T addDouble(K paramK, double paramDouble);
  
  T addTimeMillis(K paramK, long paramLong);
  
  T add(Headers<? extends K, ? extends V, ?> paramHeaders);
  
  T set(K paramK, V paramV);
  
  T set(K paramK, Iterable<? extends V> paramIterable);
  
  T set(K paramK, V... paramVarArgs);
  
  T setObject(K paramK, Object paramObject);
  
  T setObject(K paramK, Iterable<?> paramIterable);
  
  T setObject(K paramK, Object... paramVarArgs);
  
  T setBoolean(K paramK, boolean paramBoolean);
  
  T setByte(K paramK, byte paramByte);
  
  T setChar(K paramK, char paramChar);
  
  T setShort(K paramK, short paramShort);
  
  T setInt(K paramK, int paramInt);
  
  T setLong(K paramK, long paramLong);
  
  T setFloat(K paramK, float paramFloat);
  
  T setDouble(K paramK, double paramDouble);
  
  T setTimeMillis(K paramK, long paramLong);
  
  T set(Headers<? extends K, ? extends V, ?> paramHeaders);
  
  T setAll(Headers<? extends K, ? extends V, ?> paramHeaders);
  
  boolean remove(K paramK);
  
  T clear();
  
  Iterator<Map.Entry<K, V>> iterator();
}
