package com.github.steveice10.netty.util;

public interface AttributeMap {
  <T> Attribute<T> attr(AttributeKey<T> paramAttributeKey);
  
  <T> boolean hasAttr(AttributeKey<T> paramAttributeKey);
}
