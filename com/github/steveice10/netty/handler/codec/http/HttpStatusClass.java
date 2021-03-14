package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.util.AsciiString;

public enum HttpStatusClass {
  INFORMATIONAL(100, 200, "Informational"),
  SUCCESS(200, 300, "Success"),
  REDIRECTION(300, 400, "Redirection"),
  CLIENT_ERROR(400, 500, "Client Error"),
  SERVER_ERROR(500, 600, "Server Error"),
  UNKNOWN(0, 0, "Unknown Status") {
    public boolean contains(int code) {
      return (code < 100 || code >= 600);
    }
  };
  
  private final int min;
  
  private final int max;
  
  private final AsciiString defaultReasonPhrase;
  
  private static int digit(char c) {
    return c - 48;
  }
  
  private static boolean isDigit(char c) {
    return (c >= '0' && c <= '9');
  }
  
  HttpStatusClass(int min, int max, String defaultReasonPhrase) {
    this.min = min;
    this.max = max;
    this.defaultReasonPhrase = AsciiString.cached(defaultReasonPhrase);
  }
  
  public boolean contains(int code) {
    return (code >= this.min && code < this.max);
  }
  
  AsciiString defaultReasonPhrase() {
    return this.defaultReasonPhrase;
  }
}
