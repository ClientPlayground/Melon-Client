package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.handler.codec.CharSequenceValueConverter;
import com.github.steveice10.netty.handler.codec.DateFormatter;
import com.github.steveice10.netty.handler.codec.DefaultHeaders;
import com.github.steveice10.netty.handler.codec.DefaultHeadersImpl;
import com.github.steveice10.netty.handler.codec.Headers;
import com.github.steveice10.netty.handler.codec.HeadersUtils;
import com.github.steveice10.netty.handler.codec.ValueConverter;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultHttpHeaders extends HttpHeaders {
  private static final int HIGHEST_INVALID_VALUE_CHAR_MASK = -16;
  
  private static final ByteProcessor HEADER_NAME_VALIDATOR = new ByteProcessor() {
      public boolean process(byte value) throws Exception {
        DefaultHttpHeaders.validateHeaderNameElement(value);
        return true;
      }
    };
  
  static final DefaultHeaders.NameValidator<CharSequence> HttpNameValidator = new DefaultHeaders.NameValidator<CharSequence>() {
      public void validateName(CharSequence name) {
        if (name == null || name.length() == 0)
          throw new IllegalArgumentException("empty headers are not allowed [" + name + "]"); 
        if (name instanceof AsciiString) {
          try {
            ((AsciiString)name).forEachByte(DefaultHttpHeaders.HEADER_NAME_VALIDATOR);
          } catch (Exception e) {
            PlatformDependent.throwException(e);
          } 
        } else {
          for (int index = 0; index < name.length(); index++)
            DefaultHttpHeaders.validateHeaderNameElement(name.charAt(index)); 
        } 
      }
    };
  
  private final DefaultHeaders<CharSequence, CharSequence, ?> headers;
  
  public DefaultHttpHeaders() {
    this(true);
  }
  
  public DefaultHttpHeaders(boolean validate) {
    this(validate, nameValidator(validate));
  }
  
  protected DefaultHttpHeaders(boolean validate, DefaultHeaders.NameValidator<CharSequence> nameValidator) {
    this((DefaultHeaders<CharSequence, CharSequence, ?>)new DefaultHeadersImpl(AsciiString.CASE_INSENSITIVE_HASHER, 
          valueConverter(validate), nameValidator));
  }
  
  protected DefaultHttpHeaders(DefaultHeaders<CharSequence, CharSequence, ?> headers) {
    this.headers = headers;
  }
  
  public HttpHeaders add(HttpHeaders headers) {
    if (headers instanceof DefaultHttpHeaders) {
      this.headers.add((Headers)((DefaultHttpHeaders)headers).headers);
      return this;
    } 
    return super.add(headers);
  }
  
  public HttpHeaders set(HttpHeaders headers) {
    if (headers instanceof DefaultHttpHeaders) {
      this.headers.set((Headers)((DefaultHttpHeaders)headers).headers);
      return this;
    } 
    return super.set(headers);
  }
  
  public HttpHeaders add(String name, Object value) {
    this.headers.addObject(name, value);
    return this;
  }
  
  public HttpHeaders add(CharSequence name, Object value) {
    this.headers.addObject(name, value);
    return this;
  }
  
  public HttpHeaders add(String name, Iterable<?> values) {
    this.headers.addObject(name, values);
    return this;
  }
  
  public HttpHeaders add(CharSequence name, Iterable<?> values) {
    this.headers.addObject(name, values);
    return this;
  }
  
  public HttpHeaders addInt(CharSequence name, int value) {
    this.headers.addInt(name, value);
    return this;
  }
  
  public HttpHeaders addShort(CharSequence name, short value) {
    this.headers.addShort(name, value);
    return this;
  }
  
  public HttpHeaders remove(String name) {
    this.headers.remove(name);
    return this;
  }
  
  public HttpHeaders remove(CharSequence name) {
    this.headers.remove(name);
    return this;
  }
  
  public HttpHeaders set(String name, Object value) {
    this.headers.setObject(name, value);
    return this;
  }
  
  public HttpHeaders set(CharSequence name, Object value) {
    this.headers.setObject(name, value);
    return this;
  }
  
  public HttpHeaders set(String name, Iterable<?> values) {
    this.headers.setObject(name, values);
    return this;
  }
  
  public HttpHeaders set(CharSequence name, Iterable<?> values) {
    this.headers.setObject(name, values);
    return this;
  }
  
  public HttpHeaders setInt(CharSequence name, int value) {
    this.headers.setInt(name, value);
    return this;
  }
  
  public HttpHeaders setShort(CharSequence name, short value) {
    this.headers.setShort(name, value);
    return this;
  }
  
  public HttpHeaders clear() {
    this.headers.clear();
    return this;
  }
  
  public String get(String name) {
    return get(name);
  }
  
  public String get(CharSequence name) {
    return HeadersUtils.getAsString((Headers)this.headers, name);
  }
  
  public Integer getInt(CharSequence name) {
    return this.headers.getInt(name);
  }
  
  public int getInt(CharSequence name, int defaultValue) {
    return this.headers.getInt(name, defaultValue);
  }
  
  public Short getShort(CharSequence name) {
    return this.headers.getShort(name);
  }
  
  public short getShort(CharSequence name, short defaultValue) {
    return this.headers.getShort(name, defaultValue);
  }
  
  public Long getTimeMillis(CharSequence name) {
    return this.headers.getTimeMillis(name);
  }
  
  public long getTimeMillis(CharSequence name, long defaultValue) {
    return this.headers.getTimeMillis(name, defaultValue);
  }
  
  public List<String> getAll(String name) {
    return getAll(name);
  }
  
  public List<String> getAll(CharSequence name) {
    return HeadersUtils.getAllAsString((Headers)this.headers, name);
  }
  
  public List<Map.Entry<String, String>> entries() {
    if (isEmpty())
      return Collections.emptyList(); 
    List<Map.Entry<String, String>> entriesConverted = new ArrayList<Map.Entry<String, String>>(this.headers.size());
    for (Map.Entry<String, String> entry : (Iterable<Map.Entry<String, String>>)this)
      entriesConverted.add(entry); 
    return entriesConverted;
  }
  
  @Deprecated
  public Iterator<Map.Entry<String, String>> iterator() {
    return HeadersUtils.iteratorAsString((Iterable)this.headers);
  }
  
  public Iterator<Map.Entry<CharSequence, CharSequence>> iteratorCharSequence() {
    return this.headers.iterator();
  }
  
  public Iterator<String> valueStringIterator(CharSequence name) {
    final Iterator<CharSequence> itr = valueCharSequenceIterator(name);
    return new Iterator<String>() {
        public boolean hasNext() {
          return itr.hasNext();
        }
        
        public String next() {
          return ((CharSequence)itr.next()).toString();
        }
        
        public void remove() {
          itr.remove();
        }
      };
  }
  
  public Iterator<CharSequence> valueCharSequenceIterator(CharSequence name) {
    return this.headers.valueIterator(name);
  }
  
  public boolean contains(String name) {
    return contains(name);
  }
  
  public boolean contains(CharSequence name) {
    return this.headers.contains(name);
  }
  
  public boolean isEmpty() {
    return this.headers.isEmpty();
  }
  
  public int size() {
    return this.headers.size();
  }
  
  public boolean contains(String name, String value, boolean ignoreCase) {
    return contains(name, value, ignoreCase);
  }
  
  public boolean contains(CharSequence name, CharSequence value, boolean ignoreCase) {
    return this.headers.contains(name, value, ignoreCase ? AsciiString.CASE_INSENSITIVE_HASHER : AsciiString.CASE_SENSITIVE_HASHER);
  }
  
  public Set<String> names() {
    return HeadersUtils.namesAsString((Headers)this.headers);
  }
  
  public boolean equals(Object o) {
    return (o instanceof DefaultHttpHeaders && this.headers
      .equals((Headers)((DefaultHttpHeaders)o).headers, AsciiString.CASE_SENSITIVE_HASHER));
  }
  
  public int hashCode() {
    return this.headers.hashCode(AsciiString.CASE_SENSITIVE_HASHER);
  }
  
  public HttpHeaders copy() {
    return new DefaultHttpHeaders(this.headers.copy());
  }
  
  private static void validateHeaderNameElement(byte value) {
    switch (value) {
      case 0:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 32:
      case 44:
      case 58:
      case 59:
      case 61:
        throw new IllegalArgumentException("a header name cannot contain the following prohibited characters: =,;: \\t\\r\\n\\v\\f: " + value);
    } 
    if (value < 0)
      throw new IllegalArgumentException("a header name cannot contain non-ASCII character: " + value); 
  }
  
  private static void validateHeaderNameElement(char value) {
    switch (value) {
      case '\000':
      case '\t':
      case '\n':
      case '\013':
      case '\f':
      case '\r':
      case ' ':
      case ',':
      case ':':
      case ';':
      case '=':
        throw new IllegalArgumentException("a header name cannot contain the following prohibited characters: =,;: \\t\\r\\n\\v\\f: " + value);
    } 
    if (value > '')
      throw new IllegalArgumentException("a header name cannot contain non-ASCII character: " + value); 
  }
  
  static ValueConverter<CharSequence> valueConverter(boolean validate) {
    return validate ? (ValueConverter<CharSequence>)HeaderValueConverterAndValidator.INSTANCE : (ValueConverter<CharSequence>)HeaderValueConverter.INSTANCE;
  }
  
  static DefaultHeaders.NameValidator<CharSequence> nameValidator(boolean validate) {
    return validate ? HttpNameValidator : DefaultHeaders.NameValidator.NOT_NULL;
  }
  
  private static class HeaderValueConverter extends CharSequenceValueConverter {
    private HeaderValueConverter() {}
    
    static final HeaderValueConverter INSTANCE = new HeaderValueConverter();
    
    public CharSequence convertObject(Object value) {
      if (value instanceof CharSequence)
        return (CharSequence)value; 
      if (value instanceof Date)
        return DateFormatter.format((Date)value); 
      if (value instanceof Calendar)
        return DateFormatter.format(((Calendar)value).getTime()); 
      return value.toString();
    }
  }
  
  private static final class HeaderValueConverterAndValidator extends HeaderValueConverter {
    static final HeaderValueConverterAndValidator INSTANCE = new HeaderValueConverterAndValidator();
    
    public CharSequence convertObject(Object value) {
      CharSequence seq = super.convertObject(value);
      int state = 0;
      for (int index = 0; index < seq.length(); index++)
        state = validateValueChar(seq, state, seq.charAt(index)); 
      if (state != 0)
        throw new IllegalArgumentException("a header value must not end with '\\r' or '\\n':" + seq); 
      return seq;
    }
    
    private static int validateValueChar(CharSequence seq, int state, char character) {
      if ((character & 0xFFFFFFF0) == 0)
        switch (character) {
          case '\000':
            throw new IllegalArgumentException("a header value contains a prohibited character '\000': " + seq);
          case '\013':
            throw new IllegalArgumentException("a header value contains a prohibited character '\\v': " + seq);
          case '\f':
            throw new IllegalArgumentException("a header value contains a prohibited character '\\f': " + seq);
        }  
      switch (state) {
        case 0:
          switch (character) {
            case '\r':
              return 1;
            case '\n':
              return 2;
          } 
          break;
        case 1:
          switch (character) {
            case '\n':
              return 2;
          } 
          throw new IllegalArgumentException("only '\\n' is allowed after '\\r': " + seq);
        case 2:
          switch (character) {
            case '\t':
            case ' ':
              return 0;
          } 
          throw new IllegalArgumentException("only ' ' and '\\t' are allowed after '\\n': " + seq);
      } 
      return state;
    }
  }
}
