package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.handler.codec.EmptyHeaders;
import java.util.Iterator;

public final class EmptyHttp2Headers extends EmptyHeaders<CharSequence, CharSequence, Http2Headers> implements Http2Headers {
  public static final EmptyHttp2Headers INSTANCE = new EmptyHttp2Headers();
  
  public EmptyHttp2Headers method(CharSequence method) {
    throw new UnsupportedOperationException();
  }
  
  public EmptyHttp2Headers scheme(CharSequence status) {
    throw new UnsupportedOperationException();
  }
  
  public EmptyHttp2Headers authority(CharSequence authority) {
    throw new UnsupportedOperationException();
  }
  
  public EmptyHttp2Headers path(CharSequence path) {
    throw new UnsupportedOperationException();
  }
  
  public EmptyHttp2Headers status(CharSequence status) {
    throw new UnsupportedOperationException();
  }
  
  public CharSequence method() {
    return (CharSequence)get(Http2Headers.PseudoHeaderName.METHOD.value());
  }
  
  public CharSequence scheme() {
    return (CharSequence)get(Http2Headers.PseudoHeaderName.SCHEME.value());
  }
  
  public CharSequence authority() {
    return (CharSequence)get(Http2Headers.PseudoHeaderName.AUTHORITY.value());
  }
  
  public CharSequence path() {
    return (CharSequence)get(Http2Headers.PseudoHeaderName.PATH.value());
  }
  
  public CharSequence status() {
    return (CharSequence)get(Http2Headers.PseudoHeaderName.STATUS.value());
  }
  
  public boolean contains(CharSequence name, CharSequence value, boolean caseInsensitive) {
    return false;
  }
}
