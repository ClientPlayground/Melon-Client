package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.handler.codec.DefaultHeaders;
import com.github.steveice10.netty.handler.codec.UnsupportedValueConverter;
import com.github.steveice10.netty.handler.codec.ValueConverter;
import com.github.steveice10.netty.util.AsciiString;

public final class CharSequenceMap<V> extends DefaultHeaders<CharSequence, V, CharSequenceMap<V>> {
  public CharSequenceMap() {
    this(true);
  }
  
  public CharSequenceMap(boolean caseSensitive) {
    this(caseSensitive, (ValueConverter<V>)UnsupportedValueConverter.instance());
  }
  
  public CharSequenceMap(boolean caseSensitive, ValueConverter<V> valueConverter) {
    super(caseSensitive ? AsciiString.CASE_SENSITIVE_HASHER : AsciiString.CASE_INSENSITIVE_HASHER, valueConverter);
  }
  
  public CharSequenceMap(boolean caseSensitive, ValueConverter<V> valueConverter, int arraySizeHint) {
    super(caseSensitive ? AsciiString.CASE_SENSITIVE_HASHER : AsciiString.CASE_INSENSITIVE_HASHER, valueConverter, DefaultHeaders.NameValidator.NOT_NULL, arraySizeHint);
  }
}
