package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.storage;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_11Types;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ExternalJoinGameListener {
  private final Map<Integer, Entity1_11Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
  
  private final Set<Integer> holograms = Sets.newConcurrentHashSet();
  
  public EntityTracker(UserConnection user) {
    super(user);
  }
  
  public void removeEntity(int entityId) {
    this.clientEntityTypes.remove(Integer.valueOf(entityId));
    if (isHologram(entityId))
      removeHologram(entityId); 
  }
  
  public void addEntity(int entityId, Entity1_11Types.EntityType type) {
    this.clientEntityTypes.put(Integer.valueOf(entityId), type);
  }
  
  public boolean has(int entityId) {
    return this.clientEntityTypes.containsKey(Integer.valueOf(entityId));
  }
  
  public Optional<Entity1_11Types.EntityType> get(int id) {
    return Optional.fromNullable(this.clientEntityTypes.get(Integer.valueOf(id)));
  }
  
  public void addHologram(int entId) {
    this.holograms.add(Integer.valueOf(entId));
  }
  
  public boolean isHologram(int entId) {
    return this.holograms.contains(Integer.valueOf(entId));
  }
  
  public void removeHologram(int entId) {
    this.holograms.remove(Integer.valueOf(entId));
  }
  
  public void onExternalJoinGame(int playerEntityId) {
    this.clientEntityTypes.put(Integer.valueOf(playerEntityId), Entity1_11Types.EntityType.PLAYER);
  }
}
