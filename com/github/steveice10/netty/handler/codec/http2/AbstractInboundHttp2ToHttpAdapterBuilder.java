package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.internal.ObjectUtil;

public abstract class AbstractInboundHttp2ToHttpAdapterBuilder<T extends InboundHttp2ToHttpAdapter, B extends AbstractInboundHttp2ToHttpAdapterBuilder<T, B>> {
  private final Http2Connection connection;
  
  private int maxContentLength;
  
  private boolean validateHttpHeaders;
  
  private boolean propagateSettings;
  
  protected AbstractInboundHttp2ToHttpAdapterBuilder(Http2Connection connection) {
    this.connection = (Http2Connection)ObjectUtil.checkNotNull(connection, "connection");
  }
  
  protected final B self() {
    return (B)this;
  }
  
  protected Http2Connection connection() {
    return this.connection;
  }
  
  protected int maxContentLength() {
    return this.maxContentLength;
  }
  
  protected B maxContentLength(int maxContentLength) {
    this.maxContentLength = maxContentLength;
    return self();
  }
  
  protected boolean isValidateHttpHeaders() {
    return this.validateHttpHeaders;
  }
  
  protected B validateHttpHeaders(boolean validate) {
    this.validateHttpHeaders = validate;
    return self();
  }
  
  protected boolean isPropagateSettings() {
    return this.propagateSettings;
  }
  
  protected B propagateSettings(boolean propagate) {
    this.propagateSettings = propagate;
    return self();
  }
  
  protected T build() {
    T instance;
    try {
      instance = build(connection(), maxContentLength(), 
          isValidateHttpHeaders(), isPropagateSettings());
    } catch (Throwable t) {
      throw new IllegalStateException("failed to create a new InboundHttp2ToHttpAdapter", t);
    } 
    this.connection.addListener((Http2Connection.Listener)instance);
    return instance;
  }
  
  protected abstract T build(Http2Connection paramHttp2Connection, int paramInt, boolean paramBoolean1, boolean paramBoolean2) throws Exception;
}
