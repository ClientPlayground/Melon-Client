package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_15Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets;
import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
  public static void handleMetadata(int entityId, Entity1_15Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
    for (Metadata metadata : new ArrayList(metadatas)) {
      try {
        if (metadata.getMetaType() == MetaType1_14.Slot) {
          InventoryPackets.toClient((Item)metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_14.BlockID) {
          int data = ((Integer)metadata.getValue()).intValue();
          metadata.setValue(Integer.valueOf(Protocol1_15To1_14_4.getNewBlockStateId(data)));
        } 
        if (type == null)
          continue; 
        if (metadata.getId() > 11 && type.isOrHasParent(Entity1_15Types.EntityType.LIVINGENTITY))
          metadata.setId(metadata.getId() + 1); 
        if (type.isOrHasParent(Entity1_15Types.EntityType.WOLF)) {
          if (metadata.getId() == 18) {
            metadatas.remove(metadata);
            continue;
          } 
          if (metadata.getId() > 18)
            metadata.setId(metadata.getId() - 1); 
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
}
