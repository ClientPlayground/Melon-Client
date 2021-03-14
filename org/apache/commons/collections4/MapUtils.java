package org.apache.commons.collections4;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections4.map.AbstractMapDecorator;
import org.apache.commons.collections4.map.AbstractSortedMapDecorator;
import org.apache.commons.collections4.map.FixedSizeMap;
import org.apache.commons.collections4.map.FixedSizeSortedMap;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.collections4.map.LazySortedMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.collections4.map.PredicatedMap;
import org.apache.commons.collections4.map.PredicatedSortedMap;
import org.apache.commons.collections4.map.TransformedMap;
import org.apache.commons.collections4.map.TransformedSortedMap;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.apache.commons.collections4.map.UnmodifiableSortedMap;

public class MapUtils {
  public static final SortedMap EMPTY_SORTED_MAP = UnmodifiableSortedMap.unmodifiableSortedMap(new TreeMap<Object, Object>());
  
  private static final String INDENT_STRING = "    ";
  
  public static <K, V> V getObject(Map<? super K, V> map, K key) {
    if (map != null)
      return map.get(key); 
    return null;
  }
  
  public static <K> String getString(Map<? super K, ?> map, K key) {
    if (map != null) {
      Object answer = map.get(key);
      if (answer != null)
        return answer.toString(); 
    } 
    return null;
  }
  
  public static <K> Boolean getBoolean(Map<? super K, ?> map, K key) {
    if (map != null) {
      Object answer = map.get(key);
      if (answer != null) {
        if (answer instanceof Boolean)
          return (Boolean)answer; 
        if (answer instanceof String)
          return Boolean.valueOf((String)answer); 
        if (answer instanceof Number) {
          Number n = (Number)answer;
          return (n.intValue() != 0) ? Boolean.TRUE : Boolean.FALSE;
        } 
      } 
    } 
    return null;
  }
  
  public static <K> Number getNumber(Map<? super K, ?> map, K key) {
    if (map != null) {
      Object answer = map.get(key);
      if (answer != null) {
        if (answer instanceof Number)
          return (Number)answer; 
        if (answer instanceof String)
          try {
            String text = (String)answer;
            return NumberFormat.getInstance().parse(text);
          } catch (ParseException e) {} 
      } 
    } 
    return null;
  }
  
  public static <K> Byte getByte(Map<? super K, ?> map, K key) {
    Number answer = getNumber(map, key);
    if (answer == null)
      return null; 
    if (answer instanceof Byte)
      return (Byte)answer; 
    return Byte.valueOf(answer.byteValue());
  }
  
  public static <K> Short getShort(Map<? super K, ?> map, K key) {
    Number answer = getNumber(map, key);
    if (answer == null)
      return null; 
    if (answer instanceof Short)
      return (Short)answer; 
    return Short.valueOf(answer.shortValue());
  }
  
  public static <K> Integer getInteger(Map<? super K, ?> map, K key) {
    Number answer = getNumber(map, key);
    if (answer == null)
      return null; 
    if (answer instanceof Integer)
      return (Integer)answer; 
    return Integer.valueOf(answer.intValue());
  }
  
  public static <K> Long getLong(Map<? super K, ?> map, K key) {
    Number answer = getNumber(map, key);
    if (answer == null)
      return null; 
    if (answer instanceof Long)
      return (Long)answer; 
    return Long.valueOf(answer.longValue());
  }
  
  public static <K> Float getFloat(Map<? super K, ?> map, K key) {
    Number answer = getNumber(map, key);
    if (answer == null)
      return null; 
    if (answer instanceof Float)
      return (Float)answer; 
    return Float.valueOf(answer.floatValue());
  }
  
  public static <K> Double getDouble(Map<? super K, ?> map, K key) {
    Number answer = getNumber(map, key);
    if (answer == null)
      return null; 
    if (answer instanceof Double)
      return (Double)answer; 
    return Double.valueOf(answer.doubleValue());
  }
  
