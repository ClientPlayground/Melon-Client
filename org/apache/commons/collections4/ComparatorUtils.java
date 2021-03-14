package org.apache.commons.collections4;

import java.util.Collection;
import java.util.Comparator;
import org.apache.commons.collections4.comparators.BooleanComparator;
import org.apache.commons.collections4.comparators.ComparableComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.collections4.comparators.NullComparator;
import org.apache.commons.collections4.comparators.ReverseComparator;
import org.apache.commons.collections4.comparators.TransformingComparator;

public class ComparatorUtils {
  public static final Comparator NATURAL_COMPARATOR = (Comparator)ComparableComparator.comparableComparator();
  
  public static <E extends Comparable<? super E>> Comparator<E> naturalComparator() {
    return NATURAL_COMPARATOR;
  }
  
  public static <E extends Comparable<? super E>> Comparator<E> chainedComparator(Comparator<E>... comparators) {
    ComparatorChain<E> chain = new ComparatorChain();
    for (Comparator<E> comparator : comparators) {
      if (comparator == null)
        throw new NullPointerException("Comparator cannot be null"); 
      chain.addComparator(comparator);
    } 
    return (Comparator<E>)chain;
  }
  
  public static <E extends Comparable<? super E>> Comparator<E> chainedComparator(Collection<Comparator<E>> comparators) {
    return chainedComparator(comparators.<Comparator<E>>toArray((Comparator<E>[])new Comparator[comparators.size()]));
  }
  
  public static <E> Comparator<E> reversedComparator(Comparator<E> comparator) {
    return (Comparator<E>)new ReverseComparator(comparator);
  }
  
  public static Comparator<Boolean> booleanComparator(boolean trueFirst) {
    return (Comparator<Boolean>)BooleanComparator.booleanComparator(trueFirst);
  }
  
  public static <E> Comparator<E> nullLowComparator(Comparator<E> comparator) {
    if (comparator == null)
      comparator = NATURAL_COMPARATOR; 
    return (Comparator<E>)new NullComparator(comparator, false);
  }
  
  public static <E> Comparator<E> nullHighComparator(Comparator<E> comparator) {
    if (comparator == null)
      comparator = NATURAL_COMPARATOR; 
    return (Comparator<E>)new NullComparator(comparator, true);
  }
  
  public static <I, O> Comparator<I> transformedComparator(Comparator<O> comparator, Transformer<? super I, ? extends O> transformer) {
    if (comparator == null)
      comparator = NATURAL_COMPARATOR; 
    return (Comparator<I>)new TransformingComparator(transformer, comparator);
  }
  
  public static <E> E min(E o1, E o2, Comparator<E> comparator) {
    if (comparator == null)
      comparator = NATURAL_COMPARATOR; 
    int c = comparator.compare(o1, o2);
    return (c < 0) ? o1 : o2;
  }
  
  public static <E> E max(E o1, E o2, Comparator<E> comparator) {
    if (comparator == null)
      comparator = NATURAL_COMPARATOR; 
    int c = comparator.compare(o1, o2);
    return (c > 0) ? o1 : o2;
  }
}
