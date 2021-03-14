package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@GwtCompatible(serializable = true, emulated = true)
public final class HashMultimap<K, V> extends AbstractSetMultimap<K, V> {
  private static final int DEFAULT_VALUES_PER_KEY = 2;
  
  @VisibleForTesting
  transient int expectedValuesPerKey = 2;
  
  @GwtIncompatible("Not needed in emulated source")
  private static final long serialVersionUID = 0L;
  
  public static <K, V> HashMultimap<K, V> create() {
    return new HashMultimap<K, V>();
  }
  
  public static <K, V> HashMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
    return new HashMultimap<K, V>(expectedKeys, expectedValuesPerKey);
  }
  
  public static <K, V> HashMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
    return new HashMultimap<K, V>(multimap);
  }
  
  private HashMultimap() {
    super(new HashMap<K, Collection<V>>());
  }
  
  private HashMultimap(int expectedKeys, int expectedValuesPerKey) {
    super(Maps.newHashMapWithExpectedSize(expectedKeys));
    Preconditions.checkArgument((expectedValuesPerKey >= 0));
    this.expectedValuesPerKey = expectedValuesPerKey;
  }
  
  private HashMultimap(Multimap<? extends K, ? extends V> multimap) {
    super(Maps.newHashMapWithExpectedSize(multimap.keySet().size()));
    putAll(multimap);
  }
  
  Set<V> createCollection() {
    return Sets.newHashSetWithExpectedSize(this.expectedValuesPerKey);
  }
  
  @GwtIncompatible("java.io.ObjectOutputStream")
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeInt(this.expectedValuesPerKey);
    Serialization.writeMultimap(this, stream);
  }
  
  @GwtIncompatible("java.io.ObjectInputStream")
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.expectedValuesPerKey = stream.readInt();
    int distinctKeys = Serialization.readCount(stream);
    Map<K, Collection<V>> map = Maps.newHashMapWithExpectedSize(distinctKeys);
    setMap(map);
    Serialization.populateMultimap(this, stream, distinctKeys);
  }
}
