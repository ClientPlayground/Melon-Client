package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.Collections;
import java.util.List;

public final class MqttSubscribePayload {
  private final List<MqttTopicSubscription> topicSubscriptions;
  
  public MqttSubscribePayload(List<MqttTopicSubscription> topicSubscriptions) {
    this.topicSubscriptions = Collections.unmodifiableList(topicSubscriptions);
  }
  
  public List<MqttTopicSubscription> topicSubscriptions() {
    return this.topicSubscriptions;
  }
  
  public String toString() {
    StringBuilder builder = (new StringBuilder(StringUtil.simpleClassName(this))).append('[');
    for (int i = 0; i < this.topicSubscriptions.size() - 1; i++)
      builder.append(this.topicSubscriptions.get(i)).append(", "); 
    builder.append(this.topicSubscriptions.get(this.topicSubscriptions.size() - 1));
    builder.append(']');
    return builder.toString();
  }
}
