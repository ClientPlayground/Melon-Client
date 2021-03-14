package com.github.steveice10.netty.handler.codec.mqtt;

public enum MqttMessageType {
  CONNECT(1),
  CONNACK(2),
  PUBLISH(3),
  PUBACK(4),
  PUBREC(5),
  PUBREL(6),
  PUBCOMP(7),
  SUBSCRIBE(8),
  SUBACK(9),
  UNSUBSCRIBE(10),
  UNSUBACK(11),
  PINGREQ(12),
  PINGRESP(13),
  DISCONNECT(14);
  
  private final int value;
  
  MqttMessageType(int value) {
    this.value = value;
  }
  
  public int value() {
    return this.value;
  }
}
