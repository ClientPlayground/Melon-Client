package com.github.steveice10.netty.handler.codec.mqtt;

public final class MqttUnsubscribeMessage extends MqttMessage {
  public MqttUnsubscribeMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader, MqttUnsubscribePayload payload) {
    super(mqttFixedHeader, variableHeader, payload);
  }
  
  public MqttMessageIdVariableHeader variableHeader() {
    return (MqttMessageIdVariableHeader)super.variableHeader();
  }
  
  public MqttUnsubscribePayload payload() {
    return (MqttUnsubscribePayload)super.payload();
  }
}
