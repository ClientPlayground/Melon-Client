package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.util.HashingStrategy;
import com.github.steveice10.netty.util.internal.MathUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class DefaultHeaders<K, V, T extends Headers<K, V, T>> implements Headers<K, V, T> {
  static final int HASH_CODE_SEED = -1028477387;
  
  private final HeaderEntry<K, V>[] entries;
  
  protected final HeaderEntry<K, V> head;
  
  private final byte hashMask;
  
  private final ValueConverter<V> valueConverter;
  
  private final NameValidator<K> nameValidator;
  
  private final HashingStrategy<K> hashingStrategy;
  
  int size;
  
  public static interface NameValidator<K> {
    public static final NameValidator NOT_NULL = new NameValidator() {
        public void validateName(Object name) {
          ObjectUtil.checkNotNull(name, "name");
        }
      };
    
    void validateName(K param1K);
  }
  
  public DefaultHeaders(ValueConverter<V> valueConverter) {
    this(HashingStrategy.JAVA_HASHER, valueConverter);
  }
  
  public DefaultHeaders(ValueConverter<V> valueConverter, NameValidator<K> nameValidator) {
    this(HashingStrategy.JAVA_HASHER, valueConverter, nameValidator);
  }
  
  public DefaultHeaders(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter) {
    this(nameHashingStrategy, valueConverter, NameValidator.NOT_NULL);
  }
  
  public DefaultHeaders(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter, NameValidator<K> nameValidator) {
    this(nameHashingStrategy, valueConverter, nameValidator, 16);
  }
  
  public DefaultHeaders(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter, NameValidator<K> nameValidator, int arraySizeHint) {
    this.valueConverter = (ValueConverter<V>)ObjectUtil.checkNotNull(valueConverter, "valueConverter");
    this.nameValidator = (NameValidator<K>)ObjectUtil.checkNotNull(nameValidator, "nameValidator");
    this.hashingStrategy = (HashingStrategy<K>)ObjectUtil.checkNotNull(nameHashingStrategy, "nameHashingStrategy");
    this.entries = (HeaderEntry<K, V>[])new HeaderEntry[MathUtil.findNextPositivePowerOfTwo(Math.max(2, Math.min(arraySizeHint, 128)))];
    this.hashMask = (byte)(this.entries.length - 1);
    this.head = new HeaderEntry<K, V>();
  }
  
  public V get(K name) {
    ObjectUtil.checkNotNull(name, "name");
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    HeaderEntry<K, V> e = this.entries[i];
    V value = null;
    while (e != null) {
      if (e.hash == h && this.hashingStrategy.equals(name, e.key))
        value = e.value; 
      e = e.next;
    } 
    return value;
  }
  
  public V get(K name, V defaultValue) {
    V value = get(name);
    if (value == null)
      return defaultValue; 
    return value;
  }
  
  public V getAndRemove(K name) {
    int h = this.hashingStrategy.hashCode(name);
    return remove0(h, index(h), (K)ObjectUtil.checkNotNull(name, "name"));
  }
  
  public V getAndRemove(K name, V defaultValue) {
    V value = getAndRemove(name);
    if (value == null)
      return defaultValue; 
    return value;
  }
  
  public List<V> getAll(K name) {
    ObjectUtil.checkNotNull(name, "name");
    LinkedList<V> values = new LinkedList<V>();
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    HeaderEntry<K, V> e = this.entries[i];
    while (e != null) {
      if (e.hash == h && this.hashingStrategy.equals(name, e.key))
        values.addFirst(e.getValue()); 
      e = e.next;
    } 
    return values;
  }
  
  public Iterator<V> valueIterator(K name) {
    return new ValueIterator(name);
  }
  
  public List<V> getAllAndRemove(K name) {
    List<V> all = getAll(name);
    remove(name);
    return all;
  }
  
  public boolean contains(K name) {
    return (get(name) != null);
  }
  
  public boolean containsObject(K name, Object value) {
    return contains(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")));
  }
  
  public boolean containsBoolean(K name, boolean value) {
    return contains(name, this.valueConverter.convertBoolean(value));
  }
  
  public boolean containsByte(K name, byte value) {
    return contains(name, this.valueConverter.convertByte(value));
  }
  
  public boolean containsChar(K name, char value) {
    return contains(name, this.valueConverter.convertChar(value));
  }
  
  public boolean containsShort(K name, short value) {
    return contains(name, this.valueConverter.convertShort(value));
  }
  
  public boolean containsInt(K name, int value) {
    return contains(name, this.valueConverter.convertInt(value));
  }
  
  public boolean containsLong(K name, long value) {
    return contains(name, this.valueConverter.convertLong(value));
  }
  
  public boolean containsFloat(K name, float value) {
    return contains(name, this.valueConverter.convertFloat(value));
  }
  
  public boolean containsDouble(K name, double value) {
    return contains(name, this.valueConverter.convertDouble(value));
  }
  
  public boolean containsTimeMillis(K name, long value) {
    return contains(name, this.valueConverter.convertTimeMillis(value));
  }
  
  public boolean contains(K name, V value) {
    return contains(name, value, HashingStrategy.JAVA_HASHER);
  }
  
  public final boolean contains(K name, V value, HashingStrategy<? super V> valueHashingStrategy) {
    ObjectUtil.checkNotNull(name, "name");
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    HeaderEntry<K, V> e = this.entries[i];
    while (e != null) {
      if (e.hash == h && this.hashingStrategy.equals(name, e.key) && valueHashingStrategy.equals(value, e.value))
        return true; 
      e = e.next;
    } 
    return false;
  }
  
  public int size() {
    return this.size;
  }
  
  public boolean isEmpty() {
    return (this.head == this.head.after);
  }
  
  public Set<K> names() {
    if (isEmpty())
      return Collections.emptySet(); 
    Set<K> names = new LinkedHashSet<K>(size());
    HeaderEntry<K, V> e = this.head.after;
    while (e != this.head) {
      names.add(e.getKey());
      e = e.after;
    } 
    return names;
  }
  
  public T add(K name, V value) {
    this.nameValidator.validateName(name);
    ObjectUtil.checkNotNull(value, "value");
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    add0(h, i, name, value);
    return thisT();
  }
  
  public T add(K name, Iterable<? extends V> values) {
    this.nameValidator.validateName(name);
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    for (V v : values)
      add0(h, i, name, v); 
    return thisT();
  }
  
  public T add(K name, V... values) {
    this.nameValidator.validateName(name);
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    for (V v : values)
      add0(h, i, name, v); 
    return thisT();
  }
  
  public T addObject(K name, Object value) {
    return add(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")));
  }
  
  public T addObject(K name, Iterable<?> values) {
    for (Object value : values)
      addObject(name, value); 
    return thisT();
  }
  
  public T addObject(K name, Object... values) {
    for (Object value : values)
      addObject(name, value); 
    return thisT();
  }
  
  public T addInt(K name, int value) {
    return add(name, this.valueConverter.convertInt(value));
  }
  
  public T addLong(K name, long value) {
    return add(name, this.valueConverter.convertLong(value));
  }
  
  public T addDouble(K name, double value) {
    return add(name, this.valueConverter.convertDouble(value));
  }
  
  public T addTimeMillis(K name, long value) {
    return add(name, this.valueConverter.convertTimeMillis(value));
  }
  
  public T addChar(K name, char value) {
    return add(name, this.valueConverter.convertChar(value));
  }
  
  public T addBoolean(K name, boolean value) {
    return add(name, this.valueConverter.convertBoolean(value));
  }
  
  public T addFloat(K name, float value) {
    return add(name, this.valueConverter.convertFloat(value));
  }
  
  public T addByte(K name, byte value) {
    return add(name, this.valueConverter.convertByte(value));
  }
  
  public T addShort(K name, short value) {
    return add(name, this.valueConverter.convertShort(value));
  }
  
  public T add(Headers<? extends K, ? extends V, ?> headers) {
    if (headers == this)
      throw new IllegalArgumentException("can't add to itself."); 
    addImpl(headers);
    return thisT();
  }
  
  protected void addImpl(Headers<? extends K, ? extends V, ?> headers) {
    if (headers instanceof DefaultHeaders) {
      DefaultHeaders<? extends K, ? extends V, T> defaultHeaders = (DefaultHeaders)headers;
      HeaderEntry<? extends K, ? extends V> e = defaultHeaders.head.after;
      if (defaultHeaders.hashingStrategy == this.hashingStrategy && defaultHeaders.nameValidator == this.nameValidator) {
        while (e != defaultHeaders.head) {
          add0(e.hash, index(e.hash), e.key, e.value);
          e = e.after;
        } 
      } else {
        while (e != defaultHeaders.head) {
          add(e.key, e.value);
          e = e.after;
        } 
      } 
    } else {
      for (Map.Entry<? extends K, ? extends V> header : headers)
        add(header.getKey(), header.getValue()); 
    } 
  }
  
  public T set(K name, V value) {
    this.nameValidator.validateName(name);
    ObjectUtil.checkNotNull(value, "value");
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    remove0(h, i, name);
    add0(h, i, name, value);
    return thisT();
  }
  
  public T set(K name, Iterable<? extends V> values) {
    this.nameValidator.validateName(name);
    ObjectUtil.checkNotNull(values, "values");
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    remove0(h, i, name);
    for (V v : values) {
      if (v == null)
        break; 
      add0(h, i, name, v);
    } 
    return thisT();
  }
  
  public T set(K name, V... values) {
    this.nameValidator.validateName(name);
    ObjectUtil.checkNotNull(values, "values");
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    remove0(h, i, name);
    for (V v : values) {
      if (v == null)
        break; 
      add0(h, i, name, v);
    } 
    return thisT();
  }
  
  public T setObject(K name, Object value) {
    ObjectUtil.checkNotNull(value, "value");
    V convertedValue = (V)ObjectUtil.checkNotNull(this.valueConverter.convertObject(value), "convertedValue");
    return set(name, convertedValue);
  }
  
  public T setObject(K name, Iterable<?> values) {
    this.nameValidator.validateName(name);
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    remove0(h, i, name);
    for (Object v : values) {
      if (v == null)
        break; 
      add0(h, i, name, this.valueConverter.convertObject(v));
    } 
    return thisT();
  }
  
  public T setObject(K name, Object... values) {
    this.nameValidator.validateName(name);
    int h = this.hashingStrategy.hashCode(name);
    int i = index(h);
    remove0(h, i, name);
    for (Object v : values) {
      if (v == null)
        break; 
      add0(h, i, name, this.valueConverter.convertObject(v));
    } 
    return thisT();
  }
  
  public T setInt(K name, int value) {
    return set(name, this.valueConverter.convertInt(value));
  }
  
  public T setLong(K name, long value) {
    return set(name, this.valueConverter.convertLong(value));
  }
  
  public T setDouble(K name, double value) {
    return set(name, this.valueConverter.convertDouble(value));
  }
  
  public T setTimeMillis(K name, long value) {
    return set(name, this.valueConverter.convertTimeMillis(value));
  }
  
  public T setFloat(K name, float value) {
    return set(name, this.valueConverter.convertFloat(value));
  }
  
  public T setChar(K name, char value) {
    return set(name, this.valueConverter.convertChar(value));
  }
  
  public T setBoolean(K name, boolean value) {
    return set(name, this.valueConverter.convertBoolean(value));
  }
  
  public T setByte(K name, byte value) {
    return set(name, this.valueConverter.convertByte(value));
  }
  
  public T setShort(K name, short value) {
    return set(name, this.valueConverter.convertShort(value));
  }
  
  public T set(Headers<? extends K, ? extends V, ?> headers) {
    if (headers != this) {
      clear();
      addImpl(headers);
    } 
    return thisT();
  }
  
  public T setAll(Headers<? extends K, ? extends V, ?> headers) {
    if (headers != this) {
      for (K key : headers.names())
        remove(key); 
      addImpl(headers);
    } 
    return thisT();
  }
  
  public boolean remove(K name) {
    return (getAndRemove(name) != null);
  }
  
  public T clear() {
    Arrays.fill((Object[])this.entries, (Object)null);
    this.head.before = this.head.after = this.head;
    this.size = 0;
    return thisT();
  }
  
  public Iterator<Map.Entry<K, V>> iterator() {
    return new HeaderIterator();
  }
  
  public Boolean getBoolean(K name) {
    V v = get(name);
    try {
      return (v != null) ? Boolean.valueOf(this.valueConverter.convertToBoolean(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public boolean getBoolean(K name, boolean defaultValue) {
    Boolean v = getBoolean(name);
    return (v != null) ? v.booleanValue() : defaultValue;
  }
  
  public Byte getByte(K name) {
    V v = get(name);
    try {
      return (v != null) ? Byte.valueOf(this.valueConverter.convertToByte(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public byte getByte(K name, byte defaultValue) {
    Byte v = getByte(name);
    return (v != null) ? v.byteValue() : defaultValue;
  }
  
  public Character getChar(K name) {
    V v = get(name);
    try {
      return (v != null) ? Character.valueOf(this.valueConverter.convertToChar(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public char getChar(K name, char defaultValue) {
    Character v = getChar(name);
    return (v != null) ? v.charValue() : defaultValue;
  }
  
  public Short getShort(K name) {
    V v = get(name);
    try {
      return (v != null) ? Short.valueOf(this.valueConverter.convertToShort(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public short getShort(K name, short defaultValue) {
    Short v = getShort(name);
    return (v != null) ? v.shortValue() : defaultValue;
  }
  
  public Integer getInt(K name) {
    V v = get(name);
    try {
      return (v != null) ? Integer.valueOf(this.valueConverter.convertToInt(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public int getInt(K name, int defaultValue) {
    Integer v = getInt(name);
    return (v != null) ? v.intValue() : defaultValue;
  }
  
  public Long getLong(K name) {
    V v = get(name);
    try {
      return (v != null) ? Long.valueOf(this.valueConverter.convertToLong(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public long getLong(K name, long defaultValue) {
    Long v = getLong(name);
    return (v != null) ? v.longValue() : defaultValue;
  }
  
  public Float getFloat(K name) {
    V v = get(name);
    try {
      return (v != null) ? Float.valueOf(this.valueConverter.convertToFloat(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public float getFloat(K name, float defaultValue) {
    Float v = getFloat(name);
    return (v != null) ? v.floatValue() : defaultValue;
  }
  
  public Double getDouble(K name) {
    V v = get(name);
    try {
      return (v != null) ? Double.valueOf(this.valueConverter.convertToDouble(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public double getDouble(K name, double defaultValue) {
    Double v = getDouble(name);
    return (v != null) ? v.doubleValue() : defaultValue;
  }
  
  public Long getTimeMillis(K name) {
    V v = get(name);
    try {
      return (v != null) ? Long.valueOf(this.valueConverter.convertToTimeMillis(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public long getTimeMillis(K name, long defaultValue) {
    Long v = getTimeMillis(name);
    return (v != null) ? v.longValue() : defaultValue;
  }
  
  public Boolean getBooleanAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Boolean.valueOf(this.valueConverter.convertToBoolean(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public boolean getBooleanAndRemove(K name, boolean defaultValue) {
    Boolean v = getBooleanAndRemove(name);
    return (v != null) ? v.booleanValue() : defaultValue;
  }
  
  public Byte getByteAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Byte.valueOf(this.valueConverter.convertToByte(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public byte getByteAndRemove(K name, byte defaultValue) {
    Byte v = getByteAndRemove(name);
    return (v != null) ? v.byteValue() : defaultValue;
  }
  
  public Character getCharAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Character.valueOf(this.valueConverter.convertToChar(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public char getCharAndRemove(K name, char defaultValue) {
    Character v = getCharAndRemove(name);
    return (v != null) ? v.charValue() : defaultValue;
  }
  
  public Short getShortAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Short.valueOf(this.valueConverter.convertToShort(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public short getShortAndRemove(K name, short defaultValue) {
    Short v = getShortAndRemove(name);
    return (v != null) ? v.shortValue() : defaultValue;
  }
  
  public Integer getIntAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Integer.valueOf(this.valueConverter.convertToInt(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public int getIntAndRemove(K name, int defaultValue) {
    Integer v = getIntAndRemove(name);
    return (v != null) ? v.intValue() : defaultValue;
  }
  
  public Long getLongAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Long.valueOf(this.valueConverter.convertToLong(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public long getLongAndRemove(K name, long defaultValue) {
    Long v = getLongAndRemove(name);
    return (v != null) ? v.longValue() : defaultValue;
  }
  
  public Float getFloatAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Float.valueOf(this.valueConverter.convertToFloat(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public float getFloatAndRemove(K name, float defaultValue) {
    Float v = getFloatAndRemove(name);
    return (v != null) ? v.floatValue() : defaultValue;
  }
  
  public Double getDoubleAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Double.valueOf(this.valueConverter.convertToDouble(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public double getDoubleAndRemove(K name, double defaultValue) {
    Double v = getDoubleAndRemove(name);
    return (v != null) ? v.doubleValue() : defaultValue;
  }
  
  public Long getTimeMillisAndRemove(K name) {
    V v = getAndRemove(name);
    try {
      return (v != null) ? Long.valueOf(this.valueConverter.convertToTimeMillis(v)) : null;
    } catch (RuntimeException ignore) {
      return null;
    } 
  }
  
  public long getTimeMillisAndRemove(K name, long defaultValue) {
    Long v = getTimeMillisAndRemove(name);
    return (v != null) ? v.longValue() : defaultValue;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof Headers))
      return false; 
    return equals((Headers<K, V, ?>)o, HashingStrategy.JAVA_HASHER);
  }
  
  public int hashCode() {
    return hashCode(HashingStrategy.JAVA_HASHER);
  }
  
  public final boolean equals(Headers<K, V, ?> h2, HashingStrategy<V> valueHashingStrategy) {
    if (h2.size() != size())
      return false; 
    if (this == h2)
      return true; 
    for (K name : names()) {
      List<V> otherValues = h2.getAll(name);
      List<V> values = getAll(name);
      if (otherValues.size() != values.size())
        return false; 
      for (int i = 0; i < otherValues.size(); i++) {
        if (!valueHashingStrategy.equals(otherValues.get(i), values.get(i)))
          return false; 
      } 
    } 
    return true;
  }
  
  public final int hashCode(HashingStrategy<V> valueHashingStrategy) {
    int result = -1028477387;
    for (K name : names()) {
      result = 31 * result + this.hashingStrategy.hashCode(name);
      List<V> values = getAll(name);
      for (int i = 0; i < values.size(); i++)
        result = 31 * result + valueHashingStrategy.hashCode(values.get(i)); 
    } 
    return result;
  }
  
  public String toString() {
    return HeadersUtils.toString(getClass(), iterator(), size());
  }
  
  protected HeaderEntry<K, V> newHeaderEntry(int h, K name, V value, HeaderEntry<K, V> next) {
    return new HeaderEntry<K, V>(h, name, value, next, this.head);
  }
  
  protected ValueConverter<V> valueConverter() {
    return this.valueConverter;
  }
  
  private int index(int hash) {
    return hash & this.hashMask;
  }
  
  private void add0(int h, int i, K name, V value) {
    this.entries[i] = newHeaderEntry(h, name, value, this.entries[i]);
    this.size++;
  }
  
  private V remove0(int h, int i, K name) {
    HeaderEntry<K, V> e = this.entries[i];
    if (e == null)
      return null; 
    V value = null;
    HeaderEntry<K, V> next = e.next;
    while (next != null) {
      if (next.hash == h && this.hashingStrategy.equals(name, next.key)) {
        value = next.value;
        e.next = next.next;
        next.remove();
        this.size--;
      } else {
        e = next;
      } 
      next = e.next;
    } 
    e = this.entries[i];
    if (e.hash == h && this.hashingStrategy.equals(name, e.key)) {
      if (value == null)
        value = e.value; 
      this.entries[i] = e.next;
      e.remove();
      this.size--;
    } 
    return value;
  }
  
  private T thisT() {
    return (T)this;
  }
  
  public DefaultHeaders<K, V, T> copy() {
    DefaultHeaders<K, V, T> copy = new DefaultHeaders(this.hashingStrategy, this.valueConverter, this.nameValidator, this.entries.length);
    copy.addImpl(this);
    return copy;
  }
  
  private final class HeaderIterator implements Iterator<Map.Entry<K, V>> {
    private DefaultHeaders.HeaderEntry<K, V> current = DefaultHeaders.this.head;
    
    public boolean hasNext() {
      return (this.current.after != DefaultHeaders.this.head);
    }
    
    public Map.Entry<K, V> next() {
      this.current = this.current.after;
      if (this.current == DefaultHeaders.this.head)
        throw new NoSuchElementException(); 
      return this.current;
    }
    
    public void remove() {
      throw new UnsupportedOperationException("read only");
    }
    
    private HeaderIterator() {}
  }
  
  private final class ValueIterator implements Iterator<V> {
    private final K name;
    
    private final int hash;
    
    private DefaultHeaders.HeaderEntry<K, V> next;
    
    ValueIterator(K name) {
      this.name = (K)ObjectUtil.checkNotNull(name, "name");
      this.hash = DefaultHeaders.this.hashingStrategy.hashCode(name);
      calculateNext(DefaultHeaders.this.entries[DefaultHeaders.this.index(this.hash)]);
    }
    
    public boolean hasNext() {
      return (this.next != null);
    }
    
    public V next() {
      if (!hasNext())
        throw new NoSuchElementException(); 
      DefaultHeaders.HeaderEntry<K, V> current = this.next;
      calculateNext(this.next.next);
      return current.value;
    }
    
    public void remove() {
      throw new UnsupportedOperationException("read only");
    }
    
    private void calculateNext(DefaultHeaders.HeaderEntry<K, V> entry) {
      while (entry != null) {
        if (entry.hash == this.hash && DefaultHeaders.this.hashingStrategy.equals(this.name, entry.key)) {
          this.next = entry;
          return;
        } 
        entry = entry.next;
      } 
      this.next = null;
    }
  }
  
  protected static class HeaderEntry<K, V> implements Map.Entry<K, V> {
    protected final int hash;
    
    protected final K key;
    
    protected V value;
    
    protected HeaderEntry<K, V> next;
    
    protected HeaderEntry<K, V> before;
    
    protected HeaderEntry<K, V> after;
    
    protected HeaderEntry(int hash, K key) {
      this.hash = hash;
      this.key = key;
    }
    
    HeaderEntry(int hash, K key, V value, HeaderEntry<K, V> next, HeaderEntry<K, V> head) {
      this.hash = hash;
      this.key = key;
      this.value = value;
      this.next = next;
      this.after = head;
      this.before = head.before;
      pointNeighborsToThis();
    }
    
    HeaderEntry() {
      this.hash = -1;
      this.key = null;
      this.before = this.after = this;
    }
    
    protected final void pointNeighborsToThis() {
      this.before.after = this;
      this.after.before = this;
    }
    
    public final HeaderEntry<K, V> before() {
      return this.before;
    }
    
    public final HeaderEntry<K, V> after() {
      return this.after;
    }
    
    protected void remove() {
      this.before.after = this.after;
      this.after.before = this.before;
    }
    
    public final K getKey() {
      return this.key;
    }
    
    public final V getValue() {
      return this.value;
    }
    
    public final V setValue(V value) {
      ObjectUtil.checkNotNull(value, "value");
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }
    
    public final String toString() {
      return this.key.toString() + '=' + this.value.toString();
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> other = (Map.Entry<?, ?>)o;
      return (((getKey() == null) ? (other.getKey() == null) : getKey().equals(other.getKey())) && (
        (getValue() == null) ? (other.getValue() == null) : getValue().equals(other.getValue())));
    }
    
    public int hashCode() {
      return ((this.key == null) ? 0 : this.key.hashCode()) ^ ((this.value == null) ? 0 : this.value.hashCode());
    }
  }
}
