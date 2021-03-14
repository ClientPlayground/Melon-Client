package com.github.steveice10.netty.handler.codec.socks;

public abstract class SocksRequest extends SocksMessage {
  private final SocksRequestType requestType;
  
  protected SocksRequest(SocksRequestType requestType) {
    super(SocksMessageType.REQUEST);
    if (requestType == null)
      throw new NullPointerException("requestType"); 
    this.requestType = requestType;
  }
  
  public SocksRequestType requestType() {
    return this.requestType;
  }
}
