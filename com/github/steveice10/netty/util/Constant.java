package com.github.steveice10.netty.util;

public interface Constant<T extends Constant<T>> extends Comparable<T> {
  int id();
  
  String name();
}
