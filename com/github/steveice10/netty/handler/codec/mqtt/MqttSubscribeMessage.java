package com.github.steveice10.netty.handler.codec.mqtt;

public final class MqttSubscribeMessage extends MqttMessage {
  public MqttSubscribeMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader, MqttSubscribePayload payload) {
    super(mqttFixedHeader, variableHeader, payload);
  }
  
  public MqttMessageIdVariableHeader variableHeader() {
    return (MqttMessageIdVariableHeader)super.variableHeader();
  }
  
  public MqttSubscribePayload payload() {
    return (MqttSubscribePayload)super.payload();
  }
}
