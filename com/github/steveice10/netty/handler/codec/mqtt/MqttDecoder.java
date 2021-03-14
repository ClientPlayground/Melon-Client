package com.github.steveice10.netty.handler.codec.mqtt;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.util.CharsetUtil;
import java.util.ArrayList;
import java.util.List;

public final class MqttDecoder extends ReplayingDecoder<MqttDecoder.DecoderState> {
  private static final int DEFAULT_MAX_BYTES_IN_MESSAGE = 8092;
  
  private MqttFixedHeader mqttFixedHeader;
  
  private Object variableHeader;
  
  private int bytesRemainingInVariablePart;
  
  private final int maxBytesInMessage;
  
  enum DecoderState {
    READ_FIXED_HEADER, READ_VARIABLE_HEADER, READ_PAYLOAD, BAD_MESSAGE;
  }
  
  public MqttDecoder() {
    this(8092);
  }
  
  public MqttDecoder(int maxBytesInMessage) {
    super(DecoderState.READ_FIXED_HEADER);
    this.maxBytesInMessage = maxBytesInMessage;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
    switch ((DecoderState)state()) {
      case CONNECT:
        try {
          this.mqttFixedHeader = decodeFixedHeader(buffer);
          this.bytesRemainingInVariablePart = this.mqttFixedHeader.remainingLength();
          checkpoint(DecoderState.READ_VARIABLE_HEADER);
        } catch (Exception cause) {
          out.add(invalidMessage(cause));
          return;
        } 
      case CONNACK:
        try {
          if (this.bytesRemainingInVariablePart > this.maxBytesInMessage)
            throw new DecoderException("too large message: " + this.bytesRemainingInVariablePart + " bytes"); 
          Result<?> decodedVariableHeader = decodeVariableHeader(buffer, this.mqttFixedHeader);
          this.variableHeader = decodedVariableHeader.value;
          this.bytesRemainingInVariablePart -= decodedVariableHeader.numberOfBytesConsumed;
          checkpoint(DecoderState.READ_PAYLOAD);
        } catch (Exception cause) {
          out.add(invalidMessage(cause));
          return;
        } 
      case SUBSCRIBE:
        try {
          Result<?> decodedPayload = decodePayload(buffer, this.mqttFixedHeader
              
              .messageType(), this.bytesRemainingInVariablePart, this.variableHeader);
          this.bytesRemainingInVariablePart -= decodedPayload.numberOfBytesConsumed;
          if (this.bytesRemainingInVariablePart != 0)
            throw new DecoderException("non-zero remaining payload bytes: " + this.bytesRemainingInVariablePart + " (" + this.mqttFixedHeader
                
                .messageType() + ')'); 
          checkpoint(DecoderState.READ_FIXED_HEADER);
          MqttMessage message = MqttMessageFactory.newMessage(this.mqttFixedHeader, this.variableHeader, decodedPayload
              .value);
          this.mqttFixedHeader = null;
          this.variableHeader = null;
          out.add(message);
        } catch (Exception cause) {
          out.add(invalidMessage(cause));
          return;
        } 
        return;
      case UNSUBSCRIBE:
        buffer.skipBytes(actualReadableBytes());
        return;
    } 
    throw new Error();
  }
  
  private MqttMessage invalidMessage(Throwable cause) {
    checkpoint(DecoderState.BAD_MESSAGE);
    return MqttMessageFactory.newInvalidMessage(cause);
  }
  
  private static MqttFixedHeader decodeFixedHeader(ByteBuf buffer) {
    short digit, b1 = buffer.readUnsignedByte();
    MqttMessageType messageType = MqttMessageType.valueOf(b1 >> 4);
    boolean dupFlag = ((b1 & 0x8) == 8);
    int qosLevel = (b1 & 0x6) >> 1;
    boolean retain = ((b1 & 0x1) != 0);
    int remainingLength = 0;
    int multiplier = 1;
    int loops = 0;
    do {
      digit = buffer.readUnsignedByte();
      remainingLength += (digit & 0x7F) * multiplier;
      multiplier *= 128;
      loops++;
    } while ((digit & 0x80) != 0 && loops < 4);
    if (loops == 4 && (digit & 0x80) != 0)
      throw new DecoderException("remaining length exceeds 4 digits (" + messageType + ')'); 
    MqttFixedHeader decodedFixedHeader = new MqttFixedHeader(messageType, dupFlag, MqttQoS.valueOf(qosLevel), retain, remainingLength);
    return MqttCodecUtil.validateFixedHeader(MqttCodecUtil.resetUnusedFields(decodedFixedHeader));
  }
  
