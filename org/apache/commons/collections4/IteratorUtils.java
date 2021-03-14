package org.apache.commons.collections4;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.collections4.iterators.ArrayListIterator;
import org.apache.commons.collections4.iterators.CollatingIterator;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.EmptyListIterator;
import org.apache.commons.collections4.iterators.EmptyMapIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedMapIterator;
import org.apache.commons.collections4.iterators.EnumerationIterator;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.FilterListIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;
import org.apache.commons.collections4.iterators.LoopingIterator;
import org.apache.commons.collections4.iterators.LoopingListIterator;
import org.apache.commons.collections4.iterators.NodeListIterator;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;
import org.apache.commons.collections4.iterators.ObjectArrayListIterator;
import org.apache.commons.collections4.iterators.ObjectGraphIterator;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.collections4.iterators.PushbackIterator;
import org.apache.commons.collections4.iterators.SingletonIterator;
import org.apache.commons.collections4.iterators.SingletonListIterator;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.apache.commons.collections4.iterators.UnmodifiableListIterator;
import org.apache.commons.collections4.iterators.UnmodifiableMapIterator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IteratorUtils {
  public static final ResettableIterator EMPTY_ITERATOR = EmptyIterator.RESETTABLE_INSTANCE;
  
  public static final ResettableListIterator EMPTY_LIST_ITERATOR = EmptyListIterator.RESETTABLE_INSTANCE;
  
  public static final OrderedIterator EMPTY_ORDERED_ITERATOR = EmptyOrderedIterator.INSTANCE;
  
  public static final MapIterator EMPTY_MAP_ITERATOR = EmptyMapIterator.INSTANCE;
  
  public static final OrderedMapIterator EMPTY_ORDERED_MAP_ITERATOR = EmptyOrderedMapIterator.INSTANCE;
  
  public static <E> ResettableIterator<E> emptyIterator() {
    return EmptyIterator.resettableEmptyIterator();
  }
  
  public static <E> ResettableListIterator<E> emptyListIterator() {
    return EmptyListIterator.resettableEmptyListIterator();
  }
  
  public static <E> OrderedIterator<E> emptyOrderedIterator() {
    return EmptyOrderedIterator.emptyOrderedIterator();
  }
  
  public static <K, V> MapIterator<K, V> emptyMapIterator() {
    return EmptyMapIterator.emptyMapIterator();
  }
  
  public static <K, V> OrderedMapIterator<K, V> emptyOrderedMapIterator() {
    return EmptyOrderedMapIterator.emptyOrderedMapIterator();
  }
  
  public static <E> ResettableIterator<E> singletonIterator(E object) {
    return (ResettableIterator<E>)new SingletonIterator(object);
  }
  
  public static <E> ListIterator<E> singletonListIterator(E object) {
    return (ListIterator<E>)new SingletonListIterator(object);
  }
  
  public static <E> ResettableIterator<E> arrayIterator(E... array) {
    return (ResettableIterator<E>)new ObjectArrayIterator((Object[])array);
  }
  
  public static <E> ResettableIterator<E> arrayIterator(Object array) {
    return (ResettableIterator<E>)new ArrayIterator(array);
  }
  
  public static <E> ResettableIterator<E> arrayIterator(E[] array, int start) {
    return (ResettableIterator<E>)new ObjectArrayIterator((Object[])array, start);
  }
  
  public static <E> ResettableIterator<E> arrayIterator(Object array, int start) {
    return (ResettableIterator<E>)new ArrayIterator(array, start);
  }
  
  public static <E> ResettableIterator<E> arrayIterator(E[] array, int start, int end) {
    return (ResettableIterator<E>)new ObjectArrayIterator((Object[])array, start, end);
  }
  
  public static <E> ResettableIterator<E> arrayIterator(Object array, int start, int end) {
    return (ResettableIterator<E>)new ArrayIterator(array, start, end);
  }
  
  public static <E> ResettableListIterator<E> arrayListIterator(E... array) {
    return (ResettableListIterator<E>)new ObjectArrayListIterator((Object[])array);
  }
  
  public static <E> ResettableListIterator<E> arrayListIterator(Object array) {
    return (ResettableListIterator<E>)new ArrayListIterator(array);
  }
  
  public static <E> ResettableListIterator<E> arrayListIterator(E[] array, int start) {
    return (ResettableListIterator<E>)new ObjectArrayListIterator((Object[])array, start);
  }
  
  public static <E> ResettableListIterator<E> arrayListIterator(Object array, int start) {
    return (ResettableListIterator<E>)new ArrayListIterator(array, start);
  }
  
  public static <E> ResettableListIterator<E> arrayListIterator(E[] array, int start, int end) {
    return (ResettableListIterator<E>)new ObjectArrayListIterator((Object[])array, start, end);
  }
  
  public static <E> ResettableListIterator<E> arrayListIterator(Object array, int start, int end) {
    return (ResettableListIterator<E>)new ArrayListIterator(array, start, end);
  }
  
  public static <E> Iterator<E> unmodifiableIterator(Iterator<E> iterator) {
    return UnmodifiableIterator.unmodifiableIterator(iterator);
  }
  
  public static <E> ListIterator<E> unmodifiableListIterator(ListIterator<E> listIterator) {
    return UnmodifiableListIterator.umodifiableListIterator(listIterator);
  }
  
  public static <K, V> MapIterator<K, V> unmodifiableMapIterator(MapIterator<K, V> mapIterator) {
    return UnmodifiableMapIterator.unmodifiableMapIterator(mapIterator);
  }
  
  public static <E> Iterator<E> chainedIterator(Iterator<? extends E> iterator1, Iterator<? extends E> iterator2) {
    return (Iterator<E>)new IteratorChain(iterator1, iterator2);
  }
  
  public static <E> Iterator<E> chainedIterator(Iterator<? extends E>... iterators) {
    return (Iterator<E>)new IteratorChain((Iterator[])iterators);
  }
  
  public static <E> Iterator<E> chainedIterator(Collection<Iterator<? extends E>> iterators) {
    return (Iterator<E>)new IteratorChain(iterators);
  }
  
  public static <E> Iterator<E> collatedIterator(Comparator<? super E> comparator, Iterator<? extends E> iterator1, Iterator<? extends E> iterator2) {
    return (Iterator<E>)new CollatingIterator(comparator, iterator1, iterator2);
  }
  
  public static <E> Iterator<E> collatedIterator(Comparator<? super E> comparator, Iterator<? extends E>... iterators) {
    return (Iterator<E>)new CollatingIterator(comparator, (Iterator[])iterators);
  }
  
  public static <E> Iterator<E> collatedIterator(Comparator<? super E> comparator, Collection<Iterator<? extends E>> iterators) {
    return (Iterator<E>)new CollatingIterator(comparator, iterators);
  }
  
  public static <E> Iterator<E> objectGraphIterator(E root, Transformer<? super E, ? extends E> transformer) {
    return (Iterator<E>)new ObjectGraphIterator(root, transformer);
  }
  
  public static <I, O> Iterator<O> transformedIterator(Iterator<? extends I> iterator, Transformer<? super I, ? extends O> transform) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    if (transform == null)
      throw new NullPointerException("Transformer must not be null"); 
    return (Iterator<O>)new TransformIterator(iterator, transform);
  }
  
  public static <E> Iterator<E> filteredIterator(Iterator<? extends E> iterator, Predicate<? super E> predicate) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    if (predicate == null)
      throw new NullPointerException("Predicate must not be null"); 
    return (Iterator<E>)new FilterIterator(iterator, predicate);
  }
  
  public static <E> ListIterator<E> filteredListIterator(ListIterator<? extends E> listIterator, Predicate<? super E> predicate) {
    if (listIterator == null)
      throw new NullPointerException("ListIterator must not be null"); 
    if (predicate == null)
      throw new NullPointerException("Predicate must not be null"); 
    return (ListIterator<E>)new FilterListIterator(listIterator, predicate);
  }
  
  public static <E> ResettableIterator<E> loopingIterator(Collection<? extends E> coll) {
    if (coll == null)
      throw new NullPointerException("Collection must not be null"); 
    return (ResettableIterator<E>)new LoopingIterator(coll);
  }
  
  public static <E> ResettableListIterator<E> loopingListIterator(List<E> list) {
    if (list == null)
      throw new NullPointerException("List must not be null"); 
    return (ResettableListIterator<E>)new LoopingListIterator(list);
  }
  
  public static NodeListIterator nodeListIterator(NodeList nodeList) {
    if (nodeList == null)
      throw new NullPointerException("NodeList must not be null"); 
    return new NodeListIterator(nodeList);
  }
  
  public static NodeListIterator nodeListIterator(Node node) {
    if (node == null)
      throw new NullPointerException("Node must not be null"); 
    return new NodeListIterator(node);
  }
  
  public static <E> Iterator<E> peekingIterator(Iterator<? extends E> iterator) {
    return (Iterator<E>)PeekingIterator.peekingIterator(iterator);
  }
  
  public static <E> Iterator<E> pushbackIterator(Iterator<? extends E> iterator) {
    return (Iterator<E>)PushbackIterator.pushbackIterator(iterator);
  }
  
  public static <E> Iterator<E> asIterator(Enumeration<? extends E> enumeration) {
    if (enumeration == null)
      throw new NullPointerException("Enumeration must not be null"); 
    return (Iterator<E>)new EnumerationIterator(enumeration);
  }
  
  public static <E> Iterator<E> asIterator(Enumeration<? extends E> enumeration, Collection<? super E> removeCollection) {
    if (enumeration == null)
      throw new NullPointerException("Enumeration must not be null"); 
    if (removeCollection == null)
      throw new NullPointerException("Collection must not be null"); 
    return (Iterator<E>)new EnumerationIterator(enumeration, removeCollection);
  }
  
  public static <E> Enumeration<E> asEnumeration(Iterator<? extends E> iterator) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    return (Enumeration<E>)new IteratorEnumeration(iterator);
  }
  
  public static <E> Iterable<E> asIterable(Iterator<? extends E> iterator) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    return (Iterable<E>)new IteratorIterable(iterator, false);
  }
  
  public static <E> Iterable<E> asMultipleUseIterable(Iterator<? extends E> iterator) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    return (Iterable<E>)new IteratorIterable(iterator, true);
  }
  
  public static <E> ListIterator<E> toListIterator(Iterator<? extends E> iterator) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    return (ListIterator<E>)new ListIteratorWrapper(iterator);
  }
  
  public static Object[] toArray(Iterator<?> iterator) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    List<?> list = toList(iterator, 100);
    return list.toArray();
  }
  
  public static <E> E[] toArray(Iterator<? extends E> iterator, Class<E> arrayClass) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    if (arrayClass == null)
      throw new NullPointerException("Array class must not be null"); 
    List<E> list = toList(iterator, 100);
    E[] array = (E[])Array.newInstance(arrayClass, list.size());
    return list.toArray(array);
  }
  
  public static <E> List<E> toList(Iterator<? extends E> iterator) {
    return toList(iterator, 10);
  }
  
  public static <E> List<E> toList(Iterator<? extends E> iterator, int estimatedSize) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    if (estimatedSize < 1)
      throw new IllegalArgumentException("Estimated size must be greater than 0"); 
    List<E> list = new ArrayList<E>(estimatedSize);
    while (iterator.hasNext())
      list.add(iterator.next()); 
    return list;
  }
  
  public static Iterator<?> getIterator(Object obj) {
    if (obj == null)
      return emptyIterator(); 
    if (obj instanceof Iterator)
      return (Iterator)obj; 
    if (obj instanceof Collection)
      return ((Collection)obj).iterator(); 
    if (obj instanceof Object[])
      return (Iterator<?>)new ObjectArrayIterator((Object[])obj); 
    if (obj instanceof Enumeration)
      return (Iterator<?>)new EnumerationIterator((Enumeration)obj); 
    if (obj instanceof Map)
      return ((Map)obj).values().iterator(); 
    if (obj instanceof NodeList)
      return (Iterator<?>)new NodeListIterator((NodeList)obj); 
    if (obj instanceof Node)
      return (Iterator<?>)new NodeListIterator((Node)obj); 
    if (obj instanceof Dictionary)
      return (Iterator<?>)new EnumerationIterator(((Dictionary)obj).elements()); 
    if (obj.getClass().isArray())
      return (Iterator<?>)new ArrayIterator(obj); 
    try {
      Method method = obj.getClass().getMethod("iterator", (Class[])null);
      if (Iterator.class.isAssignableFrom(method.getReturnType())) {
        Iterator<?> it = (Iterator)method.invoke(obj, (Object[])null);
        if (it != null)
          return it; 
      } 
    } catch (RuntimeException e) {
    
    } catch (NoSuchMethodException e) {
    
    } catch (IllegalAccessException e) {
    
    } catch (InvocationTargetException e) {}
    return singletonIterator(obj);
  }
}
