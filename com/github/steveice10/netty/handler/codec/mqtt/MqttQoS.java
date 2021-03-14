package com.github.steveice10.netty.handler.codec.mqtt;

public enum MqttQoS {
  AT_MOST_ONCE(0),
  AT_LEAST_ONCE(1),
  EXACTLY_ONCE(2),
  FAILURE(128);
  
  private final int value;
  
  MqttQoS(int value) {
    this.value = value;
  }
  
  public int value() {
    return this.value;
  }
}
