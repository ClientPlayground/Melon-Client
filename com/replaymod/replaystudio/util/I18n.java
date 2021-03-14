package com.replaymod.replaystudio.util;

public class I18n {
  private static volatile Impl impl;
  
  public static void setI18n(Impl impl) {
    I18n.impl = impl;
  }
  
  public static String format(String key, Object... args) {
    return impl.format(key, args);
  }
  
  public static interface Impl {
    String format(String param1String, Object... param1VarArgs);
  }
}
