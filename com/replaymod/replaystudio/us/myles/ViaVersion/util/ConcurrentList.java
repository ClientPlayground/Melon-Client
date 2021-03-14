package com.replaymod.replaystudio.us.myles.ViaVersion.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ConcurrentList<E> extends ArrayList<E> {
  private final Object lock = new Object();
  
  public boolean add(E e) {
    synchronized (this.lock) {
      return super.add(e);
    } 
  }
  
  public void add(int index, E element) {
    synchronized (this.lock) {
      super.add(index, element);
    } 
  }
  
  public boolean addAll(Collection<? extends E> c) {
    synchronized (this.lock) {
      return super.addAll(c);
    } 
  }
  
  public boolean addAll(int index, Collection<? extends E> c) {
    synchronized (this.lock) {
      return super.addAll(index, c);
    } 
  }
  
  public void clear() {
    synchronized (this.lock) {
      super.clear();
    } 
  }
  
  public Object clone() {
    try {
      synchronized (this.lock) {
        ConcurrentList<E> clist = (ConcurrentList<E>)super.clone();
        clist.modCount = 0;
        Field f = ArrayList.class.getDeclaredField("elementData");
        f.setAccessible(true);
        f.set(clist, Arrays.copyOf((Object[])f.get(this), size()));
        return clist;
      } 
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public boolean contains(Object o) {
    synchronized (this.lock) {
      return super.contains(o);
    } 
  }
  
  public void ensureCapacity(int minCapacity) {
    synchronized (this.lock) {
      super.ensureCapacity(minCapacity);
    } 
  }
  
  public E get(int index) {
    synchronized (this.lock) {
      return super.get(index);
    } 
  }
  
  public int indexOf(Object o) {
    synchronized (this.lock) {
      return super.indexOf(o);
    } 
  }
  
  public int lastIndexOf(Object o) {
    synchronized (this.lock) {
      return super.lastIndexOf(o);
    } 
  }
  
  public E remove(int index) {
    synchronized (this.lock) {
      return super.remove(index);
    } 
  }
  
  public boolean remove(Object o) {
    synchronized (this.lock) {
      return super.remove(o);
    } 
  }
  
  public boolean removeAll(Collection<?> c) {
    synchronized (this.lock) {
      return super.removeAll(c);
    } 
  }
  
  public boolean retainAll(Collection<?> c) {
    synchronized (this.lock) {
      return super.retainAll(c);
    } 
  }
  
  public E set(int index, E element) {
    synchronized (this.lock) {
      return super.set(index, element);
    } 
  }
  
  public List<E> subList(int fromIndex, int toIndex) {
    synchronized (this.lock) {
      return super.subList(fromIndex, toIndex);
    } 
  }
  
  public Object[] toArray() {
    synchronized (this.lock) {
      return super.toArray();
    } 
  }
  
  public <T> T[] toArray(T[] a) {
    synchronized (this.lock) {
      return super.toArray(a);
    } 
  }
  
  public void trimToSize() {
    synchronized (this.lock) {
      super.trimToSize();
    } 
  }
  
  public ListIterator<E> listIterator() {
    return new ListItr(0);
  }
  
  public Iterator<E> iterator() {
    return new Itr();
  }
  
  private class Itr implements Iterator<E> {
    protected int cursor = 0;
    
    protected int lastRet = -1;
    
    final ConcurrentList l = (ConcurrentList)ConcurrentList.this.clone();
    
    public boolean hasNext() {
      return (this.cursor < this.l.size());
    }
    
    public E next() {
      int i = this.cursor;
      if (i >= this.l.size())
        throw new NoSuchElementException(); 
      this.cursor = i + 1;
      return this.l.get(this.lastRet = i);
    }
    
    public void remove() {
      if (this.lastRet < 0)
        throw new IllegalStateException(); 
      this.l.remove(this.lastRet);
      ConcurrentList.this.remove(this.lastRet);
      this.cursor = this.lastRet;
      this.lastRet = -1;
    }
  }
  
  public class ListItr extends Itr implements ListIterator<E> {
    ListItr(int index) {
      this.cursor = index;
    }
    
    public boolean hasPrevious() {
      return (this.cursor > 0);
    }
    
    public int nextIndex() {
      return this.cursor;
    }
    
    public int previousIndex() {
      return this.cursor - 1;
    }
    
    public E previous() {
      int i = this.cursor - 1;
      if (i < 0)
        throw new NoSuchElementException(); 
      this.cursor = i;
      return this.l.get(this.lastRet = i);
    }
    
    public void set(E e) {
      if (this.lastRet < 0)
        throw new IllegalStateException(); 
      this.l.set(this.lastRet, e);
      ConcurrentList.this.set(this.lastRet, e);
    }
    
    public void add(E e) {
      int i = this.cursor;
      this.l.add(i, e);
      ConcurrentList.this.add(i, e);
      this.cursor = i + 1;
      this.lastRet = -1;
    }
  }
}
