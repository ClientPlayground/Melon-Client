package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.util.HashingStrategy;

public final class DefaultHeadersImpl<K, V> extends DefaultHeaders<K, V, DefaultHeadersImpl<K, V>> {
  public DefaultHeadersImpl(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter, DefaultHeaders.NameValidator<K> nameValidator) {
    super(nameHashingStrategy, valueConverter, nameValidator);
  }
}
