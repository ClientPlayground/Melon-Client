package org.apache.commons.lang3.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;

public class DurationFormatUtils {
  public static final String ISO_EXTENDED_FORMAT_PATTERN = "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.S'S'";
  
  public static String formatDurationHMS(long durationMillis) {
    return formatDuration(durationMillis, "H:mm:ss.SSS");
  }
  
  public static String formatDurationISO(long durationMillis) {
    return formatDuration(durationMillis, "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.S'S'", false);
  }
  
  public static String formatDuration(long durationMillis, String format) {
    return formatDuration(durationMillis, format, true);
  }
  
  public static String formatDuration(long durationMillis, String format, boolean padWithZeros) {
    Token[] tokens = lexx(format);
    long days = 0L;
    long hours = 0L;
    long minutes = 0L;
    long seconds = 0L;
    long milliseconds = durationMillis;
    if (Token.containsTokenWithValue(tokens, d)) {
      days = milliseconds / 86400000L;
      milliseconds -= days * 86400000L;
    } 
    if (Token.containsTokenWithValue(tokens, H)) {
      hours = milliseconds / 3600000L;
      milliseconds -= hours * 3600000L;
    } 
    if (Token.containsTokenWithValue(tokens, m)) {
      minutes = milliseconds / 60000L;
      milliseconds -= minutes * 60000L;
    } 
    if (Token.containsTokenWithValue(tokens, s)) {
      seconds = milliseconds / 1000L;
      milliseconds -= seconds * 1000L;
    } 
    return format(tokens, 0L, 0L, days, hours, minutes, seconds, milliseconds, padWithZeros);
  }
  
  public static String formatDurationWords(long durationMillis, boolean suppressLeadingZeroElements, boolean suppressTrailingZeroElements) {
    String duration = formatDuration(durationMillis, "d' days 'H' hours 'm' minutes 's' seconds'");
    if (suppressLeadingZeroElements) {
      duration = " " + duration;
      String tmp = StringUtils.replaceOnce(duration, " 0 days", "");
      if (tmp.length() != duration.length()) {
        duration = tmp;
        tmp = StringUtils.replaceOnce(duration, " 0 hours", "");
        if (tmp.length() != duration.length()) {
          duration = tmp;
          tmp = StringUtils.replaceOnce(duration, " 0 minutes", "");
          duration = tmp;
          if (tmp.length() != duration.length())
            duration = StringUtils.replaceOnce(tmp, " 0 seconds", ""); 
        } 
      } 
      if (duration.length() != 0)
        duration = duration.substring(1); 
    } 
    if (suppressTrailingZeroElements) {
      String tmp = StringUtils.replaceOnce(duration, " 0 seconds", "");
      if (tmp.length() != duration.length()) {
        duration = tmp;
        tmp = StringUtils.replaceOnce(duration, " 0 minutes", "");
        if (tmp.length() != duration.length()) {
          duration = tmp;
          tmp = StringUtils.replaceOnce(duration, " 0 hours", "");
          if (tmp.length() != duration.length())
            duration = StringUtils.replaceOnce(tmp, " 0 days", ""); 
        } 
      } 
    } 
    duration = " " + duration;
    duration = StringUtils.replaceOnce(duration, " 1 seconds", " 1 second");
    duration = StringUtils.replaceOnce(duration, " 1 minutes", " 1 minute");
    duration = StringUtils.replaceOnce(duration, " 1 hours", " 1 hour");
    duration = StringUtils.replaceOnce(duration, " 1 days", " 1 day");
    return duration.trim();
  }
  
  public static String formatPeriodISO(long startMillis, long endMillis) {
    return formatPeriod(startMillis, endMillis, "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.S'S'", false, TimeZone.getDefault());
  }
  
  public static String formatPeriod(long startMillis, long endMillis, String format) {
    return formatPeriod(startMillis, endMillis, format, true, TimeZone.getDefault());
  }
  
  public static String formatPeriod(long startMillis, long endMillis, String format, boolean padWithZeros, TimeZone timezone) {
    Token[] tokens = lexx(format);
    Calendar start = Calendar.getInstance(timezone);
    start.setTime(new Date(startMillis));
    Calendar end = Calendar.getInstance(timezone);
    end.setTime(new Date(endMillis));
    int milliseconds = end.get(14) - start.get(14);
    int seconds = end.get(13) - start.get(13);
    int minutes = end.get(12) - start.get(12);
    int hours = end.get(11) - start.get(11);
    int days = end.get(5) - start.get(5);
    int months = end.get(2) - start.get(2);
    int years = end.get(1) - start.get(1);
    while (milliseconds < 0) {
      milliseconds += 1000;
      seconds--;
    } 
    while (seconds < 0) {
      seconds += 60;
      minutes--;
    } 
    while (minutes < 0) {
      minutes += 60;
      hours--;
    } 
    while (hours < 0) {
      hours += 24;
      days--;
    } 
    if (Token.containsTokenWithValue(tokens, M)) {
      while (days < 0) {
        days += start.getActualMaximum(5);
        months--;
        start.add(2, 1);
      } 
      while (months < 0) {
        months += 12;
        years--;
      } 
      if (!Token.containsTokenWithValue(tokens, y) && years != 0)
        while (years != 0) {
          months += 12 * years;
          years = 0;
        }  
    } else {
      if (!Token.containsTokenWithValue(tokens, y)) {
        int target = end.get(1);
        if (months < 0)
          target--; 
        while (start.get(1) != target) {
          days += start.getActualMaximum(6) - start.get(6);
          if (start instanceof java.util.GregorianCalendar && start.get(2) == 1 && start.get(5) == 29)
            days++; 
          start.add(1, 1);
          days += start.get(6);
        } 
        years = 0;
      } 
      while (start.get(2) != end.get(2)) {
        days += start.getActualMaximum(5);
        start.add(2, 1);
      } 
      months = 0;
      while (days < 0) {
        days += start.getActualMaximum(5);
        months--;
        start.add(2, 1);
      } 
    } 
    if (!Token.containsTokenWithValue(tokens, d)) {
      hours += 24 * days;
      days = 0;
    } 
    if (!Token.containsTokenWithValue(tokens, H)) {
      minutes += 60 * hours;
      hours = 0;
    } 
    if (!Token.containsTokenWithValue(tokens, m)) {
      seconds += 60 * minutes;
      minutes = 0;
    } 
    if (!Token.containsTokenWithValue(tokens, s)) {
      milliseconds += 1000 * seconds;
      seconds = 0;
    } 
    return format(tokens, years, months, days, hours, minutes, seconds, milliseconds, padWithZeros);
  }
  
