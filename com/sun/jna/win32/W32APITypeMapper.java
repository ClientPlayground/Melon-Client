package com.sun.jna.win32;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.FromNativeContext;
import com.sun.jna.StringArray;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeConverter;
import com.sun.jna.TypeMapper;
import com.sun.jna.WString;

public class W32APITypeMapper extends DefaultTypeMapper {
  public static final TypeMapper UNICODE = (TypeMapper)new W32APITypeMapper(true);
  
  public static final TypeMapper ASCII = (TypeMapper)new W32APITypeMapper(false);
  
  public static final TypeMapper DEFAULT = Boolean.getBoolean("w32.ascii") ? ASCII : UNICODE;
  
  protected W32APITypeMapper(boolean unicode) {
    if (unicode) {
      TypeConverter stringConverter = new TypeConverter() {
          public Object toNative(Object value, ToNativeContext context) {
            if (value == null)
              return null; 
            if (value instanceof String[])
              return new StringArray((String[])value, true); 
            return new WString(value.toString());
          }
          
          public Object fromNative(Object value, FromNativeContext context) {
            if (value == null)
              return null; 
            return value.toString();
          }
          
          public Class<?> nativeType() {
            return WString.class;
          }
        };
      addTypeConverter(String.class, stringConverter);
      addToNativeConverter(String[].class, (ToNativeConverter)stringConverter);
    } 
    TypeConverter booleanConverter = new TypeConverter() {
        public Object toNative(Object value, ToNativeContext context) {
          return Integer.valueOf(Boolean.TRUE.equals(value) ? 1 : 0);
        }
        
        public Object fromNative(Object value, FromNativeContext context) {
          return (((Integer)value).intValue() != 0) ? Boolean.TRUE : Boolean.FALSE;
        }
        
        public Class<?> nativeType() {
          return Integer.class;
        }
      };
    addTypeConverter(Boolean.class, booleanConverter);
  }
}
