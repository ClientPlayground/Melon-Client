package org.apache.commons.collections4.iterators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.collections4.list.UnmodifiableList;

public class CollatingIterator<E> implements Iterator<E> {
  private Comparator<? super E> comparator = null;
  
  private ArrayList<Iterator<? extends E>> iterators = null;
  
  private ArrayList<E> values = null;
  
  private BitSet valueSet = null;
  
  private int lastReturned = -1;
  
  public CollatingIterator() {
    this((Comparator<? super E>)null, 2);
  }
  
  public CollatingIterator(Comparator<? super E> comp) {
    this(comp, 2);
  }
  
  public CollatingIterator(Comparator<? super E> comp, int initIterCapacity) {
    this.iterators = new ArrayList<Iterator<? extends E>>(initIterCapacity);
    setComparator(comp);
  }
  
  public CollatingIterator(Comparator<? super E> comp, Iterator<? extends E> a, Iterator<? extends E> b) {
    this(comp, 2);
    addIterator(a);
    addIterator(b);
  }
  
  public CollatingIterator(Comparator<? super E> comp, Iterator<? extends E>[] iterators) {
    this(comp, iterators.length);
    for (Iterator<? extends E> iterator : iterators)
      addIterator(iterator); 
  }
  
  public CollatingIterator(Comparator<? super E> comp, Collection<Iterator<? extends E>> iterators) {
    this(comp, iterators.size());
    for (Iterator<? extends E> iterator : iterators)
      addIterator(iterator); 
  }
  
  public void addIterator(Iterator<? extends E> iterator) {
    checkNotStarted();
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    this.iterators.add(iterator);
  }
  
  public void setIterator(int index, Iterator<? extends E> iterator) {
    checkNotStarted();
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    this.iterators.set(index, iterator);
  }
  
  public List<Iterator<? extends E>> getIterators() {
    return UnmodifiableList.unmodifiableList(this.iterators);
  }
  
  public Comparator<? super E> getComparator() {
    return this.comparator;
  }
  
  public void setComparator(Comparator<? super E> comp) {
    checkNotStarted();
    this.comparator = comp;
  }
  
  public boolean hasNext() {
    start();
    return (anyValueSet(this.valueSet) || anyHasNext(this.iterators));
  }
  
  public E next() throws NoSuchElementException {
    if (!hasNext())
      throw new NoSuchElementException(); 
    int leastIndex = least();
    if (leastIndex == -1)
      throw new NoSuchElementException(); 
    E val = this.values.get(leastIndex);
    clear(leastIndex);
    this.lastReturned = leastIndex;
    return val;
  }
  
  public void remove() {
    if (this.lastReturned == -1)
      throw new IllegalStateException("No value can be removed at present"); 
    ((Iterator)this.iterators.get(this.lastReturned)).remove();
  }
  
  public int getIteratorIndex() {
    if (this.lastReturned == -1)
      throw new IllegalStateException("No value has been returned yet"); 
    return this.lastReturned;
  }
  
  private void start() {
    if (this.values == null) {
      this.values = new ArrayList<E>(this.iterators.size());
      this.valueSet = new BitSet(this.iterators.size());
      for (int i = 0; i < this.iterators.size(); i++) {
        this.values.add(null);
        this.valueSet.clear(i);
      } 
    } 
  }
  
  private boolean set(int i) {
    Iterator<? extends E> it = this.iterators.get(i);
    if (it.hasNext()) {
      this.values.set(i, it.next());
      this.valueSet.set(i);
      return true;
    } 
    this.values.set(i, null);
    this.valueSet.clear(i);
    return false;
  }
  
  private void clear(int i) {
    this.values.set(i, null);
    this.valueSet.clear(i);
  }
  
  private void checkNotStarted() throws IllegalStateException {
    if (this.values != null)
      throw new IllegalStateException("Can't do that after next or hasNext has been called."); 
  }
  
  private int least() {
    int leastIndex = -1;
    E leastObject = null;
    for (int i = 0; i < this.values.size(); i++) {
      if (!this.valueSet.get(i))
        set(i); 
      if (this.valueSet.get(i))
        if (leastIndex == -1) {
          leastIndex = i;
          leastObject = this.values.get(i);
        } else {
          E curObject = this.values.get(i);
          if (this.comparator == null)
            throw new NullPointerException("You must invoke setComparator() to set a comparator first."); 
          if (this.comparator.compare(curObject, leastObject) < 0) {
            leastObject = curObject;
            leastIndex = i;
          } 
        }  
    } 
    return leastIndex;
  }
  
  private boolean anyValueSet(BitSet set) {
    for (int i = 0; i < set.size(); i++) {
      if (set.get(i))
        return true; 
    } 
    return false;
  }
  
  private boolean anyHasNext(ArrayList<Iterator<? extends E>> iters) {
    for (Iterator<? extends E> iterator : iters) {
      if (iterator.hasNext())
        return true; 
    } 
    return false;
  }
}
