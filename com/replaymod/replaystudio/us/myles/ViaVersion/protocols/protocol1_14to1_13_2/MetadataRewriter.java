package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_14Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.VillagerData;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.Particle;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
  public static void handleMetadata(int entityId, Entity1_14Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
    for (Metadata metadata : new ArrayList(metadatas)) {
      try {
        metadata.setMetaType((MetaType)MetaType1_14.byId(metadata.getMetaType().getTypeID()));
        EntityTracker tracker = (EntityTracker)connection.get(EntityTracker.class);
        if (metadata.getMetaType() == MetaType1_14.Slot) {
          InventoryPackets.toClient((Item)metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_14.BlockID) {
          int data = ((Integer)metadata.getValue()).intValue();
          metadata.setValue(Integer.valueOf(Protocol1_14To1_13_2.getNewBlockStateId(data)));
        } 
        if (type == null)
          continue; 
        if (metadata.getId() > 5)
          metadata.setId(metadata.getId() + 1); 
        if (metadata.getId() == 8 && type.isOrHasParent(Entity1_14Types.EntityType.LIVINGENTITY)) {
          float v = ((Number)metadata.getValue()).floatValue();
          if (Float.isNaN(v) && Via.getConfig().is1_14HealthNaNFix())
            metadata.setValue(Float.valueOf(1.0F)); 
        } 
        if (metadata.getId() > 11 && type.isOrHasParent(Entity1_14Types.EntityType.LIVINGENTITY))
          metadata.setId(metadata.getId() + 1); 
        if (type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_INSENTIENT) && 
          metadata.getId() == 13) {
          tracker.setInsentientData(entityId, 
              (byte)(((Number)metadata.getValue()).byteValue() & 0xFFFFFFFB | tracker.getInsentientData(entityId) & 0x4));
          metadata.setValue(Byte.valueOf(tracker.getInsentientData(entityId)));
        } 
        if (type.isOrHasParent(Entity1_14Types.EntityType.PLAYER)) {
          if (entityId != tracker.getClientEntityId()) {
            if (metadata.getId() == 0) {
              byte flags = ((Number)metadata.getValue()).byteValue();
              tracker.setEntityFlags(entityId, flags);
            } else if (metadata.getId() == 7) {
              tracker.setRiptide(entityId, ((((Number)metadata.getValue()).byteValue() & 0x4) != 0));
            } 
            if (metadata.getId() == 0 || metadata.getId() == 7)
              metadatas.add(new Metadata(6, (MetaType)MetaType1_14.Pose, Integer.valueOf(recalculatePlayerPose(entityId, tracker)))); 
          } 
        } else if (type.isOrHasParent(Entity1_14Types.EntityType.ZOMBIE)) {
          if (metadata.getId() == 16) {
            tracker.setInsentientData(entityId, 
                (byte)(tracker.getInsentientData(entityId) & 0xFFFFFFFB | (((Boolean)metadata.getValue()).booleanValue() ? 4 : 0)));
            metadatas.remove(metadata);
            metadatas.add(new Metadata(13, (MetaType)MetaType1_14.Byte, Byte.valueOf(tracker.getInsentientData(entityId))));
          } else if (metadata.getId() > 16) {
            metadata.setId(metadata.getId() - 1);
          } 
        } 
        if (type.isOrHasParent(Entity1_14Types.EntityType.MINECART_ABSTRACT)) {
          if (metadata.getId() == 10) {
            int data = ((Integer)metadata.getValue()).intValue();
            metadata.setValue(Integer.valueOf(Protocol1_14To1_13_2.getNewBlockStateId(data)));
          } 
        } else if (type.is(Entity1_14Types.EntityType.HORSE)) {
          if (metadata.getId() == 18) {
            metadatas.remove(metadata);
            int armorType = ((Integer)metadata.getValue()).intValue();
            Item armorItem = null;
            if (armorType == 1) {
              armorItem = new Item(InventoryPackets.getNewItemId(727), (byte)1, (short)0, null);
            } else if (armorType == 2) {
              armorItem = new Item(InventoryPackets.getNewItemId(728), (byte)1, (short)0, null);
            } else if (armorType == 3) {
              armorItem = new Item(InventoryPackets.getNewItemId(729), (byte)1, (short)0, null);
            } 
            PacketWrapper equipmentPacket = new PacketWrapper(70, null, connection);
            equipmentPacket.write(Type.VAR_INT, Integer.valueOf(entityId));
            equipmentPacket.write(Type.VAR_INT, Integer.valueOf(4));
            equipmentPacket.write(Type.FLAT_VAR_INT_ITEM, armorItem);
            equipmentPacket.send(Protocol1_14To1_13_2.class);
          } 
        } else if (type.is(Entity1_14Types.EntityType.VILLAGER)) {
          if (metadata.getId() == 15) {
            metadata.setValue(new VillagerData(2, getNewProfessionId(((Integer)metadata.getValue()).intValue()), 0));
            metadata.setMetaType((MetaType)MetaType1_14.VillagerData);
          } 
        } else if (type.is(Entity1_14Types.EntityType.ZOMBIE_VILLAGER)) {
          if (metadata.getId() == 18) {
            metadata.setValue(new VillagerData(2, getNewProfessionId(((Integer)metadata.getValue()).intValue()), 0));
            metadata.setMetaType((MetaType)MetaType1_14.VillagerData);
          } 
        } else if (type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ARROW)) {
          if (metadata.getId() >= 9)
            metadata.setId(metadata.getId() + 1); 
        } else if (type.is(Entity1_14Types.EntityType.FIREWORKS_ROCKET)) {
          if (metadata.getId() == 8) {
            if (metadata.getValue().equals(Integer.valueOf(0)))
              metadata.setValue(null); 
            metadata.setMetaType((MetaType)MetaType1_14.OptVarInt);
          } 
        } else if (type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_SKELETON)) {
          if (metadata.getId() == 14) {
            tracker.setInsentientData(entityId, 
                (byte)(tracker.getInsentientData(entityId) & 0xFFFFFFFB | (((Boolean)metadata.getValue()).booleanValue() ? 4 : 0)));
            metadatas.remove(metadata);
            metadatas.add(new Metadata(13, (MetaType)MetaType1_14.Byte, Byte.valueOf(tracker.getInsentientData(entityId))));
          } 
        } else if (type.is(Entity1_14Types.EntityType.AREA_EFFECT_CLOUD) && 
          metadata.getId() == 10) {
          Particle particle = (Particle)metadata.getValue();
          particle.setId(getNewParticleId(particle.getId()));
        } 
        if (type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ILLAGER_BASE) && 
          metadata.getId() == 14) {
          tracker.setInsentientData(entityId, 
              (byte)(tracker.getInsentientData(entityId) & 0xFFFFFFFB | ((((Number)metadata.getValue()).byteValue() != 0) ? 4 : 0)));
          metadatas.remove(metadata);
          metadatas.add(new Metadata(13, (MetaType)MetaType1_14.Byte, Byte.valueOf(tracker.getInsentientData(entityId))));
        } 
        if ((type.is(Entity1_14Types.EntityType.WITCH) || type.is(Entity1_14Types.EntityType.RAVAGER) || type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ILLAGER_BASE)) && 
          metadata.getId() >= 14)
          metadata.setId(metadata.getId() + 1); 
      } catch (Exception e) {
        metadatas.remove(metadata);
        if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
          Via.getPlatform().getLogger().warning("An error occurred with entity metadata handler");
          Via.getPlatform().getLogger().warning("Metadata: " + metadata);
          e.printStackTrace();
        } 
      } 
    } 
  }
  
  private static boolean isSneaking(byte flags) {
    return ((flags & 0x2) != 0);
  }
  
  private static boolean isSwimming(byte flags) {
    return ((flags & 0x10) != 0);
  }
  
  private static int getNewProfessionId(int old) {
    switch (old) {
      case 0:
        return 5;
      case 1:
        return 9;
      case 2:
        return 4;
      case 3:
        return 1;
      case 4:
        return 2;
      case 5:
        return 11;
    } 
    return 0;
  }
  
  private static boolean isFallFlying(int entityFlags) {
    return ((entityFlags & 0x80) != 0);
  }
  
  public static int recalculatePlayerPose(int entityId, EntityTracker tracker) {
    byte flags = tracker.getEntityFlags(entityId);
    int pose = 0;
    if (isFallFlying(flags)) {
      pose = 1;
    } else if (tracker.isSleeping(entityId)) {
      pose = 2;
    } else if (isSwimming(flags)) {
      pose = 3;
    } else if (tracker.isRiptide(entityId)) {
      pose = 4;
    } else if (isSneaking(flags)) {
      pose = 5;
    } 
    return pose;
  }
  
  public static int getNewParticleId(int id) {
    if (id >= 10)
      id += 2; 
    if (id >= 13)
      id++; 
    if (id >= 27)
      id++; 
    if (id >= 29)
      id++; 
    if (id >= 44)
      id++; 
    return id;
  }
}
