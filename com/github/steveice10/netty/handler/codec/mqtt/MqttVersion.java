package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public enum MqttVersion {
  MQTT_3_1("MQIsdp", (byte)3),
  MQTT_3_1_1("MQTT", (byte)4);
  
  private final String name;
  
  private final byte level;
  
  MqttVersion(String protocolName, byte protocolLevel) {
    this.name = (String)ObjectUtil.checkNotNull(protocolName, "protocolName");
    this.level = protocolLevel;
  }
  
  public String protocolName() {
    return this.name;
  }
  
  public byte[] protocolNameBytes() {
    return this.name.getBytes(CharsetUtil.UTF_8);
  }
  
  public byte protocolLevel() {
    return this.level;
  }
  
  public static MqttVersion fromProtocolNameAndLevel(String protocolName, byte protocolLevel) {
    for (MqttVersion mv : values()) {
      if (mv.name.equals(protocolName)) {
        if (mv.level == protocolLevel)
          return mv; 
        throw new MqttUnacceptableProtocolVersionException(protocolName + " and " + protocolLevel + " are not match");
      } 
    } 
    throw new MqttUnacceptableProtocolVersionException(protocolName + "is unknown protocol name");
  }
}
