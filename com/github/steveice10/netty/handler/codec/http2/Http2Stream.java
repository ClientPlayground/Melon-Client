package com.github.steveice10.netty.handler.codec.http2;

public interface Http2Stream {
  int id();
  
  State state();
  
  Http2Stream open(boolean paramBoolean) throws Http2Exception;
  
  Http2Stream close();
  
  Http2Stream closeLocalSide();
  
  Http2Stream closeRemoteSide();
  
  boolean isResetSent();
  
  Http2Stream resetSent();
  
  <V> V setProperty(Http2Connection.PropertyKey paramPropertyKey, V paramV);
  
  <V> V getProperty(Http2Connection.PropertyKey paramPropertyKey);
  
  <V> V removeProperty(Http2Connection.PropertyKey paramPropertyKey);
  
  Http2Stream headersSent(boolean paramBoolean);
  
  boolean isHeadersSent();
  
  boolean isTrailersSent();
  
  Http2Stream headersReceived(boolean paramBoolean);
  
  boolean isHeadersReceived();
  
  boolean isTrailersReceived();
  
  Http2Stream pushPromiseSent();
  
  boolean isPushPromiseSent();
  
  public enum State {
    IDLE(false, false),
    RESERVED_LOCAL(false, false),
    RESERVED_REMOTE(false, false),
    OPEN(true, true),
    HALF_CLOSED_LOCAL(false, true),
    HALF_CLOSED_REMOTE(true, false),
    CLOSED(false, false);
    
    private final boolean localSideOpen;
    
    private final boolean remoteSideOpen;
    
    State(boolean localSideOpen, boolean remoteSideOpen) {
      this.localSideOpen = localSideOpen;
      this.remoteSideOpen = remoteSideOpen;
    }
    
    public boolean localSideOpen() {
      return this.localSideOpen;
    }
    
    public boolean remoteSideOpen() {
      return this.remoteSideOpen;
    }
  }
}
