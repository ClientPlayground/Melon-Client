package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.handler.codec.CharSequenceValueConverter;
import com.github.steveice10.netty.handler.codec.DefaultHeaders;
import com.github.steveice10.netty.handler.codec.HeadersUtils;
import com.github.steveice10.netty.handler.codec.ValueConverter;
import com.github.steveice10.netty.util.AsciiString;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultStompHeaders extends DefaultHeaders<CharSequence, CharSequence, StompHeaders> implements StompHeaders {
  public DefaultStompHeaders() {
    super(AsciiString.CASE_SENSITIVE_HASHER, (ValueConverter)CharSequenceValueConverter.INSTANCE);
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
  
  public DefaultStompHeaders copy() {
    DefaultStompHeaders copyHeaders = new DefaultStompHeaders();
    copyHeaders.addImpl(this);
    return copyHeaders;
  }
}
