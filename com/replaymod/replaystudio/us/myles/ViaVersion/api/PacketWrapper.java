package com.replaymod.replaystudio.us.myles.ViaVersion.api;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.CancelException;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.InformativeException;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.Direction;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.PipelineUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class PacketWrapper {
  public static final int PASSTHROUGH_ID = 1000;
  
  private final ByteBuf inputBuffer;
  
  private final UserConnection userConnection;
  
  private boolean send = true;
  
  private int id = -1;
  
  public void setId(int id) {
    this.id = id;
  }
  
  public int getId() {
    return this.id;
  }
  
  private final LinkedList<Pair<Type, Object>> readableObjects = new LinkedList<>();
  
  private final List<Pair<Type, Object>> packetValues = new ArrayList<>();
  
  public PacketWrapper(int packetID, ByteBuf inputBuffer, UserConnection userConnection) {
    this.id = packetID;
    this.inputBuffer = inputBuffer;
    this.userConnection = userConnection;
  }
  
  public <T> T get(Type<T> type, int index) throws Exception {
    int currentIndex = 0;
    for (Pair<Type, Object> packetValue : this.packetValues) {
      if (packetValue.getKey() == type) {
        if (currentIndex == index)
          return (T)packetValue.getValue(); 
        currentIndex++;
      } 
    } 
    Exception e = new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
    throw (new InformativeException(e)).set("Type", type.getTypeName()).set("Index", Integer.valueOf(index)).set("Packet ID", Integer.valueOf(getId())).set("Data", this.packetValues);
  }
  
  public boolean is(Type type, int index) {
    int currentIndex = 0;
    for (Pair<Type, Object> packetValue : this.packetValues) {
      if (packetValue.getKey() == type) {
        if (currentIndex == index)
          return true; 
        currentIndex++;
      } 
    } 
    return false;
  }
  
  public boolean isReadable(Type type, int index) {
    int currentIndex = 0;
    for (Pair<Type, Object> packetValue : this.readableObjects) {
      if (((Type)packetValue.getKey()).getBaseClass() == type.getBaseClass()) {
        if (currentIndex == index)
          return true; 
        currentIndex++;
      } 
    } 
    return false;
  }
  
  public <T> void set(Type<T> type, int index, T value) throws Exception {
    int currentIndex = 0;
    for (Pair<Type, Object> packetValue : this.packetValues) {
      if (packetValue.getKey() == type) {
        if (currentIndex == index) {
          packetValue.setValue(value);
          return;
        } 
        currentIndex++;
      } 
    } 
    Exception e = new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
    throw (new InformativeException(e)).set("Type", type.getTypeName()).set("Index", Integer.valueOf(index)).set("Packet ID", Integer.valueOf(getId()));
  }
  
  public <T> T read(Type<T> type) throws Exception {
    if (type == Type.NOTHING)
      return null; 
    if (this.readableObjects.isEmpty()) {
      Preconditions.checkNotNull(this.inputBuffer, "This packet does not have an input buffer.");
      try {
        return (T)type.read(this.inputBuffer);
      } catch (Exception exception) {
        throw (new InformativeException(exception)).set("Type", type.getTypeName()).set("Packet ID", Integer.valueOf(getId())).set("Data", this.packetValues);
      } 
    } 
    Pair<Type, Object> read = this.readableObjects.poll();
    Type rtype = read.getKey();
    if (rtype.equals(type) || (type.getBaseClass().equals(rtype.getBaseClass()) && type.getOutputClass().equals(rtype.getOutputClass())))
      return (T)read.getValue(); 
    if (rtype == Type.NOTHING)
      return read(type); 
    Exception e = new IOException("Unable to read type " + type.getTypeName() + ", found " + ((Type)read.getKey()).getTypeName());
    throw (new InformativeException(e)).set("Type", type.getTypeName()).set("Packet ID", Integer.valueOf(getId())).set("Data", this.packetValues);
  }
  
  public <T> void write(Type<T> type, T value) {
    if (value != null && 
      !type.getOutputClass().isAssignableFrom(value.getClass()))
      if (type instanceof TypeConverter) {
        value = (T)((TypeConverter)type).from(value);
      } else {
        Via.getPlatform().getLogger().warning("Possible type mismatch: " + value.getClass().getName() + " -> " + type.getOutputClass());
      }  
    this.packetValues.add(new Pair<>(type, value));
  }
  
  public <T> T passthrough(Type<T> type) throws Exception {
    T value = read(type);
    write(type, value);
    return value;
  }
  
  public void passthroughAll() throws Exception {
    this.packetValues.addAll(this.readableObjects);
    this.readableObjects.clear();
    if (this.inputBuffer.readableBytes() > 0)
      passthrough(Type.REMAINING_BYTES); 
  }
  
  public void writeToBuffer(ByteBuf buffer) throws Exception {
    if (this.id != -1)
      Type.VAR_INT.write(buffer, Integer.valueOf(this.id)); 
    if (this.readableObjects.size() > 0) {
      this.packetValues.addAll(this.readableObjects);
      this.readableObjects.clear();
    } 
    int index = 0;
    for (Pair<Type, Object> packetValue : this.packetValues) {
      try {
        Object value = packetValue.getValue();
        if (value != null && 
          !((Type)packetValue.getKey()).getOutputClass().isAssignableFrom(value.getClass()))
          if (packetValue.getKey() instanceof TypeConverter) {
            value = ((TypeConverter)packetValue.getKey()).from(value);
          } else {
            Via.getPlatform().getLogger().warning("Possible type mismatch: " + value.getClass().getName() + " -> " + ((Type)packetValue.getKey()).getOutputClass());
          }  
        ((Type)packetValue.getKey()).write(buffer, value);
      } catch (Exception e) {
        throw (new InformativeException(e)).set("Index", Integer.valueOf(index)).set("Type", ((Type)packetValue.getKey()).getTypeName()).set("Packet ID", Integer.valueOf(getId())).set("Data", this.packetValues);
      } 
      index++;
    } 
    writeRemaining(buffer);
  }
  
  public void clearInputBuffer() {
    if (this.inputBuffer != null)
      this.inputBuffer.clear(); 
    this.readableObjects.clear();
  }
  
  public void clearPacket() {
    clearInputBuffer();
    this.packetValues.clear();
  }
  
  private void writeRemaining(ByteBuf output) {
    if (this.inputBuffer != null)
      output.writeBytes(this.inputBuffer, this.inputBuffer.readableBytes()); 
  }
  
  public void send(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline) throws Exception {
    send(packetProtocol, skipCurrentPipeline, false);
  }
  
  public void send(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) throws Exception {
    if (!isCancelled())
      try {
        ByteBuf output = constructPacket(packetProtocol, skipCurrentPipeline, Direction.OUTGOING);
        user().sendRawPacket(output, currentThread);
      } catch (Exception e) {
        if (!PipelineUtil.containsCause(e, CancelException.class))
          throw e; 
      }  
  }
  
  private ByteBuf constructPacket(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, Direction direction) throws Exception {
    List<Protocol> protocols = new ArrayList<>(((ProtocolInfo)user().get(ProtocolInfo.class)).getPipeline().pipes());
    if (direction == Direction.OUTGOING)
      Collections.reverse(protocols); 
    int index = -1;
    for (int i = 0; i < protocols.size(); i++) {
      if (((Protocol)protocols.get(i)).getClass().equals(packetProtocol)) {
        index = skipCurrentPipeline ? (i + 1) : i;
        break;
      } 
    } 
    if (index == -1)
      throw new NoSuchElementException(packetProtocol.getCanonicalName()); 
    resetReader();
    apply(direction, ((ProtocolInfo)user().get(ProtocolInfo.class)).getState(), index, protocols);
    ByteBuf output = (this.inputBuffer == null) ? user().getChannel().alloc().buffer() : this.inputBuffer.alloc().buffer();
    writeToBuffer(output);
    return output;
  }
  
  public void send(Class<? extends Protocol> packetProtocol) throws Exception {
    send(packetProtocol, true);
  }
  
  public ChannelFuture sendFuture(Class<? extends Protocol> packetProtocol) throws Exception {
    if (!isCancelled()) {
      ByteBuf output = constructPacket(packetProtocol, true, Direction.OUTGOING);
      return user().sendRawPacketFuture(output);
    } 
    return user().getChannel().newFailedFuture(new Exception("Cancelled packet"));
  }
  
  @Deprecated
  public void send() throws Exception {
    if (!isCancelled()) {
      ByteBuf output = (this.inputBuffer == null) ? user().getChannel().alloc().buffer() : this.inputBuffer.alloc().buffer();
      writeToBuffer(output);
      user().sendRawPacket(output);
    } 
  }
  
  public PacketWrapper create(int packetID) {
    return new PacketWrapper(packetID, null, user());
  }
  
  public PacketWrapper create(int packetID, ValueCreator init) throws Exception {
    PacketWrapper wrapper = create(packetID);
    init.write(wrapper);
    return wrapper;
  }
  
  public PacketWrapper apply(Direction direction, State state, int index, List<Protocol> pipeline) throws Exception {
    for (int i = index; i < pipeline.size(); i++) {
      ((Protocol)pipeline.get(i)).transform(direction, state, this);
      resetReader();
    } 
    return this;
  }
  
  public void cancel() {
    this.send = false;
  }
  
  public boolean isCancelled() {
    return !this.send;
  }
  
  public UserConnection user() {
    return this.userConnection;
  }
  
  public void resetReader() {
    this.packetValues.addAll(this.readableObjects);
    this.readableObjects.clear();
    this.readableObjects.addAll(this.packetValues);
    this.packetValues.clear();
  }
  
  @Deprecated
  public void sendToServer() throws Exception {
    if (!isCancelled()) {
      ByteBuf output = (this.inputBuffer == null) ? user().getChannel().alloc().buffer() : this.inputBuffer.alloc().buffer();
      writeToBuffer(output);
      user().sendRawPacketToServer(output, true);
    } 
  }
  
  public void sendToServer(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) throws Exception {
    if (!isCancelled())
      try {
        ByteBuf output = constructPacket(packetProtocol, skipCurrentPipeline, Direction.INCOMING);
        user().sendRawPacketToServer(output, currentThread);
      } catch (Exception e) {
        if (!PipelineUtil.containsCause(e, CancelException.class))
          throw e; 
      }  
  }
  
  public void sendToServer(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline) throws Exception {
    sendToServer(packetProtocol, skipCurrentPipeline, false);
  }
  
  public void sendToServer(Class<? extends Protocol> packetProtocol) throws Exception {
    sendToServer(packetProtocol, true);
  }
  
  public String toString() {
    return "PacketWrapper{packetValues=" + this.packetValues + ", readableObjects=" + this.readableObjects + ", id=" + this.id + '}';
  }
}
