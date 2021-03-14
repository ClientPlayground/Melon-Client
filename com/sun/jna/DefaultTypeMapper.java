package com.sun.jna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultTypeMapper implements TypeMapper {
  private static class Entry {
    public Class<?> type;
    
    public Object converter;
    
    public Entry(Class<?> type, Object converter) {
      this.type = type;
      this.converter = converter;
    }
  }
  
  private List<Entry> toNativeConverters = new ArrayList<Entry>();
  
  private List<Entry> fromNativeConverters = new ArrayList<Entry>();
  
  private Class<?> getAltClass(Class<?> cls) {
    if (cls == Boolean.class)
      return boolean.class; 
    if (cls == boolean.class)
      return Boolean.class; 
    if (cls == Byte.class)
      return byte.class; 
    if (cls == byte.class)
      return Byte.class; 
    if (cls == Character.class)
      return char.class; 
    if (cls == char.class)
      return Character.class; 
    if (cls == Short.class)
      return short.class; 
    if (cls == short.class)
      return Short.class; 
    if (cls == Integer.class)
      return int.class; 
    if (cls == int.class)
      return Integer.class; 
    if (cls == Long.class)
      return long.class; 
    if (cls == long.class)
      return Long.class; 
    if (cls == Float.class)
      return float.class; 
    if (cls == float.class)
      return Float.class; 
    if (cls == Double.class)
      return double.class; 
    if (cls == double.class)
      return Double.class; 
    return null;
  }
  
  public void addToNativeConverter(Class<?> cls, ToNativeConverter converter) {
    this.toNativeConverters.add(new Entry(cls, converter));
    Class<?> alt = getAltClass(cls);
    if (alt != null)
      this.toNativeConverters.add(new Entry(alt, converter)); 
  }
  
  public void addFromNativeConverter(Class<?> cls, FromNativeConverter converter) {
    this.fromNativeConverters.add(new Entry(cls, converter));
    Class<?> alt = getAltClass(cls);
    if (alt != null)
      this.fromNativeConverters.add(new Entry(alt, converter)); 
  }
  
  public void addTypeConverter(Class<?> cls, TypeConverter converter) {
    addFromNativeConverter(cls, converter);
    addToNativeConverter(cls, converter);
  }
  
  private Object lookupConverter(Class<?> javaClass, Collection<? extends Entry> converters) {
    for (Entry entry : converters) {
      if (entry.type.isAssignableFrom(javaClass))
        return entry.converter; 
    } 
    return null;
  }
  
  public FromNativeConverter getFromNativeConverter(Class<?> javaType) {
    return (FromNativeConverter)lookupConverter(javaType, this.fromNativeConverters);
  }
  
  public ToNativeConverter getToNativeConverter(Class<?> javaType) {
    return (ToNativeConverter)lookupConverter(javaType, this.toNativeConverters);
  }
}
