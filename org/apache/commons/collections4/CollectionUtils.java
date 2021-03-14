package org.apache.commons.collections4;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.collection.PredicatedCollection;
import org.apache.commons.collections4.collection.SynchronizedCollection;
import org.apache.commons.collections4.collection.TransformedCollection;
import org.apache.commons.collections4.collection.UnmodifiableBoundedCollection;
import org.apache.commons.collections4.collection.UnmodifiableCollection;
import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.collections4.iterators.CollatingIterator;
import org.apache.commons.collections4.iterators.PermutationIterator;

public class CollectionUtils {
  private static class CardinalityHelper<O> {
    final Map<O, Integer> cardinalityA;
    
    final Map<O, Integer> cardinalityB;
    
    public CardinalityHelper(Iterable<? extends O> a, Iterable<? extends O> b) {
      this.cardinalityA = CollectionUtils.getCardinalityMap(a);
      this.cardinalityB = CollectionUtils.getCardinalityMap(b);
    }
    
    public final int max(Object obj) {
      return Math.max(freqA(obj), freqB(obj));
    }
    
    public final int min(Object obj) {
      return Math.min(freqA(obj), freqB(obj));
    }
    
    public int freqA(Object obj) {
      return getFreq(obj, this.cardinalityA);
    }
    
    public int freqB(Object obj) {
      return getFreq(obj, this.cardinalityB);
    }
    
    private final int getFreq(Object obj, Map<?, Integer> freqMap) {
      Integer count = freqMap.get(obj);
      if (count != null)
        return count.intValue(); 
      return 0;
    }
  }
  
  private static class SetOperationCardinalityHelper<O> extends CardinalityHelper<O> implements Iterable<O> {
    private final Set<O> elements;
    
    private final List<O> newList;
    
    public SetOperationCardinalityHelper(Iterable<? extends O> a, Iterable<? extends O> b) {
      super(a, b);
      this.elements = new HashSet<O>();
      CollectionUtils.addAll(this.elements, a);
      CollectionUtils.addAll(this.elements, b);
      this.newList = new ArrayList<O>(this.elements.size());
    }
    
    public Iterator<O> iterator() {
      return this.elements.iterator();
    }
    
    public void setCardinality(O obj, int count) {
      for (int i = 0; i < count; i++)
        this.newList.add(obj); 
    }
    
    public Collection<O> list() {
      return this.newList;
    }
  }
  
  public static final Collection EMPTY_COLLECTION = UnmodifiableCollection.unmodifiableCollection(new ArrayList());
  
  public static <T> Collection<T> emptyCollection() {
    return EMPTY_COLLECTION;
  }
  
  public static <T> Collection<T> emptyIfNull(Collection<T> collection) {
    return (collection == null) ? EMPTY_COLLECTION : collection;
  }
  
  public static <O> Collection<O> union(Iterable<? extends O> a, Iterable<? extends O> b) {
    SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<O>(a, b);
    for (O obj : helper)
      helper.setCardinality(obj, helper.max(obj)); 
    return helper.list();
  }
  
  public static <O> Collection<O> intersection(Iterable<? extends O> a, Iterable<? extends O> b) {
    SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<O>(a, b);
    for (O obj : helper)
      helper.setCardinality(obj, helper.min(obj)); 
    return helper.list();
  }
  
  public static <O> Collection<O> disjunction(Iterable<? extends O> a, Iterable<? extends O> b) {
    SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<O>(a, b);
    for (O obj : helper)
      helper.setCardinality(obj, helper.max(obj) - helper.min(obj)); 
    return helper.list();
  }
  
  public static <O> Collection<O> subtract(Iterable<? extends O> a, Iterable<? extends O> b) {
    Predicate<O> p = TruePredicate.truePredicate();
    return subtract(a, b, p);
  }
  
