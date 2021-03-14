package org.apache.commons.lang3.text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public class StrSubstitutor {
  public static final char DEFAULT_ESCAPE = '$';
  
  public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");
  
  public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
  
  public static final StrMatcher DEFAULT_VALUE_DELIMITER = StrMatcher.stringMatcher(":-");
  
  private char escapeChar;
  
  private StrMatcher prefixMatcher;
  
  private StrMatcher suffixMatcher;
  
  private StrMatcher valueDelimiterMatcher;
  
  private StrLookup<?> variableResolver;
  
  private boolean enableSubstitutionInVariables;
  
  public static <V> String replace(Object source, Map<String, V> valueMap) {
    return (new StrSubstitutor(valueMap)).replace(source);
  }
  
  public static <V> String replace(Object source, Map<String, V> valueMap, String prefix, String suffix) {
    return (new StrSubstitutor(valueMap, prefix, suffix)).replace(source);
  }
  
  public static String replace(Object source, Properties valueProperties) {
    if (valueProperties == null)
      return source.toString(); 
    Map<String, String> valueMap = new HashMap<String, String>();
    Enumeration<?> propNames = valueProperties.propertyNames();
    while (propNames.hasMoreElements()) {
      String propName = (String)propNames.nextElement();
      String propValue = valueProperties.getProperty(propName);
      valueMap.put(propName, propValue);
    } 
    return replace(source, valueMap);
  }
  
  public static String replaceSystemProperties(Object source) {
    return (new StrSubstitutor(StrLookup.systemPropertiesLookup())).replace(source);
  }
  
  public StrSubstitutor() {
    this((StrLookup)null, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
  }
  
  public <V> StrSubstitutor(Map<String, V> valueMap) {
    this(StrLookup.mapLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
  }
  
  public <V> StrSubstitutor(Map<String, V> valueMap, String prefix, String suffix) {
    this(StrLookup.mapLookup(valueMap), prefix, suffix, '$');
  }
  
  public <V> StrSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape) {
    this(StrLookup.mapLookup(valueMap), prefix, suffix, escape);
  }
  
  public <V> StrSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape, String valueDelimiter) {
    this(StrLookup.mapLookup(valueMap), prefix, suffix, escape, valueDelimiter);
  }
  
  public StrSubstitutor(StrLookup<?> variableResolver) {
    this(variableResolver, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
  }
  
  public StrSubstitutor(StrLookup<?> variableResolver, String prefix, String suffix, char escape) {
    setVariableResolver(variableResolver);
    setVariablePrefix(prefix);
    setVariableSuffix(suffix);
    setEscapeChar(escape);
    setValueDelimiterMatcher(DEFAULT_VALUE_DELIMITER);
  }
  
  public StrSubstitutor(StrLookup<?> variableResolver, String prefix, String suffix, char escape, String valueDelimiter) {
    setVariableResolver(variableResolver);
    setVariablePrefix(prefix);
    setVariableSuffix(suffix);
    setEscapeChar(escape);
    setValueDelimiter(valueDelimiter);
  }
  
  public StrSubstitutor(StrLookup<?> variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape) {
    this(variableResolver, prefixMatcher, suffixMatcher, escape, DEFAULT_VALUE_DELIMITER);
  }
  
  public StrSubstitutor(StrLookup<?> variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape, StrMatcher valueDelimiterMatcher) {
    setVariableResolver(variableResolver);
    setVariablePrefixMatcher(prefixMatcher);
    setVariableSuffixMatcher(suffixMatcher);
    setEscapeChar(escape);
    setValueDelimiterMatcher(valueDelimiterMatcher);
  }
  
  public String replace(String source) {
    if (source == null)
      return null; 
    StrBuilder buf = new StrBuilder(source);
    if (!substitute(buf, 0, source.length()))
      return source; 
    return buf.toString();
  }
  
  public String replace(String source, int offset, int length) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
    if (!substitute(buf, 0, length))
      return source.substring(offset, offset + length); 
    return buf.toString();
  }
  
  public String replace(char[] source) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(source.length)).append(source);
    substitute(buf, 0, source.length);
    return buf.toString();
  }
  
  public String replace(char[] source, int offset, int length) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
    substitute(buf, 0, length);
    return buf.toString();
  }
  
  public String replace(StringBuffer source) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(source.length())).append(source);
    substitute(buf, 0, buf.length());
    return buf.toString();
  }
  
  public String replace(StringBuffer source, int offset, int length) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
    substitute(buf, 0, length);
    return buf.toString();
  }
  
  public String replace(CharSequence source) {
    if (source == null)
      return null; 
    return replace(source, 0, source.length());
  }
  
  public String replace(CharSequence source, int offset, int length) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
    substitute(buf, 0, length);
    return buf.toString();
  }
  
  public String replace(StrBuilder source) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(source.length())).append(source);
    substitute(buf, 0, buf.length());
    return buf.toString();
  }
  
  public String replace(StrBuilder source, int offset, int length) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
    substitute(buf, 0, length);
    return buf.toString();
  }
  
  public String replace(Object source) {
    if (source == null)
      return null; 
    StrBuilder buf = (new StrBuilder()).append(source);
    substitute(buf, 0, buf.length());
    return buf.toString();
  }
  
  public boolean replaceIn(StringBuffer source) {
    if (source == null)
      return false; 
    return replaceIn(source, 0, source.length());
  }
  
  public boolean replaceIn(StringBuffer source, int offset, int length) {
    if (source == null)
      return false; 
    StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
    if (!substitute(buf, 0, length))
      return false; 
    source.replace(offset, offset + length, buf.toString());
    return true;
  }
  
  public boolean replaceIn(StringBuilder source) {
    if (source == null)
      return false; 
    return replaceIn(source, 0, source.length());
  }
  
  public boolean replaceIn(StringBuilder source, int offset, int length) {
    if (source == null)
      return false; 
    StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
    if (!substitute(buf, 0, length))
      return false; 
    source.replace(offset, offset + length, buf.toString());
    return true;
  }
  
  public boolean replaceIn(StrBuilder source) {
    if (source == null)
      return false; 
    return substitute(source, 0, source.length());
  }
  
  public boolean replaceIn(StrBuilder source, int offset, int length) {
    if (source == null)
      return false; 
    return substitute(source, offset, length);
  }
  
  protected boolean substitute(StrBuilder buf, int offset, int length) {
    return (substitute(buf, offset, length, null) > 0);
  }
  
  private int substitute(StrBuilder buf, int offset, int length, List<String> priorVariables) {
    StrMatcher pfxMatcher = getVariablePrefixMatcher();
    StrMatcher suffMatcher = getVariableSuffixMatcher();
    char escape = getEscapeChar();
    StrMatcher valueDelimMatcher = getValueDelimiterMatcher();
    boolean substitutionInVariablesEnabled = isEnableSubstitutionInVariables();
    boolean top = (priorVariables == null);
    boolean altered = false;
    int lengthChange = 0;
    char[] chars = buf.buffer;
    int bufEnd = offset + length;
    int pos = offset;
    while (pos < bufEnd) {
      int startMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd);
      if (startMatchLen == 0) {
        pos++;
        continue;
      } 
      if (pos > offset && chars[pos - 1] == escape) {
        buf.deleteCharAt(pos - 1);
        chars = buf.buffer;
        lengthChange--;
        altered = true;
        bufEnd--;
        continue;
      } 
      int startPos = pos;
      pos += startMatchLen;
      int endMatchLen = 0;
      int nestedVarCount = 0;
      while (pos < bufEnd) {
        if (substitutionInVariablesEnabled && (endMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd)) != 0) {
          nestedVarCount++;
          pos += endMatchLen;
          continue;
        } 
        endMatchLen = suffMatcher.isMatch(chars, pos, offset, bufEnd);
        if (endMatchLen == 0) {
          pos++;
          continue;
        } 
        if (nestedVarCount == 0) {
          String varNameExpr = new String(chars, startPos + startMatchLen, pos - startPos - startMatchLen);
          if (substitutionInVariablesEnabled) {
            StrBuilder bufName = new StrBuilder(varNameExpr);
            substitute(bufName, 0, bufName.length());
            varNameExpr = bufName.toString();
          } 
          pos += endMatchLen;
          int endPos = pos;
          String varName = varNameExpr;
          String varDefaultValue = null;
          if (valueDelimMatcher != null) {
            char[] varNameExprChars = varNameExpr.toCharArray();
            int valueDelimiterMatchLen = 0;
            for (int i = 0; i < varNameExprChars.length; i++) {
              if (!substitutionInVariablesEnabled && pfxMatcher.isMatch(varNameExprChars, i, i, varNameExprChars.length) != 0)
                break; 
              if ((valueDelimiterMatchLen = valueDelimMatcher.isMatch(varNameExprChars, i)) != 0) {
                varName = varNameExpr.substring(0, i);
                varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                break;
              } 
            } 
          } 
          if (priorVariables == null) {
            priorVariables = new ArrayList<String>();
            priorVariables.add(new String(chars, offset, length));
          } 
          checkCyclicSubstitution(varName, priorVariables);
          priorVariables.add(varName);
          String varValue = resolveVariable(varName, buf, startPos, endPos);
          if (varValue == null)
            varValue = varDefaultValue; 
          if (varValue != null) {
            int varLen = varValue.length();
            buf.replace(startPos, endPos, varValue);
            altered = true;
            int change = substitute(buf, startPos, varLen, priorVariables);
            change = change + varLen - endPos - startPos;
            pos += change;
            bufEnd += change;
            lengthChange += change;
            chars = buf.buffer;
          } 
          priorVariables.remove(priorVariables.size() - 1);
          break;
        } 
        nestedVarCount--;
        pos += endMatchLen;
      } 
    } 
    if (top)
      return altered ? 1 : 0; 
    return lengthChange;
  }
  
  private void checkCyclicSubstitution(String varName, List<String> priorVariables) {
    if (!priorVariables.contains(varName))
      return; 
    StrBuilder buf = new StrBuilder(256);
    buf.append("Infinite loop in property interpolation of ");
    buf.append(priorVariables.remove(0));
    buf.append(": ");
    buf.appendWithSeparators(priorVariables, "->");
    throw new IllegalStateException(buf.toString());
  }
  
  protected String resolveVariable(String variableName, StrBuilder buf, int startPos, int endPos) {
    StrLookup<?> resolver = getVariableResolver();
    if (resolver == null)
      return null; 
    return resolver.lookup(variableName);
  }
  
  public char getEscapeChar() {
    return this.escapeChar;
  }
  
  public void setEscapeChar(char escapeCharacter) {
    this.escapeChar = escapeCharacter;
  }
  
  public StrMatcher getVariablePrefixMatcher() {
    return this.prefixMatcher;
  }
  
  public StrSubstitutor setVariablePrefixMatcher(StrMatcher prefixMatcher) {
    if (prefixMatcher == null)
      throw new IllegalArgumentException("Variable prefix matcher must not be null!"); 
    this.prefixMatcher = prefixMatcher;
    return this;
  }
  
  public StrSubstitutor setVariablePrefix(char prefix) {
    return setVariablePrefixMatcher(StrMatcher.charMatcher(prefix));
  }
  
  public StrSubstitutor setVariablePrefix(String prefix) {
    if (prefix == null)
      throw new IllegalArgumentException("Variable prefix must not be null!"); 
    return setVariablePrefixMatcher(StrMatcher.stringMatcher(prefix));
  }
  
  public StrMatcher getVariableSuffixMatcher() {
    return this.suffixMatcher;
  }
  
  public StrSubstitutor setVariableSuffixMatcher(StrMatcher suffixMatcher) {
    if (suffixMatcher == null)
      throw new IllegalArgumentException("Variable suffix matcher must not be null!"); 
    this.suffixMatcher = suffixMatcher;
    return this;
  }
  
  public StrSubstitutor setVariableSuffix(char suffix) {
    return setVariableSuffixMatcher(StrMatcher.charMatcher(suffix));
  }
  
  public StrSubstitutor setVariableSuffix(String suffix) {
    if (suffix == null)
      throw new IllegalArgumentException("Variable suffix must not be null!"); 
    return setVariableSuffixMatcher(StrMatcher.stringMatcher(suffix));
  }
  
  public StrMatcher getValueDelimiterMatcher() {
    return this.valueDelimiterMatcher;
  }
  
  public StrSubstitutor setValueDelimiterMatcher(StrMatcher valueDelimiterMatcher) {
    this.valueDelimiterMatcher = valueDelimiterMatcher;
    return this;
  }
  
  public StrSubstitutor setValueDelimiter(char valueDelimiter) {
    return setValueDelimiterMatcher(StrMatcher.charMatcher(valueDelimiter));
  }
  
  public StrSubstitutor setValueDelimiter(String valueDelimiter) {
    if (StringUtils.isEmpty(valueDelimiter)) {
      setValueDelimiterMatcher(null);
      return this;
    } 
    return setValueDelimiterMatcher(StrMatcher.stringMatcher(valueDelimiter));
  }
  
  public StrLookup<?> getVariableResolver() {
    return this.variableResolver;
  }
  
  public void setVariableResolver(StrLookup<?> variableResolver) {
    this.variableResolver = variableResolver;
  }
  
  public boolean isEnableSubstitutionInVariables() {
    return this.enableSubstitutionInVariables;
  }
  
  public void setEnableSubstitutionInVariables(boolean enableSubstitutionInVariables) {
    this.enableSubstitutionInVariables = enableSubstitutionInVariables;
  }
}
