package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_13Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.InventoryPackets;
import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
  public static void handleMetadata(int entityId, Entity1_13Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
    for (Metadata metadata : new ArrayList(metadatas)) {
      try {
        if (metadata.getMetaType() == MetaType1_13.Slot) {
          InventoryPackets.toClient((Item)metadata.getValue());
        } else if (metadata.getMetaType() == MetaType1_13.BlockID) {
          int data = ((Integer)metadata.getValue()).intValue();
          metadata.setValue(Integer.valueOf(Protocol1_13_1To1_13.getNewBlockStateId(data)));
        } 
        if (type == null)
          continue; 
        if (type.isOrHasParent(Entity1_13Types.EntityType.MINECART_ABSTRACT) && metadata.getId() == 9) {
          int data = ((Integer)metadata.getValue()).intValue();
          metadata.setValue(Integer.valueOf(Protocol1_13_1To1_13.getNewBlockStateId(data)));
        } 
        if (type.isOrHasParent(Entity1_13Types.EntityType.ABSTRACT_ARROW) && metadata.getId() >= 7)
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
}