  public static <K> Map<?, ?> getMap(Map<? super K, ?> map, K key) {
    if (map != null) {
      Object answer = map.get(key);
      if (answer != null && answer instanceof Map)
        return (Map<?, ?>)answer; 
    } 
    return null;
  }
  
  public static <K, V> V getObject(Map<K, V> map, K key, V defaultValue) {
    if (map != null) {
      V answer = map.get(key);
      if (answer != null)
        return answer; 
    } 
    return defaultValue;
  }
  
  public static <K> String getString(Map<? super K, ?> map, K key, String defaultValue) {
    String answer = getString(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Boolean getBoolean(Map<? super K, ?> map, K key, Boolean defaultValue) {
    Boolean answer = getBoolean(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Number getNumber(Map<? super K, ?> map, K key, Number defaultValue) {
    Number answer = getNumber(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Byte getByte(Map<? super K, ?> map, K key, Byte defaultValue) {
    Byte answer = getByte(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Short getShort(Map<? super K, ?> map, K key, Short defaultValue) {
    Short answer = getShort(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Integer getInteger(Map<? super K, ?> map, K key, Integer defaultValue) {
    Integer answer = getInteger(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Long getLong(Map<? super K, ?> map, K key, Long defaultValue) {
    Long answer = getLong(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Float getFloat(Map<? super K, ?> map, K key, Float defaultValue) {
    Float answer = getFloat(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Double getDouble(Map<? super K, ?> map, K key, Double defaultValue) {
    Double answer = getDouble(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> Map<?, ?> getMap(Map<? super K, ?> map, K key, Map<?, ?> defaultValue) {
    Map<?, ?> answer = getMap(map, key);
    if (answer == null)
      answer = defaultValue; 
    return answer;
  }
  
  public static <K> boolean getBooleanValue(Map<? super K, ?> map, K key) {
    return Boolean.TRUE.equals(getBoolean(map, key));
  }
  
  public static <K> byte getByteValue(Map<? super K, ?> map, K key) {
    Byte byteObject = getByte(map, key);
    if (byteObject == null)
      return 0; 
    return byteObject.byteValue();
  }
  
  public static <K> short getShortValue(Map<? super K, ?> map, K key) {
    Short shortObject = getShort(map, key);
    if (shortObject == null)
      return 0; 
    return shortObject.shortValue();
  }
  
  public static <K> int getIntValue(Map<? super K, ?> map, K key) {
    Integer integerObject = getInteger(map, key);
    if (integerObject == null)
      return 0; 
    return integerObject.intValue();
  }
  
  public static <K> long getLongValue(Map<? super K, ?> map, K key) {
    Long longObject = getLong(map, key);
    if (longObject == null)
      return 0L; 
    return longObject.longValue();
  }
  
  public static <K> float getFloatValue(Map<? super K, ?> map, K key) {
    Float floatObject = getFloat(map, key);
    if (floatObject == null)
      return 0.0F; 
    return floatObject.floatValue();
  }
  
  public static <K> double getDoubleValue(Map<? super K, ?> map, K key) {
    Double doubleObject = getDouble(map, key);
    if (doubleObject == null)
      return 0.0D; 
    return doubleObject.doubleValue();
  }
  
  public static <K> boolean getBooleanValue(Map<? super K, ?> map, K key, boolean defaultValue) {
    Boolean booleanObject = getBoolean(map, key);
    if (booleanObject == null)
      return defaultValue; 
    return booleanObject.booleanValue();
  }
  
  public static <K> byte getByteValue(Map<? super K, ?> map, K key, byte defaultValue) {
    Byte byteObject = getByte(map, key);
    if (byteObject == null)
      return defaultValue; 
    return byteObject.byteValue();
  }
  
  public static <K> short getShortValue(Map<? super K, ?> map, K key, short defaultValue) {
    Short shortObject = getShort(map, key);
    if (shortObject == null)
      return defaultValue; 
    return shortObject.shortValue();
  }
  
  public static <K> int getIntValue(Map<? super K, ?> map, K key, int defaultValue) {
    Integer integerObject = getInteger(map, key);
    if (integerObject == null)
      return defaultValue; 
    return integerObject.intValue();
  }
  
  public static <K> long getLongValue(Map<? super K, ?> map, K key, long defaultValue) {
    Long longObject = getLong(map, key);
    if (longObject == null)
      return defaultValue; 
    return longObject.longValue();
  }
  
  public static <K> float getFloatValue(Map<? super K, ?> map, K key, float defaultValue) {
    Float floatObject = getFloat(map, key);
    if (floatObject == null)
      return defaultValue; 
    return floatObject.floatValue();
  }
  
  public static <K> double getDoubleValue(Map<? super K, ?> map, K key, double defaultValue) {
    Double doubleObject = getDouble(map, key);
    if (doubleObject == null)
      return defaultValue; 
    return doubleObject.doubleValue();
  }
  
  public static <K, V> Properties toProperties(Map<K, V> map) {
    Properties answer = new Properties();
    if (map != null)
      for (Map.Entry<K, V> entry2 : map.entrySet()) {
        Map.Entry<?, ?> entry = entry2;
        Object key = entry.getKey();
        Object value = entry.getValue();
        answer.put(key, value);
      }  
    return answer;
  }
  
  public static Map<String, Object> toMap(ResourceBundle resourceBundle) {
    Enumeration<String> enumeration = resourceBundle.getKeys();
    Map<String, Object> map = new HashMap<String, Object>();
    while (enumeration.hasMoreElements()) {
      String key = enumeration.nextElement();
      Object value = resourceBundle.getObject(key);
      map.put(key, value);
    } 
    return map;
  }
  
  public static void verbosePrint(PrintStream out, Object label, Map<?, ?> map) {
    verbosePrintInternal(out, label, map, new ArrayStack<Map<?, ?>>(), false);
  }
  
  public static void debugPrint(PrintStream out, Object label, Map<?, ?> map) {
    verbosePrintInternal(out, label, map, new ArrayStack<Map<?, ?>>(), true);
  }
  
  private static void verbosePrintInternal(PrintStream out, Object label, Map<?, ?> map, ArrayStack<Map<?, ?>> lineage, boolean debug) {
    printIndent(out, lineage.size());
    if (map == null) {
      if (label != null) {
        out.print(label);
        out.print(" = ");
      } 
      out.println("null");
      return;
    } 
    if (label != null) {
      out.print(label);
      out.println(" = ");
    } 
    printIndent(out, lineage.size());
    out.println("{");
    lineage.push(map);
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      Object childKey = entry.getKey();
      Object childValue = entry.getValue();
      if (childValue instanceof Map && !lineage.contains(childValue)) {
        verbosePrintInternal(out, (childKey == null) ? "null" : childKey, (Map<?, ?>)childValue, lineage, debug);
        continue;
      } 
      printIndent(out, lineage.size());
      out.print(childKey);
      out.print(" = ");
      int lineageIndex = lineage.indexOf(childValue);
      if (lineageIndex == -1) {
        out.print(childValue);
      } else if (lineage.size() - 1 == lineageIndex) {
        out.print("(this Map)");
      } else {
        out.print("(ancestor[" + (lineage.size() - 1 - lineageIndex - 1) + "] Map)");
      } 
      if (debug && childValue != null) {
        out.print(' ');
        out.println(childValue.getClass().getName());
        continue;
      } 
      out.println();
    } 
    lineage.pop();
    printIndent(out, lineage.size());
    out.println(debug ? ("} " + map.getClass().getName()) : "}");
  }
  
  private static void printIndent(PrintStream out, int indent) {
    for (int i = 0; i < indent; i++)
      out.print("    "); 
  }
  
  public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
    Map<V, K> out = new HashMap<V, K>(map.size());
    for (Map.Entry<K, V> entry : map.entrySet())
      out.put(entry.getValue(), entry.getKey()); 
    return out;
  }
  
  public static <K> void safeAddToMap(Map<? super K, Object> map, K key, Object value) throws NullPointerException {
    map.put(key, (value == null) ? "" : value);
  }
  
  public static <K, V> Map<K, V> putAll(Map<K, V> map, Object[] array) {
    map.size();
    if (array == null || array.length == 0)
      return map; 
    Object obj = array[0];
    if (obj instanceof Map.Entry) {
      for (Object element : array) {
        Map.Entry<K, V> entry = (Map.Entry<K, V>)element;
        map.put(entry.getKey(), entry.getValue());
      } 
    } else if (obj instanceof KeyValue) {
      for (Object element : array) {
        KeyValue<K, V> keyval = (KeyValue<K, V>)element;
        map.put(keyval.getKey(), keyval.getValue());
      } 
    } else if (obj instanceof Object[]) {
      for (int i = 0; i < array.length; i++) {
        Object[] sub = (Object[])array[i];
        if (sub == null || sub.length < 2)
          throw new IllegalArgumentException("Invalid array element: " + i); 
        map.put((K)sub[0], (V)sub[1]);
      } 
    } else {
      for (int i = 0; i < array.length - 1;)
        map.put((K)array[i++], (V)array[i++]); 
    } 
    return map;
  }
  
  public static <K, V> Map<K, V> emptyIfNull(Map<K, V> map) {
    return (map == null) ? Collections.<K, V>emptyMap() : map;
  }
  
  public static boolean isEmpty(Map<?, ?> map) {
    return (map == null || map.isEmpty());
  }
  
  public static boolean isNotEmpty(Map<?, ?> map) {
    return !isEmpty(map);
  }
  
  public static <K, V> Map<K, V> synchronizedMap(Map<K, V> map) {
    return Collections.synchronizedMap(map);
  }
  
  public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> map) {
    return UnmodifiableMap.unmodifiableMap(map);
  }
  
  public static <K, V> IterableMap<K, V> predicatedMap(Map<K, V> map, Predicate<? super K> keyPred, Predicate<? super V> valuePred) {
    return (IterableMap<K, V>)PredicatedMap.predicatedMap(map, keyPred, valuePred);
  }
  
  public static <K, V> IterableMap<K, V> transformedMap(Map<K, V> map, Transformer<? super K, ? extends K> keyTransformer, Transformer<? super V, ? extends V> valueTransformer) {
    return (IterableMap<K, V>)TransformedMap.transformingMap(map, keyTransformer, valueTransformer);
  }
  
  public static <K, V> IterableMap<K, V> fixedSizeMap(Map<K, V> map) {
    return (IterableMap<K, V>)FixedSizeMap.fixedSizeMap(map);
  }
  
  public static <K, V> IterableMap<K, V> lazyMap(Map<K, V> map, Factory<? extends V> factory) {
    return (IterableMap<K, V>)LazyMap.lazyMap(map, factory);
  }
  
  public static <K, V> IterableMap<K, V> lazyMap(Map<K, V> map, Transformer<? super K, ? extends V> transformerFactory) {
    return (IterableMap<K, V>)LazyMap.lazyMap(map, transformerFactory);
  }
  
  public static <K, V> OrderedMap<K, V> orderedMap(Map<K, V> map) {
    return (OrderedMap<K, V>)ListOrderedMap.listOrderedMap(map);
  }
  
  public static <K, V> MultiValueMap<K, V> multiValueMap(Map<K, ? super Collection<V>> map) {
    return MultiValueMap.multiValueMap(map);
  }
  
  public static <K, V, C extends Collection<V>> MultiValueMap<K, V> multiValueMap(Map<K, C> map, Class<C> collectionClass) {
    return MultiValueMap.multiValueMap(map, collectionClass);
  }
  
  public static <K, V, C extends Collection<V>> MultiValueMap<K, V> multiValueMap(Map<K, C> map, Factory<C> collectionFactory) {
    return MultiValueMap.multiValueMap(map, collectionFactory);
  }
  
  public static <K, V> SortedMap<K, V> synchronizedSortedMap(SortedMap<K, V> map) {
    return Collections.synchronizedSortedMap(map);
  }
  
  public static <K, V> SortedMap<K, V> unmodifiableSortedMap(SortedMap<K, ? extends V> map) {
    return UnmodifiableSortedMap.unmodifiableSortedMap(map);
  }
  
  public static <K, V> SortedMap<K, V> predicatedSortedMap(SortedMap<K, V> map, Predicate<? super K> keyPred, Predicate<? super V> valuePred) {
    return (SortedMap<K, V>)PredicatedSortedMap.predicatedSortedMap(map, keyPred, valuePred);
  }
  
  public static <K, V> SortedMap<K, V> transformedSortedMap(SortedMap<K, V> map, Transformer<? super K, ? extends K> keyTransformer, Transformer<? super V, ? extends V> valueTransformer) {
    return (SortedMap<K, V>)TransformedSortedMap.transformingSortedMap(map, keyTransformer, valueTransformer);
  }
  
  public static <K, V> SortedMap<K, V> fixedSizeSortedMap(SortedMap<K, V> map) {
    return (SortedMap<K, V>)FixedSizeSortedMap.fixedSizeSortedMap(map);
  }
  
  public static <K, V> SortedMap<K, V> lazySortedMap(SortedMap<K, V> map, Factory<? extends V> factory) {
    return (SortedMap<K, V>)LazySortedMap.lazySortedMap(map, factory);
  }
  
  public static <K, V> SortedMap<K, V> lazySortedMap(SortedMap<K, V> map, Transformer<? super K, ? extends V> transformerFactory) {
    return (SortedMap<K, V>)LazySortedMap.lazySortedMap(map, transformerFactory);
  }
  
  public static <K, V> void populateMap(Map<K, V> map, Iterable<? extends V> elements, Transformer<V, K> keyTransformer) {
    populateMap(map, elements, keyTransformer, TransformerUtils.nopTransformer());
  }
  
  public static <K, V, E> void populateMap(Map<K, V> map, Iterable<? extends E> elements, Transformer<E, K> keyTransformer, Transformer<E, V> valueTransformer) {
    Iterator<? extends E> iter = elements.iterator();
    while (iter.hasNext()) {
      E temp = iter.next();
      map.put(keyTransformer.transform(temp), valueTransformer.transform(temp));
    } 
  }
  
  public static <K, V> void populateMap(MultiMap<K, V> map, Iterable<? extends V> elements, Transformer<V, K> keyTransformer) {
    populateMap(map, elements, keyTransformer, TransformerUtils.nopTransformer());
  }
  
  public static <K, V, E> void populateMap(MultiMap<K, V> map, Iterable<? extends E> elements, Transformer<E, K> keyTransformer, Transformer<E, V> valueTransformer) {
    Iterator<? extends E> iter = elements.iterator();
    while (iter.hasNext()) {
      E temp = iter.next();
      map.put(keyTransformer.transform(temp), valueTransformer.transform(temp));
    } 
  }
  
  public static <K, V> IterableMap<K, V> iterableMap(Map<K, V> map) {
    if (map == null)
      throw new IllegalArgumentException("Map must not be null"); 
    return (map instanceof IterableMap) ? (IterableMap<K, V>)map : (IterableMap<K, V>)new AbstractMapDecorator<K, V>(map) {
      
      };
  }
  
  public static <K, V> IterableSortedMap<K, V> iterableSortedMap(SortedMap<K, V> sortedMap) {
    if (sortedMap == null)
      throw new IllegalArgumentException("Map must not be null"); 
    return (sortedMap instanceof IterableSortedMap) ? (IterableSortedMap<K, V>)sortedMap : (IterableSortedMap<K, V>)new AbstractSortedMapDecorator<K, V>(sortedMap) {
      
      };
  }
}
