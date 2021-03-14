package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MqttSubAckPayload {
  private final List<Integer> grantedQoSLevels;
  
  public MqttSubAckPayload(int... grantedQoSLevels) {
    if (grantedQoSLevels == null)
      throw new NullPointerException("grantedQoSLevels"); 
    List<Integer> list = new ArrayList<Integer>(grantedQoSLevels.length);
    for (int v : grantedQoSLevels)
      list.add(Integer.valueOf(v)); 
    this.grantedQoSLevels = Collections.unmodifiableList(list);
  }
  
  public MqttSubAckPayload(Iterable<Integer> grantedQoSLevels) {
    if (grantedQoSLevels == null)
      throw new NullPointerException("grantedQoSLevels"); 
    List<Integer> list = new ArrayList<Integer>();
    for (Integer v : grantedQoSLevels) {
      if (v == null)
        break; 
      list.add(v);
    } 
    this.grantedQoSLevels = Collections.unmodifiableList(list);
  }
  
  public List<Integer> grantedQoSLevels() {
    return this.grantedQoSLevels;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "grantedQoSLevels=" + 
      this.grantedQoSLevels + ']';
  }
}
