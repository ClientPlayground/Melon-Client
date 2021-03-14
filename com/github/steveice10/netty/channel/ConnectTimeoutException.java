package com.github.steveice10.netty.channel;

import java.net.ConnectException;

public class ConnectTimeoutException extends ConnectException {
  private static final long serialVersionUID = 2317065249988317463L;
  
  public ConnectTimeoutException(String msg) {
    super(msg);
  }
  
  public ConnectTimeoutException() {}
}