  public static <O> Collection<O> subtract(Iterable<? extends O> a, Iterable<? extends O> b, Predicate<O> p) {
    ArrayList<O> list = new ArrayList<O>();
    HashBag<O> bag = new HashBag();
    for (O element : b) {
      if (p.evaluate(element))
        bag.add(element); 
    } 
    for (O element : a) {
      if (!bag.remove(element, 1))
        list.add(element); 
    } 
    return list;
  }
  
  public static boolean containsAll(Collection<?> coll1, Collection<?> coll2) {
    if (coll2.isEmpty())
      return true; 
    Iterator<?> it = coll1.iterator();
    Set<Object> elementsAlreadySeen = new HashSet();
    for (Object nextElement : coll2) {
      if (elementsAlreadySeen.contains(nextElement))
        continue; 
      boolean foundCurrentElement = false;
      while (it.hasNext()) {
        Object p = it.next();
        elementsAlreadySeen.add(p);
        if ((nextElement == null) ? (p == null) : nextElement.equals(p)) {
          foundCurrentElement = true;
          break;
        } 
      } 
      if (foundCurrentElement)
        continue; 
      return false;
    } 
    return true;
  }
  
  public static boolean containsAny(Collection<?> coll1, Collection<?> coll2) {
    if (coll1.size() < coll2.size()) {
      for (Object aColl1 : coll1) {
        if (coll2.contains(aColl1))
          return true; 
      } 
    } else {
      for (Object aColl2 : coll2) {
        if (coll1.contains(aColl2))
          return true; 
      } 
    } 
    return false;
  }
  
  public static <O> Map<O, Integer> getCardinalityMap(Iterable<? extends O> coll) {
    Map<O, Integer> count = new HashMap<O, Integer>();
    for (O obj : coll) {
      Integer c = count.get(obj);
      if (c == null) {
        count.put(obj, Integer.valueOf(1));
        continue;
      } 
      count.put(obj, Integer.valueOf(c.intValue() + 1));
    } 
    return count;
  }
  
  public static boolean isSubCollection(Collection<?> a, Collection<?> b) {
    CardinalityHelper<Object> helper = new CardinalityHelper(a, b);
    for (Object obj : a) {
      if (helper.freqA(obj) > helper.freqB(obj))
        return false; 
    } 
    return true;
  }
  
  public static boolean isProperSubCollection(Collection<?> a, Collection<?> b) {
    return (a.size() < b.size() && isSubCollection(a, b));
  }
  
  public static boolean isEqualCollection(Collection<?> a, Collection<?> b) {
    if (a.size() != b.size())
      return false; 
    CardinalityHelper<Object> helper = new CardinalityHelper(a, b);
    if (helper.cardinalityA.size() != helper.cardinalityB.size())
      return false; 
    for (Object obj : helper.cardinalityA.keySet()) {
      if (helper.freqA(obj) != helper.freqB(obj))
        return false; 
    } 
    return true;
  }
  
  public static boolean isEqualCollection(Collection<?> a, Collection<?> b, final Equator<?> equator) {
    if (equator == null)
      throw new IllegalArgumentException("equator may not be null"); 
    if (a.size() != b.size())
      return false; 
    Transformer<Object, Object> transformer = new Transformer<Object, Object>() {
        public CollectionUtils.EquatorWrapper<?> transform(Object input) {
          return new CollectionUtils.EquatorWrapper(equator, input);
        }
      };
    return isEqualCollection(collect(a, transformer), collect(b, transformer));
  }
  
  private static class EquatorWrapper<O> {
    private final Equator<O> equator;
    
    private final O object;
    
    public EquatorWrapper(Equator<O> equator, O object) {
      this.equator = equator;
      this.object = object;
    }
    
    public O getObject() {
      return this.object;
    }
    
    public boolean equals(Object obj) {
      if (!(obj instanceof EquatorWrapper))
        return false; 
      EquatorWrapper<O> otherObj = (EquatorWrapper<O>)obj;
      return this.equator.equate(this.object, otherObj.getObject());
    }
    
