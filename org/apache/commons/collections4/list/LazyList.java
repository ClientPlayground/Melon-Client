package org.apache.commons.collections4.list;

import java.util.List;
import org.apache.commons.collections4.Factory;

public class LazyList<E> extends AbstractSerializableListDecorator<E> {
  private static final long serialVersionUID = -1708388017160694542L;
  
  private final Factory<? extends E> factory;
  
  public static <E> LazyList<E> lazyList(List<E> list, Factory<? extends E> factory) {
    return new LazyList<E>(list, factory);
  }
  
  protected LazyList(List<E> list, Factory<? extends E> factory) {
    super(list);
    if (factory == null)
      throw new IllegalArgumentException("Factory must not be null"); 
    this.factory = factory;
  }
  
  public E get(int index) {
    int size = decorated().size();
    if (index < size) {
      E e = decorated().get(index);
      if (e == null) {
        e = (E)this.factory.create();
        decorated().set(index, e);
        return e;
      } 
      return e;
    } 
    for (int i = size; i < index; i++)
      decorated().add(null); 
    E object = (E)this.factory.create();
    decorated().add(object);
    return object;
  }
  
  public List<E> subList(int fromIndex, int toIndex) {
    List<E> sub = decorated().subList(fromIndex, toIndex);
    return new LazyList(sub, this.factory);
  }
}
