package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;

public class MqttPublishMessage extends MqttMessage implements ByteBufHolder {
  public MqttPublishMessage(MqttFixedHeader mqttFixedHeader, MqttPublishVariableHeader variableHeader, ByteBuf payload) {
    super(mqttFixedHeader, variableHeader, payload);
  }
  
  public MqttPublishVariableHeader variableHeader() {
    return (MqttPublishVariableHeader)super.variableHeader();
  }
  
  public ByteBuf payload() {
    return content();
  }
  
  public ByteBuf content() {
    ByteBuf data = (ByteBuf)super.payload();
    if (data.refCnt() <= 0)
      throw new IllegalReferenceCountException(data.refCnt()); 
    return data;
  }
  
  public MqttPublishMessage copy() {
    return replace(content().copy());
  }
  
  public MqttPublishMessage duplicate() {
    return replace(content().duplicate());
  }
  
  public MqttPublishMessage retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public MqttPublishMessage replace(ByteBuf content) {
    return new MqttPublishMessage(fixedHeader(), variableHeader(), content);
  }
  
  public int refCnt() {
    return content().refCnt();
  }
  
  public MqttPublishMessage retain() {
    content().retain();
    return this;
  }
  
  public MqttPublishMessage retain(int increment) {
    content().retain(increment);
    return this;
  }
  
  public MqttPublishMessage touch() {
    content().touch();
    return this;
  }
  
  public MqttPublishMessage touch(Object hint) {
    content().touch(hint);
    return this;
  }
  
  public boolean release() {
    return content().release();
  }
  
  public boolean release(int decrement) {
    return content().release(decrement);
  }
}
