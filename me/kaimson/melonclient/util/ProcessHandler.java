package me.kaimson.melonclient.util;

public class ProcessHandler {
  private Runnable runnable;
  
  public static ProcessHandler instantiate() {
    return new ProcessHandler();
  }
  
  public long time(Runnable runnable) {
    long start = System.currentTimeMillis();
    runnable.run();
    return System.currentTimeMillis() - start;
  }
}
