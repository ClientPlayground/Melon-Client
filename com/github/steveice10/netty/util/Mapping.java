package com.github.steveice10.netty.util;

public interface Mapping<IN, OUT> {
  OUT map(IN paramIN);
}
