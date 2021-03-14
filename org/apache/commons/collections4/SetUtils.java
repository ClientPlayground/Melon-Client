package org.apache.commons.collections4;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.collections4.set.PredicatedSet;
import org.apache.commons.collections4.set.PredicatedSortedSet;
import org.apache.commons.collections4.set.TransformedSet;
import org.apache.commons.collections4.set.TransformedSortedSet;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.collections4.set.UnmodifiableSortedSet;

public class SetUtils {
  public static <E> Set<E> emptySet() {
    return Collections.emptySet();
  }
  
  public static final SortedSet EMPTY_SORTED_SET = UnmodifiableSortedSet.unmodifiableSortedSet(new TreeSet());
  
  public static <E> SortedSet<E> emptySortedSet() {
    return EMPTY_SORTED_SET;
  }
  
  public static <T> Set<T> emptyIfNull(Set<T> set) {
    return (set == null) ? Collections.<T>emptySet() : set;
  }
  
  public static boolean isEqualSet(Collection<?> set1, Collection<?> set2) {
    if (set1 == set2)
      return true; 
    if (set1 == null || set2 == null || set1.size() != set2.size())
      return false; 
    return set1.containsAll(set2);
  }
  
  public static <T> int hashCodeForSet(Collection<T> set) {
    if (set == null)
      return 0; 
    int hashCode = 0;
    for (T obj : set) {
      if (obj != null)
        hashCode += obj.hashCode(); 
    } 
    return hashCode;
  }
  
  public static <E> Set<E> synchronizedSet(Set<E> set) {
    return Collections.synchronizedSet(set);
  }
  
  public static <E> Set<E> unmodifiableSet(Set<? extends E> set) {
    return UnmodifiableSet.unmodifiableSet(set);
  }
  
  public static <E> Set<E> predicatedSet(Set<E> set, Predicate<? super E> predicate) {
    return (Set<E>)PredicatedSet.predicatedSet(set, predicate);
  }
  
  public static <E> Set<E> transformedSet(Set<E> set, Transformer<? super E, ? extends E> transformer) {
    return (Set<E>)TransformedSet.transformingSet(set, transformer);
  }
  
  public static <E> Set<E> orderedSet(Set<E> set) {
    return (Set<E>)ListOrderedSet.listOrderedSet(set);
  }
  
  public static <E> SortedSet<E> synchronizedSortedSet(SortedSet<E> set) {
    return Collections.synchronizedSortedSet(set);
  }
  
  public static <E> SortedSet<E> unmodifiableSortedSet(SortedSet<E> set) {
    return UnmodifiableSortedSet.unmodifiableSortedSet(set);
  }
  
  public static <E> SortedSet<E> predicatedSortedSet(SortedSet<E> set, Predicate<? super E> predicate) {
    return (SortedSet<E>)PredicatedSortedSet.predicatedSortedSet(set, predicate);
  }
  
  public static <E> SortedSet<E> transformedSortedSet(SortedSet<E> set, Transformer<? super E, ? extends E> transformer) {
    return (SortedSet<E>)TransformedSortedSet.transformingSortedSet(set, transformer);
  }
}
