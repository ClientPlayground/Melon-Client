package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.handler.codec.CharSequenceValueConverter;
import com.github.steveice10.netty.handler.codec.DefaultHeaders;
import com.github.steveice10.netty.handler.codec.HeadersUtils;
import com.github.steveice10.netty.handler.codec.ValueConverter;
import com.github.steveice10.netty.util.AsciiString;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultSpdyHeaders extends DefaultHeaders<CharSequence, CharSequence, SpdyHeaders> implements SpdyHeaders {
  private static final DefaultHeaders.NameValidator<CharSequence> SpdyNameValidator = new DefaultHeaders.NameValidator<CharSequence>() {
      public void validateName(CharSequence name) {
        SpdyCodecUtil.validateHeaderName(name);
      }
    };
  
  public DefaultSpdyHeaders() {
    this(true);
  }
  
  public DefaultSpdyHeaders(boolean validate) {
    super(AsciiString.CASE_INSENSITIVE_HASHER, validate ? (ValueConverter)HeaderValueConverterAndValidator.INSTANCE : (ValueConverter)CharSequenceValueConverter.INSTANCE, validate ? SpdyNameValidator : DefaultHeaders.NameValidator.NOT_NULL);
  }
  
  public String getAsString(CharSequence name) {
    return HeadersUtils.getAsString(this, name);
  }
  
  public List<String> getAllAsString(CharSequence name) {
    return HeadersUtils.getAllAsString(this, name);
  }
  
  public Iterator<Map.Entry<String, String>> iteratorAsString() {
    return HeadersUtils.iteratorAsString((Iterable)this);
  }
  
  public boolean contains(CharSequence name, CharSequence value) {
    return contains(name, value, false);
  }
  
  public boolean contains(CharSequence name, CharSequence value, boolean ignoreCase) {
    return contains(name, value, ignoreCase ? AsciiString.CASE_INSENSITIVE_HASHER : AsciiString.CASE_SENSITIVE_HASHER);
  }
  
  private static final class HeaderValueConverterAndValidator extends CharSequenceValueConverter {
    public static final HeaderValueConverterAndValidator INSTANCE = new HeaderValueConverterAndValidator();
    
    public CharSequence convertObject(Object value) {
      CharSequence seq = super.convertObject(value);
      SpdyCodecUtil.validateHeaderValue(seq);
      return seq;
    }
  }
}
