package com.github.steveice10.netty.handler.codec;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmptyHeaders<K, V, T extends Headers<K, V, T>> implements Headers<K, V, T> {
  public V get(K name) {
    return null;
  }
  
  public V get(K name, V defaultValue) {
    return defaultValue;
  }
  
  public V getAndRemove(K name) {
    return null;
  }
  
  public V getAndRemove(K name, V defaultValue) {
    return defaultValue;
  }
  
  public List<V> getAll(K name) {
    return Collections.emptyList();
  }
  
  public List<V> getAllAndRemove(K name) {
    return Collections.emptyList();
  }
  
  public Boolean getBoolean(K name) {
    return null;
  }
  
  public boolean getBoolean(K name, boolean defaultValue) {
    return defaultValue;
  }
  
  public Byte getByte(K name) {
    return null;
  }
  
  public byte getByte(K name, byte defaultValue) {
    return defaultValue;
  }
  
  public Character getChar(K name) {
    return null;
  }
  
  public char getChar(K name, char defaultValue) {
    return defaultValue;
  }
  
  public Short getShort(K name) {
    return null;
  }
  
  public short getShort(K name, short defaultValue) {
    return defaultValue;
  }
  
  public Integer getInt(K name) {
    return null;
  }
  
  public int getInt(K name, int defaultValue) {
    return defaultValue;
  }
  
  public Long getLong(K name) {
    return null;
  }
  
  public long getLong(K name, long defaultValue) {
    return defaultValue;
  }
  
  public Float getFloat(K name) {
    return null;
  }
  
  public float getFloat(K name, float defaultValue) {
    return defaultValue;
  }
  
  public Double getDouble(K name) {
    return null;
  }
  
  public double getDouble(K name, double defaultValue) {
    return defaultValue;
  }
  
  public Long getTimeMillis(K name) {
    return null;
  }
  
  public long getTimeMillis(K name, long defaultValue) {
    return defaultValue;
  }
  
  public Boolean getBooleanAndRemove(K name) {
    return null;
  }
  
  public boolean getBooleanAndRemove(K name, boolean defaultValue) {
    return defaultValue;
  }
  
  public Byte getByteAndRemove(K name) {
    return null;
  }
  
  public byte getByteAndRemove(K name, byte defaultValue) {
    return defaultValue;
  }
  
  public Character getCharAndRemove(K name) {
    return null;
  }
  
  public char getCharAndRemove(K name, char defaultValue) {
    return defaultValue;
  }
  
  public Short getShortAndRemove(K name) {
    return null;
  }
  
  public short getShortAndRemove(K name, short defaultValue) {
    return defaultValue;
  }
  
  public Integer getIntAndRemove(K name) {
    return null;
  }
  
  public int getIntAndRemove(K name, int defaultValue) {
    return defaultValue;
  }
  
  public Long getLongAndRemove(K name) {
    return null;
  }
  
  public long getLongAndRemove(K name, long defaultValue) {
    return defaultValue;
  }
  
  public Float getFloatAndRemove(K name) {
    return null;
  }
  
  public float getFloatAndRemove(K name, float defaultValue) {
    return defaultValue;
  }
  
  public Double getDoubleAndRemove(K name) {
    return null;
  }
  
  public double getDoubleAndRemove(K name, double defaultValue) {
    return defaultValue;
  }
  
  public Long getTimeMillisAndRemove(K name) {
    return null;
  }
  
  public long getTimeMillisAndRemove(K name, long defaultValue) {
    return defaultValue;
  }
  
  public boolean contains(K name) {
    return false;
  }
  
  public boolean contains(K name, V value) {
    return false;
  }
  
  public boolean containsObject(K name, Object value) {
    return false;
  }
  
  public boolean containsBoolean(K name, boolean value) {
    return false;
  }
  
  public boolean containsByte(K name, byte value) {
    return false;
  }
  
  public boolean containsChar(K name, char value) {
    return false;
  }
  
  public boolean containsShort(K name, short value) {
    return false;
  }
  
  public boolean containsInt(K name, int value) {
    return false;
  }
  
  public boolean containsLong(K name, long value) {
    return false;
  }
  
  public boolean containsFloat(K name, float value) {
    return false;
  }
  
  public boolean containsDouble(K name, double value) {
    return false;
  }
  
  public boolean containsTimeMillis(K name, long value) {
    return false;
  }
  
  public int size() {
    return 0;
  }
  
  public boolean isEmpty() {
    return true;
  }
  
  public Set<K> names() {
    return Collections.emptySet();
  }
  
  public T add(K name, V value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T add(K name, Iterable<? extends V> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T add(K name, V... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addObject(K name, Object value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addObject(K name, Iterable<?> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addObject(K name, Object... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addBoolean(K name, boolean value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addByte(K name, byte value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addChar(K name, char value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addShort(K name, short value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addInt(K name, int value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addLong(K name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addFloat(K name, float value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addDouble(K name, double value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T addTimeMillis(K name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T add(Headers<? extends K, ? extends V, ?> headers) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T set(K name, V value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T set(K name, Iterable<? extends V> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T set(K name, V... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setObject(K name, Object value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setObject(K name, Iterable<?> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setObject(K name, Object... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setBoolean(K name, boolean value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setByte(K name, byte value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setChar(K name, char value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setShort(K name, short value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setInt(K name, int value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setLong(K name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setFloat(K name, float value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setDouble(K name, double value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setTimeMillis(K name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T set(Headers<? extends K, ? extends V, ?> headers) {
    throw new UnsupportedOperationException("read only");
  }
  
  public T setAll(Headers<? extends K, ? extends V, ?> headers) {
    throw new UnsupportedOperationException("read only");
  }
  
  public boolean remove(K name) {
    return false;
  }
  
  public T clear() {
    return thisT();
  }
  
  public Iterator<V> valueIterator(K name) {
    List<V> empty = Collections.emptyList();
    return empty.iterator();
  }
  
  public Iterator<Map.Entry<K, V>> iterator() {
    List<Map.Entry<K, V>> empty = Collections.emptyList();
    return empty.iterator();
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof Headers))
      return false; 
    Headers<?, ?, ?> rhs = (Headers<?, ?, ?>)o;
    return (isEmpty() && rhs.isEmpty());
  }
  
  public int hashCode() {
    return -1028477387;
  }
  
  public String toString() {
    return getClass().getSimpleName() + '[' + ']';
  }
  
  private T thisT() {
    return (T)this;
  }
}
