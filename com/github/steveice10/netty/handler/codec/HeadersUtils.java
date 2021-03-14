package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HeadersUtils {
  public static <K, V> List<String> getAllAsString(Headers<K, V, ?> headers, K name) {
    final List<V> allNames = headers.getAll(name);
    return new AbstractList<String>() {
        public String get(int index) {
          V value = allNames.get(index);
          return (value != null) ? value.toString() : null;
        }
        
        public int size() {
          return allNames.size();
        }
      };
  }
  
  public static <K, V> String getAsString(Headers<K, V, ?> headers, K name) {
    V orig = headers.get(name);
    return (orig != null) ? orig.toString() : null;
  }
  
  public static Iterator<Map.Entry<String, String>> iteratorAsString(Iterable<Map.Entry<CharSequence, CharSequence>> headers) {
    return new StringEntryIterator(headers.iterator());
  }
  
  public static <K, V> String toString(Class<?> headersClass, Iterator<Map.Entry<K, V>> headersIt, int size) {
    String simpleName = headersClass.getSimpleName();
    if (size == 0)
      return simpleName + "[]"; 
    StringBuilder sb = (new StringBuilder(simpleName.length() + 2 + size * 20)).append(simpleName).append('[');
    while (headersIt.hasNext()) {
      Map.Entry<?, ?> header = headersIt.next();
      sb.append(header.getKey()).append(": ").append(header.getValue()).append(", ");
    } 
    sb.setLength(sb.length() - 2);
    return sb.append(']').toString();
  }
  
  public static Set<String> namesAsString(Headers<CharSequence, CharSequence, ?> headers) {
    return new CharSequenceDelegatingStringSet(headers.names());
  }
  
  private static final class StringEntryIterator implements Iterator<Map.Entry<String, String>> {
    private final Iterator<Map.Entry<CharSequence, CharSequence>> iter;
    
    StringEntryIterator(Iterator<Map.Entry<CharSequence, CharSequence>> iter) {
      this.iter = iter;
    }
    
    public boolean hasNext() {
      return this.iter.hasNext();
    }
    
    public Map.Entry<String, String> next() {
      return new HeadersUtils.StringEntry(this.iter.next());
    }
    
    public void remove() {
      this.iter.remove();
    }
  }
  
  private static final class StringEntry implements Map.Entry<String, String> {
    private final Map.Entry<CharSequence, CharSequence> entry;
    
    private String name;
    
    private String value;
    
    StringEntry(Map.Entry<CharSequence, CharSequence> entry) {
      this.entry = entry;
    }
    
    public String getKey() {
      if (this.name == null)
        this.name = ((CharSequence)this.entry.getKey()).toString(); 
      return this.name;
    }
    
    public String getValue() {
      if (this.value == null && this.entry.getValue() != null)
        this.value = ((CharSequence)this.entry.getValue()).toString(); 
      return this.value;
    }
    
    public String setValue(String value) {
      String old = getValue();
      this.entry.setValue(value);
      return old;
    }
    
    public String toString() {
      return this.entry.toString();
    }
  }
  
  private static final class StringIterator<T> implements Iterator<String> {
    private final Iterator<T> iter;
    
    StringIterator(Iterator<T> iter) {
      this.iter = iter;
    }
    
    public boolean hasNext() {
      return this.iter.hasNext();
    }
    
    public String next() {
      T next = this.iter.next();
      return (next != null) ? next.toString() : null;
    }
    
    public void remove() {
      this.iter.remove();
    }
  }
  
  private static final class CharSequenceDelegatingStringSet extends DelegatingStringSet<CharSequence> {
    CharSequenceDelegatingStringSet(Set<CharSequence> allNames) {
      super(allNames);
    }
    
    public boolean add(String e) {
      return this.allNames.add(e);
    }
    
    public boolean addAll(Collection<? extends String> c) {
      return this.allNames.addAll((Collection)c);
    }
  }
  
  private static abstract class DelegatingStringSet<T> extends AbstractCollection<String> implements Set<String> {
    protected final Set<T> allNames;
    
    DelegatingStringSet(Set<T> allNames) {
      this.allNames = (Set<T>)ObjectUtil.checkNotNull(allNames, "allNames");
    }
    
    public int size() {
      return this.allNames.size();
    }
    
    public boolean isEmpty() {
      return this.allNames.isEmpty();
    }
    
    public boolean contains(Object o) {
      return this.allNames.contains(o.toString());
    }
    
    public Iterator<String> iterator() {
      return new HeadersUtils.StringIterator(this.allNames.iterator());
    }
    
    public boolean remove(Object o) {
      return this.allNames.remove(o);
    }
    
    public void clear() {
      this.allNames.clear();
    }
  }
}
