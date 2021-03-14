package org.apache.commons.collections4.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractCollectionDecorator<E> implements Collection<E>, Serializable {
  private static final long serialVersionUID = 6249888059822088500L;
  
  private Collection<E> collection;
  
  protected AbstractCollectionDecorator() {}
  
  protected AbstractCollectionDecorator(Collection<E> coll) {
    if (coll == null)
      throw new IllegalArgumentException("Collection must not be null"); 
    this.collection = coll;
  }
  
  protected Collection<E> decorated() {
    return this.collection;
  }
  
  protected void setCollection(Collection<E> coll) {
    this.collection = coll;
  }
  
  public boolean add(E object) {
    return decorated().add(object);
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    return decorated().addAll(coll);
  }
  
  public void clear() {
    decorated().clear();
  }
  
  public boolean contains(Object object) {
    return decorated().contains(object);
  }
  
  public boolean isEmpty() {
    return decorated().isEmpty();
  }
  
  public Iterator<E> iterator() {
    return decorated().iterator();
  }
  
  public boolean remove(Object object) {
    return decorated().remove(object);
  }
  
  public int size() {
    return decorated().size();
  }
  
  public Object[] toArray() {
    return decorated().toArray();
  }
  
  public <T> T[] toArray(T[] object) {
    return decorated().toArray(object);
  }
  
  public boolean containsAll(Collection<?> coll) {
    return decorated().containsAll(coll);
  }
  
  public boolean removeAll(Collection<?> coll) {
    return decorated().removeAll(coll);
  }
  
  public boolean retainAll(Collection<?> coll) {
    return decorated().retainAll(coll);
  }
  
  public boolean equals(Object object) {
    return (object == this || decorated().equals(object));
  }
  
  public int hashCode() {
    return decorated().hashCode();
  }
  
  public String toString() {
    return decorated().toString();
  }
}
