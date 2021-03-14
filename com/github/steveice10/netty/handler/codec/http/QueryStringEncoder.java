package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class QueryStringEncoder {
  private final String charsetName;
  
  private final StringBuilder uriBuilder;
  
  private boolean hasParams;
  
  public QueryStringEncoder(String uri) {
    this(uri, HttpConstants.DEFAULT_CHARSET);
  }
  
  public QueryStringEncoder(String uri, Charset charset) {
    this.uriBuilder = new StringBuilder(uri);
    this.charsetName = charset.name();
  }
  
  public void addParam(String name, String value) {
    ObjectUtil.checkNotNull(name, "name");
    if (this.hasParams) {
      this.uriBuilder.append('&');
    } else {
      this.uriBuilder.append('?');
      this.hasParams = true;
    } 
    appendComponent(name, this.charsetName, this.uriBuilder);
    if (value != null) {
      this.uriBuilder.append('=');
      appendComponent(value, this.charsetName, this.uriBuilder);
    } 
  }
  
  public URI toUri() throws URISyntaxException {
    return new URI(toString());
  }
  
  public String toString() {
    return this.uriBuilder.toString();
  }
  
  private static void appendComponent(String s, String charset, StringBuilder sb) {
    try {
      s = URLEncoder.encode(s, charset);
    } catch (UnsupportedEncodingException ignored) {
      throw new UnsupportedCharsetException(charset);
    } 
    int idx = s.indexOf('+');
    if (idx == -1) {
      sb.append(s);
      return;
    } 
    sb.append(s, 0, idx).append("%20");
    int size = s.length();
    idx++;
    for (; idx < size; idx++) {
      char c = s.charAt(idx);
      if (c != '+') {
        sb.append(c);
      } else {
        sb.append("%20");
      } 
    } 
  }
}
