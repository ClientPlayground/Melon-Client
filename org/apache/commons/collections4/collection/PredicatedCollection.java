package org.apache.commons.collections4.collection;

import java.util.Collection;
import org.apache.commons.collections4.Predicate;

public class PredicatedCollection<E> extends AbstractCollectionDecorator<E> {
  private static final long serialVersionUID = -5259182142076705162L;
  
  protected final Predicate<? super E> predicate;
  
  public static <T> PredicatedCollection<T> predicatedCollection(Collection<T> coll, Predicate<? super T> predicate) {
    return new PredicatedCollection<T>(coll, predicate);
  }
  
  protected PredicatedCollection(Collection<E> coll, Predicate<? super E> predicate) {
    super(coll);
    if (predicate == null)
      throw new IllegalArgumentException("Predicate must not be null"); 
    this.predicate = predicate;
    for (E item : coll)
      validate(item); 
  }
  
  protected void validate(E object) {
    if (!this.predicate.evaluate(object))
      throw new IllegalArgumentException("Cannot add Object '" + object + "' - Predicate '" + this.predicate + "' rejected it"); 
  }
  
  public boolean add(E object) {
    validate(object);
    return decorated().add(object);
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    for (E item : coll)
      validate(item); 
    return decorated().addAll(coll);
  }
}
