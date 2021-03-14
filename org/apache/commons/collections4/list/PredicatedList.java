package org.apache.commons.collections4.list;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.collection.PredicatedCollection;
import org.apache.commons.collections4.iterators.AbstractListIteratorDecorator;

public class PredicatedList<E> extends PredicatedCollection<E> implements List<E> {
  private static final long serialVersionUID = -5722039223898659102L;
  
  public static <T> PredicatedList<T> predicatedList(List<T> list, Predicate<? super T> predicate) {
    return new PredicatedList<T>(list, predicate);
  }
  
  protected PredicatedList(List<E> list, Predicate<? super E> predicate) {
    super(list, predicate);
  }
  
  protected List<E> decorated() {
    return (List<E>)super.decorated();
  }
  
  public E get(int index) {
    return decorated().get(index);
  }
  
  public int indexOf(Object object) {
    return decorated().indexOf(object);
  }
  
  public int lastIndexOf(Object object) {
    return decorated().lastIndexOf(object);
  }
  
  public E remove(int index) {
    return decorated().remove(index);
  }
  
  public void add(int index, E object) {
    validate(object);
    decorated().add(index, object);
  }
  
  public boolean addAll(int index, Collection<? extends E> coll) {
    for (E aColl : coll)
      validate(aColl); 
    return decorated().addAll(index, coll);
  }
  
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }
  
  public ListIterator<E> listIterator(int i) {
    return (ListIterator<E>)new PredicatedListIterator(decorated().listIterator(i));
  }
  
  public E set(int index, E object) {
    validate(object);
    return decorated().set(index, object);
  }
  
  public List<E> subList(int fromIndex, int toIndex) {
    List<E> sub = decorated().subList(fromIndex, toIndex);
    return new PredicatedList(sub, this.predicate);
  }
  
  protected class PredicatedListIterator extends AbstractListIteratorDecorator<E> {
    protected PredicatedListIterator(ListIterator<E> iterator) {
      super(iterator);
    }
    
    public void add(E object) {
      PredicatedList.this.validate(object);
      getListIterator().add(object);
    }
    
    public void set(E object) {
      PredicatedList.this.validate(object);
      getListIterator().set(object);
    }
  }
}
