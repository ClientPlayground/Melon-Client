package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.handler.codec.http.HttpConstants;
import com.github.steveice10.netty.util.AbstractReferenceCounted;
import com.github.steveice10.netty.util.ReferenceCounted;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public abstract class AbstractHttpData extends AbstractReferenceCounted implements HttpData {
  private static final Pattern STRIP_PATTERN = Pattern.compile("(?:^\\s+|\\s+$|\\n)");
  
  private static final Pattern REPLACE_PATTERN = Pattern.compile("[\\r\\t]");
  
  private final String name;
  
  protected long definedSize;
  
  protected long size;
  
  private Charset charset = HttpConstants.DEFAULT_CHARSET;
  
  private boolean completed;
  
  private long maxSize = -1L;
  
  protected AbstractHttpData(String name, Charset charset, long size) {
    if (name == null)
      throw new NullPointerException("name"); 
    name = REPLACE_PATTERN.matcher(name).replaceAll(" ");
    name = STRIP_PATTERN.matcher(name).replaceAll("");
    if (name.isEmpty())
      throw new IllegalArgumentException("empty name"); 
    this.name = name;
    if (charset != null)
      setCharset(charset); 
    this.definedSize = size;
  }
  
  public long getMaxSize() {
    return this.maxSize;
  }
  
  public void setMaxSize(long maxSize) {
    this.maxSize = maxSize;
  }
  
  public void checkSize(long newSize) throws IOException {
    if (this.maxSize >= 0L && newSize > this.maxSize)
      throw new IOException("Size exceed allowed maximum capacity"); 
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean isCompleted() {
    return this.completed;
  }
  
  protected void setCompleted() {
    this.completed = true;
  }
  
  public Charset getCharset() {
    return this.charset;
  }
  
  public void setCharset(Charset charset) {
    if (charset == null)
      throw new NullPointerException("charset"); 
    this.charset = charset;
  }
  
  public long length() {
    return this.size;
  }
  
  public long definedLength() {
    return this.definedSize;
  }
  
  public ByteBuf content() {
    try {
      return getByteBuf();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  protected void deallocate() {
    delete();
  }
  
  public HttpData retain() {
    super.retain();
    return this;
  }
  
  public HttpData retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public abstract HttpData touch();
  
  public abstract HttpData touch(Object paramObject);
}
