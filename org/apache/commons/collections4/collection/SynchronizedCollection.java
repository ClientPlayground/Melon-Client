package org.apache.commons.collections4.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class SynchronizedCollection<E> implements Collection<E>, Serializable {
  private static final long serialVersionUID = 2412805092710877986L;
  
  private final Collection<E> collection;
  
  protected final Object lock;
  
  public static <T> SynchronizedCollection<T> synchronizedCollection(Collection<T> coll) {
    return new SynchronizedCollection<T>(coll);
  }
  
  protected SynchronizedCollection(Collection<E> collection) {
    if (collection == null)
      throw new IllegalArgumentException("Collection must not be null"); 
    this.collection = collection;
    this.lock = this;
  }
  
  protected SynchronizedCollection(Collection<E> collection, Object lock) {
    if (collection == null)
      throw new IllegalArgumentException("Collection must not be null"); 
    this.collection = collection;
    this.lock = lock;
  }
  
  protected Collection<E> decorated() {
    return this.collection;
  }
  
  public boolean add(E object) {
    synchronized (this.lock) {
      return decorated().add(object);
    } 
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    synchronized (this.lock) {
      return decorated().addAll(coll);
    } 
  }
  
  public void clear() {
    synchronized (this.lock) {
      decorated().clear();
    } 
  }
  
  public boolean contains(Object object) {
    synchronized (this.lock) {
      return decorated().contains(object);
    } 
  }
  
  public boolean containsAll(Collection<?> coll) {
    synchronized (this.lock) {
      return decorated().containsAll(coll);
    } 
  }
  
  public boolean isEmpty() {
    synchronized (this.lock) {
      return decorated().isEmpty();
    } 
  }
  
  public Iterator<E> iterator() {
    return decorated().iterator();
  }
  
  public Object[] toArray() {
    synchronized (this.lock) {
      return decorated().toArray();
    } 
  }
  
  public <T> T[] toArray(T[] object) {
    synchronized (this.lock) {
      return decorated().toArray(object);
    } 
  }
  
  public boolean remove(Object object) {
    synchronized (this.lock) {
      return decorated().remove(object);
    } 
  }
  
  public boolean removeAll(Collection<?> coll) {
    synchronized (this.lock) {
      return decorated().removeAll(coll);
    } 
  }
  
  public boolean retainAll(Collection<?> coll) {
    synchronized (this.lock) {
      return decorated().retainAll(coll);
    } 
  }
  
  public int size() {
    synchronized (this.lock) {
      return decorated().size();
    } 
  }
  
  public boolean equals(Object object) {
    synchronized (this.lock) {
      if (object == this)
        return true; 
      return (object == this || decorated().equals(object));
    } 
  }
  
  public int hashCode() {
    synchronized (this.lock) {
      return decorated().hashCode();
    } 
  }
  
  public String toString() {
    synchronized (this.lock) {
      return decorated().toString();
    } 
  }
}
