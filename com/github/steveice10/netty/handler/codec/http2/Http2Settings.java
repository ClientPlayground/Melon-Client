package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.collection.CharObjectHashMap;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Map;

public final class Http2Settings extends CharObjectHashMap<Long> {
  private static final int DEFAULT_CAPACITY = 13;
  
  private static final Long FALSE = Long.valueOf(0L);
  
  private static final Long TRUE = Long.valueOf(1L);
  
  public Http2Settings() {
    this(13);
  }
  
  public Http2Settings(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }
  
  public Http2Settings(int initialCapacity) {
    super(initialCapacity);
  }
  
  public Long put(char key, Long value) {
    verifyStandardSetting(key, value);
    return (Long)super.put(key, value);
  }
  
  public Long headerTableSize() {
    return (Long)get('\001');
  }
  
  public Http2Settings headerTableSize(long value) {
    put('\001', Long.valueOf(value));
    return this;
  }
  
  public Boolean pushEnabled() {
    Long value = (Long)get('\002');
    if (value == null)
      return null; 
    return Boolean.valueOf(TRUE.equals(value));
  }
  
  public Http2Settings pushEnabled(boolean enabled) {
    put('\002', enabled ? TRUE : FALSE);
    return this;
  }
  
  public Long maxConcurrentStreams() {
    return (Long)get('\003');
  }
  
  public Http2Settings maxConcurrentStreams(long value) {
    put('\003', Long.valueOf(value));
    return this;
  }
  
  public Integer initialWindowSize() {
    return getIntValue('\004');
  }
  
  public Http2Settings initialWindowSize(int value) {
    put('\004', Long.valueOf(value));
    return this;
  }
  
  public Integer maxFrameSize() {
    return getIntValue('\005');
  }
  
  public Http2Settings maxFrameSize(int value) {
    put('\005', Long.valueOf(value));
    return this;
  }
  
  public Long maxHeaderListSize() {
    return (Long)get('\006');
  }
  
  public Http2Settings maxHeaderListSize(long value) {
    put('\006', Long.valueOf(value));
    return this;
  }
  
  public Http2Settings copyFrom(Http2Settings settings) {
    clear();
    putAll((Map)settings);
    return this;
  }
  
  public Integer getIntValue(char key) {
    Long value = (Long)get(key);
    if (value == null)
      return null; 
    return Integer.valueOf(value.intValue());
  }
  
  private static void verifyStandardSetting(int key, Long value) {
    ObjectUtil.checkNotNull(value, "value");
    switch (key) {
      case 1:
        if (value.longValue() < 0L || value.longValue() > 4294967295L)
          throw new IllegalArgumentException("Setting HEADER_TABLE_SIZE is invalid: " + value); 
        break;
      case 2:
        if (value.longValue() != 0L && value.longValue() != 1L)
          throw new IllegalArgumentException("Setting ENABLE_PUSH is invalid: " + value); 
        break;
      case 3:
        if (value.longValue() < 0L || value.longValue() > 4294967295L)
          throw new IllegalArgumentException("Setting MAX_CONCURRENT_STREAMS is invalid: " + value); 
        break;
      case 4:
        if (value.longValue() < 0L || value.longValue() > 2147483647L)
          throw new IllegalArgumentException("Setting INITIAL_WINDOW_SIZE is invalid: " + value); 
        break;
      case 5:
        if (!Http2CodecUtil.isMaxFrameSizeValid(value.intValue()))
          throw new IllegalArgumentException("Setting MAX_FRAME_SIZE is invalid: " + value); 
        break;
      case 6:
        if (value.longValue() < 0L || value.longValue() > 4294967295L)
          throw new IllegalArgumentException("Setting MAX_HEADER_LIST_SIZE is invalid: " + value); 
        break;
    } 
  }
  
  protected String keyToString(char key) {
    switch (key) {
      case '\001':
        return "HEADER_TABLE_SIZE";
      case '\002':
        return "ENABLE_PUSH";
      case '\003':
        return "MAX_CONCURRENT_STREAMS";
      case '\004':
        return "INITIAL_WINDOW_SIZE";
      case '\005':
        return "MAX_FRAME_SIZE";
      case '\006':
        return "MAX_HEADER_LIST_SIZE";
    } 
    return super.keyToString(key);
  }
  
  public static Http2Settings defaultSettings() {
    return (new Http2Settings()).maxHeaderListSize(8192L);
  }
}