  private static Result<?> decodeVariableHeader(ByteBuf buffer, MqttFixedHeader mqttFixedHeader) {
    switch (mqttFixedHeader.messageType()) {
      case CONNECT:
        return decodeConnectionVariableHeader(buffer);
      case CONNACK:
        return decodeConnAckVariableHeader(buffer);
      case SUBSCRIBE:
      case UNSUBSCRIBE:
      case SUBACK:
      case UNSUBACK:
      case PUBACK:
      case PUBREC:
      case PUBCOMP:
      case PUBREL:
        return decodeMessageIdVariableHeader(buffer);
      case PUBLISH:
        return decodePublishVariableHeader(buffer, mqttFixedHeader);
      case PINGREQ:
      case PINGRESP:
      case DISCONNECT:
        return new Result(null, 0);
    } 
    return new Result(null, 0);
  }
  
  private static Result<MqttConnectVariableHeader> decodeConnectionVariableHeader(ByteBuf buffer) {
    Result<String> protoString = decodeString(buffer);
    int numberOfBytesConsumed = protoString.numberOfBytesConsumed;
    byte protocolLevel = buffer.readByte();
    numberOfBytesConsumed++;
    MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel((String)protoString.value, protocolLevel);
    int b1 = buffer.readUnsignedByte();
    numberOfBytesConsumed++;
    Result<Integer> keepAlive = decodeMsbLsb(buffer);
    numberOfBytesConsumed += keepAlive.numberOfBytesConsumed;
    boolean hasUserName = ((b1 & 0x80) == 128);
    boolean hasPassword = ((b1 & 0x40) == 64);
    boolean willRetain = ((b1 & 0x20) == 32);
    int willQos = (b1 & 0x18) >> 3;
    boolean willFlag = ((b1 & 0x4) == 4);
    boolean cleanSession = ((b1 & 0x2) == 2);
    if (mqttVersion == MqttVersion.MQTT_3_1_1) {
      boolean zeroReservedFlag = ((b1 & 0x1) == 0);
      if (!zeroReservedFlag)
        throw new DecoderException("non-zero reserved flag"); 
    } 
    MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(mqttVersion.protocolName(), mqttVersion.protocolLevel(), hasUserName, hasPassword, willRetain, willQos, willFlag, cleanSession, ((Integer)keepAlive.value).intValue());
    return new Result<MqttConnectVariableHeader>(mqttConnectVariableHeader, numberOfBytesConsumed);
  }
  
  private static Result<MqttConnAckVariableHeader> decodeConnAckVariableHeader(ByteBuf buffer) {
    boolean sessionPresent = ((buffer.readUnsignedByte() & 0x1) == 1);
    byte returnCode = buffer.readByte();
    int numberOfBytesConsumed = 2;
    MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(MqttConnectReturnCode.valueOf(returnCode), sessionPresent);
    return new Result<MqttConnAckVariableHeader>(mqttConnAckVariableHeader, 2);
  }
  
  private static Result<MqttMessageIdVariableHeader> decodeMessageIdVariableHeader(ByteBuf buffer) {
    Result<Integer> messageId = decodeMessageId(buffer);
    return new Result<MqttMessageIdVariableHeader>(
        MqttMessageIdVariableHeader.from(((Integer)messageId.value).intValue()), messageId
        .numberOfBytesConsumed);
  }
  
