package com.replaymod.replaystudio.us.myles.ViaVersion.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class ListWrapper implements List {
  private final List list;
  
  public ListWrapper(List inputList) {
    this.list = inputList;
  }
  
  public abstract void handleAdd(Object paramObject);
  
  public List getOriginalList() {
    return this.list;
  }
  
  public int size() {
    synchronized (this) {
      return this.list.size();
    } 
  }
  
  public boolean isEmpty() {
    synchronized (this) {
      return this.list.isEmpty();
    } 
  }
  
  public boolean contains(Object o) {
    synchronized (this) {
      return this.list.contains(o);
    } 
  }
  
  public Iterator iterator() {
    synchronized (this) {
      return listIterator();
    } 
  }
  
  public Object[] toArray() {
    synchronized (this) {
      return this.list.toArray();
    } 
  }
  
  public boolean add(Object o) {
    handleAdd(o);
    synchronized (this) {
      return this.list.add(o);
    } 
  }
  
  public boolean remove(Object o) {
    synchronized (this) {
      return this.list.remove(o);
    } 
  }
  
  public boolean addAll(Collection c) {
    for (Object o : c)
      handleAdd(o); 
    synchronized (this) {
      return this.list.addAll(c);
    } 
  }
  
  public boolean addAll(int index, Collection c) {
    for (Object o : c)
      handleAdd(o); 
    synchronized (this) {
      return this.list.addAll(index, c);
    } 
  }
  
  public void clear() {
    synchronized (this) {
      this.list.clear();
    } 
  }
  
  public Object get(int index) {
    synchronized (this) {
      return this.list.get(index);
    } 
  }
  
  public Object set(int index, Object element) {
    synchronized (this) {
      return this.list.set(index, element);
    } 
  }
  
  public void add(int index, Object element) {
    synchronized (this) {
      this.list.add(index, element);
    } 
  }
  
  public Object remove(int index) {
    synchronized (this) {
      return this.list.remove(index);
    } 
  }
  
  public int indexOf(Object o) {
    synchronized (this) {
      return this.list.indexOf(o);
    } 
  }
  
  public int lastIndexOf(Object o) {
    synchronized (this) {
      return this.list.lastIndexOf(o);
    } 
  }
  
  public ListIterator listIterator() {
    synchronized (this) {
      return this.list.listIterator();
    } 
  }
  
  public ListIterator listIterator(int index) {
    synchronized (this) {
      return this.list.listIterator(index);
    } 
  }
  
  public List subList(int fromIndex, int toIndex) {
    synchronized (this) {
      return this.list.subList(fromIndex, toIndex);
    } 
  }
  
  public boolean retainAll(Collection<?> c) {
    synchronized (this) {
      return this.list.retainAll(c);
    } 
  }
  
  public boolean removeAll(Collection<?> c) {
    synchronized (this) {
      return this.list.removeAll(c);
    } 
  }
  
  public boolean containsAll(Collection<?> c) {
    synchronized (this) {
      return this.list.containsAll(c);
    } 
  }
  
  public Object[] toArray(Object[] a) {
    synchronized (this) {
      return this.list.toArray(a);
    } 
  }
}
