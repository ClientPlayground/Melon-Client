package org.apache.commons.collections4.bag;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections4.SortedBag;

public class TreeBag<E> extends AbstractMapBag<E> implements SortedBag<E>, Serializable {
  private static final long serialVersionUID = -7740146511091606676L;
  
  public TreeBag() {
    super(new TreeMap<E, AbstractMapBag.MutableInteger>());
  }
  
  public TreeBag(Comparator<? super E> comparator) {
    super(new TreeMap<E, AbstractMapBag.MutableInteger>(comparator));
  }
  
  public TreeBag(Collection<? extends E> coll) {
    this();
    addAll(coll);
  }
  
  public boolean add(E object) {
    if (comparator() == null && !(object instanceof Comparable))
      throw new IllegalArgumentException("Objects of type " + object.getClass() + " cannot be added to " + "a naturally ordered TreeBag as it does not implement Comparable"); 
    return super.add(object);
  }
  
  public E first() {
    return getMap().firstKey();
  }
  
  public E last() {
    return getMap().lastKey();
  }
  
  public Comparator<? super E> comparator() {
    return getMap().comparator();
  }
  
  protected SortedMap<E, AbstractMapBag.MutableInteger> getMap() {
    return (SortedMap<E, AbstractMapBag.MutableInteger>)super.getMap();
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(comparator());
    doWriteObject(out);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Comparator<? super E> comp = (Comparator<? super E>)in.readObject();
    doReadObject(new TreeMap<E, AbstractMapBag.MutableInteger>(comp), in);
  }
}
