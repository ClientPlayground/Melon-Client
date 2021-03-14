package org.apache.commons.collections4.functors;

import java.util.Collection;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;

class FunctorUtils {
  static <T> Predicate<T>[] copy(Predicate<? super T>... predicates) {
    if (predicates == null)
      return null; 
    return (Predicate<T>[])predicates.clone();
  }
  
  static <T> Predicate<T> coerce(Predicate<? super T> predicate) {
    return (Predicate)predicate;
  }
  
  static void validate(Predicate<?>... predicates) {
    if (predicates == null)
      throw new IllegalArgumentException("The predicate array must not be null"); 
    for (int i = 0; i < predicates.length; i++) {
      if (predicates[i] == null)
        throw new IllegalArgumentException("The predicate array must not contain a null predicate, index " + i + " was null"); 
    } 
  }
  
  static <T> Predicate<T>[] validate(Collection<? extends Predicate<T>> predicates) {
    if (predicates == null)
      throw new IllegalArgumentException("The predicate collection must not be null"); 
    Predicate[] arrayOfPredicate = new Predicate[predicates.size()];
    int i = 0;
    for (Predicate<T> predicate : predicates) {
      arrayOfPredicate[i] = predicate;
      if (arrayOfPredicate[i] == null)
        throw new IllegalArgumentException("The predicate collection must not contain a null predicate, index " + i + " was null"); 
      i++;
    } 
    return (Predicate<T>[])arrayOfPredicate;
  }
  
  static <E> Closure<E>[] copy(Closure<? super E>... closures) {
    if (closures == null)
      return null; 
    return (Closure<E>[])closures.clone();
  }
  
  static void validate(Closure<?>... closures) {
    if (closures == null)
      throw new IllegalArgumentException("The closure array must not be null"); 
    for (int i = 0; i < closures.length; i++) {
      if (closures[i] == null)
        throw new IllegalArgumentException("The closure array must not contain a null closure, index " + i + " was null"); 
    } 
  }
  
  static <T> Closure<T> coerce(Closure<? super T> closure) {
    return (Closure)closure;
  }
  
  static <I, O> Transformer<I, O>[] copy(Transformer<? super I, ? extends O>... transformers) {
    if (transformers == null)
      return null; 
    return (Transformer<I, O>[])transformers.clone();
  }
  
  static void validate(Transformer<?, ?>... transformers) {
    if (transformers == null)
      throw new IllegalArgumentException("The transformer array must not be null"); 
    for (int i = 0; i < transformers.length; i++) {
      if (transformers[i] == null)
        throw new IllegalArgumentException("The transformer array must not contain a null transformer, index " + i + " was null"); 
    } 
  }
  
  static <I, O> Transformer<I, O> coerce(Transformer<? super I, ? extends O> transformer) {
    return (Transformer)transformer;
  }
}
