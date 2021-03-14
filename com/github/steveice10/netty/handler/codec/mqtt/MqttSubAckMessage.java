package com.github.steveice10.netty.handler.codec.mqtt;

public final class MqttSubAckMessage extends MqttMessage {
  public MqttSubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader, MqttSubAckPayload payload) {
    super(mqttFixedHeader, variableHeader, payload);
  }
  
  public MqttMessageIdVariableHeader variableHeader() {
    return (MqttMessageIdVariableHeader)super.variableHeader();
  }
  
  public MqttSubAckPayload payload() {
    return (MqttSubAckPayload)super.payload();
  }
}
