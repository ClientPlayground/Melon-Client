package com.github.steveice10.opennbt.conversion;

public class ConverterRegisterException extends RuntimeException {
  private static final long serialVersionUID = -2022049594558041160L;
  
  public ConverterRegisterException() {}
  
  public ConverterRegisterException(String message) {
    super(message);
  }
  
  public ConverterRegisterException(Throwable cause) {
    super(cause);
  }
  
  public ConverterRegisterException(String message, Throwable cause) {
    super(message, cause);
  }
}
