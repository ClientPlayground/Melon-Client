package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.handler.codec.CharSequenceValueConverter;
import com.github.steveice10.netty.handler.codec.Headers;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.HashingStrategy;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class ReadOnlyHttp2Headers implements Http2Headers {
  private static final byte PSEUDO_HEADER_TOKEN = 58;
  
  private final AsciiString[] pseudoHeaders;
  
  private final AsciiString[] otherHeaders;
  
  public static ReadOnlyHttp2Headers trailers(boolean validateHeaders, AsciiString... otherHeaders) {
    return new ReadOnlyHttp2Headers(validateHeaders, EmptyArrays.EMPTY_ASCII_STRINGS, otherHeaders);
  }
  
  public static ReadOnlyHttp2Headers clientHeaders(boolean validateHeaders, AsciiString method, AsciiString path, AsciiString scheme, AsciiString authority, AsciiString... otherHeaders) {
    return new ReadOnlyHttp2Headers(validateHeaders, new AsciiString[] { Http2Headers.PseudoHeaderName.METHOD
          
          .value(), method, Http2Headers.PseudoHeaderName.PATH.value(), path, Http2Headers.PseudoHeaderName.SCHEME
          .value(), scheme, Http2Headers.PseudoHeaderName.AUTHORITY.value(), authority }, otherHeaders);
  }
  
  public static ReadOnlyHttp2Headers serverHeaders(boolean validateHeaders, AsciiString status, AsciiString... otherHeaders) {
    return new ReadOnlyHttp2Headers(validateHeaders, new AsciiString[] { Http2Headers.PseudoHeaderName.STATUS
          .value(), status }, otherHeaders);
  }
  
  private ReadOnlyHttp2Headers(boolean validateHeaders, AsciiString[] pseudoHeaders, AsciiString... otherHeaders) {
    assert (pseudoHeaders.length & 0x1) == 0;
    if ((otherHeaders.length & 0x1) != 0)
      throw newInvalidArraySizeException(); 
    if (validateHeaders)
      validateHeaders(pseudoHeaders, otherHeaders); 
    this.pseudoHeaders = pseudoHeaders;
    this.otherHeaders = otherHeaders;
  }
  
  private static IllegalArgumentException newInvalidArraySizeException() {
    return new IllegalArgumentException("pseudoHeaders and otherHeaders must be arrays of [name, value] pairs");
  }
  
  private static void validateHeaders(AsciiString[] pseudoHeaders, AsciiString... otherHeaders) {
    for (int i = 1; i < pseudoHeaders.length; i += 2) {
      if (pseudoHeaders[i] == null)
        throw new IllegalArgumentException("pseudoHeaders value at index " + i + " is null"); 
    } 
    boolean seenNonPseudoHeader = false;
    int otherHeadersEnd = otherHeaders.length - 1;
    for (int j = 0; j < otherHeadersEnd; j += 2) {
      AsciiString name = otherHeaders[j];
      DefaultHttp2Headers.HTTP2_NAME_VALIDATOR.validateName(name);
      if (!seenNonPseudoHeader && !name.isEmpty() && name.byteAt(0) != 58) {
        seenNonPseudoHeader = true;
      } else if (seenNonPseudoHeader && !name.isEmpty() && name.byteAt(0) == 58) {
        throw new IllegalArgumentException("otherHeaders name at index " + j + " is a pseudo header that appears after non-pseudo headers.");
      } 
      if (otherHeaders[j + 1] == null)
        throw new IllegalArgumentException("otherHeaders value at index " + (j + 1) + " is null"); 
    } 
  }
  
  private AsciiString get0(CharSequence name) {
    int nameHash = AsciiString.hashCode(name);
    int pseudoHeadersEnd = this.pseudoHeaders.length - 1;
    for (int i = 0; i < pseudoHeadersEnd; i += 2) {
      AsciiString roName = this.pseudoHeaders[i];
      if (roName.hashCode() == nameHash && roName.contentEqualsIgnoreCase(name))
        return this.pseudoHeaders[i + 1]; 
    } 
    int otherHeadersEnd = this.otherHeaders.length - 1;
    for (int j = 0; j < otherHeadersEnd; j += 2) {
      AsciiString roName = this.otherHeaders[j];
      if (roName.hashCode() == nameHash && roName.contentEqualsIgnoreCase(name))
        return this.otherHeaders[j + 1]; 
    } 
    return null;
  }
  
  public CharSequence get(CharSequence name) {
    return (CharSequence)get0(name);
  }
  
  public CharSequence get(CharSequence name, CharSequence defaultValue) {
    CharSequence value = get(name);
    return (value != null) ? value : defaultValue;
  }
  
  public CharSequence getAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public CharSequence getAndRemove(CharSequence name, CharSequence defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public List<CharSequence> getAll(CharSequence name) {
    int nameHash = AsciiString.hashCode(name);
    List<CharSequence> values = new ArrayList<CharSequence>();
    int pseudoHeadersEnd = this.pseudoHeaders.length - 1;
    for (int i = 0; i < pseudoHeadersEnd; i += 2) {
      AsciiString roName = this.pseudoHeaders[i];
      if (roName.hashCode() == nameHash && roName.contentEqualsIgnoreCase(name))
        values.add(this.pseudoHeaders[i + 1]); 
    } 
    int otherHeadersEnd = this.otherHeaders.length - 1;
    for (int j = 0; j < otherHeadersEnd; j += 2) {
      AsciiString roName = this.otherHeaders[j];
      if (roName.hashCode() == nameHash && roName.contentEqualsIgnoreCase(name))
        values.add(this.otherHeaders[j + 1]); 
    } 
    return values;
  }
  
  public List<CharSequence> getAllAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Boolean getBoolean(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Boolean.valueOf(CharSequenceValueConverter.INSTANCE.convertToBoolean((CharSequence)value)) : null;
  }
  
  public boolean getBoolean(CharSequence name, boolean defaultValue) {
    Boolean value = getBoolean(name);
    return (value != null) ? value.booleanValue() : defaultValue;
  }
  
  public Byte getByte(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Byte.valueOf(CharSequenceValueConverter.INSTANCE.convertToByte((CharSequence)value)) : null;
  }
  
  public byte getByte(CharSequence name, byte defaultValue) {
    Byte value = getByte(name);
    return (value != null) ? value.byteValue() : defaultValue;
  }
  
  public Character getChar(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Character.valueOf(CharSequenceValueConverter.INSTANCE.convertToChar((CharSequence)value)) : null;
  }
  
  public char getChar(CharSequence name, char defaultValue) {
    Character value = getChar(name);
    return (value != null) ? value.charValue() : defaultValue;
  }
  
  public Short getShort(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Short.valueOf(CharSequenceValueConverter.INSTANCE.convertToShort((CharSequence)value)) : null;
  }
  
  public short getShort(CharSequence name, short defaultValue) {
    Short value = getShort(name);
    return (value != null) ? value.shortValue() : defaultValue;
  }
  
  public Integer getInt(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Integer.valueOf(CharSequenceValueConverter.INSTANCE.convertToInt((CharSequence)value)) : null;
  }
  
  public int getInt(CharSequence name, int defaultValue) {
    Integer value = getInt(name);
    return (value != null) ? value.intValue() : defaultValue;
  }
  
  public Long getLong(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Long.valueOf(CharSequenceValueConverter.INSTANCE.convertToLong((CharSequence)value)) : null;
  }
  
  public long getLong(CharSequence name, long defaultValue) {
    Long value = getLong(name);
    return (value != null) ? value.longValue() : defaultValue;
  }
  
  public Float getFloat(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Float.valueOf(CharSequenceValueConverter.INSTANCE.convertToFloat((CharSequence)value)) : null;
  }
  
  public float getFloat(CharSequence name, float defaultValue) {
    Float value = getFloat(name);
    return (value != null) ? value.floatValue() : defaultValue;
  }
  
  public Double getDouble(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Double.valueOf(CharSequenceValueConverter.INSTANCE.convertToDouble((CharSequence)value)) : null;
  }
  
  public double getDouble(CharSequence name, double defaultValue) {
    Double value = getDouble(name);
    return (value != null) ? value.doubleValue() : defaultValue;
  }
  
  public Long getTimeMillis(CharSequence name) {
    AsciiString value = get0(name);
    return (value != null) ? Long.valueOf(CharSequenceValueConverter.INSTANCE.convertToTimeMillis((CharSequence)value)) : null;
  }
  
  public long getTimeMillis(CharSequence name, long defaultValue) {
    Long value = getTimeMillis(name);
    return (value != null) ? value.longValue() : defaultValue;
  }
  
  public Boolean getBooleanAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public boolean getBooleanAndRemove(CharSequence name, boolean defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Byte getByteAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public byte getByteAndRemove(CharSequence name, byte defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Character getCharAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public char getCharAndRemove(CharSequence name, char defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Short getShortAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public short getShortAndRemove(CharSequence name, short defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Integer getIntAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public int getIntAndRemove(CharSequence name, int defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Long getLongAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public long getLongAndRemove(CharSequence name, long defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Float getFloatAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public float getFloatAndRemove(CharSequence name, float defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Double getDoubleAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public double getDoubleAndRemove(CharSequence name, double defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Long getTimeMillisAndRemove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public long getTimeMillisAndRemove(CharSequence name, long defaultValue) {
    throw new UnsupportedOperationException("read only");
  }
  
  public boolean contains(CharSequence name) {
    return (get(name) != null);
  }
  
  public boolean contains(CharSequence name, CharSequence value) {
    return contains(name, value, false);
  }
  
  public boolean containsObject(CharSequence name, Object value) {
    if (value instanceof CharSequence)
      return contains(name, (CharSequence)value); 
    return contains(name, value.toString());
  }
  
  public boolean containsBoolean(CharSequence name, boolean value) {
    return contains(name, String.valueOf(value));
  }
  
  public boolean containsByte(CharSequence name, byte value) {
    return contains(name, String.valueOf(value));
  }
  
  public boolean containsChar(CharSequence name, char value) {
    return contains(name, String.valueOf(value));
  }
  
  public boolean containsShort(CharSequence name, short value) {
    return contains(name, String.valueOf(value));
  }
  
  public boolean containsInt(CharSequence name, int value) {
    return contains(name, String.valueOf(value));
  }
  
  public boolean containsLong(CharSequence name, long value) {
    return contains(name, String.valueOf(value));
  }
  
  public boolean containsFloat(CharSequence name, float value) {
    return false;
  }
  
  public boolean containsDouble(CharSequence name, double value) {
    return contains(name, String.valueOf(value));
  }
  
  public boolean containsTimeMillis(CharSequence name, long value) {
    return contains(name, String.valueOf(value));
  }
  
  public int size() {
    return this.pseudoHeaders.length + this.otherHeaders.length >>> 1;
  }
  
  public boolean isEmpty() {
    return (this.pseudoHeaders.length == 0 && this.otherHeaders.length == 0);
  }
  
  public Set<CharSequence> names() {
    if (isEmpty())
      return Collections.emptySet(); 
    Set<CharSequence> names = new LinkedHashSet<CharSequence>(size());
    int pseudoHeadersEnd = this.pseudoHeaders.length - 1;
    for (int i = 0; i < pseudoHeadersEnd; i += 2)
      names.add(this.pseudoHeaders[i]); 
    int otherHeadersEnd = this.otherHeaders.length - 1;
    for (int j = 0; j < otherHeadersEnd; j += 2)
      names.add(this.otherHeaders[j]); 
    return names;
  }
  
  public Http2Headers add(CharSequence name, CharSequence value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers add(CharSequence name, Iterable<? extends CharSequence> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers add(CharSequence name, CharSequence... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addObject(CharSequence name, Object value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addObject(CharSequence name, Iterable<?> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addObject(CharSequence name, Object... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addBoolean(CharSequence name, boolean value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addByte(CharSequence name, byte value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addChar(CharSequence name, char value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addShort(CharSequence name, short value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addInt(CharSequence name, int value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addLong(CharSequence name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addFloat(CharSequence name, float value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addDouble(CharSequence name, double value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers addTimeMillis(CharSequence name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers add(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers set(CharSequence name, CharSequence value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers set(CharSequence name, Iterable<? extends CharSequence> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers set(CharSequence name, CharSequence... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setObject(CharSequence name, Object value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setObject(CharSequence name, Iterable<?> values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setObject(CharSequence name, Object... values) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setBoolean(CharSequence name, boolean value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setByte(CharSequence name, byte value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setChar(CharSequence name, char value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setShort(CharSequence name, short value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setInt(CharSequence name, int value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setLong(CharSequence name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setFloat(CharSequence name, float value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setDouble(CharSequence name, double value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setTimeMillis(CharSequence name, long value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers set(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers setAll(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
    throw new UnsupportedOperationException("read only");
  }
  
  public boolean remove(CharSequence name) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers clear() {
    throw new UnsupportedOperationException("read only");
  }
  
  public Iterator<Map.Entry<CharSequence, CharSequence>> iterator() {
    return new ReadOnlyIterator();
  }
  
  public Iterator<CharSequence> valueIterator(CharSequence name) {
    return new ReadOnlyValueIterator(name);
  }
  
  public Http2Headers method(CharSequence value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers scheme(CharSequence value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers authority(CharSequence value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers path(CharSequence value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public Http2Headers status(CharSequence value) {
    throw new UnsupportedOperationException("read only");
  }
  
  public CharSequence method() {
    return get((CharSequence)Http2Headers.PseudoHeaderName.METHOD.value());
  }
  
  public CharSequence scheme() {
    return get((CharSequence)Http2Headers.PseudoHeaderName.SCHEME.value());
  }
  
  public CharSequence authority() {
    return get((CharSequence)Http2Headers.PseudoHeaderName.AUTHORITY.value());
  }
  
  public CharSequence path() {
    return get((CharSequence)Http2Headers.PseudoHeaderName.PATH.value());
  }
  
  public CharSequence status() {
    return get((CharSequence)Http2Headers.PseudoHeaderName.STATUS.value());
  }
  
  public boolean contains(CharSequence name, CharSequence value, boolean caseInsensitive) {
    int nameHash = AsciiString.hashCode(name);
    HashingStrategy<CharSequence> strategy = caseInsensitive ? AsciiString.CASE_INSENSITIVE_HASHER : AsciiString.CASE_SENSITIVE_HASHER;
    int valueHash = strategy.hashCode(value);
    return (contains(name, nameHash, value, valueHash, strategy, this.otherHeaders) || 
      contains(name, nameHash, value, valueHash, strategy, this.pseudoHeaders));
  }
  
  private static boolean contains(CharSequence name, int nameHash, CharSequence value, int valueHash, HashingStrategy<CharSequence> hashingStrategy, AsciiString[] headers) {
    int headersEnd = headers.length - 1;
    for (int i = 0; i < headersEnd; i += 2) {
      AsciiString roName = headers[i];
      AsciiString roValue = headers[i + 1];
      if (roName.hashCode() == nameHash && roValue.hashCode() == valueHash && roName
        .contentEqualsIgnoreCase(name) && hashingStrategy.equals(roValue, value))
        return true; 
    } 
    return false;
  }
  
  public String toString() {
    StringBuilder builder = (new StringBuilder(getClass().getSimpleName())).append('[');
    String separator = "";
    for (Map.Entry<CharSequence, CharSequence> entry : (Iterable<Map.Entry<CharSequence, CharSequence>>)this) {
      builder.append(separator);
      builder.append(entry.getKey()).append(": ").append(entry.getValue());
      separator = ", ";
    } 
    return builder.append(']').toString();
  }
  
  private final class ReadOnlyValueIterator implements Iterator<CharSequence> {
    private int i;
    
    private final int nameHash;
    
    private final CharSequence name;
    
    private AsciiString[] current = (ReadOnlyHttp2Headers.this.pseudoHeaders.length != 0) ? ReadOnlyHttp2Headers.this.pseudoHeaders : ReadOnlyHttp2Headers.this.otherHeaders;
    
    private AsciiString next;
    
    ReadOnlyValueIterator(CharSequence name) {
      this.nameHash = AsciiString.hashCode(name);
      this.name = name;
      calculateNext();
    }
    
    public boolean hasNext() {
      return (this.next != null);
    }
    
    public CharSequence next() {
      if (!hasNext())
        throw new NoSuchElementException(); 
      AsciiString asciiString = this.next;
      calculateNext();
      return (CharSequence)asciiString;
    }
    
    public void remove() {
      throw new UnsupportedOperationException("read only");
    }
    
    private void calculateNext() {
      for (; this.i < this.current.length; this.i += 2) {
        AsciiString roName = this.current[this.i];
        if (roName.hashCode() == this.nameHash && roName.contentEqualsIgnoreCase(this.name)) {
          this.next = this.current[this.i + 1];
          this.i += 2;
          return;
        } 
      } 
      if (this.i >= this.current.length && this.current == ReadOnlyHttp2Headers.this.pseudoHeaders) {
        this.i = 0;
        this.current = ReadOnlyHttp2Headers.this.otherHeaders;
        calculateNext();
      } else {
        this.next = null;
      } 
    }
  }
  
  private final class ReadOnlyIterator implements Map.Entry<CharSequence, CharSequence>, Iterator<Map.Entry<CharSequence, CharSequence>> {
    private int i;
    
    private AsciiString[] current = (ReadOnlyHttp2Headers.this.pseudoHeaders.length != 0) ? ReadOnlyHttp2Headers.this.pseudoHeaders : ReadOnlyHttp2Headers.this.otherHeaders;
    
    private AsciiString key;
    
    private AsciiString value;
    
    public boolean hasNext() {
      return (this.i != this.current.length);
    }
    
    public Map.Entry<CharSequence, CharSequence> next() {
      if (!hasNext())
        throw new NoSuchElementException(); 
      this.key = this.current[this.i];
      this.value = this.current[this.i + 1];
      this.i += 2;
      if (this.i == this.current.length && this.current == ReadOnlyHttp2Headers.this.pseudoHeaders) {
        this.current = ReadOnlyHttp2Headers.this.otherHeaders;
        this.i = 0;
      } 
      return this;
    }
    
    public CharSequence getKey() {
      return (CharSequence)this.key;
    }
    
    public CharSequence getValue() {
      return (CharSequence)this.value;
    }
    
    public CharSequence setValue(CharSequence value) {
      throw new UnsupportedOperationException("read only");
    }
    
    public void remove() {
      throw new UnsupportedOperationException("read only");
    }
    
    public String toString() {
      return this.key.toString() + '=' + this.value.toString();
    }
    
    private ReadOnlyIterator() {}
  }
}
