package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.channel.ChannelHandlerContext;

public interface Http2RemoteFlowController extends Http2FlowController {
  ChannelHandlerContext channelHandlerContext();
  
  void addFlowControlled(Http2Stream paramHttp2Stream, FlowControlled paramFlowControlled);
  
  boolean hasFlowControlled(Http2Stream paramHttp2Stream);
  
  void writePendingBytes() throws Http2Exception;
  
  void listener(Listener paramListener);
  
  boolean isWritable(Http2Stream paramHttp2Stream);
  
  void channelWritabilityChanged() throws Http2Exception;
  
  void updateDependencyTree(int paramInt1, int paramInt2, short paramShort, boolean paramBoolean);
  
  public static interface Listener {
    void writabilityChanged(Http2Stream param1Http2Stream);
  }
  
  public static interface FlowControlled {
    int size();
    
    void error(ChannelHandlerContext param1ChannelHandlerContext, Throwable param1Throwable);
    
    void writeComplete();
    
    void write(ChannelHandlerContext param1ChannelHandlerContext, int param1Int);
    
    boolean merge(ChannelHandlerContext param1ChannelHandlerContext, FlowControlled param1FlowControlled);
  }
}