    public int hashCode() {
      return this.equator.hash(this.object);
    }
  }
  
  public static <O> int cardinality(O obj, Iterable<? super O> coll) {
    if (coll instanceof Set)
      return ((Set)coll).contains(obj) ? 1 : 0; 
    if (coll instanceof Bag)
      return ((Bag)coll).getCount(obj); 
    int count = 0;
    if (obj == null) {
      for (O element : coll) {
        if (element == null)
          count++; 
      } 
    } else {
      for (O element : coll) {
        if (obj.equals(element))
          count++; 
      } 
    } 
    return count;
  }
  
  public static <T> T find(Iterable<T> collection, Predicate<? super T> predicate) {
    if (collection != null && predicate != null)
      for (T item : collection) {
        if (predicate.evaluate(item))
          return item; 
      }  
    return null;
  }
  
  public static <T, C extends Closure<? super T>> C forAllDo(Iterable<T> collection, C closure) {
    if (collection != null && closure != null)
      for (T element : collection)
        closure.execute(element);  
    return closure;
  }
  
  public static <T, C extends Closure<? super T>> C forAllDo(Iterator<T> iterator, C closure) {
    if (iterator != null && closure != null)
      while (iterator.hasNext())
        closure.execute(iterator.next());  
    return closure;
  }
  
  public static <T, C extends Closure<? super T>> T forAllButLastDo(Iterable<T> collection, C closure) {
    return (collection != null && closure != null) ? forAllButLastDo(collection.iterator(), closure) : null;
  }
  
  public static <T, C extends Closure<? super T>> T forAllButLastDo(Iterator<T> iterator, C closure) {
    if (iterator != null && closure != null)
      while (iterator.hasNext()) {
        T element = iterator.next();
        if (iterator.hasNext()) {
          closure.execute(element);
          continue;
        } 
        return element;
      }  
    return null;
  }
  
  public static <T> boolean filter(Iterable<T> collection, Predicate<? super T> predicate) {
    boolean result = false;
    if (collection != null && predicate != null)
      for (Iterator<T> it = collection.iterator(); it.hasNext();) {
        if (!predicate.evaluate(it.next())) {
          it.remove();
          result = true;
        } 
      }  
    return result;
  }
  
  public static <T> boolean filterInverse(Iterable<T> collection, Predicate<? super T> predicate) {
    return filter(collection, (predicate == null) ? null : PredicateUtils.<T>notPredicate(predicate));
  }
  
  public static <C> void transform(Collection<C> collection, Transformer<? super C, ? extends C> transformer) {
    if (collection != null && transformer != null)
      if (collection instanceof List) {
        List<C> list = (List<C>)collection;
        for (ListIterator<C> it = list.listIterator(); it.hasNext();)
          it.set(transformer.transform(it.next())); 
      } else {
        Collection<C> resultCollection = collect(collection, transformer);
        collection.clear();
        collection.addAll(resultCollection);
      }  
  }
  
  public static <C> int countMatches(Iterable<C> input, Predicate<? super C> predicate) {
    int count = 0;
    if (input != null && predicate != null)
      for (C o : input) {
        if (predicate.evaluate(o))
          count++; 
      }  
    return count;
  }
  
  public static <C> boolean exists(Iterable<C> input, Predicate<? super C> predicate) {
    if (input != null && predicate != null)
      for (C o : input) {
        if (predicate.evaluate(o))
          return true; 
      }  
    return false;
  }
  
  public static <C> boolean matchesAll(Iterable<C> input, Predicate<? super C> predicate) {
    if (predicate == null)
      return false; 
    if (input != null)
      for (C o : input) {
        if (!predicate.evaluate(o))
          return false; 
      }  
    return true;
  }
  
  public static <O> Collection<O> select(Iterable<? extends O> inputCollection, Predicate<? super O> predicate) {
    Collection<O> answer = (inputCollection instanceof Collection) ? new ArrayList<O>(((Collection)inputCollection).size()) : new ArrayList<O>();
    return select(inputCollection, predicate, answer);
  }
  
