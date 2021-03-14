package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.text.ParseException;
import java.util.Date;

public class CharSequenceValueConverter implements ValueConverter<CharSequence> {
  public static final CharSequenceValueConverter INSTANCE = new CharSequenceValueConverter();
  
  private static final AsciiString TRUE_ASCII = new AsciiString("true");
  
  public CharSequence convertObject(Object value) {
    if (value instanceof CharSequence)
      return (CharSequence)value; 
    return value.toString();
  }
  
  public CharSequence convertInt(int value) {
    return String.valueOf(value);
  }
  
  public CharSequence convertLong(long value) {
    return String.valueOf(value);
  }
  
  public CharSequence convertDouble(double value) {
    return String.valueOf(value);
  }
  
  public CharSequence convertChar(char value) {
    return String.valueOf(value);
  }
  
  public CharSequence convertBoolean(boolean value) {
    return String.valueOf(value);
  }
  
  public CharSequence convertFloat(float value) {
    return String.valueOf(value);
  }
  
  public boolean convertToBoolean(CharSequence value) {
    return AsciiString.contentEqualsIgnoreCase(value, (CharSequence)TRUE_ASCII);
  }
  
  public CharSequence convertByte(byte value) {
    return String.valueOf(value);
  }
  
  public byte convertToByte(CharSequence value) {
    if (value instanceof AsciiString)
      return ((AsciiString)value).byteAt(0); 
    return Byte.parseByte(value.toString());
  }
  
  public char convertToChar(CharSequence value) {
    return value.charAt(0);
  }
  
  public CharSequence convertShort(short value) {
    return String.valueOf(value);
  }
  
  public short convertToShort(CharSequence value) {
    if (value instanceof AsciiString)
      return ((AsciiString)value).parseShort(); 
    return Short.parseShort(value.toString());
  }
  
  public int convertToInt(CharSequence value) {
    if (value instanceof AsciiString)
      return ((AsciiString)value).parseInt(); 
    return Integer.parseInt(value.toString());
  }
  
  public long convertToLong(CharSequence value) {
    if (value instanceof AsciiString)
      return ((AsciiString)value).parseLong(); 
    return Long.parseLong(value.toString());
  }
  
  public CharSequence convertTimeMillis(long value) {
    return DateFormatter.format(new Date(value));
  }
  
  public long convertToTimeMillis(CharSequence value) {
    Date date = DateFormatter.parseHttpDate(value);
    if (date == null) {
      PlatformDependent.throwException(new ParseException("header can't be parsed into a Date: " + value, 0));
      return 0L;
    } 
    return date.getTime();
  }
  
  public float convertToFloat(CharSequence value) {
    if (value instanceof AsciiString)
      return ((AsciiString)value).parseFloat(); 
    return Float.parseFloat(value.toString());
  }
  
  public double convertToDouble(CharSequence value) {
    if (value instanceof AsciiString)
      return ((AsciiString)value).parseDouble(); 
    return Double.parseDouble(value.toString());
  }
}
