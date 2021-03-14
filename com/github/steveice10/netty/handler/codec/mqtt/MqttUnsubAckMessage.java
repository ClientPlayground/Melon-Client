package com.github.steveice10.netty.handler.codec.mqtt;

public final class MqttUnsubAckMessage extends MqttMessage {
  public MqttUnsubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader) {
    super(mqttFixedHeader, variableHeader, null);
  }
  
  public MqttMessageIdVariableHeader variableHeader() {
    return (MqttMessageIdVariableHeader)super.variableHeader();
  }
}