  public static <O, R extends Collection<? super O>> R select(Iterable<? extends O> inputCollection, Predicate<? super O> predicate, R outputCollection) {
    if (inputCollection != null && predicate != null)
      for (O item : inputCollection) {
        if (predicate.evaluate(item))
          outputCollection.add(item); 
      }  
    return outputCollection;
  }
  
  public static <O> Collection<O> selectRejected(Iterable<? extends O> inputCollection, Predicate<? super O> predicate) {
    Collection<O> answer = (inputCollection instanceof Collection) ? new ArrayList<O>(((Collection)inputCollection).size()) : new ArrayList<O>();
    return selectRejected(inputCollection, predicate, answer);
  }
  
  public static <O, R extends Collection<? super O>> R selectRejected(Iterable<? extends O> inputCollection, Predicate<? super O> predicate, R outputCollection) {
    if (inputCollection != null && predicate != null)
      for (O item : inputCollection) {
        if (!predicate.evaluate(item))
          outputCollection.add(item); 
      }  
    return outputCollection;
  }
  
  public static <I, O> Collection<O> collect(Iterable<I> inputCollection, Transformer<? super I, ? extends O> transformer) {
    Collection<O> answer = (inputCollection instanceof Collection) ? new ArrayList<O>(((Collection)inputCollection).size()) : new ArrayList<O>();
    return collect(inputCollection, transformer, answer);
  }
  
  public static <I, O> Collection<O> collect(Iterator<I> inputIterator, Transformer<? super I, ? extends O> transformer) {
    return collect(inputIterator, transformer, new ArrayList<O>());
  }
  
  public static <I, O, R extends Collection<? super O>> R collect(Iterable<? extends I> inputCollection, Transformer<? super I, ? extends O> transformer, R outputCollection) {
    if (inputCollection != null)
      return collect(inputCollection.iterator(), transformer, outputCollection); 
    return outputCollection;
  }
  
  public static <I, O, R extends Collection<? super O>> R collect(Iterator<? extends I> inputIterator, Transformer<? super I, ? extends O> transformer, R outputCollection) {
    if (inputIterator != null && transformer != null)
      while (inputIterator.hasNext()) {
        I item = inputIterator.next();
        O value = transformer.transform(item);
        outputCollection.add(value);
      }  
    return outputCollection;
  }
  
  public static <T> boolean addIgnoreNull(Collection<T> collection, T object) {
    if (collection == null)
      throw new NullPointerException("The collection must not be null"); 
    return (object != null && collection.add(object));
  }
  
  public static <C> boolean addAll(Collection<C> collection, Iterable<? extends C> iterable) {
    if (iterable instanceof Collection)
      return collection.addAll((Collection<? extends C>)iterable); 
    return addAll(collection, iterable.iterator());
  }
  
  public static <C> boolean addAll(Collection<C> collection, Iterator<? extends C> iterator) {
    boolean changed = false;
    while (iterator.hasNext())
      changed |= collection.add(iterator.next()); 
    return changed;
  }
  
  public static <C> boolean addAll(Collection<C> collection, Enumeration<? extends C> enumeration) {
    boolean changed = false;
    while (enumeration.hasMoreElements())
      changed |= collection.add(enumeration.nextElement()); 
    return changed;
  }
  
  public static <C> boolean addAll(Collection<C> collection, C[] elements) {
    boolean changed = false;
    for (C element : elements)
      changed |= collection.add(element); 
    return changed;
  }
  
  public static <T> T get(Iterator<T> iterator, int index) {
    int i = index;
    checkIndexBounds(i);
    while (iterator.hasNext()) {
      i--;
      if (i == -1)
        return iterator.next(); 
      iterator.next();
    } 
    throw new IndexOutOfBoundsException("Entry does not exist: " + i);
  }
  
