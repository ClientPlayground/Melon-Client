package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.util.internal.StringUtil;

public final class MqttMessageIdVariableHeader {
  private final int messageId;
  
  public static MqttMessageIdVariableHeader from(int messageId) {
    if (messageId < 1 || messageId > 65535)
      throw new IllegalArgumentException("messageId: " + messageId + " (expected: 1 ~ 65535)"); 
    return new MqttMessageIdVariableHeader(messageId);
  }
  
  private MqttMessageIdVariableHeader(int messageId) {
    this.messageId = messageId;
  }
  
  public int messageId() {
    return this.messageId;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "messageId=" + 
      this.messageId + ']';
  }
}
