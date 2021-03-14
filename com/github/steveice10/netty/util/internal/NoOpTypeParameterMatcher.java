package com.github.steveice10.netty.util.internal;

public final class NoOpTypeParameterMatcher extends TypeParameterMatcher {
  public boolean match(Object msg) {
    return true;
  }
}