  private static Result<MqttPublishVariableHeader> decodePublishVariableHeader(ByteBuf buffer, MqttFixedHeader mqttFixedHeader) {
    Result<String> decodedTopic = decodeString(buffer);
    if (!MqttCodecUtil.isValidPublishTopicName((String)decodedTopic.value))
      throw new DecoderException("invalid publish topic name: " + (String)decodedTopic.value + " (contains wildcards)"); 
    int numberOfBytesConsumed = decodedTopic.numberOfBytesConsumed;
    int messageId = -1;
    if (mqttFixedHeader.qosLevel().value() > 0) {
      Result<Integer> decodedMessageId = decodeMessageId(buffer);
      messageId = ((Integer)decodedMessageId.value).intValue();
      numberOfBytesConsumed += decodedMessageId.numberOfBytesConsumed;
    } 
    MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader((String)decodedTopic.value, messageId);
    return new Result<MqttPublishVariableHeader>(mqttPublishVariableHeader, numberOfBytesConsumed);
  }
  
  private static Result<Integer> decodeMessageId(ByteBuf buffer) {
    Result<Integer> messageId = decodeMsbLsb(buffer);
    if (!MqttCodecUtil.isValidMessageId(((Integer)messageId.value).intValue()))
      throw new DecoderException("invalid messageId: " + messageId.value); 
    return messageId;
  }
  
  private static Result<?> decodePayload(ByteBuf buffer, MqttMessageType messageType, int bytesRemainingInVariablePart, Object variableHeader) {
    switch (messageType) {
      case CONNECT:
        return decodeConnectionPayload(buffer, (MqttConnectVariableHeader)variableHeader);
      case SUBSCRIBE:
        return decodeSubscribePayload(buffer, bytesRemainingInVariablePart);
      case SUBACK:
        return decodeSubackPayload(buffer, bytesRemainingInVariablePart);
      case UNSUBSCRIBE:
        return decodeUnsubscribePayload(buffer, bytesRemainingInVariablePart);
      case PUBLISH:
        return decodePublishPayload(buffer, bytesRemainingInVariablePart);
    } 
    return new Result(null, 0);
  }
  
  private static Result<MqttConnectPayload> decodeConnectionPayload(ByteBuf buffer, MqttConnectVariableHeader mqttConnectVariableHeader) {
    Result<String> decodedClientId = decodeString(buffer);
    String decodedClientIdValue = (String)decodedClientId.value;
    MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(mqttConnectVariableHeader.name(), 
        (byte)mqttConnectVariableHeader.version());
    if (!MqttCodecUtil.isValidClientId(mqttVersion, decodedClientIdValue))
      throw new MqttIdentifierRejectedException("invalid clientIdentifier: " + decodedClientIdValue); 
    int numberOfBytesConsumed = decodedClientId.numberOfBytesConsumed;
    Result<String> decodedWillTopic = null;
    Result<byte[]> decodedWillMessage = null;
    if (mqttConnectVariableHeader.isWillFlag()) {
      decodedWillTopic = decodeString(buffer, 0, 32767);
      numberOfBytesConsumed += decodedWillTopic.numberOfBytesConsumed;
      decodedWillMessage = decodeByteArray(buffer);
      numberOfBytesConsumed += decodedWillMessage.numberOfBytesConsumed;
    } 
    Result<String> decodedUserName = null;
    Result<byte[]> decodedPassword = null;
    if (mqttConnectVariableHeader.hasUserName()) {
      decodedUserName = decodeString(buffer);
      numberOfBytesConsumed += decodedUserName.numberOfBytesConsumed;
    } 
    if (mqttConnectVariableHeader.hasPassword()) {
      decodedPassword = decodeByteArray(buffer);
      numberOfBytesConsumed += decodedPassword.numberOfBytesConsumed;
    } 
    MqttConnectPayload mqttConnectPayload = new MqttConnectPayload((String)decodedClientId.value, (decodedWillTopic != null) ? (String)decodedWillTopic.value : null, (decodedWillMessage != null) ? (byte[])decodedWillMessage.value : null, (decodedUserName != null) ? (String)decodedUserName.value : null, (decodedPassword != null) ? (byte[])decodedPassword.value : null);
    return new Result<MqttConnectPayload>(mqttConnectPayload, numberOfBytesConsumed);
  }
  
