package org.apache.commons.collections4.list;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections4.collection.AbstractCollectionDecorator;

public abstract class AbstractListDecorator<E> extends AbstractCollectionDecorator<E> implements List<E> {
  private static final long serialVersionUID = 4500739654952315623L;
  
  protected AbstractListDecorator() {}
  
  protected AbstractListDecorator(List<E> list) {
    super(list);
  }
  
  protected List<E> decorated() {
    return (List<E>)super.decorated();
  }
  
  public void add(int index, E object) {
    decorated().add(index, object);
  }
  
  public boolean addAll(int index, Collection<? extends E> coll) {
    return decorated().addAll(index, coll);
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
  
  public ListIterator<E> listIterator() {
    return decorated().listIterator();
  }
  
  public ListIterator<E> listIterator(int index) {
    return decorated().listIterator(index);
  }
  
  public E remove(int index) {
    return decorated().remove(index);
  }
  
  public E set(int index, E object) {
    return decorated().set(index, object);
  }
  
  public List<E> subList(int fromIndex, int toIndex) {
    return decorated().subList(fromIndex, toIndex);
  }
}
