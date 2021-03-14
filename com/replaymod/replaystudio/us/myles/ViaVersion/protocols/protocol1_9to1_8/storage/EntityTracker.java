package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossBar;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossColor;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossStyle;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_10Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.chat.GameMode;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.MetadataRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EntityTracker extends StoredObject implements ExternalJoinGameListener {
  private final Map<Integer, UUID> uuidMap = new ConcurrentHashMap<>();
  
  public Map<Integer, UUID> getUuidMap() {
    return this.uuidMap;
  }
  
  private final Map<Integer, Entity1_10Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
  
  public Map<Integer, Entity1_10Types.EntityType> getClientEntityTypes() {
    return this.clientEntityTypes;
  }
  
  private final Map<Integer, List<Metadata>> metadataBuffer = new ConcurrentHashMap<>();
  
  public Map<Integer, List<Metadata>> getMetadataBuffer() {
    return this.metadataBuffer;
  }
  
  private final Map<Integer, Integer> vehicleMap = new ConcurrentHashMap<>();
  
  public Map<Integer, Integer> getVehicleMap() {
    return this.vehicleMap;
  }
  
  private final Map<Integer, BossBar> bossBarMap = new ConcurrentHashMap<>();
  
  public Map<Integer, BossBar> getBossBarMap() {
    return this.bossBarMap;
  }
  
  private final Set<Integer> validBlocking = Sets.newConcurrentHashSet();
  
  public Set<Integer> getValidBlocking() {
    return this.validBlocking;
  }
  
  private final Set<Integer> knownHolograms = Sets.newConcurrentHashSet();
  
  public Set<Integer> getKnownHolograms() {
    return this.knownHolograms;
  }
  
  private final Cache<Position, Integer> blockInteractions = CacheBuilder.newBuilder().maximumSize(10L).expireAfterAccess(250L, TimeUnit.MILLISECONDS).build();
  
  public Cache<Position, Integer> getBlockInteractions() {
    return this.blockInteractions;
  }
  
  private boolean blocking = false;
  
  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }
  
  public boolean isBlocking() {
    return this.blocking;
  }
  
  private boolean autoTeam = false;
  
  public void setAutoTeam(boolean autoTeam) {
    this.autoTeam = autoTeam;
  }
  
  public boolean isAutoTeam() {
    return this.autoTeam;
  }
  
  private int entityID = -1;
  
  public void setEntityID(int entityID) {
    this.entityID = entityID;
  }
  
  public int getEntityID() {
    return this.entityID;
  }
  
  private Position currentlyDigging = null;
  
  public void setCurrentlyDigging(Position currentlyDigging) {
    this.currentlyDigging = currentlyDigging;
  }
  
  public Position getCurrentlyDigging() {
    return this.currentlyDigging;
  }
  
  private boolean teamExists = false;
  
  private GameMode gameMode;
  
  private String currentTeam;
  
  public boolean isTeamExists() {
    return this.teamExists;
  }
  
  public void setGameMode(GameMode gameMode) {
    this.gameMode = gameMode;
  }
  
  public GameMode getGameMode() {
    return this.gameMode;
  }
  
  public void setCurrentTeam(String currentTeam) {
    this.currentTeam = currentTeam;
  }
  
  public String getCurrentTeam() {
    return this.currentTeam;
  }
  
  public EntityTracker(UserConnection user) {
    super(user);
  }
  
  public UUID getEntityUUID(int id) {
    UUID uuid = this.uuidMap.get(Integer.valueOf(id));
    if (uuid == null) {
      uuid = UUID.randomUUID();
      this.uuidMap.put(Integer.valueOf(id), uuid);
    } 
    return uuid;
  }
  
  public void setSecondHand(Item item) {
    setSecondHand(this.entityID, item);
  }
  
  public void setSecondHand(int entityID, Item item) {
    PacketWrapper wrapper = new PacketWrapper(60, null, getUser());
    wrapper.write(Type.VAR_INT, Integer.valueOf(entityID));
    wrapper.write(Type.VAR_INT, Integer.valueOf(1));
    wrapper.write(Type.ITEM, item);
    try {
      wrapper.send(Protocol1_9To1_8.class);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public void removeEntity(Integer entityID) {
    this.clientEntityTypes.remove(entityID);
    this.vehicleMap.remove(entityID);
    this.uuidMap.remove(entityID);
    this.validBlocking.remove(entityID);
    this.knownHolograms.remove(entityID);
    this.metadataBuffer.remove(entityID);
    BossBar bar = this.bossBarMap.remove(entityID);
    if (bar != null) {
      bar.hide();
      ((BossBarProvider)Via.getManager().getProviders().get(BossBarProvider.class)).handleRemove(getUser(), bar.getId());
    } 
  }
  
  public boolean interactedBlockRecently(int x, int y, int z) {
    if (this.blockInteractions.size() == 0L)
      return false; 
    for (Position p : this.blockInteractions.asMap().keySet()) {
      if (p.getX().longValue() == x && 
        p.getY().longValue() == y && 
        p.getZ().longValue() == z)
        return true; 
    } 
    return false;
  }
  
  public void addBlockInteraction(Position p) {
    this.blockInteractions.put(p, Integer.valueOf(0));
  }
  
  public void handleMetadata(int entityID, List<Metadata> metadataList) {
    Entity1_10Types.EntityType type = this.clientEntityTypes.get(Integer.valueOf(entityID));
    if (type == null)
      return; 
    for (Metadata metadata : new ArrayList(metadataList)) {
      if (type == Entity1_10Types.EntityType.WITHER && 
        metadata.getId() == 10)
        metadataList.remove(metadata); 
      if (type == Entity1_10Types.EntityType.ENDER_DRAGON && 
        metadata.getId() == 11)
        metadataList.remove(metadata); 
      if (type == Entity1_10Types.EntityType.SKELETON && 
        getMetaByIndex(metadataList, 12) == null)
        metadataList.add(new Metadata(12, (MetaType)MetaType1_9.Boolean, Boolean.valueOf(true))); 
      if (type == Entity1_10Types.EntityType.HORSE)
        if (metadata.getId() == 16 && ((Integer)metadata.getValue()).intValue() == Integer.MIN_VALUE)
          metadata.setValue(Integer.valueOf(0));  
      if (type == Entity1_10Types.EntityType.PLAYER) {
        if (metadata.getId() == 0) {
          byte data = ((Byte)metadata.getValue()).byteValue();
          if (entityID != getProvidedEntityId() && Via.getConfig().isShieldBlocking())
            if ((data & 0x10) == 16) {
              if (this.validBlocking.contains(Integer.valueOf(entityID))) {
                Item shield = new Item((short)442, (byte)1, (short)0, null);
                setSecondHand(entityID, shield);
              } else {
                setSecondHand(entityID, null);
              } 
            } else {
              setSecondHand(entityID, null);
            }  
        } 
        if (metadata.getId() == 12 && Via.getConfig().isLeftHandedHandling())
          metadataList.add(new Metadata(13, (MetaType)MetaType1_9.Byte, 
                
                Byte.valueOf((byte)(((((Byte)metadata.getValue()).byteValue() & 0x80) != 0) ? 0 : 1)))); 
      } 
      if (type == Entity1_10Types.EntityType.ARMOR_STAND && Via.getConfig().isHologramPatch() && 
        metadata.getId() == 0 && getMetaByIndex(metadataList, 10) != null) {
        Metadata meta = getMetaByIndex(metadataList, 10);
        byte data = ((Byte)metadata.getValue()).byteValue();
        if ((data & 0x20) == 32 && (((Byte)meta.getValue()).byteValue() & 0x1) == 1 && 
          getMetaByIndex(metadataList, 2) != null && 
          !Strings.isNullOrEmpty((String)getMetaByIndex(metadataList, 2).getValue()) && 
          getMetaByIndex(metadataList, 3) != null && 
          getMetaByIndex(metadataList, 3).getValue() == Boolean.TRUE && 
          !this.knownHolograms.contains(Integer.valueOf(entityID))) {
          this.knownHolograms.add(Integer.valueOf(entityID));
          try {
            PacketWrapper wrapper = new PacketWrapper(37, null, getUser());
            wrapper.write(Type.VAR_INT, Integer.valueOf(entityID));
            wrapper.write(Type.SHORT, Short.valueOf((short)0));
            wrapper.write(Type.SHORT, Short.valueOf((short)(int)(128.0D * Via.getConfig().getHologramYOffset() * 32.0D)));
            wrapper.write(Type.SHORT, Short.valueOf((short)0));
            wrapper.write(Type.BOOLEAN, Boolean.valueOf(true));
            wrapper.send(Protocol1_9To1_8.class, true, false);
          } catch (Exception exception) {}
        } 
      } 
      UUID uuid = ((ProtocolInfo)getUser().get(ProtocolInfo.class)).getUuid();
      if (Via.getConfig().isBossbarPatch() && (
        type == Entity1_10Types.EntityType.ENDER_DRAGON || type == Entity1_10Types.EntityType.WITHER)) {
        if (metadata.getId() == 2) {
          BossBar bar = this.bossBarMap.get(Integer.valueOf(entityID));
          String title = (String)metadata.getValue();
          title = title.isEmpty() ? ((type == Entity1_10Types.EntityType.ENDER_DRAGON) ? "Ender Dragon" : "Wither") : title;
          if (bar == null) {
            bar = Via.getAPI().createBossBar(title, BossColor.PINK, BossStyle.SOLID);
            this.bossBarMap.put(Integer.valueOf(entityID), bar);
            bar.addPlayer(uuid);
            bar.show();
            ((BossBarProvider)Via.getManager().getProviders().get(BossBarProvider.class)).handleAdd(getUser(), bar.getId());
            continue;
          } 
          bar.setTitle(title);
          continue;
        } 
        if (metadata.getId() == 6 && !Via.getConfig().isBossbarAntiflicker()) {
          BossBar bar = this.bossBarMap.get(Integer.valueOf(entityID));
          float maxHealth = (type == Entity1_10Types.EntityType.ENDER_DRAGON) ? 200.0F : 300.0F;
          float health = Math.max(0.0F, Math.min(((Float)metadata.getValue()).floatValue() / maxHealth, 1.0F));
          if (bar == null) {
            String title = (type == Entity1_10Types.EntityType.ENDER_DRAGON) ? "Ender Dragon" : "Wither";
            bar = Via.getAPI().createBossBar(title, health, BossColor.PINK, BossStyle.SOLID);
            this.bossBarMap.put(Integer.valueOf(entityID), bar);
            bar.addPlayer(uuid);
            bar.show();
            ((BossBarProvider)Via.getManager().getProviders().get(BossBarProvider.class)).handleAdd(getUser(), bar.getId());
            continue;
          } 
          bar.setHealth(health);
        } 
      } 
    } 
  }
  
  public Metadata getMetaByIndex(List<Metadata> list, int index) {
    for (Metadata meta : list) {
      if (index == meta.getId())
        return meta; 
    } 
    return null;
  }
  
  public void sendTeamPacket(boolean add, boolean now) {
    PacketWrapper wrapper = new PacketWrapper(65, null, getUser());
    wrapper.write(Type.STRING, "viaversion");
    if (add) {
      if (!this.teamExists) {
        wrapper.write(Type.BYTE, Byte.valueOf((byte)0));
        wrapper.write(Type.STRING, "viaversion");
        wrapper.write(Type.STRING, "");
        wrapper.write(Type.STRING, "");
        wrapper.write(Type.BYTE, Byte.valueOf((byte)0));
        wrapper.write(Type.STRING, "");
        wrapper.write(Type.STRING, "never");
        wrapper.write(Type.BYTE, Byte.valueOf((byte)0));
      } else {
        wrapper.write(Type.BYTE, Byte.valueOf((byte)3));
      } 
      wrapper.write(Type.STRING_ARRAY, new String[] { ((ProtocolInfo)getUser().get(ProtocolInfo.class)).getUsername() });
    } else {
      wrapper.write(Type.BYTE, Byte.valueOf((byte)1));
    } 
    this.teamExists = add;
    try {
      wrapper.send(Protocol1_9To1_8.class, true, now);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public void addMetadataToBuffer(int entityID, List<Metadata> metadataList) {
    List<Metadata> metadata = this.metadataBuffer.get(Integer.valueOf(entityID));
    if (metadata != null) {
      metadata.addAll(metadataList);
    } else {
      this.metadataBuffer.put(Integer.valueOf(entityID), metadataList);
    } 
  }
  
  public void sendMetadataBuffer(int entityID) {
    List<Metadata> metadataList = this.metadataBuffer.get(Integer.valueOf(entityID));
    if (metadataList != null) {
      PacketWrapper wrapper = new PacketWrapper(57, null, getUser());
      wrapper.write(Type.VAR_INT, Integer.valueOf(entityID));
      wrapper.write(Types1_9.METADATA_LIST, metadataList);
      MetadataRewriter.transform(getClientEntityTypes().get(Integer.valueOf(entityID)), metadataList);
      handleMetadata(entityID, metadataList);
      if (metadataList.size() > 0)
        try {
          wrapper.send(Protocol1_9To1_8.class);
        } catch (Exception e) {
          e.printStackTrace();
        }  
      this.metadataBuffer.remove(Integer.valueOf(entityID));
    } 
  }
  
  public int getProvidedEntityId() {
    try {
      return ((EntityIdProvider)Via.getManager().getProviders().get(EntityIdProvider.class)).getEntityId(getUser());
    } catch (Exception e) {
      return this.entityID;
    } 
  }
  
  public void onExternalJoinGame(int playerEntityId) {
    this.clientEntityTypes.put(Integer.valueOf(playerEntityId), Entity1_10Types.EntityType.PLAYER);
  }
}
