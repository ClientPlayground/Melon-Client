package com.github.steveice10.netty.handler.codec.serialization;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

final class WeakReferenceMap<K, V> extends ReferenceMap<K, V> {
  WeakReferenceMap(Map<K, Reference<V>> delegate) {
    super(delegate);
  }
  
  Reference<V> fold(V value) {
    return new WeakReference<V>(value);
  }
}
