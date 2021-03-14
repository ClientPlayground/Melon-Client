package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class DomainNameMappingBuilder<V> {
  private final V defaultValue;
  
  private final Map<String, V> map;
  
  public DomainNameMappingBuilder(V defaultValue) {
    this(4, defaultValue);
  }
  
  public DomainNameMappingBuilder(int initialCapacity, V defaultValue) {
    this.defaultValue = (V)ObjectUtil.checkNotNull(defaultValue, "defaultValue");
    this.map = new LinkedHashMap<String, V>(initialCapacity);
  }
  
  public DomainNameMappingBuilder<V> add(String hostname, V output) {
    this.map.put(ObjectUtil.checkNotNull(hostname, "hostname"), (V)ObjectUtil.checkNotNull(output, "output"));
    return this;
  }
  
  public DomainNameMapping<V> build() {
    return new ImmutableDomainNameMapping<V>(this.defaultValue, this.map);
  }
  
  private static final class ImmutableDomainNameMapping<V> extends DomainNameMapping<V> {
    private static final String REPR_HEADER = "ImmutableDomainNameMapping(default: ";
    
    private static final String REPR_MAP_OPENING = ", map: {";
    
    private static final String REPR_MAP_CLOSING = "})";
    
    private static final int REPR_CONST_PART_LENGTH = "ImmutableDomainNameMapping(default: "
      .length() + ", map: {".length() + "})".length();
    
    private final String[] domainNamePatterns;
    
    private final V[] values;
    
    private final Map<String, V> map;
    
    private ImmutableDomainNameMapping(V defaultValue, Map<String, V> map) {
      super((Map<String, V>)null, defaultValue);
      Set<Map.Entry<String, V>> mappings = map.entrySet();
      int numberOfMappings = mappings.size();
      this.domainNamePatterns = new String[numberOfMappings];
      this.values = (V[])new Object[numberOfMappings];
      Map<String, V> mapCopy = new LinkedHashMap<String, V>(map.size());
      int index = 0;
      for (Map.Entry<String, V> mapping : mappings) {
        String hostname = normalizeHostname(mapping.getKey());
        V value = mapping.getValue();
        this.domainNamePatterns[index] = hostname;
        this.values[index] = value;
        mapCopy.put(hostname, value);
        index++;
      } 
      this.map = Collections.unmodifiableMap(mapCopy);
    }
    
    @Deprecated
    public DomainNameMapping<V> add(String hostname, V output) {
      throw new UnsupportedOperationException("Immutable DomainNameMapping does not support modification after initial creation");
    }
    
    public V map(String hostname) {
      if (hostname != null) {
        hostname = normalizeHostname(hostname);
        int length = this.domainNamePatterns.length;
        for (int index = 0; index < length; index++) {
          if (matches(this.domainNamePatterns[index], hostname))
            return this.values[index]; 
        } 
      } 
      return this.defaultValue;
    }
    
    public Map<String, V> asMap() {
      return this.map;
    }
    
    public String toString() {
      String defaultValueStr = this.defaultValue.toString();
      int numberOfMappings = this.domainNamePatterns.length;
      if (numberOfMappings == 0)
        return "ImmutableDomainNameMapping(default: " + defaultValueStr + ", map: {" + "})"; 
      String pattern0 = this.domainNamePatterns[0];
      String value0 = this.values[0].toString();
      int oneMappingLength = pattern0.length() + value0.length() + 3;
      int estimatedBufferSize = estimateBufferSize(defaultValueStr.length(), numberOfMappings, oneMappingLength);
      StringBuilder sb = (new StringBuilder(estimatedBufferSize)).append("ImmutableDomainNameMapping(default: ").append(defaultValueStr).append(", map: {");
      appendMapping(sb, pattern0, value0);
      for (int index = 1; index < numberOfMappings; index++) {
        sb.append(", ");
        appendMapping(sb, index);
      } 
      return sb.append("})").toString();
    }
    
    private static int estimateBufferSize(int defaultValueLength, int numberOfMappings, int estimatedMappingLength) {
      return REPR_CONST_PART_LENGTH + defaultValueLength + (int)((estimatedMappingLength * numberOfMappings) * 1.1D);
    }
    
    private StringBuilder appendMapping(StringBuilder sb, int mappingIndex) {
      return appendMapping(sb, this.domainNamePatterns[mappingIndex], this.values[mappingIndex].toString());
    }
    
    private static StringBuilder appendMapping(StringBuilder sb, String domainNamePattern, String value) {
      return sb.append(domainNamePattern).append('=').append(value);
    }
  }
}
