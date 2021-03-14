package com.github.steveice10.opennbt.tag;

public class TagRegisterException extends RuntimeException {
  private static final long serialVersionUID = -2022049594558041160L;
  
  public TagRegisterException() {}
  
  public TagRegisterException(String message) {
    super(message);
  }
  
  public TagRegisterException(Throwable cause) {
    super(cause);
  }
  
  public TagRegisterException(String message, Throwable cause) {
    super(message, cause);
  }
}
