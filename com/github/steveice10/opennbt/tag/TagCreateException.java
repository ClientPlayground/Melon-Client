package com.github.steveice10.opennbt.tag;

public class TagCreateException extends Exception {
  private static final long serialVersionUID = -2022049594558041160L;
  
  public TagCreateException() {}
  
  public TagCreateException(String message) {
    super(message);
  }
  
  public TagCreateException(Throwable cause) {
    super(cause);
  }
  
  public TagCreateException(String message, Throwable cause) {
    super(message, cause);
  }
}
