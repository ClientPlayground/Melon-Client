package com.github.steveice10.netty.util;

public interface BooleanSupplier {
  public static final BooleanSupplier FALSE_SUPPLIER = new BooleanSupplier() {
      public boolean get() {
        return false;
      }
    };
  
  public static final BooleanSupplier TRUE_SUPPLIER = new BooleanSupplier() {
      public boolean get() {
        return true;
      }
    };
  
  boolean get() throws Exception;
}
