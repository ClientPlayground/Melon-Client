package com.github.steveice10.packetlib;

public interface ConnectionListener {
  String getHost();
  
  int getPort();
  
  boolean isListening();
  
  void bind();
  
  void bind(boolean paramBoolean);
  
  void bind(boolean paramBoolean, Runnable paramRunnable);
  
  void close();
  
  void close(boolean paramBoolean);
  
  void close(boolean paramBoolean, Runnable paramRunnable);
}
