package org.apache.commons.collections4.list;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.collection.TransformedCollection;
import org.apache.commons.collections4.iterators.AbstractListIteratorDecorator;

public class TransformedList<E> extends TransformedCollection<E> implements List<E> {
  private static final long serialVersionUID = 1077193035000013141L;
  
  public static <E> TransformedList<E> transformingList(List<E> list, Transformer<? super E, ? extends E> transformer) {
    return new TransformedList<E>(list, transformer);
  }
  
  public static <E> TransformedList<E> transformedList(List<E> list, Transformer<? super E, ? extends E> transformer) {
    TransformedList<E> decorated = new TransformedList<E>(list, transformer);
    if (transformer != null && list != null && list.size() > 0) {
      E[] values = (E[])list.toArray();
      list.clear();
      for (E value : values)
        decorated.decorated().add(transformer.transform(value)); 
    } 
    return decorated;
  }
  
  protected TransformedList(List<E> list, Transformer<? super E, ? extends E> transformer) {
    super(list, transformer);
  }
  
  protected List<E> getList() {
    return (List<E>)decorated();
  }
  
  public E get(int index) {
    return getList().get(index);
  }
  
  public int indexOf(Object object) {
    return getList().indexOf(object);
  }
  
  public int lastIndexOf(Object object) {
    return getList().lastIndexOf(object);
  }
  
  public E remove(int index) {
    return getList().remove(index);
  }
  
  public void add(int index, E object) {
    object = (E)transform(object);
    getList().add(index, object);
  }
  
  public boolean addAll(int index, Collection<? extends E> coll) {
    coll = transform(coll);
    return getList().addAll(index, coll);
  }
  
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }
  
  public ListIterator<E> listIterator(int i) {
    return (ListIterator<E>)new TransformedListIterator(getList().listIterator(i));
  }
  
  public E set(int index, E object) {
    object = (E)transform(object);
    return getList().set(index, object);
  }
  
  public List<E> subList(int fromIndex, int toIndex) {
    List<E> sub = getList().subList(fromIndex, toIndex);
    return new TransformedList(sub, this.transformer);
  }
  
  protected class TransformedListIterator extends AbstractListIteratorDecorator<E> {
    protected TransformedListIterator(ListIterator<E> iterator) {
      super(iterator);
    }
    
    public void add(E object) {
      object = (E)TransformedList.this.transform(object);
      getListIterator().add(object);
    }
    
    public void set(E object) {
      object = (E)TransformedList.this.transform(object);
      getListIterator().set(object);
    }
  }
}
