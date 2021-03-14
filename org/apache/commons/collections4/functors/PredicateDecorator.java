package org.apache.commons.collections4.functors;

import org.apache.commons.collections4.Predicate;

public interface PredicateDecorator<T> extends Predicate<T> {
  Predicate<? super T>[] getPredicates();
}
