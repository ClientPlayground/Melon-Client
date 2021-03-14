package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_13Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.Particle;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
  public static void handleMetadata(int entityId, Entity1_13Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
    int particleId = -1, parameter1 = 0, parameter2 = 0;
    for (Metadata metadata : new ArrayList(metadatas)) {
      try {
        if (metadata.getMetaType().getTypeID() > 4) {
          metadata.setMetaType((MetaType)MetaType1_13.byId(metadata.getMetaType().getTypeID() + 1));
        } else {
          metadata.setMetaType((MetaType)MetaType1_13.byId(metadata.getMetaType().getTypeID()));
        } 
        if (metadata.getId() == 2) {
          metadata.setMetaType((MetaType)MetaType1_13.OptChat);
          if (metadata.getValue() != null && !((String)metadata.getValue()).isEmpty()) {
            metadata.setValue(ChatRewriter.legacyTextToJson((String)metadata.getValue()));
          } else {
            metadata.setValue(null);
          } 
        } 
        if (metadata.getMetaType() == MetaType1_13.Slot) {
          metadata.setMetaType((MetaType)MetaType1_13.Slot);
          InventoryPackets.toClient((Item)metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_13.BlockID) {
          metadata.setValue(Integer.valueOf(WorldPackets.toNewId(((Integer)metadata.getValue()).intValue())));
        } 
        if (type == null)
          continue; 
        if (type.is(Entity1_13Types.EntityType.WOLF) && metadata.getId() == 17)
          metadata.setValue(Integer.valueOf(15 - ((Integer)metadata.getValue()).intValue())); 
        if (type.isOrHasParent(Entity1_13Types.EntityType.ZOMBIE) && 
          metadata.getId() > 14)
          metadata.setId(metadata.getId() + 1); 
        if (type.isOrHasParent(Entity1_13Types.EntityType.MINECART_ABSTRACT) && metadata.getId() == 9) {
          int oldId = ((Integer)metadata.getValue()).intValue();
          int combined = (oldId & 0xFFF) << 4 | oldId >> 12 & 0xF;
          int newId = WorldPackets.toNewId(combined);
          metadata.setValue(Integer.valueOf(newId));
        } 
        if (type.is(Entity1_13Types.EntityType.AREA_EFFECT_CLOUD)) {
          if (metadata.getId() == 9) {
            particleId = ((Integer)metadata.getValue()).intValue();
          } else if (metadata.getId() == 10) {
            parameter1 = ((Integer)metadata.getValue()).intValue();
          } else if (metadata.getId() == 11) {
            parameter2 = ((Integer)metadata.getValue()).intValue();
          } 
          if (metadata.getId() >= 9)
            metadatas.remove(metadata); 
        } 
        if (metadata.getId() == 0)
          metadata.setValue(Byte.valueOf((byte)(((Byte)metadata.getValue()).byteValue() & 0xFFFFFFEF))); 
      } catch (Exception e) {
        metadatas.remove(metadata);
        if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
          Via.getPlatform().getLogger().warning("An error occurred with entity metadata handler");
          Via.getPlatform().getLogger().warning("Metadata: " + metadata);
          e.printStackTrace();
        } 
      } 
    } 
    if (type != null && type.is(Entity1_13Types.EntityType.AREA_EFFECT_CLOUD) && particleId != -1) {
      Particle particle = ParticleRewriter.rewriteParticle(particleId, new Integer[] { Integer.valueOf(parameter1), Integer.valueOf(parameter2) });
      if (particle != null && particle.getId() != -1)
        metadatas.add(new Metadata(9, (MetaType)MetaType1_13.PARTICLE, particle)); 
    } 
  }
}
