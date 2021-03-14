package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.internal.StringUtil;

public class MqttMessage {
  private final MqttFixedHeader mqttFixedHeader;
  
  private final Object variableHeader;
  
  private final Object payload;
  
  private final DecoderResult decoderResult;
  
  public MqttMessage(MqttFixedHeader mqttFixedHeader) {
    this(mqttFixedHeader, null, null);
  }
  
  public MqttMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader) {
    this(mqttFixedHeader, variableHeader, null);
  }
  
  public MqttMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload) {
    this(mqttFixedHeader, variableHeader, payload, DecoderResult.SUCCESS);
  }
  
  public MqttMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload, DecoderResult decoderResult) {
    this.mqttFixedHeader = mqttFixedHeader;
    this.variableHeader = variableHeader;
    this.payload = payload;
    this.decoderResult = decoderResult;
  }
  
  public MqttFixedHeader fixedHeader() {
    return this.mqttFixedHeader;
  }
  
  public Object variableHeader() {
    return this.variableHeader;
  }
  
  public Object payload() {
    return this.payload;
  }
  
  public DecoderResult decoderResult() {
    return this.decoderResult;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "fixedHeader=" + (
      (fixedHeader() != null) ? fixedHeader().toString() : "") + ", variableHeader=" + (
      (variableHeader() != null) ? this.variableHeader.toString() : "") + ", payload=" + (
      (payload() != null) ? this.payload.toString() : "") + ']';
  }
}
