package com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.ViaProviders;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.CancelException;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.Direction;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class Protocol {
  private final Map<Pair<State, Integer>, ProtocolPacket> incoming = new HashMap<>();
  
  private final Map<Pair<State, Integer>, ProtocolPacket> outgoing = new HashMap<>();
  
  public Protocol() {
    registerPackets();
  }
  
  public boolean isFiltered(Class packetClass) {
    return false;
  }
  
  protected void filterPacket(UserConnection info, Object packet, List<Object> output) throws Exception {
    output.add(packet);
  }
  
  @Deprecated
  protected void registerListeners() {}
  
  protected void register(ViaProviders providers) {}
  
  protected abstract void registerPackets();
  
  public abstract void init(UserConnection paramUserConnection);
  
  public void registerIncoming(State state, int oldPacketID, int newPacketID) {
    registerIncoming(state, oldPacketID, newPacketID, null);
  }
  
  public void registerIncoming(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
    registerIncoming(state, oldPacketID, newPacketID, packetRemapper, false);
  }
  
  public void registerIncoming(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override) {
    ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
    Pair<State, Integer> pair = new Pair(state, Integer.valueOf(newPacketID));
    if (!override && this.incoming.containsKey(pair))
      Via.getPlatform().getLogger().log(Level.WARNING, pair + " already registered! If this override is intentional, set override to true. Stacktrace: ", new Exception()); 
    this.incoming.put(pair, protocolPacket);
  }
  
  public void registerOutgoing(State state, int oldPacketID, int newPacketID) {
    registerOutgoing(state, oldPacketID, newPacketID, null);
  }
  
  public void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper) {
    registerOutgoing(state, oldPacketID, newPacketID, packetRemapper, false);
  }
  
  public void registerOutgoing(State state, int oldPacketID, int newPacketID, PacketRemapper packetRemapper, boolean override) {
    ProtocolPacket protocolPacket = new ProtocolPacket(state, oldPacketID, newPacketID, packetRemapper);
    Pair<State, Integer> pair = new Pair(state, Integer.valueOf(oldPacketID));
    if (!override && this.outgoing.containsKey(pair))
      Via.getPlatform().getLogger().log(Level.WARNING, pair + " already registered! If override is intentional, set override to true. Stacktrace: ", new Exception()); 
    this.outgoing.put(pair, protocolPacket);
  }
  
  public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
    Pair<State, Integer> statePacket = new Pair(state, Integer.valueOf(packetWrapper.getId()));
    Map<Pair<State, Integer>, ProtocolPacket> packetMap = (direction == Direction.OUTGOING) ? this.outgoing : this.incoming;
    ProtocolPacket protocolPacket = packetMap.get(statePacket);
    if (protocolPacket == null)
      return; 
    int newID = (direction == Direction.OUTGOING) ? protocolPacket.getNewID() : protocolPacket.getOldID();
    packetWrapper.setId(newID);
    if (protocolPacket.getRemapper() != null) {
      protocolPacket.getRemapper().remap(packetWrapper);
      if (packetWrapper.isCancelled())
        throw Via.getManager().isDebug() ? new CancelException() : CancelException.CACHED; 
    } 
  }
  
  public String toString() {
    return "Protocol:" + getClass().getSimpleName();
  }
  
  class ProtocolPacket {
    State state;
    
    int oldID;
    
    int newID;
    
    PacketRemapper remapper;
    
    public ProtocolPacket(State state, int oldID, int newID, PacketRemapper remapper) {
      this.state = state;
      this.oldID = oldID;
      this.newID = newID;
      this.remapper = remapper;
    }
    
    public State getState() {
      return this.state;
    }
    
    public int getOldID() {
      return this.oldID;
    }
    
    public int getNewID() {
      return this.newID;
    }
    
    public PacketRemapper getRemapper() {
      return this.remapper;
    }
  }
}