  private static void checkIndexBounds(int index) {
    if (index < 0)
      throw new IndexOutOfBoundsException("Index cannot be negative: " + index); 
  }
  
  public static <T> T get(Iterable<T> iterable, int index) {
    checkIndexBounds(index);
    if (iterable instanceof List)
      return ((List<T>)iterable).get(index); 
    return get(iterable.iterator(), index);
  }
  
  public static Object get(Object object, int index) {
    int i = index;
    if (i < 0)
      throw new IndexOutOfBoundsException("Index cannot be negative: " + i); 
    if (object instanceof Map) {
      Map<?, ?> map = (Map<?, ?>)object;
      Iterator<?> iterator = map.entrySet().iterator();
      return get(iterator, i);
    } 
    if (object instanceof Object[])
      return ((Object[])object)[i]; 
    if (object instanceof Iterator) {
      Iterator<?> it = (Iterator)object;
      while (it.hasNext()) {
        i--;
        if (i == -1)
          return it.next(); 
        it.next();
      } 
      throw new IndexOutOfBoundsException("Entry does not exist: " + i);
    } 
    if (object instanceof Collection) {
      Iterator<?> iterator = ((Collection)object).iterator();
      return get(iterator, i);
    } 
    if (object instanceof Enumeration) {
      Enumeration<?> it = (Enumeration)object;
      while (it.hasMoreElements()) {
        i--;
        if (i == -1)
          return it.nextElement(); 
        it.nextElement();
      } 
      throw new IndexOutOfBoundsException("Entry does not exist: " + i);
    } 
    if (object == null)
      throw new IllegalArgumentException("Unsupported object type: null"); 
    try {
      return Array.get(object, i);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
    } 
  }
  
  public static <K, V> Map.Entry<K, V> get(Map<K, V> map, int index) {
    checkIndexBounds(index);
    return get(map.entrySet(), index);
  }
  
  public static int size(Object object) {
    if (object == null)
      return 0; 
    int total = 0;
    if (object instanceof Map) {
      total = ((Map)object).size();
    } else if (object instanceof Collection) {
      total = ((Collection)object).size();
    } else if (object instanceof Object[]) {
      total = ((Object[])object).length;
    } else if (object instanceof Iterator) {
      Iterator<?> it = (Iterator)object;
      while (it.hasNext()) {
        total++;
        it.next();
      } 
    } else if (object instanceof Enumeration) {
      Enumeration<?> it = (Enumeration)object;
      while (it.hasMoreElements()) {
        total++;
        it.nextElement();
      } 
    } else {
      try {
        total = Array.getLength(object);
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
      } 
    } 
    return total;
  }
  
  public static boolean sizeIsEmpty(Object object) {
    if (object == null)
      return true; 
    if (object instanceof Collection)
      return ((Collection)object).isEmpty(); 
    if (object instanceof Map)
      return ((Map)object).isEmpty(); 
    if (object instanceof Object[])
      return (((Object[])object).length == 0); 
    if (object instanceof Iterator)
      return !((Iterator)object).hasNext(); 
    if (object instanceof Enumeration)
      return !((Enumeration)object).hasMoreElements(); 
    try {
      return (Array.getLength(object) == 0);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
    } 
  }
  
  public static boolean isEmpty(Collection<?> coll) {
    return (coll == null || coll.isEmpty());
  }
  
  public static boolean isNotEmpty(Collection<?> coll) {
    return !isEmpty(coll);
  }
  
  public static void reverseArray(Object[] array) {
    int i = 0;
    int j = array.length - 1;
    while (j > i) {
      Object tmp = array[j];
      array[j] = array[i];
      array[i] = tmp;
      j--;
      i++;
    } 
  }
  
  public static boolean isFull(Collection<? extends Object> coll) {
    if (coll == null)
      throw new NullPointerException("The collection must not be null"); 
    if (coll instanceof BoundedCollection)
      return ((BoundedCollection)coll).isFull(); 
    try {
      BoundedCollection<?> bcoll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(coll);
      return bcoll.isFull();
    } catch (IllegalArgumentException ex) {
      return false;
    } 
  }
  
