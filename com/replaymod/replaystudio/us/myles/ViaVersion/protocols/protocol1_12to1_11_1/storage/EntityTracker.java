package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_12Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ExternalJoinGameListener {
  private final Map<Integer, Entity1_12Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
  
  public EntityTracker(UserConnection user) {
    super(user);
  }
  
  public void removeEntity(int entityId) {
    this.clientEntityTypes.remove(Integer.valueOf(entityId));
  }
  
  public void addEntity(int entityId, Entity1_12Types.EntityType type) {
    this.clientEntityTypes.put(Integer.valueOf(entityId), type);
  }
  
  public boolean has(int entityId) {
    return this.clientEntityTypes.containsKey(Integer.valueOf(entityId));
  }
  
  public Optional<Entity1_12Types.EntityType> get(int id) {
    return Optional.fromNullable(this.clientEntityTypes.get(Integer.valueOf(id)));
  }
  
  public void onExternalJoinGame(int playerEntityId) {
    this.clientEntityTypes.put(Integer.valueOf(playerEntityId), Entity1_12Types.EntityType.PLAYER);
  }
}
