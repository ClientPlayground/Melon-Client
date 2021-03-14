package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.internal.tcnative.SSL;
import com.github.steveice10.netty.internal.tcnative.SSLContext;
import java.util.concurrent.locks.Lock;

public final class OpenSslServerSessionContext extends OpenSslSessionContext {
  OpenSslServerSessionContext(ReferenceCountedOpenSslContext context) {
    super(context);
  }
  
  public void setSessionTimeout(int seconds) {
    if (seconds < 0)
      throw new IllegalArgumentException(); 
    Lock writerLock = this.context.ctxLock.writeLock();
    writerLock.lock();
    try {
      SSLContext.setSessionCacheTimeout(this.context.ctx, seconds);
    } finally {
      writerLock.unlock();
    } 
  }
  
  public int getSessionTimeout() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return (int)SSLContext.getSessionCacheTimeout(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public void setSessionCacheSize(int size) {
    if (size < 0)
      throw new IllegalArgumentException(); 
    Lock writerLock = this.context.ctxLock.writeLock();
    writerLock.lock();
    try {
      SSLContext.setSessionCacheSize(this.context.ctx, size);
    } finally {
      writerLock.unlock();
    } 
  }
  
  public int getSessionCacheSize() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return (int)SSLContext.getSessionCacheSize(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public void setSessionCacheEnabled(boolean enabled) {
    long mode = enabled ? SSL.SSL_SESS_CACHE_SERVER : SSL.SSL_SESS_CACHE_OFF;
    Lock writerLock = this.context.ctxLock.writeLock();
    writerLock.lock();
    try {
      SSLContext.setSessionCacheMode(this.context.ctx, mode);
    } finally {
      writerLock.unlock();
    } 
  }
  
  public boolean isSessionCacheEnabled() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return (SSLContext.getSessionCacheMode(this.context.ctx) == SSL.SSL_SESS_CACHE_SERVER);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public boolean setSessionIdContext(byte[] sidCtx) {
    Lock writerLock = this.context.ctxLock.writeLock();
    writerLock.lock();
    try {
      return SSLContext.setSessionIdContext(this.context.ctx, sidCtx);
    } finally {
      writerLock.unlock();
    } 
  }
}
