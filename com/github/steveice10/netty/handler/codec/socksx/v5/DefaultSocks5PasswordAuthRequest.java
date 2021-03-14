package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSocks5PasswordAuthRequest extends AbstractSocks5Message implements Socks5PasswordAuthRequest {
  private final String username;
  
  private final String password;
  
  public DefaultSocks5PasswordAuthRequest(String username, String password) {
    if (username == null)
      throw new NullPointerException("username"); 
    if (password == null)
      throw new NullPointerException("password"); 
    if (username.length() > 255)
      throw new IllegalArgumentException("username: **** (expected: less than 256 chars)"); 
    if (password.length() > 255)
      throw new IllegalArgumentException("password: **** (expected: less than 256 chars)"); 
    this.username = username;
    this.password = password;
  }
  
  public String username() {
    return this.username;
  }
  
  public String password() {
    return this.password;
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));
    DecoderResult decoderResult = decoderResult();
    if (!decoderResult.isSuccess()) {
      buf.append("(decoderResult: ");
      buf.append(decoderResult);
      buf.append(", username: ");
    } else {
      buf.append("(username: ");
    } 
    buf.append(username());
    buf.append(", password: ****)");
    return buf.toString();
  }
}
