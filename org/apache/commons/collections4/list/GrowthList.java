package org.apache.commons.collections4.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GrowthList<E> extends AbstractSerializableListDecorator<E> {
  private static final long serialVersionUID = -3620001881672L;
  
  public static <E> GrowthList<E> growthList(List<E> list) {
    return new GrowthList<E>(list);
  }
  
  public GrowthList() {
    super(new ArrayList<E>());
  }
  
  public GrowthList(int initialSize) {
    super(new ArrayList<E>(initialSize));
  }
  
  protected GrowthList(List<E> list) {
    super(list);
  }
  
  public void add(int index, E element) {
    int size = decorated().size();
    if (index > size)
      decorated().addAll(Collections.nCopies(index - size, null)); 
    decorated().add(index, element);
  }
  
  public boolean addAll(int index, Collection<? extends E> coll) {
    int size = decorated().size();
    boolean result = false;
    if (index > size) {
      decorated().addAll(Collections.nCopies(index - size, null));
      result = true;
    } 
    return decorated().addAll(index, coll) | result;
  }
  
  public E set(int index, E element) {
    int size = decorated().size();
    if (index >= size)
      decorated().addAll(Collections.nCopies(index - size + 1, null)); 
    return decorated().set(index, element);
  }
}
