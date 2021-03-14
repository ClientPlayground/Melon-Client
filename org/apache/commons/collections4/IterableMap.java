package org.apache.commons.collections4;

import java.util.Map;

public interface IterableMap<K, V> extends Map<K, V>, Put<K, V>, IterableGet<K, V> {}
