package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.internal.tcnative.SSLContext;
import java.util.concurrent.locks.Lock;

public final class OpenSslSessionStats {
  private final ReferenceCountedOpenSslContext context;
  
  OpenSslSessionStats(ReferenceCountedOpenSslContext context) {
    this.context = context;
  }
  
  public long number() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionNumber(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long connect() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionConnect(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long connectGood() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionConnectGood(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long connectRenegotiate() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionConnectRenegotiate(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long accept() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionAccept(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long acceptGood() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionAcceptGood(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long acceptRenegotiate() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionAcceptRenegotiate(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long hits() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionHits(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long cbHits() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionCbHits(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long misses() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionMisses(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long timeouts() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionTimeouts(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long cacheFull() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionCacheFull(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long ticketKeyFail() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionTicketKeyFail(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long ticketKeyNew() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionTicketKeyNew(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long ticketKeyRenew() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionTicketKeyRenew(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
  
  public long ticketKeyResume() {
    Lock readerLock = this.context.ctxLock.readLock();
    readerLock.lock();
    try {
      return SSLContext.sessionTicketKeyResume(this.context.ctx);
    } finally {
      readerLock.unlock();
    } 
  }
}
