package org.apache.commons.cli;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

public class PatternOptionBuilder {
  public static final Class STRING_VALUE = String.class;
  
  public static final Class OBJECT_VALUE = Object.class;
  
  public static final Class NUMBER_VALUE = Number.class;
  
  public static final Class DATE_VALUE = Date.class;
  
  public static final Class CLASS_VALUE = Class.class;
  
  public static final Class EXISTING_FILE_VALUE = FileInputStream.class;
  
  public static final Class FILE_VALUE = File.class;
  
  public static final Class FILES_VALUE = (array$Ljava$io$File == null) ? (array$Ljava$io$File = class$("[Ljava.io.File;")) : array$Ljava$io$File;
  
  public static final Class URL_VALUE = URL.class;
  
  static Class array$Ljava$io$File;
  
  public static Object getValueClass(char ch) {
    switch (ch) {
      case '@':
        return OBJECT_VALUE;
      case ':':
        return STRING_VALUE;
      case '%':
        return NUMBER_VALUE;
      case '+':
        return CLASS_VALUE;
      case '#':
        return DATE_VALUE;
      case '<':
        return EXISTING_FILE_VALUE;
      case '>':
        return FILE_VALUE;
      case '*':
        return FILES_VALUE;
      case '/':
        return URL_VALUE;
    } 
    return null;
  }
  
  public static boolean isValueCode(char ch) {
    return (ch == '@' || ch == ':' || ch == '%' || ch == '+' || ch == '#' || ch == '<' || ch == '>' || ch == '*' || ch == '/' || ch == '!');
  }
  
  public static Options parsePattern(String pattern) {
    char opt = ' ';
    boolean required = false;
    Object type = null;
    Options options = new Options();
    for (int i = 0; i < pattern.length(); i++) {
      char ch = pattern.charAt(i);
      if (!isValueCode(ch)) {
        if (opt != ' ') {
          OptionBuilder.hasArg((type != null));
          OptionBuilder.isRequired(required);
          OptionBuilder.withType(type);
          options.addOption(OptionBuilder.create(opt));
          required = false;
          type = null;
          opt = ' ';
        } 
        opt = ch;
      } else if (ch == '!') {
        required = true;
      } else {
        type = getValueClass(ch);
      } 
    } 
    if (opt != ' ') {
      OptionBuilder.hasArg((type != null));
      OptionBuilder.isRequired(required);
      OptionBuilder.withType(type);
      options.addOption(OptionBuilder.create(opt));
    } 
    return options;
  }
}
