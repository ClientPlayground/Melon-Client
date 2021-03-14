package com.replaymod.lib.org.apache.commons.exec.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class StringUtils {
  private static final String SINGLE_QUOTE = "'";
  
  private static final String DOUBLE_QUOTE = "\"";
  
  private static final char SLASH_CHAR = '/';
  
  private static final char BACKSLASH_CHAR = '\\';
  
  public static StringBuffer stringSubstitution(String argStr, Map<? super String, ?> vars, boolean isLenient) {
    StringBuffer argBuf = new StringBuffer();
    if (argStr == null || argStr.length() == 0)
      return argBuf; 
    if (vars == null || vars.size() == 0)
      return argBuf.append(argStr); 
    int argStrLength = argStr.length();
    for (int cIdx = 0; cIdx < argStrLength; ) {
      StringBuilder nameBuf;
      char ch = argStr.charAt(cIdx);
      char del = ' ';
      switch (ch) {
        case '$':
          nameBuf = new StringBuilder();
          del = argStr.charAt(cIdx + 1);
          if (del == '{') {
            cIdx++;
            while (++cIdx < argStr.length()) {
              ch = argStr.charAt(cIdx);
              if (ch == '_' || ch == '.' || ch == '-' || ch == '+' || Character.isLetterOrDigit(ch)) {
                nameBuf.append(ch);
                cIdx++;
              } 
            } 
            if (nameBuf.length() >= 0) {
              String value;
              Object temp = vars.get(nameBuf.toString());
              if (temp instanceof File) {
                value = fixFileSeparatorChar(((File)temp).getAbsolutePath());
              } else {
                value = (temp != null) ? temp.toString() : null;
              } 
              if (value != null) {
                argBuf.append(value);
              } else if (isLenient) {
                argBuf.append("${").append(nameBuf.toString()).append("}");
              } else {
                throw new RuntimeException("No value found for : " + nameBuf);
              } 
              del = argStr.charAt(cIdx);
              if (del != '}')
                throw new RuntimeException("Delimiter not found for : " + nameBuf); 
            } 
            cIdx++;
            continue;
          } 
          argBuf.append(ch);
          cIdx++;
          continue;
      } 
      argBuf.append(ch);
      cIdx++;
    } 
    return argBuf;
  }
  
  public static String[] split(String input, String splitChar) {
    StringTokenizer tokens = new StringTokenizer(input, splitChar);
    List<String> strList = new ArrayList<String>();
    while (tokens.hasMoreTokens())
      strList.add(tokens.nextToken()); 
    return strList.<String>toArray(new String[strList.size()]);
  }
  
  public static String fixFileSeparatorChar(String arg) {
    return arg.replace('/', File.separatorChar).replace('\\', File.separatorChar);
  }
  
  public static String toString(String[] strings, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < strings.length; i++) {
      if (i > 0)
        sb.append(separator); 
      sb.append(strings[i]);
    } 
    return sb.toString();
  }
  
  public static String quoteArgument(String argument) {
    String cleanedArgument = argument.trim();
    while (cleanedArgument.startsWith("'") || cleanedArgument.startsWith("\""))
      cleanedArgument = cleanedArgument.substring(1); 
    while (cleanedArgument.endsWith("'") || cleanedArgument.endsWith("\""))
      cleanedArgument = cleanedArgument.substring(0, cleanedArgument.length() - 1); 
    StringBuilder buf = new StringBuilder();
    if (cleanedArgument.indexOf("\"") > -1) {
      if (cleanedArgument.indexOf("'") > -1)
        throw new IllegalArgumentException("Can't handle single and double quotes in same argument"); 
      return buf.append("'").append(cleanedArgument).append("'").toString();
    } 
    if (cleanedArgument.indexOf("'") > -1 || cleanedArgument.indexOf(" ") > -1)
      return buf.append("\"").append(cleanedArgument).append("\"").toString(); 
    return cleanedArgument;
  }
  
  public static boolean isQuoted(String argument) {
    return ((argument.startsWith("'") && argument.endsWith("'")) || (argument.startsWith("\"") && argument.endsWith("\"")));
  }
}
