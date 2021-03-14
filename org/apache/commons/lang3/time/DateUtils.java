package org.apache.commons.lang3.time;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class DateUtils {
  public static final long MILLIS_PER_SECOND = 1000L;
  
  public static final long MILLIS_PER_MINUTE = 60000L;
  
  public static final long MILLIS_PER_HOUR = 3600000L;
  
  public static final long MILLIS_PER_DAY = 86400000L;
  
  public static final int SEMI_MONTH = 1001;
  
  private static final int[][] fields = new int[][] { { 14 }, { 13 }, { 12 }, { 11, 10 }, { 5, 5, 9 }, { 2, 1001 }, { 1 }, { 0 } };
  
  public static final int RANGE_WEEK_SUNDAY = 1;
  
  public static final int RANGE_WEEK_MONDAY = 2;
  
  public static final int RANGE_WEEK_RELATIVE = 3;
  
  public static final int RANGE_WEEK_CENTER = 4;
  
  public static final int RANGE_MONTH_SUNDAY = 5;
  
  public static final int RANGE_MONTH_MONDAY = 6;
  
  private static final int MODIFY_TRUNCATE = 0;
  
  private static final int MODIFY_ROUND = 1;
  
  private static final int MODIFY_CEILING = 2;
  
  public static boolean isSameDay(Date date1, Date date2) {
    if (date1 == null || date2 == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(date1);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(date2);
    return isSameDay(cal1, cal2);
  }
  
  public static boolean isSameDay(Calendar cal1, Calendar cal2) {
    if (cal1 == null || cal2 == null)
      throw new IllegalArgumentException("The date must not be null"); 
    return (cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6));
  }
  
  public static boolean isSameInstant(Date date1, Date date2) {
    if (date1 == null || date2 == null)
      throw new IllegalArgumentException("The date must not be null"); 
    return (date1.getTime() == date2.getTime());
  }
  
  public static boolean isSameInstant(Calendar cal1, Calendar cal2) {
    if (cal1 == null || cal2 == null)
      throw new IllegalArgumentException("The date must not be null"); 
    return (cal1.getTime().getTime() == cal2.getTime().getTime());
  }
  
  public static boolean isSameLocalTime(Calendar cal1, Calendar cal2) {
    if (cal1 == null || cal2 == null)
      throw new IllegalArgumentException("The date must not be null"); 
    return (cal1.get(14) == cal2.get(14) && cal1.get(13) == cal2.get(13) && cal1.get(12) == cal2.get(12) && cal1.get(11) == cal2.get(11) && cal1.get(6) == cal2.get(6) && cal1.get(1) == cal2.get(1) && cal1.get(0) == cal2.get(0) && cal1.getClass() == cal2.getClass());
  }
  
  public static Date parseDate(String str, String... parsePatterns) throws ParseException {
    return parseDate(str, null, parsePatterns);
  }
  
  public static Date parseDate(String str, Locale locale, String... parsePatterns) throws ParseException {
    return parseDateWithLeniency(str, locale, parsePatterns, true);
  }
  
  public static Date parseDateStrictly(String str, String... parsePatterns) throws ParseException {
    return parseDateStrictly(str, null, parsePatterns);
  }
  
  public static Date parseDateStrictly(String str, Locale locale, String... parsePatterns) throws ParseException {
    return parseDateWithLeniency(str, null, parsePatterns, false);
  }
  
  private static Date parseDateWithLeniency(String str, Locale locale, String[] parsePatterns, boolean lenient) throws ParseException {
    SimpleDateFormat parser;
    if (str == null || parsePatterns == null)
      throw new IllegalArgumentException("Date and Patterns must not be null"); 
    if (locale == null) {
      parser = new SimpleDateFormat();
    } else {
      parser = new SimpleDateFormat("", locale);
    } 
    parser.setLenient(lenient);
    ParsePosition pos = new ParsePosition(0);
    for (String parsePattern : parsePatterns) {
      String pattern = parsePattern;
      if (parsePattern.endsWith("ZZ"))
        pattern = pattern.substring(0, pattern.length() - 1); 
      parser.applyPattern(pattern);
      pos.setIndex(0);
      String str2 = str;
      if (parsePattern.endsWith("ZZ"))
        str2 = str.replaceAll("([-+][0-9][0-9]):([0-9][0-9])$", "$1$2"); 
      Date date = parser.parse(str2, pos);
      if (date != null && pos.getIndex() == str2.length())
        return date; 
    } 
    throw new ParseException("Unable to parse the date: " + str, -1);
  }
  
  public static Date addYears(Date date, int amount) {
    return add(date, 1, amount);
  }
  
  public static Date addMonths(Date date, int amount) {
    return add(date, 2, amount);
  }
  
  public static Date addWeeks(Date date, int amount) {
    return add(date, 3, amount);
  }
  
  public static Date addDays(Date date, int amount) {
    return add(date, 5, amount);
  }
  
  public static Date addHours(Date date, int amount) {
    return add(date, 11, amount);
  }
  
  public static Date addMinutes(Date date, int amount) {
    return add(date, 12, amount);
  }
  
  public static Date addSeconds(Date date, int amount) {
    return add(date, 13, amount);
  }
  
  public static Date addMilliseconds(Date date, int amount) {
    return add(date, 14, amount);
  }
  
  private static Date add(Date date, int calendarField, int amount) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(calendarField, amount);
    return c.getTime();
  }
  
  public static Date setYears(Date date, int amount) {
    return set(date, 1, amount);
  }
  
  public static Date setMonths(Date date, int amount) {
    return set(date, 2, amount);
  }
  
  public static Date setDays(Date date, int amount) {
    return set(date, 5, amount);
  }
  
  public static Date setHours(Date date, int amount) {
    return set(date, 11, amount);
  }
  
  public static Date setMinutes(Date date, int amount) {
    return set(date, 12, amount);
  }
  
  public static Date setSeconds(Date date, int amount) {
    return set(date, 13, amount);
  }
  
  public static Date setMilliseconds(Date date, int amount) {
    return set(date, 14, amount);
  }
  
  private static Date set(Date date, int calendarField, int amount) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar c = Calendar.getInstance();
    c.setLenient(false);
    c.setTime(date);
    c.set(calendarField, amount);
    return c.getTime();
  }
  
  public static Calendar toCalendar(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c;
  }
  
  public static Date round(Date date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar gval = Calendar.getInstance();
    gval.setTime(date);
    modify(gval, field, 1);
    return gval.getTime();
  }
  
  public static Calendar round(Calendar date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar rounded = (Calendar)date.clone();
    modify(rounded, field, 1);
    return rounded;
  }
  
  public static Date round(Object date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    if (date instanceof Date)
      return round((Date)date, field); 
    if (date instanceof Calendar)
      return round((Calendar)date, field).getTime(); 
    throw new ClassCastException("Could not round " + date);
  }
  
  public static Date truncate(Date date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar gval = Calendar.getInstance();
    gval.setTime(date);
    modify(gval, field, 0);
    return gval.getTime();
  }
  
  public static Calendar truncate(Calendar date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar truncated = (Calendar)date.clone();
    modify(truncated, field, 0);
    return truncated;
  }
  
  public static Date truncate(Object date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    if (date instanceof Date)
      return truncate((Date)date, field); 
    if (date instanceof Calendar)
      return truncate((Calendar)date, field).getTime(); 
    throw new ClassCastException("Could not truncate " + date);
  }
  
  public static Date ceiling(Date date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar gval = Calendar.getInstance();
    gval.setTime(date);
    modify(gval, field, 2);
    return gval.getTime();
  }
  
  public static Calendar ceiling(Calendar date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar ceiled = (Calendar)date.clone();
    modify(ceiled, field, 2);
    return ceiled;
  }
  
  public static Date ceiling(Object date, int field) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    if (date instanceof Date)
      return ceiling((Date)date, field); 
    if (date instanceof Calendar)
      return ceiling((Calendar)date, field).getTime(); 
    throw new ClassCastException("Could not find ceiling of for type: " + date.getClass());
  }
  
  private static void modify(Calendar val, int field, int modType) {
    if (val.get(1) > 280000000)
      throw new ArithmeticException("Calendar value too large for accurate calculations"); 
    if (field == 14)
      return; 
    Date date = val.getTime();
    long time = date.getTime();
    boolean done = false;
    int millisecs = val.get(14);
    if (0 == modType || millisecs < 500)
      time -= millisecs; 
    if (field == 13)
      done = true; 
    int seconds = val.get(13);
    if (!done && (0 == modType || seconds < 30))
      time -= seconds * 1000L; 
    if (field == 12)
      done = true; 
    int minutes = val.get(12);
    if (!done && (0 == modType || minutes < 30))
      time -= minutes * 60000L; 
    if (date.getTime() != time) {
      date.setTime(time);
      val.setTime(date);
    } 
    boolean roundUp = false;
    for (int[] aField : fields) {
      for (int element : aField) {
        if (element == field) {
          if (modType == 2 || (modType == 1 && roundUp))
            if (field == 1001) {
              if (val.get(5) == 1) {
                val.add(5, 15);
              } else {
                val.add(5, -15);
                val.add(2, 1);
              } 
            } else if (field == 9) {
              if (val.get(11) == 0) {
                val.add(11, 12);
              } else {
                val.add(11, -12);
                val.add(5, 1);
              } 
            } else {
              val.add(aField[0], 1);
            }  
          return;
        } 
      } 
      int offset = 0;
      boolean offsetSet = false;
      switch (field) {
        case 1001:
          if (aField[0] == 5) {
            offset = val.get(5) - 1;
            if (offset >= 15)
              offset -= 15; 
            roundUp = (offset > 7);
            offsetSet = true;
          } 
          break;
        case 9:
          if (aField[0] == 11) {
            offset = val.get(11);
            if (offset >= 12)
              offset -= 12; 
            roundUp = (offset >= 6);
            offsetSet = true;
          } 
          break;
      } 
      if (!offsetSet) {
        int min = val.getActualMinimum(aField[0]);
        int max = val.getActualMaximum(aField[0]);
        offset = val.get(aField[0]) - min;
        roundUp = (offset > (max - min) / 2);
      } 
      if (offset != 0)
        val.set(aField[0], val.get(aField[0]) - offset); 
    } 
    throw new IllegalArgumentException("The field " + field + " is not supported");
  }
  
  public static Iterator<Calendar> iterator(Date focus, int rangeStyle) {
    if (focus == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar gval = Calendar.getInstance();
    gval.setTime(focus);
    return iterator(gval, rangeStyle);
  }
  
  public static Iterator<Calendar> iterator(Calendar focus, int rangeStyle) {
    if (focus == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar start = null;
    Calendar end = null;
    int startCutoff = 1;
    int endCutoff = 7;
    switch (rangeStyle) {
      case 5:
      case 6:
        start = truncate(focus, 2);
        end = (Calendar)start.clone();
        end.add(2, 1);
        end.add(5, -1);
        if (rangeStyle == 6) {
          startCutoff = 2;
          endCutoff = 1;
        } 
        break;
      case 1:
      case 2:
      case 3:
      case 4:
        start = truncate(focus, 5);
        end = truncate(focus, 5);
        switch (rangeStyle) {
          case 2:
            startCutoff = 2;
            endCutoff = 1;
            break;
          case 3:
            startCutoff = focus.get(7);
            endCutoff = startCutoff - 1;
            break;
          case 4:
            startCutoff = focus.get(7) - 3;
            endCutoff = focus.get(7) + 3;
            break;
        } 
        break;
      default:
        throw new IllegalArgumentException("The range style " + rangeStyle + " is not valid.");
    } 
    if (startCutoff < 1)
      startCutoff += 7; 
    if (startCutoff > 7)
      startCutoff -= 7; 
    if (endCutoff < 1)
      endCutoff += 7; 
    if (endCutoff > 7)
      endCutoff -= 7; 
    while (start.get(7) != startCutoff)
      start.add(5, -1); 
    while (end.get(7) != endCutoff)
      end.add(5, 1); 
    return new DateIterator(start, end);
  }
  
  public static Iterator<?> iterator(Object focus, int rangeStyle) {
    if (focus == null)
      throw new IllegalArgumentException("The date must not be null"); 
    if (focus instanceof Date)
      return iterator((Date)focus, rangeStyle); 
    if (focus instanceof Calendar)
      return iterator((Calendar)focus, rangeStyle); 
    throw new ClassCastException("Could not iterate based on " + focus);
  }
  
  public static long getFragmentInMilliseconds(Date date, int fragment) {
    return getFragment(date, fragment, TimeUnit.MILLISECONDS);
  }
  
  public static long getFragmentInSeconds(Date date, int fragment) {
    return getFragment(date, fragment, TimeUnit.SECONDS);
  }
  
  public static long getFragmentInMinutes(Date date, int fragment) {
    return getFragment(date, fragment, TimeUnit.MINUTES);
  }
  
  public static long getFragmentInHours(Date date, int fragment) {
    return getFragment(date, fragment, TimeUnit.HOURS);
  }
  
  public static long getFragmentInDays(Date date, int fragment) {
    return getFragment(date, fragment, TimeUnit.DAYS);
  }
  
  public static long getFragmentInMilliseconds(Calendar calendar, int fragment) {
    return getFragment(calendar, fragment, TimeUnit.MILLISECONDS);
  }
  
  public static long getFragmentInSeconds(Calendar calendar, int fragment) {
    return getFragment(calendar, fragment, TimeUnit.SECONDS);
  }
  
  public static long getFragmentInMinutes(Calendar calendar, int fragment) {
    return getFragment(calendar, fragment, TimeUnit.MINUTES);
  }
  
  public static long getFragmentInHours(Calendar calendar, int fragment) {
    return getFragment(calendar, fragment, TimeUnit.HOURS);
  }
  
  public static long getFragmentInDays(Calendar calendar, int fragment) {
    return getFragment(calendar, fragment, TimeUnit.DAYS);
  }
  
  private static long getFragment(Date date, int fragment, TimeUnit unit) {
    if (date == null)
      throw new IllegalArgumentException("The date must not be null"); 
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return getFragment(calendar, fragment, unit);
  }
  
  private static long getFragment(Calendar calendar, int fragment, TimeUnit unit) {
    if (calendar == null)
      throw new IllegalArgumentException("The date must not be null"); 
    long result = 0L;
    int offset = (unit == TimeUnit.DAYS) ? 0 : 1;
    switch (fragment) {
      case 1:
        result += unit.convert((calendar.get(6) - offset), TimeUnit.DAYS);
        break;
      case 2:
        result += unit.convert((calendar.get(5) - offset), TimeUnit.DAYS);
        break;
    } 
    switch (fragment) {
      case 1:
      case 2:
      case 5:
      case 6:
        result += unit.convert(calendar.get(11), TimeUnit.HOURS);
      case 11:
        result += unit.convert(calendar.get(12), TimeUnit.MINUTES);
      case 12:
        result += unit.convert(calendar.get(13), TimeUnit.SECONDS);
      case 13:
        result += unit.convert(calendar.get(14), TimeUnit.MILLISECONDS);
      case 14:
        return result;
    } 
    throw new IllegalArgumentException("The fragment " + fragment + " is not supported");
  }
  
  public static boolean truncatedEquals(Calendar cal1, Calendar cal2, int field) {
    return (truncatedCompareTo(cal1, cal2, field) == 0);
  }
  
  public static boolean truncatedEquals(Date date1, Date date2, int field) {
    return (truncatedCompareTo(date1, date2, field) == 0);
  }
  
  public static int truncatedCompareTo(Calendar cal1, Calendar cal2, int field) {
    Calendar truncatedCal1 = truncate(cal1, field);
    Calendar truncatedCal2 = truncate(cal2, field);
    return truncatedCal1.compareTo(truncatedCal2);
  }
  
  public static int truncatedCompareTo(Date date1, Date date2, int field) {
    Date truncatedDate1 = truncate(date1, field);
    Date truncatedDate2 = truncate(date2, field);
    return truncatedDate1.compareTo(truncatedDate2);
  }
  
  static class DateIterator implements Iterator<Calendar> {
    private final Calendar endFinal;
    
    private final Calendar spot;
    
    DateIterator(Calendar startFinal, Calendar endFinal) {
      this.endFinal = endFinal;
      this.spot = startFinal;
      this.spot.add(5, -1);
    }
    
    public boolean hasNext() {
      return this.spot.before(this.endFinal);
    }
    
    public Calendar next() {
      if (this.spot.equals(this.endFinal))
        throw new NoSuchElementException(); 
      this.spot.add(5, 1);
      return (Calendar)this.spot.clone();
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
