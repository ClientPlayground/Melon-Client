package com.github.steveice10.netty.handler.codec.mqtt;

public final class MqttConnAckMessage extends MqttMessage {
  public MqttConnAckMessage(MqttFixedHeader mqttFixedHeader, MqttConnAckVariableHeader variableHeader) {
    super(mqttFixedHeader, variableHeader);
  }
  
  public MqttConnAckVariableHeader variableHeader() {
    return (MqttConnAckVariableHeader)super.variableHeader();
  }
}
