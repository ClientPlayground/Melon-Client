package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.DecoderResult;

public final class MqttMessageFactory {
  public static MqttMessage newMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload) {
    switch (mqttFixedHeader.messageType()) {
      case CONNECT:
        return new MqttConnectMessage(mqttFixedHeader, (MqttConnectVariableHeader)variableHeader, (MqttConnectPayload)payload);
      case CONNACK:
        return new MqttConnAckMessage(mqttFixedHeader, (MqttConnAckVariableHeader)variableHeader);
      case SUBSCRIBE:
        return new MqttSubscribeMessage(mqttFixedHeader, (MqttMessageIdVariableHeader)variableHeader, (MqttSubscribePayload)payload);
      case SUBACK:
        return new MqttSubAckMessage(mqttFixedHeader, (MqttMessageIdVariableHeader)variableHeader, (MqttSubAckPayload)payload);
      case UNSUBACK:
        return new MqttUnsubAckMessage(mqttFixedHeader, (MqttMessageIdVariableHeader)variableHeader);
      case UNSUBSCRIBE:
        return new MqttUnsubscribeMessage(mqttFixedHeader, (MqttMessageIdVariableHeader)variableHeader, (MqttUnsubscribePayload)payload);
      case PUBLISH:
        return new MqttPublishMessage(mqttFixedHeader, (MqttPublishVariableHeader)variableHeader, (ByteBuf)payload);
      case PUBACK:
        return new MqttPubAckMessage(mqttFixedHeader, (MqttMessageIdVariableHeader)variableHeader);
      case PUBREC:
      case PUBREL:
      case PUBCOMP:
        return new MqttMessage(mqttFixedHeader, variableHeader);
      case PINGREQ:
      case PINGRESP:
      case DISCONNECT:
        return new MqttMessage(mqttFixedHeader);
    } 
    throw new IllegalArgumentException("unknown message type: " + mqttFixedHeader.messageType());
  }
  
  public static MqttMessage newInvalidMessage(Throwable cause) {
    return new MqttMessage(null, null, null, DecoderResult.failure(cause));
  }
}
