package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.net.IDN;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class DomainNameMapping<V> implements Mapping<String, V> {
  final V defaultValue;
  
  private final Map<String, V> map;
  
  private final Map<String, V> unmodifiableMap;
  
  @Deprecated
  public DomainNameMapping(V defaultValue) {
    this(4, defaultValue);
  }
  
  @Deprecated
  public DomainNameMapping(int initialCapacity, V defaultValue) {
    this(new LinkedHashMap<String, V>(initialCapacity), defaultValue);
  }
  
  DomainNameMapping(Map<String, V> map, V defaultValue) {
    this.defaultValue = (V)ObjectUtil.checkNotNull(defaultValue, "defaultValue");
    this.map = map;
    this.unmodifiableMap = (map != null) ? Collections.<String, V>unmodifiableMap(map) : null;
  }
  
  @Deprecated
  public DomainNameMapping<V> add(String hostname, V output) {
    this.map.put(normalizeHostname((String)ObjectUtil.checkNotNull(hostname, "hostname")), (V)ObjectUtil.checkNotNull(output, "output"));
    return this;
  }
  
  static boolean matches(String template, String hostName) {
    if (template.startsWith("*."))
      return (template.regionMatches(2, hostName, 0, hostName.length()) || 
        StringUtil.commonSuffixOfLength(hostName, template, template.length() - 1)); 
    return template.equals(hostName);
  }
  
  static String normalizeHostname(String hostname) {
    if (needsNormalization(hostname))
      hostname = IDN.toASCII(hostname, 1); 
    return hostname.toLowerCase(Locale.US);
  }
  
  private static boolean needsNormalization(String hostname) {
    int length = hostname.length();
    for (int i = 0; i < length; i++) {
      int c = hostname.charAt(i);
      if (c > 127)
        return true; 
    } 
    return false;
  }
  
  public V map(String hostname) {
    if (hostname != null) {
      hostname = normalizeHostname(hostname);
      for (Map.Entry<String, V> entry : this.map.entrySet()) {
        if (matches(entry.getKey(), hostname))
          return entry.getValue(); 
      } 
    } 
    return this.defaultValue;
  }
  
  public Map<String, V> asMap() {
    return this.unmodifiableMap;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(default: " + this.defaultValue + ", map: " + this.map + ')';
  }
}
