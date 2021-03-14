package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.util.internal.StringUtil;

public final class MqttTopicSubscription {
  private final String topicFilter;
  
  private final MqttQoS qualityOfService;
  
  public MqttTopicSubscription(String topicFilter, MqttQoS qualityOfService) {
    this.topicFilter = topicFilter;
    this.qualityOfService = qualityOfService;
  }
  
  public String topicName() {
    return this.topicFilter;
  }
  
  public MqttQoS qualityOfService() {
    return this.qualityOfService;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "topicFilter=" + 
      this.topicFilter + ", qualityOfService=" + 
      this.qualityOfService + ']';
  }
}
