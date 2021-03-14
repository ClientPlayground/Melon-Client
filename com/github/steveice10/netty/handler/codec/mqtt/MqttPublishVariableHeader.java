package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.util.internal.StringUtil;

public final class MqttPublishVariableHeader {
  private final String topicName;
  
  private final int packetId;
  
  public MqttPublishVariableHeader(String topicName, int packetId) {
    this.topicName = topicName;
    this.packetId = packetId;
  }
  
  public String topicName() {
    return this.topicName;
  }
  
  @Deprecated
  public int messageId() {
    return this.packetId;
  }
  
  public int packetId() {
    return this.packetId;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "topicName=" + 
      this.topicName + ", packetId=" + 
      this.packetId + ']';
  }
}
