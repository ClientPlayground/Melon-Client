package com.github.steveice10.netty.util;

public interface UncheckedBooleanSupplier extends BooleanSupplier {
  public static final UncheckedBooleanSupplier FALSE_SUPPLIER = new UncheckedBooleanSupplier() {
      public boolean get() {
        return false;
      }
    };
  
  public static final UncheckedBooleanSupplier TRUE_SUPPLIER = new UncheckedBooleanSupplier() {
      public boolean get() {
        return true;
      }
    };
  
  boolean get();
}
