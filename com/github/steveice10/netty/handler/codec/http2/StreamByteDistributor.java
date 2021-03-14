package com.github.steveice10.netty.handler.codec.http2;

public interface StreamByteDistributor {
  void updateStreamableBytes(StreamState paramStreamState);
  
  void updateDependencyTree(int paramInt1, int paramInt2, short paramShort, boolean paramBoolean);
  
  boolean distribute(int paramInt, Writer paramWriter) throws Http2Exception;
  
  public static interface Writer {
    void write(Http2Stream param1Http2Stream, int param1Int);
  }
  
  public static interface StreamState {
    Http2Stream stream();
    
    long pendingBytes();
    
    boolean hasFrame();
    
    int windowSize();
  }
}
