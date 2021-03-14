package org.apache.commons.cli;

public class MissingArgumentException extends ParseException {
  private Option option;
  
  public MissingArgumentException(String message) {
    super(message);
  }
  
  public MissingArgumentException(Option option) {
    this("Missing argument for option: " + option.getKey());
    this.option = option;
  }
  
  public Option getOption() {
    return this.option;
  }
}
