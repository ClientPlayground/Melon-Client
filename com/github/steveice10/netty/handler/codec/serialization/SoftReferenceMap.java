package com.github.steveice10.netty.handler.codec.serialization;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;

final class SoftReferenceMap<K, V> extends ReferenceMap<K, V> {
  SoftReferenceMap(Map<K, Reference<V>> delegate) {
    super(delegate);
  }
  
  Reference<V> fold(V value) {
    return new SoftReference<V>(value);
  }
}
