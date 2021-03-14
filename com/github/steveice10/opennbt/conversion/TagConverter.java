package com.github.steveice10.opennbt.conversion;

public interface TagConverter<T extends com.github.steveice10.opennbt.tag.builtin.Tag, V> {
  V convert(T paramT);
  
  T convert(String paramString, V paramV);
}
