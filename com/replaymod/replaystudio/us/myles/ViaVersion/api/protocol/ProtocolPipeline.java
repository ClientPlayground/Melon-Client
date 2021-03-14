package com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.ViaPlatform;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.Direction;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.PacketType;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ProtocolPipeline extends Protocol {
  private List<Protocol> protocolList;
  
  private UserConnection userConnection;
  
  public ProtocolPipeline(UserConnection userConnection) {
    init(userConnection);
  }
  
  protected void registerPackets() {
    this.protocolList = new CopyOnWriteArrayList<>();
    this.protocolList.add(ProtocolRegistry.BASE_PROTOCOL);
  }
  
  public void init(UserConnection userConnection) {
    this.userConnection = userConnection;
    ProtocolInfo protocolInfo = new ProtocolInfo(userConnection);
    protocolInfo.setPipeline(this);
    userConnection.put((StoredObject)protocolInfo);
    for (Protocol protocol : this.protocolList)
      protocol.init(userConnection); 
  }
  
  public void add(Protocol protocol) {
    if (this.protocolList != null) {
      this.protocolList.add(protocol);
      protocol.init(this.userConnection);
      List<Protocol> toMove = new ArrayList<>();
      for (Protocol p : this.protocolList) {
        if (ProtocolRegistry.isBaseProtocol(p))
          toMove.add(p); 
      } 
      this.protocolList.removeAll(toMove);
      this.protocolList.addAll(toMove);
    } else {
      throw new NullPointerException("Tried to add protocol to early");
    } 
  }
  
  public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
    int originalID = packetWrapper.getId();
    List<Protocol> protocols = new ArrayList<>(this.protocolList);
    if (direction == Direction.OUTGOING)
      Collections.reverse(protocols); 
    packetWrapper.apply(direction, state, 0, protocols);
    super.transform(direction, state, packetWrapper);
    if (Via.getManager().isDebug()) {
      String packet = "UNKNOWN";
      int serverProtocol = ((ProtocolInfo)this.userConnection.get(ProtocolInfo.class)).getServerProtocolVersion();
      int clientProtocol = ((ProtocolInfo)this.userConnection.get(ProtocolInfo.class)).getProtocolVersion();
      if (serverProtocol >= ProtocolVersion.v1_8.getId() && serverProtocol <= ProtocolVersion.v1_9_3
        .getId()) {
        PacketType type;
        if (serverProtocol <= ProtocolVersion.v1_8.getId()) {
          if (direction == Direction.INCOMING) {
            type = PacketType.findNewPacket(state, direction, originalID);
          } else {
            type = PacketType.findOldPacket(state, direction, originalID);
          } 
        } else {
          type = PacketType.findNewPacket(state, direction, originalID);
        } 
        if (type != null) {
          if (type == PacketType.PLAY_CHUNK_DATA)
            return; 
          if (type == PacketType.PLAY_TIME_UPDATE)
            return; 
          if (type == PacketType.PLAY_KEEP_ALIVE)
            return; 
          if (type == PacketType.PLAY_KEEP_ALIVE_REQUEST)
            return; 
          if (type == PacketType.PLAY_ENTITY_LOOK_MOVE)
            return; 
          if (type == PacketType.PLAY_ENTITY_LOOK)
            return; 
          if (type == PacketType.PLAY_ENTITY_RELATIVE_MOVE)
            return; 
          if (type == PacketType.PLAY_PLAYER_POSITION_LOOK_REQUEST)
            return; 
          if (type == PacketType.PLAY_PLAYER_LOOK_REQUEST)
            return; 
          if (type == PacketType.PLAY_PLAYER_POSITION_REQUEST)
            return; 
          packet = type.name();
        } 
      } 
      String name = packet + "[" + clientProtocol + "]";
      ViaPlatform platform = Via.getPlatform();
      String actualUsername = ((ProtocolInfo)packetWrapper.user().get(ProtocolInfo.class)).getUsername();
      String username = (actualUsername != null) ? (actualUsername + " ") : "";
      platform.getLogger().log(Level.INFO, "{0}{1}: {2} {3} -> {4} [{5}] Value: {6}", new Object[] { username, direction, state, 
            
            Integer.valueOf(originalID), 
            Integer.valueOf(packetWrapper.getId()), name, packetWrapper });
    } 
  }
  
  public boolean contains(Class<? extends Protocol> pipeClass) {
    for (Protocol protocol : this.protocolList) {
      if (protocol.getClass().equals(pipeClass))
        return true; 
    } 
    return false;
  }
  
  public boolean filter(Object o, List list) throws Exception {
    for (Protocol protocol : this.protocolList) {
      if (protocol.isFiltered(o.getClass())) {
        protocol.filterPacket(this.userConnection, o, list);
        return true;
      } 
    } 
    return false;
  }
  
  public List<Protocol> pipes() {
    return this.protocolList;
  }
  
  public void cleanPipes() {
    pipes().clear();
    registerPackets();
  }
}
