package org.apache.commons.collections4;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.collections4.iterators.EnumerationIterator;

public class EnumerationUtils {
  public static <E> List<E> toList(Enumeration<? extends E> enumeration) {
    return IteratorUtils.toList((Iterator<? extends E>)new EnumerationIterator(enumeration));
  }
  
  public static List<String> toList(StringTokenizer stringTokenizer) {
    List<String> result = new ArrayList<String>(stringTokenizer.countTokens());
    while (stringTokenizer.hasMoreTokens())
      result.add(stringTokenizer.nextToken()); 
    return result;
  }
}
