package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.channel.ChannelHandlerContext;

public interface Http2PromisedRequestVerifier {
  public static final Http2PromisedRequestVerifier ALWAYS_VERIFY = new Http2PromisedRequestVerifier() {
      public boolean isAuthoritative(ChannelHandlerContext ctx, Http2Headers headers) {
        return true;
      }
      
      public boolean isCacheable(Http2Headers headers) {
        return true;
      }
      
      public boolean isSafe(Http2Headers headers) {
        return true;
      }
    };
  
  boolean isAuthoritative(ChannelHandlerContext paramChannelHandlerContext, Http2Headers paramHttp2Headers);
  
  boolean isCacheable(Http2Headers paramHttp2Headers);
  
  boolean isSafe(Http2Headers paramHttp2Headers);
}