  private static Result<MqttSubscribePayload> decodeSubscribePayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
    List<MqttTopicSubscription> subscribeTopics = new ArrayList<MqttTopicSubscription>();
    int numberOfBytesConsumed = 0;
    while (numberOfBytesConsumed < bytesRemainingInVariablePart) {
      Result<String> decodedTopicName = decodeString(buffer);
      numberOfBytesConsumed += decodedTopicName.numberOfBytesConsumed;
      int qos = buffer.readUnsignedByte() & 0x3;
      numberOfBytesConsumed++;
      subscribeTopics.add(new MqttTopicSubscription((String)decodedTopicName.value, MqttQoS.valueOf(qos)));
    } 
    return new Result<MqttSubscribePayload>(new MqttSubscribePayload(subscribeTopics), numberOfBytesConsumed);
  }
  
  private static Result<MqttSubAckPayload> decodeSubackPayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
    List<Integer> grantedQos = new ArrayList<Integer>();
    int numberOfBytesConsumed = 0;
    while (numberOfBytesConsumed < bytesRemainingInVariablePart) {
      int qos = buffer.readUnsignedByte();
      if (qos != MqttQoS.FAILURE.value())
        qos &= 0x3; 
      numberOfBytesConsumed++;
      grantedQos.add(Integer.valueOf(qos));
    } 
    return new Result<MqttSubAckPayload>(new MqttSubAckPayload(grantedQos), numberOfBytesConsumed);
  }
  
  private static Result<MqttUnsubscribePayload> decodeUnsubscribePayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
    List<String> unsubscribeTopics = new ArrayList<String>();
    int numberOfBytesConsumed = 0;
    while (numberOfBytesConsumed < bytesRemainingInVariablePart) {
      Result<String> decodedTopicName = decodeString(buffer);
      numberOfBytesConsumed += decodedTopicName.numberOfBytesConsumed;
      unsubscribeTopics.add(decodedTopicName.value);
    } 
    return new Result<MqttUnsubscribePayload>(new MqttUnsubscribePayload(unsubscribeTopics), numberOfBytesConsumed);
  }
  
  private static Result<ByteBuf> decodePublishPayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
    ByteBuf b = buffer.readRetainedSlice(bytesRemainingInVariablePart);
    return new Result<ByteBuf>(b, bytesRemainingInVariablePart);
  }
  
  private static Result<String> decodeString(ByteBuf buffer) {
    return decodeString(buffer, 0, 2147483647);
  }
  
  private static Result<String> decodeString(ByteBuf buffer, int minBytes, int maxBytes) {
    Result<Integer> decodedSize = decodeMsbLsb(buffer);
    int size = ((Integer)decodedSize.value).intValue();
    int numberOfBytesConsumed = decodedSize.numberOfBytesConsumed;
    if (size < minBytes || size > maxBytes) {
      buffer.skipBytes(size);
      numberOfBytesConsumed += size;
      return new Result<String>(null, numberOfBytesConsumed);
    } 
    String s = buffer.toString(buffer.readerIndex(), size, CharsetUtil.UTF_8);
    buffer.skipBytes(size);
    numberOfBytesConsumed += size;
    return new Result<String>(s, numberOfBytesConsumed);
  }
  
  private static Result<byte[]> decodeByteArray(ByteBuf buffer) {
    Result<Integer> decodedSize = decodeMsbLsb(buffer);
    int size = ((Integer)decodedSize.value).intValue();
    byte[] bytes = new byte[size];
    buffer.readBytes(bytes);
    return (Result)new Result<byte>(bytes, decodedSize.numberOfBytesConsumed + size);
  }
  
  private static Result<Integer> decodeMsbLsb(ByteBuf buffer) {
    return decodeMsbLsb(buffer, 0, 65535);
  }
  
  private static Result<Integer> decodeMsbLsb(ByteBuf buffer, int min, int max) {
    short msbSize = buffer.readUnsignedByte();
    short lsbSize = buffer.readUnsignedByte();
    int numberOfBytesConsumed = 2;
    int result = msbSize << 8 | lsbSize;
    if (result < min || result > max)
      result = -1; 
    return new Result<Integer>(Integer.valueOf(result), 2);
  }
  
  private static final class Result<T> {
    private final T value;
    
    private final int numberOfBytesConsumed;
    
    Result(T value, int numberOfBytesConsumed) {
      this.value = value;
      this.numberOfBytesConsumed = numberOfBytesConsumed;
    }
  }
}
