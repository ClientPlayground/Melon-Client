package com.github.steveice10.netty.handler.codec.mqtt;

public final class MqttConnectMessage extends MqttMessage {
  public MqttConnectMessage(MqttFixedHeader mqttFixedHeader, MqttConnectVariableHeader variableHeader, MqttConnectPayload payload) {
    super(mqttFixedHeader, variableHeader, payload);
  }
  
  public MqttConnectVariableHeader variableHeader() {
    return (MqttConnectVariableHeader)super.variableHeader();
  }
  
  public MqttConnectPayload payload() {
    return (MqttConnectPayload)super.payload();
  }
}
