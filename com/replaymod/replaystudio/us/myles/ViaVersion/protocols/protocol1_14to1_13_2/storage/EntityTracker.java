package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_14Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ExternalJoinGameListener {
  private final Map<Integer, Entity1_14Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
  
  private final Map<Integer, Byte> insentientData = new ConcurrentHashMap<>();
  
  private final Map<Integer, Byte> sleepingAndRiptideData = new ConcurrentHashMap<>();
  
  private final Map<Integer, Byte> playerEntityFlags = new ConcurrentHashMap<>();
  
  private int latestTradeWindowId;
  
  private int clientEntityId;
  
  public int getLatestTradeWindowId() {
    return this.latestTradeWindowId;
  }
  
  public void setLatestTradeWindowId(int latestTradeWindowId) {
    this.latestTradeWindowId = latestTradeWindowId;
  }
  
  public int getClientEntityId() {
    return this.clientEntityId;
  }
  
  public void setClientEntityId(int clientEntityId) {
    this.clientEntityId = clientEntityId;
  }
  
  private boolean forceSendCenterChunk = true;
  
  private int chunkCenterX;
  
  private int chunkCenterZ;
  
  public boolean isForceSendCenterChunk() {
    return this.forceSendCenterChunk;
  }
  
  public void setForceSendCenterChunk(boolean forceSendCenterChunk) {
    this.forceSendCenterChunk = forceSendCenterChunk;
  }
  
  public int getChunkCenterX() {
    return this.chunkCenterX;
  }
  
  public int getChunkCenterZ() {
    return this.chunkCenterZ;
  }
  
  public void setChunkCenterX(int chunkCenterX) {
    this.chunkCenterX = chunkCenterX;
  }
  
  public void setChunkCenterZ(int chunkCenterZ) {
    this.chunkCenterZ = chunkCenterZ;
  }
  
  public EntityTracker(UserConnection user) {
    super(user);
  }
  
  public void removeEntity(int entityId) {
    this.clientEntityTypes.remove(Integer.valueOf(entityId));
    this.insentientData.remove(Integer.valueOf(entityId));
    this.sleepingAndRiptideData.remove(Integer.valueOf(entityId));
    this.playerEntityFlags.remove(Integer.valueOf(entityId));
  }
  
  public void addEntity(int entityId, Entity1_14Types.EntityType type) {
    this.clientEntityTypes.put(Integer.valueOf(entityId), type);
  }
  
  public byte getInsentientData(int entity) {
    Byte val = this.insentientData.get(Integer.valueOf(entity));
    return (val == null) ? 0 : val.byteValue();
  }
  
  public void setInsentientData(int entity, byte value) {
    this.insentientData.put(Integer.valueOf(entity), Byte.valueOf(value));
  }
  
  private static byte zeroIfNull(Byte val) {
    if (val == null)
      return 0; 
    return val.byteValue();
  }
  
  public boolean isSleeping(int player) {
    return ((zeroIfNull(this.sleepingAndRiptideData.get(Integer.valueOf(player))) & 0x1) != 0);
  }
  
  public void setSleeping(int player, boolean value) {
    byte newValue = (byte)(zeroIfNull(this.sleepingAndRiptideData.get(Integer.valueOf(player))) & 0xFFFFFFFE | (value ? 1 : 0));
    if (newValue == 0) {
      this.sleepingAndRiptideData.remove(Integer.valueOf(player));
    } else {
      this.sleepingAndRiptideData.put(Integer.valueOf(player), Byte.valueOf(newValue));
    } 
  }
  
  public boolean isRiptide(int player) {
    return ((zeroIfNull(this.sleepingAndRiptideData.get(Integer.valueOf(player))) & 0x2) != 0);
  }
  
  public void setRiptide(int player, boolean value) {
    byte newValue = (byte)(zeroIfNull(this.sleepingAndRiptideData.get(Integer.valueOf(player))) & 0xFFFFFFFD | (value ? 2 : 0));
    if (newValue == 0) {
      this.sleepingAndRiptideData.remove(Integer.valueOf(player));
    } else {
      this.sleepingAndRiptideData.put(Integer.valueOf(player), Byte.valueOf(newValue));
    } 
  }
  
  public boolean has(int entityId) {
    return this.clientEntityTypes.containsKey(Integer.valueOf(entityId));
  }
  
  public Optional<Entity1_14Types.EntityType> get(int id) {
    return Optional.fromNullable(this.clientEntityTypes.get(Integer.valueOf(id)));
  }
  
  public void onExternalJoinGame(int playerEntityId) {
    this.clientEntityId = playerEntityId;
    this.clientEntityTypes.put(Integer.valueOf(playerEntityId), Entity1_14Types.EntityType.PLAYER);
    PacketWrapper setViewDistance = new PacketWrapper(65, null, getUser());
    setViewDistance.write(Type.VAR_INT, Integer.valueOf(64));
    try {
      setViewDistance.send(Protocol1_14To1_13_2.class, true, true);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public byte getEntityFlags(int player) {
    return zeroIfNull(this.playerEntityFlags.get(Integer.valueOf(player)));
  }
  
  public void setEntityFlags(int player, byte data) {
    this.playerEntityFlags.put(Integer.valueOf(player), Byte.valueOf(data));
  }
}
