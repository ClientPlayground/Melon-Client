package org.apache.commons.cli;

class OptionValidator {
  static void validateOption(String opt) throws IllegalArgumentException {
    if (opt == null)
      return; 
    if (opt.length() == 1) {
      char ch = opt.charAt(0);
      if (!isValidOpt(ch))
        throw new IllegalArgumentException("illegal option value '" + ch + "'"); 
    } else {
      char[] chars = opt.toCharArray();
      for (int i = 0; i < chars.length; i++) {
        if (!isValidChar(chars[i]))
          throw new IllegalArgumentException("opt contains illegal character value '" + chars[i] + "'"); 
      } 
    } 
  }
  
  private static boolean isValidOpt(char c) {
    return (isValidChar(c) || c == ' ' || c == '?' || c == '@');
  }
  
  private static boolean isValidChar(char c) {
    return Character.isJavaIdentifierPart(c);
  }
}
