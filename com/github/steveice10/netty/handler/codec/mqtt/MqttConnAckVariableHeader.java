package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.util.internal.StringUtil;

public final class MqttConnAckVariableHeader {
  private final MqttConnectReturnCode connectReturnCode;
  
  private final boolean sessionPresent;
  
  public MqttConnAckVariableHeader(MqttConnectReturnCode connectReturnCode, boolean sessionPresent) {
    this.connectReturnCode = connectReturnCode;
    this.sessionPresent = sessionPresent;
  }
  
  public MqttConnectReturnCode connectReturnCode() {
    return this.connectReturnCode;
  }
  
  public boolean isSessionPresent() {
    return this.sessionPresent;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "connectReturnCode=" + 
      this.connectReturnCode + ", sessionPresent=" + 
      this.sessionPresent + ']';
  }
}
