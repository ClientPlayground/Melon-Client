package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSocks5PasswordAuthResponse extends AbstractSocks5Message implements Socks5PasswordAuthResponse {
  private final Socks5PasswordAuthStatus status;
  
  public DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus status) {
    if (status == null)
      throw new NullPointerException("status"); 
    this.status = status;
  }
  
  public Socks5PasswordAuthStatus status() {
    return this.status;
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));
    DecoderResult decoderResult = decoderResult();
    if (!decoderResult.isSuccess()) {
      buf.append("(decoderResult: ");
      buf.append(decoderResult);
      buf.append(", status: ");
    } else {
      buf.append("(status: ");
    } 
    buf.append(status());
    buf.append(')');
    return buf.toString();
  }
}
