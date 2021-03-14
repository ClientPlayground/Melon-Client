package com.github.steveice10.netty.handler.codec.mqtt;

public final class MqttPubAckMessage extends MqttMessage {
  public MqttPubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader) {
    super(mqttFixedHeader, variableHeader);
  }
  
  public MqttMessageIdVariableHeader variableHeader() {
    return (MqttMessageIdVariableHeader)super.variableHeader();
  }
}
