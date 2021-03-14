package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_11Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.storage.EntityTracker;
import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
  public static Entity1_11Types.EntityType rewriteEntityType(int numType, List<Metadata> metadata) {
    Optional<Entity1_11Types.EntityType> optType = Entity1_11Types.EntityType.findById(numType);
    if (!optType.isPresent()) {
      Via.getManager().getPlatform().getLogger().severe("Error: could not find Entity type " + numType + " with metadata: " + metadata);
      return null;
    } 
    Entity1_11Types.EntityType type = (Entity1_11Types.EntityType)optType.get();
    try {
      if (type.is(Entity1_11Types.EntityType.GUARDIAN)) {
        Optional<Metadata> options = getById(metadata, 12);
        if (options.isPresent() && ((
          (Byte)((Metadata)options.get()).getValue()).byteValue() & 0x4) == 4)
          return Entity1_11Types.EntityType.ELDER_GUARDIAN; 
      } 
      if (type.is(Entity1_11Types.EntityType.SKELETON)) {
        Optional<Metadata> options = getById(metadata, 12);
        if (options.isPresent()) {
          if (((Integer)((Metadata)options.get()).getValue()).intValue() == 1)
            return Entity1_11Types.EntityType.WITHER_SKELETON; 
          if (((Integer)((Metadata)options.get()).getValue()).intValue() == 2)
            return Entity1_11Types.EntityType.STRAY; 
        } 
      } 
      if (type.is(Entity1_11Types.EntityType.ZOMBIE)) {
        Optional<Metadata> options = getById(metadata, 13);
        if (options.isPresent()) {
          int value = ((Integer)((Metadata)options.get()).getValue()).intValue();
          if (value > 0 && value < 6) {
            metadata.add(new Metadata(16, (MetaType)MetaType1_9.VarInt, Integer.valueOf(value - 1)));
            return Entity1_11Types.EntityType.ZOMBIE_VILLAGER;
          } 
          if (value == 6)
            return Entity1_11Types.EntityType.HUSK; 
        } 
      } 
      if (type.is(Entity1_11Types.EntityType.HORSE)) {
        Optional<Metadata> options = getById(metadata, 14);
        if (options.isPresent()) {
          if (((Integer)((Metadata)options.get()).getValue()).intValue() == 0)
            return Entity1_11Types.EntityType.HORSE; 
          if (((Integer)((Metadata)options.get()).getValue()).intValue() == 1)
            return Entity1_11Types.EntityType.DONKEY; 
          if (((Integer)((Metadata)options.get()).getValue()).intValue() == 2)
            return Entity1_11Types.EntityType.MULE; 
          if (((Integer)((Metadata)options.get()).getValue()).intValue() == 3)
            return Entity1_11Types.EntityType.ZOMBIE_HORSE; 
          if (((Integer)((Metadata)options.get()).getValue()).intValue() == 4)
            return Entity1_11Types.EntityType.SKELETON_HORSE; 
        } 
      } 
    } catch (Exception e) {
      if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
        Via.getPlatform().getLogger().warning("An error occurred with entity type rewriter");
        Via.getPlatform().getLogger().warning("Metadata: " + metadata);
        e.printStackTrace();
      } 
    } 
    return type;
  }
  
  public static void handleMetadata(int entityId, Entity1_11Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
    for (Metadata metadata : new ArrayList(metadatas)) {
      try {
        if (metadata.getValue() instanceof Item)
          EntityIdRewriter.toClientItem((Item)metadata.getValue()); 
        if (type.is(Entity1_11Types.EntityType.ELDER_GUARDIAN) || type.is(Entity1_11Types.EntityType.GUARDIAN)) {
          int oldid = metadata.getId();
          if (oldid == 12) {
            metadata.setMetaType((MetaType)MetaType1_9.Boolean);
            boolean val = ((((Byte)metadata.getValue()).byteValue() & 0x2) == 2);
            metadata.setValue(Boolean.valueOf(val));
          } 
        } 
        if (type.isOrHasParent(Entity1_11Types.EntityType.ABSTRACT_SKELETON)) {
          int oldid = metadata.getId();
          if (oldid == 12)
            metadatas.remove(metadata); 
          if (oldid == 13)
            metadata.setId(12); 
        } 
        if (type.isOrHasParent(Entity1_11Types.EntityType.ZOMBIE))
          if (type.is(new Entity1_11Types.EntityType[] { Entity1_11Types.EntityType.ZOMBIE, Entity1_11Types.EntityType.HUSK }) && metadata.getId() == 14) {
            metadatas.remove(metadata);
          } else if (metadata.getId() == 15) {
            metadata.setId(14);
          } else if (metadata.getId() == 14) {
            metadata.setId(15);
          }  
        if (type.isOrHasParent(Entity1_11Types.EntityType.ABSTRACT_HORSE)) {
          int oldid = metadata.getId();
          if (oldid == 14)
            metadatas.remove(metadata); 
          if (oldid == 16)
            metadata.setId(14); 
          if (oldid == 17)
            metadata.setId(16); 
          if (!type.is(Entity1_11Types.EntityType.HORSE))
            if (metadata.getId() == 15 || metadata.getId() == 16)
              metadatas.remove(metadata);  
          if (type.is(new Entity1_11Types.EntityType[] { Entity1_11Types.EntityType.DONKEY, Entity1_11Types.EntityType.MULE }))
            if (metadata.getId() == 13)
              if ((((Byte)metadata.getValue()).byteValue() & 0x8) == 8) {
                metadatas.add(new Metadata(15, (MetaType)MetaType1_9.Boolean, Boolean.valueOf(true)));
              } else {
                metadatas.add(new Metadata(15, (MetaType)MetaType1_9.Boolean, Boolean.valueOf(false)));
              }   
        } 
        if (type.is(Entity1_11Types.EntityType.ARMOR_STAND) && Via.getConfig().isHologramPatch()) {
          Optional<Metadata> flags = getById(metadatas, 11);
          Optional<Metadata> customName = getById(metadatas, 2);
          Optional<Metadata> customNameVisible = getById(metadatas, 3);
          if (metadata.getId() == 0 && flags.isPresent() && customName.isPresent() && customNameVisible.isPresent()) {
            Metadata meta = (Metadata)flags.get();
            byte data = ((Byte)metadata.getValue()).byteValue();
            if ((data & 0x20) == 32 && (((Byte)meta.getValue()).byteValue() & 0x1) == 1 && ((String)((Metadata)customName
              .get()).getValue()).length() != 0 && ((Boolean)((Metadata)customNameVisible.get()).getValue()).booleanValue()) {
              EntityTracker tracker = (EntityTracker)connection.get(EntityTracker.class);
              if (!tracker.isHologram(entityId)) {
                tracker.addHologram(entityId);
                try {
                  PacketWrapper wrapper = new PacketWrapper(37, null, connection);
                  wrapper.write(Type.VAR_INT, Integer.valueOf(entityId));
                  wrapper.write(Type.SHORT, Short.valueOf((short)0));
                  wrapper.write(Type.SHORT, Short.valueOf((short)(int)(128.0D * -Via.getConfig().getHologramYOffset() * 32.0D)));
                  wrapper.write(Type.SHORT, Short.valueOf((short)0));
                  wrapper.write(Type.BOOLEAN, Boolean.valueOf(true));
                  wrapper.send(Protocol1_11To1_10.class);
                } catch (Exception e) {
                  e.printStackTrace();
                } 
              } 
            } 
          } 
        } 
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
  
  public static Optional<Metadata> getById(List<Metadata> metadatas, int id) {
    for (Metadata metadata : metadatas) {
      if (metadata.getId() == id)
        return Optional.of(metadata); 
    } 
    return Optional.absent();
  }
}
