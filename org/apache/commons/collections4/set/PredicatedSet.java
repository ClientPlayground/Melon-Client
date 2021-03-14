package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Set;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.collection.PredicatedCollection;

public class PredicatedSet<E> extends PredicatedCollection<E> implements Set<E> {
  private static final long serialVersionUID = -684521469108685117L;
  
  public static <E> PredicatedSet<E> predicatedSet(Set<E> set, Predicate<? super E> predicate) {
    return new PredicatedSet<E>(set, predicate);
  }
  
  protected PredicatedSet(Set<E> set, Predicate<? super E> predicate) {
    super(set, predicate);
  }
  
  protected Set<E> decorated() {
    return (Set<E>)super.decorated();
  }
}
