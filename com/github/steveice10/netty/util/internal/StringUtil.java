package com.github.steveice10.netty.util.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class StringUtil {
  public static final String EMPTY_STRING = "";
  
  public static final String NEWLINE = SystemPropertyUtil.get("line.separator", "\n");
  
  public static final char DOUBLE_QUOTE = '"';
  
  public static final char COMMA = ',';
  
  public static final char LINE_FEED = '\n';
  
  public static final char CARRIAGE_RETURN = '\r';
  
  public static final char TAB = '\t';
  
  public static final char SPACE = ' ';
  
  private static final String[] BYTE2HEX_PAD = new String[256];
  
  private static final String[] BYTE2HEX_NOPAD = new String[256];
  
  private static final int CSV_NUMBER_ESCAPE_CHARACTERS = 7;
  
  private static final char PACKAGE_SEPARATOR_CHAR = '.';
  
  static {
    for (int i = 0; i < BYTE2HEX_PAD.length; i++) {
      String str = Integer.toHexString(i);
      BYTE2HEX_PAD[i] = (i > 15) ? str : ('0' + str);
      BYTE2HEX_NOPAD[i] = str;
    } 
  }
  
  public static String substringAfter(String value, char delim) {
    int pos = value.indexOf(delim);
    if (pos >= 0)
      return value.substring(pos + 1); 
    return null;
  }
  
  public static boolean commonSuffixOfLength(String s, String p, int len) {
    return (s != null && p != null && len >= 0 && s.regionMatches(s.length() - len, p, p.length() - len, len));
  }
  
  public static String byteToHexStringPadded(int value) {
    return BYTE2HEX_PAD[value & 0xFF];
  }
  
  public static <T extends Appendable> T byteToHexStringPadded(T buf, int value) {
    try {
      buf.append(byteToHexStringPadded(value));
    } catch (IOException e) {
      PlatformDependent.throwException(e);
    } 
    return buf;
  }
  
  public static String toHexStringPadded(byte[] src) {
    return toHexStringPadded(src, 0, src.length);
  }
  
  public static String toHexStringPadded(byte[] src, int offset, int length) {
    return ((StringBuilder)toHexStringPadded(new StringBuilder(length << 1), src, offset, length)).toString();
  }
  
  public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src) {
    return toHexStringPadded(dst, src, 0, src.length);
  }
  
  public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src, int offset, int length) {
    int end = offset + length;
    for (int i = offset; i < end; i++)
      byteToHexStringPadded(dst, src[i]); 
    return dst;
  }
  
  public static String byteToHexString(int value) {
    return BYTE2HEX_NOPAD[value & 0xFF];
  }
  
  public static <T extends Appendable> T byteToHexString(T buf, int value) {
    try {
      buf.append(byteToHexString(value));
    } catch (IOException e) {
      PlatformDependent.throwException(e);
    } 
    return buf;
  }
  
  public static String toHexString(byte[] src) {
    return toHexString(src, 0, src.length);
  }
  
  public static String toHexString(byte[] src, int offset, int length) {
    return ((StringBuilder)toHexString(new StringBuilder(length << 1), src, offset, length)).toString();
  }
  
  public static <T extends Appendable> T toHexString(T dst, byte[] src) {
    return toHexString(dst, src, 0, src.length);
  }
  
  public static <T extends Appendable> T toHexString(T dst, byte[] src, int offset, int length) {
    assert length >= 0;
    if (length == 0)
      return dst; 
    int end = offset + length;
    int endMinusOne = end - 1;
    int i;
    for (i = offset; i < endMinusOne && 
      src[i] == 0; i++);
    byteToHexString(dst, src[i++]);
    int remaining = end - i;
    toHexStringPadded(dst, src, i, remaining);
    return dst;
  }
  
  public static int decodeHexNibble(char c) {
    if (c >= '0' && c <= '9')
      return c - 48; 
    if (c >= 'A' && c <= 'F')
      return c - 55; 
    if (c >= 'a' && c <= 'f')
      return c - 87; 
    return -1;
  }
  
  public static byte decodeHexByte(CharSequence s, int pos) {
    int hi = decodeHexNibble(s.charAt(pos));
    int lo = decodeHexNibble(s.charAt(pos + 1));
    if (hi == -1 || lo == -1)
      throw new IllegalArgumentException(String.format("invalid hex byte '%s' at index %d of '%s'", new Object[] { s
              .subSequence(pos, pos + 2), Integer.valueOf(pos), s })); 
    return (byte)((hi << 4) + lo);
  }
  
  public static byte[] decodeHexDump(CharSequence hexDump, int fromIndex, int length) {
    if (length < 0 || (length & 0x1) != 0)
      throw new IllegalArgumentException("length: " + length); 
    if (length == 0)
      return EmptyArrays.EMPTY_BYTES; 
    byte[] bytes = new byte[length >>> 1];
    for (int i = 0; i < length; i += 2)
      bytes[i >>> 1] = decodeHexByte(hexDump, fromIndex + i); 
    return bytes;
  }
  
  public static byte[] decodeHexDump(CharSequence hexDump) {
    return decodeHexDump(hexDump, 0, hexDump.length());
  }
  
  public static String simpleClassName(Object o) {
    if (o == null)
      return "null_object"; 
    return simpleClassName(o.getClass());
  }
  
  public static String simpleClassName(Class<?> clazz) {
    String className = ((Class)ObjectUtil.<Class<?>>checkNotNull(clazz, "clazz")).getName();
    int lastDotIdx = className.lastIndexOf('.');
    if (lastDotIdx > -1)
      return className.substring(lastDotIdx + 1); 
    return className;
  }
  
  public static CharSequence escapeCsv(CharSequence value) {
    return escapeCsv(value, false);
  }
  
  public static CharSequence escapeCsv(CharSequence value, boolean trimWhiteSpace) {
    int start, last, length = ((CharSequence)ObjectUtil.<CharSequence>checkNotNull(value, "value")).length();
    if (trimWhiteSpace) {
      start = indexOfFirstNonOwsChar(value, length);
      last = indexOfLastNonOwsChar(value, start, length);
    } else {
      start = 0;
      last = length - 1;
    } 
    if (start > last)
      return ""; 
    int firstUnescapedSpecial = -1;
    boolean quoted = false;
    if (isDoubleQuote(value.charAt(start))) {
      quoted = (isDoubleQuote(value.charAt(last)) && last > start);
      if (quoted) {
        start++;
        last--;
      } else {
        firstUnescapedSpecial = start;
      } 
    } 
    if (firstUnescapedSpecial < 0) {
      if (quoted) {
        for (int j = start; j <= last; j++) {
          if (isDoubleQuote(value.charAt(j))) {
            if (j == last || !isDoubleQuote(value.charAt(j + 1))) {
              firstUnescapedSpecial = j;
              break;
            } 
            j++;
          } 
        } 
      } else {
        for (int j = start; j <= last; j++) {
          char c = value.charAt(j);
          if (c == '\n' || c == '\r' || c == ',') {
            firstUnescapedSpecial = j;
            break;
          } 
          if (isDoubleQuote(c)) {
            if (j == last || !isDoubleQuote(value.charAt(j + 1))) {
              firstUnescapedSpecial = j;
              break;
            } 
            j++;
          } 
        } 
      } 
      if (firstUnescapedSpecial < 0)
        return quoted ? value.subSequence(start - 1, last + 2) : value.subSequence(start, last + 1); 
    } 
    StringBuilder result = new StringBuilder(last - start + 1 + 7);
    result.append('"').append(value, start, firstUnescapedSpecial);
    for (int i = firstUnescapedSpecial; i <= last; i++) {
      char c = value.charAt(i);
      if (isDoubleQuote(c)) {
        result.append('"');
        if (i < last && isDoubleQuote(value.charAt(i + 1)))
          i++; 
      } 
      result.append(c);
    } 
    return result.append('"');
  }
  
  public static CharSequence unescapeCsv(CharSequence value) {
    int length = ((CharSequence)ObjectUtil.<CharSequence>checkNotNull(value, "value")).length();
    if (length == 0)
      return value; 
    int last = length - 1;
    boolean quoted = (isDoubleQuote(value.charAt(0)) && isDoubleQuote(value.charAt(last)) && length != 1);
    if (!quoted) {
      validateCsvFormat(value);
      return value;
    } 
    StringBuilder unescaped = InternalThreadLocalMap.get().stringBuilder();
    for (int i = 1; i < last; i++) {
      char current = value.charAt(i);
      if (current == '"')
        if (isDoubleQuote(value.charAt(i + 1)) && i + 1 != last) {
          i++;
        } else {
          throw newInvalidEscapedCsvFieldException(value, i);
        }  
      unescaped.append(current);
    } 
    return unescaped.toString();
  }
  
  public static List<CharSequence> unescapeCsvFields(CharSequence value) {
    List<CharSequence> unescaped = new ArrayList<CharSequence>(2);
    StringBuilder current = InternalThreadLocalMap.get().stringBuilder();
    boolean quoted = false;
    int last = value.length() - 1;
    for (int i = 0; i <= last; i++) {
      char c = value.charAt(i);
      if (quoted) {
        char next;
        switch (c) {
          case '"':
            if (i == last) {
              unescaped.add(current.toString());
              return unescaped;
            } 
            next = value.charAt(++i);
            if (next == '"') {
              current.append('"');
              break;
            } 
            if (next == ',') {
              quoted = false;
              unescaped.add(current.toString());
              current.setLength(0);
              break;
            } 
            throw newInvalidEscapedCsvFieldException(value, i - 1);
          default:
            current.append(c);
            break;
        } 
      } else {
        switch (c) {
          case ',':
            unescaped.add(current.toString());
            current.setLength(0);
            break;
          case '"':
            if (current.length() == 0) {
              quoted = true;
              break;
            } 
          case '\n':
          case '\r':
            throw newInvalidEscapedCsvFieldException(value, i);
          default:
            current.append(c);
            break;
        } 
      } 
    } 
    if (quoted)
      throw newInvalidEscapedCsvFieldException(value, last); 
    unescaped.add(current.toString());
    return unescaped;
  }
  
  private static void validateCsvFormat(CharSequence value) {
    int length = value.length();
    for (int i = 0; i < length; i++) {
      switch (value.charAt(i)) {
        case '\n':
        case '\r':
        case '"':
        case ',':
          throw newInvalidEscapedCsvFieldException(value, i);
      } 
    } 
  }
  
  private static IllegalArgumentException newInvalidEscapedCsvFieldException(CharSequence value, int index) {
    return new IllegalArgumentException("invalid escaped CSV field: " + value + " index: " + index);
  }
  
  public static int length(String s) {
    return (s == null) ? 0 : s.length();
  }
  
  public static boolean isNullOrEmpty(String s) {
    return (s == null || s.isEmpty());
  }
  
  public static int indexOfNonWhiteSpace(CharSequence seq, int offset) {
    for (; offset < seq.length(); offset++) {
      if (!Character.isWhitespace(seq.charAt(offset)))
        return offset; 
    } 
    return -1;
  }
  
  public static boolean isSurrogate(char c) {
    return (c >= '?' && c <= '?');
  }
  
  private static boolean isDoubleQuote(char c) {
    return (c == '"');
  }
  
  public static boolean endsWith(CharSequence s, char c) {
    int len = s.length();
    return (len > 0 && s.charAt(len - 1) == c);
  }
  
  public static CharSequence trimOws(CharSequence value) {
    int length = value.length();
    if (length == 0)
      return value; 
    int start = indexOfFirstNonOwsChar(value, length);
    int end = indexOfLastNonOwsChar(value, start, length);
    return (start == 0 && end == length - 1) ? value : value.subSequence(start, end + 1);
  }
  
  private static int indexOfFirstNonOwsChar(CharSequence value, int length) {
    int i = 0;
    while (i < length && isOws(value.charAt(i)))
      i++; 
    return i;
  }
  
  private static int indexOfLastNonOwsChar(CharSequence value, int start, int length) {
    int i = length - 1;
    while (i > start && isOws(value.charAt(i)))
      i--; 
    return i;
  }
  
  private static boolean isOws(char c) {
    return (c == ' ' || c == '\t');
  }
}
