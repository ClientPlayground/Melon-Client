package com.github.steveice10.netty.handler.ssl;

interface OpenSslEngineMap {
  ReferenceCountedOpenSslEngine remove(long paramLong);
  
  void add(ReferenceCountedOpenSslEngine paramReferenceCountedOpenSslEngine);
  
  ReferenceCountedOpenSslEngine get(long paramLong);
}