  static String format(Token[] tokens, long years, long months, long days, long hours, long minutes, long seconds, long milliseconds, boolean padWithZeros) {
    StringBuilder buffer = new StringBuilder();
    boolean lastOutputSeconds = false;
    int sz = tokens.length;
    for (int i = 0; i < sz; i++) {
      Token token = tokens[i];
      Object value = token.getValue();
      int count = token.getCount();
      if (value instanceof StringBuilder) {
        buffer.append(value.toString());
      } else if (value == y) {
        buffer.append(paddedValue(years, padWithZeros, count));
        lastOutputSeconds = false;
      } else if (value == M) {
        buffer.append(paddedValue(months, padWithZeros, count));
        lastOutputSeconds = false;
      } else if (value == d) {
        buffer.append(paddedValue(days, padWithZeros, count));
        lastOutputSeconds = false;
      } else if (value == H) {
        buffer.append(paddedValue(hours, padWithZeros, count));
        lastOutputSeconds = false;
      } else if (value == m) {
        buffer.append(paddedValue(minutes, padWithZeros, count));
        lastOutputSeconds = false;
      } else if (value == s) {
        buffer.append(paddedValue(seconds, padWithZeros, count));
        lastOutputSeconds = true;
      } else if (value == S) {
        if (lastOutputSeconds) {
          int width = padWithZeros ? Math.max(3, count) : 3;
          buffer.append(paddedValue(milliseconds, true, width));
        } else {
          buffer.append(paddedValue(milliseconds, padWithZeros, count));
        } 
        lastOutputSeconds = false;
      } 
    } 
    return buffer.toString();
  }
  
  private static String paddedValue(long value, boolean padWithZeros, int count) {
    String longString = Long.toString(value);
    return padWithZeros ? StringUtils.leftPad(longString, count, '0') : longString;
  }
  
  static final Object y = "y";
  
  static final Object M = "M";
  
  static final Object d = "d";
  
  static final Object H = "H";
  
  static final Object m = "m";
  
  static final Object s = "s";
  
  static final Object S = "S";
  
  static Token[] lexx(String format) {
    ArrayList<Token> list = new ArrayList<Token>(format.length());
    boolean inLiteral = false;
    StringBuilder buffer = null;
    Token previous = null;
    for (int i = 0; i < format.length(); i++) {
      char ch = format.charAt(i);
      if (inLiteral && ch != '\'') {
        buffer.append(ch);
      } else {
        Object value = null;
        switch (ch) {
          case '\'':
            if (inLiteral) {
              buffer = null;
              inLiteral = false;
              break;
            } 
            buffer = new StringBuilder();
            list.add(new Token(buffer));
            inLiteral = true;
            break;
          case 'y':
            value = y;
            break;
          case 'M':
            value = M;
            break;
          case 'd':
            value = d;
            break;
          case 'H':
            value = H;
            break;
          case 'm':
            value = m;
            break;
          case 's':
            value = s;
            break;
          case 'S':
            value = S;
            break;
          default:
            if (buffer == null) {
              buffer = new StringBuilder();
              list.add(new Token(buffer));
            } 
            buffer.append(ch);
            break;
        } 
        if (value != null) {
          if (previous != null && previous.getValue() == value) {
            previous.increment();
          } else {
            Token token = new Token(value);
            list.add(token);
            previous = token;
          } 
          buffer = null;
        } 
      } 
    } 
    if (inLiteral)
      throw new IllegalArgumentException("Unmatched quote in format: " + format); 
    return list.<Token>toArray(new Token[list.size()]);
  }
  
  static class Token {
    private final Object value;
    
    private int count;
    
    static boolean containsTokenWithValue(Token[] tokens, Object value) {
      int sz = tokens.length;
      for (int i = 0; i < sz; i++) {
        if (tokens[i].getValue() == value)
          return true; 
      } 
      return false;
    }
    
    Token(Object value) {
      this.value = value;
      this.count = 1;
    }
    
    Token(Object value, int count) {
      this.value = value;
      this.count = count;
    }
    
    void increment() {
      this.count++;
    }
    
    int getCount() {
      return this.count;
    }
    
    Object getValue() {
      return this.value;
    }
    
    public boolean equals(Object obj2) {
      if (obj2 instanceof Token) {
        Token tok2 = (Token)obj2;
        if (this.value.getClass() != tok2.value.getClass())
          return false; 
        if (this.count != tok2.count)
          return false; 
        if (this.value instanceof StringBuilder)
          return this.value.toString().equals(tok2.value.toString()); 
        if (this.value instanceof Number)
          return this.value.equals(tok2.value); 
        return (this.value == tok2.value);
      } 
      return false;
    }
    
    public int hashCode() {
      return this.value.hashCode();
    }
    
    public String toString() {
      return StringUtils.repeat(this.value.toString(), this.count);
    }
  }
}