  public static int maxSize(Collection<? extends Object> coll) {
    if (coll == null)
      throw new NullPointerException("The collection must not be null"); 
    if (coll instanceof BoundedCollection)
      return ((BoundedCollection)coll).maxSize(); 
    try {
      BoundedCollection<?> bcoll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(coll);
      return bcoll.maxSize();
    } catch (IllegalArgumentException ex) {
      return -1;
    } 
  }
  
  public static <O extends Comparable<? super O>> List<O> collate(Iterable<? extends O> a, Iterable<? extends O> b) {
    return collate(a, b, ComparatorUtils.naturalComparator(), true);
  }
  
  public static <O extends Comparable<? super O>> List<O> collate(Iterable<? extends O> a, Iterable<? extends O> b, boolean includeDuplicates) {
    return collate(a, b, ComparatorUtils.naturalComparator(), includeDuplicates);
  }
  
  public static <O> List<O> collate(Iterable<? extends O> a, Iterable<? extends O> b, Comparator<? super O> c) {
    return collate(a, b, c, true);
  }
  
  public static <O> List<O> collate(Iterable<? extends O> a, Iterable<? extends O> b, Comparator<? super O> c, boolean includeDuplicates) {
    if (a == null || b == null)
      throw new IllegalArgumentException("The collections must not be null"); 
    if (c == null)
      throw new IllegalArgumentException("The comparator must not be null"); 
    int totalSize = (a instanceof Collection && b instanceof Collection) ? Math.max(1, ((Collection)a).size() + ((Collection)b).size()) : 10;
    CollatingIterator<O> collatingIterator = new CollatingIterator(c, a.iterator(), b.iterator());
    if (includeDuplicates)
      return IteratorUtils.toList((Iterator<? extends O>)collatingIterator, totalSize); 
    ArrayList<O> mergedList = new ArrayList<O>(totalSize);
    O lastItem = null;
    while (collatingIterator.hasNext()) {
      O item = collatingIterator.next();
      if (lastItem == null || !lastItem.equals(item))
        mergedList.add(item); 
      lastItem = item;
    } 
    mergedList.trimToSize();
    return mergedList;
  }
  
  public static <E> Collection<List<E>> permutations(Collection<E> collection) {
    PermutationIterator<E> it = new PermutationIterator(collection);
    Collection<List<E>> result = new LinkedList<List<E>>();
    while (it.hasNext())
      result.add(it.next()); 
    return result;
  }
  
  public static <C> Collection<C> retainAll(Collection<C> collection, Collection<?> retain) {
    return ListUtils.retainAll(collection, retain);
  }
  
  public static <E> Collection<E> removeAll(Collection<E> collection, Collection<?> remove) {
    return ListUtils.removeAll(collection, remove);
  }
  
  public static <C> Collection<C> synchronizedCollection(Collection<C> collection) {
    return (Collection<C>)SynchronizedCollection.synchronizedCollection(collection);
  }
  
  public static <C> Collection<C> unmodifiableCollection(Collection<? extends C> collection) {
    return UnmodifiableCollection.unmodifiableCollection(collection);
  }
  
  public static <C> Collection<C> predicatedCollection(Collection<C> collection, Predicate<? super C> predicate) {
    return (Collection<C>)PredicatedCollection.predicatedCollection(collection, predicate);
  }
  
  public static <E> Collection<E> transformingCollection(Collection<E> collection, Transformer<? super E, ? extends E> transformer) {
    return (Collection<E>)TransformedCollection.transformingCollection(collection, transformer);
  }
  
  public static <E> E extractSingleton(Collection<E> collection) {
    if (collection == null || collection.size() != 1)
      throw new IllegalArgumentException("Can extract singleton only when collection size == 1"); 
    return collection.iterator().next();
  }
}
