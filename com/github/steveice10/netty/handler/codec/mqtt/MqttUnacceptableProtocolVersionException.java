package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.handler.codec.DecoderException;

public final class MqttUnacceptableProtocolVersionException extends DecoderException {
  private static final long serialVersionUID = 4914652213232455749L;
  
  public MqttUnacceptableProtocolVersionException() {}
  
  public MqttUnacceptableProtocolVersionException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public MqttUnacceptableProtocolVersionException(String message) {
    super(message);
  }
  
  public MqttUnacceptableProtocolVersionException(Throwable cause) {
    super(cause);
  }
}
