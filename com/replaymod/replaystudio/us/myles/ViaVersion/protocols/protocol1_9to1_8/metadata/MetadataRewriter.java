package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_10Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.EulerAngle;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Vector;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MetadataRewriter {
  public static void transform(Entity1_10Types.EntityType type, List<Metadata> list) {
    short id = -1;
    int data = -1;
    for (Metadata entry : new ArrayList(list)) {
      MetaIndex metaIndex = MetaIndex.searchIndex(type, entry.getId());
      try {
        if (metaIndex != null) {
          if (metaIndex.getNewType() != MetaType1_9.Discontinued) {
            String owner;
            UUID toWrite;
            Vector vector;
            EulerAngle angle;
            if (metaIndex.getNewType() != MetaType1_9.BlockID || (id != -1 && data == -1) || (id == -1 && data != -1)) {
              entry.setId(metaIndex.getNewIndex());
              entry.setMetaType((MetaType)metaIndex.getNewType());
            } 
            Object value = entry.getValue();
            switch (metaIndex.getNewType()) {
              case Byte:
                if (metaIndex.getOldType() == MetaType1_8.Byte)
                  entry.setValue(value); 
                if (metaIndex.getOldType() == MetaType1_8.Int)
                  entry.setValue(Byte.valueOf(((Integer)value).byteValue())); 
                if (metaIndex == MetaIndex.ENTITY_STATUS && type == Entity1_10Types.EntityType.PLAYER) {
                  Byte val = Byte.valueOf((byte)0);
                  if ((((Byte)value).byteValue() & 0x10) == 16)
                    val = Byte.valueOf((byte)1); 
                  int newIndex = MetaIndex.PLAYER_HAND.getNewIndex();
                  MetaType1_9 metaType1_9 = MetaIndex.PLAYER_HAND.getNewType();
                  Metadata metadata = new Metadata(newIndex, (MetaType)metaType1_9, val);
                  list.add(metadata);
                } 
                continue;
              case OptUUID:
                owner = (String)value;
                toWrite = null;
                if (owner.length() != 0)
                  try {
                    toWrite = UUID.fromString(owner);
                  } catch (Exception exception) {} 
                entry.setValue(toWrite);
                continue;
              case BlockID:
                if (metaIndex.getOldType() == MetaType1_8.Byte)
                  data = ((Byte)value).byteValue(); 
                if (metaIndex.getOldType() == MetaType1_8.Short)
                  id = ((Short)value).shortValue(); 
                if (id != -1 && data != -1) {
                  int combined = id | data & 0xF;
                  data = -1;
                  id = -1;
                  entry.setValue(Integer.valueOf(combined));
                  continue;
                } 
                list.remove(entry);
                continue;
              case VarInt:
                if (metaIndex.getOldType() == MetaType1_8.Byte)
                  entry.setValue(Integer.valueOf(((Byte)value).intValue())); 
                if (metaIndex.getOldType() == MetaType1_8.Short)
                  entry.setValue(Integer.valueOf(((Short)value).intValue())); 
                if (metaIndex.getOldType() == MetaType1_8.Int)
                  entry.setValue(value); 
                continue;
              case Float:
                entry.setValue(value);
                continue;
              case String:
                if (!(value instanceof String))
                  throw new Exception("Invalid metadata value"); 
                entry.setValue(value);
                continue;
              case Boolean:
                if (metaIndex == MetaIndex.AGEABLE_AGE) {
                  entry.setValue(Boolean.valueOf((((Byte)value).byteValue() < 0)));
                  continue;
                } 
                entry.setValue(Boolean.valueOf((((Byte)value).byteValue() != 0)));
                continue;
              case Slot:
                entry.setValue(value);
                ItemRewriter.toClient((Item)entry.getValue());
                continue;
              case Position:
                vector = (Vector)value;
                entry.setValue(vector);
                continue;
              case Vector3F:
                angle = (EulerAngle)value;
                entry.setValue(angle);
                continue;
              case Chat:
                value = Protocol1_9To1_8.fixJson((String)value);
                entry.setValue(value);
                continue;
            } 
            Via.getPlatform().getLogger().warning("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
            list.remove(entry);
            continue;
          } 
          list.remove(entry);
          continue;
        } 
        throw new Exception("Could not find valid metadata");
      } catch (Exception e) {
        list.remove(entry);
        if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
          Logger log = Via.getPlatform().getLogger();
          log.warning("This is most likely down to one of your plugins sending bad datawatchers. Please test if this occurs without any plugins except ViaVersion before reporting it on GitHub");
          log.warning("Also make sure that all your plugins are compatible with your server version.");
          if (type != null) {
            log.severe("An error occurred with entity meta data for " + type + " OldID: " + entry.getId());
          } else {
            log.severe("An error occurred with entity meta data for UNKNOWN_ENTITY OldID: " + entry.getId());
          } 
          if (metaIndex != null) {
            log.severe("Value: " + entry.getValue());
            log.severe("Old ID: " + metaIndex.getIndex() + " New ID: " + metaIndex.getNewIndex());
            log.severe("Old Type: " + metaIndex.getOldType() + " New Type: " + metaIndex.getNewType());
          } 
          e.printStackTrace();
        } 
      } 
    } 
  }
}
