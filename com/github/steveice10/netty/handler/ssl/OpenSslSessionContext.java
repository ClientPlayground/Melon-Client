package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.internal.tcnative.SSL;
import com.github.steveice10.netty.internal.tcnative.SSLContext;
import com.github.steveice10.netty.internal.tcnative.SessionTicketKey;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

public abstract class OpenSslSessionContext implements SSLSessionContext {
  private static final Enumeration<byte[]> EMPTY = new EmptyEnumeration();
  
  private final OpenSslSessionStats stats;
  
  final ReferenceCountedOpenSslContext context;
  
  OpenSslSessionContext(ReferenceCountedOpenSslContext context) {
    this.context = context;
    this.stats = new OpenSslSessionStats(context);
  }
  
  public SSLSession getSession(byte[] bytes) {
    if (bytes == null)
      throw new NullPointerException("bytes"); 
    return null;
  }
  
  public Enumeration<byte[]> getIds() {
    return EMPTY;
  }
  
  @Deprecated
  public void setTicketKeys(byte[] keys) {
    if (keys.length % 48 != 0)
      throw new IllegalArgumentException("keys.length % 48 != 0"); 
    SessionTicketKey[] tickets = new SessionTicketKey[keys.length / 48];
    for (int i = 0, a = 0; i < tickets.length; i++) {
      byte[] name = Arrays.copyOfRange(keys, a, 16);
      a += 16;
      byte[] hmacKey = Arrays.copyOfRange(keys, a, 16);
      i += 16;
      byte[] aesKey = Arrays.copyOfRange(keys, a, 16);
      a += 16;
      tickets[i] = new SessionTicketKey(name, hmacKey, aesKey);
    } 
    Lock writerLock = this.context.ctxLock.writeLock();
    writerLock.lock();
    try {
      SSLContext.clearOptions(this.context.ctx, SSL.SSL_OP_NO_TICKET);
      SSLContext.setSessionTicketKeys(this.context.ctx, tickets);
    } finally {
      writerLock.unlock();
    } 
  }
  
  public void setTicketKeys(OpenSslSessionTicketKey... keys) {
    ObjectUtil.checkNotNull(keys, "keys");
    SessionTicketKey[] ticketKeys = new SessionTicketKey[keys.length];
    for (int i = 0; i < ticketKeys.length; i++)
      ticketKeys[i] = (keys[i]).key; 
    Lock writerLock = this.context.ctxLock.writeLock();
    writerLock.lock();
    try {
      SSLContext.clearOptions(this.context.ctx, SSL.SSL_OP_NO_TICKET);
      SSLContext.setSessionTicketKeys(this.context.ctx, ticketKeys);
    } finally {
      writerLock.unlock();
    } 
  }
  
  public abstract void setSessionCacheEnabled(boolean paramBoolean);
  
  public abstract boolean isSessionCacheEnabled();
  
  public OpenSslSessionStats stats() {
    return this.stats;
  }
  
  private static final class EmptyEnumeration implements Enumeration<byte[]> {
    private EmptyEnumeration() {}
    
    public boolean hasMoreElements() {
      return false;
    }
    
    public byte[] nextElement() {
      throw new NoSuchElementException();
    }
  }
}
