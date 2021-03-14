package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_13;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_13_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public class EntityPackets {
  public static void register(Protocol protocol) {
    final PacketHandler metaTypeHandler = new PacketHandler() {
        public void handle(PacketWrapper wrapper) throws Exception {
          for (Metadata metadata : wrapper.get(Types1_13_2.METADATA_LIST, 0))
            metadata.setMetaType((MetaType)MetaType1_13_2.byId(metadata.getMetaType().getTypeID())); 
        }
      };
    protocol.registerOutgoing(State.PLAY, 3, 3, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.VAR_INT);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Types1_13.METADATA_LIST, Types1_13_2.METADATA_LIST);
            handler(metaTypeHandler);
          }
        });
    protocol.registerOutgoing(State.PLAY, 5, 5, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Types1_13.METADATA_LIST, Types1_13_2.METADATA_LIST);
            handler(metaTypeHandler);
          }
        });
    protocol.registerOutgoing(State.PLAY, 63, 63, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_13.METADATA_LIST, Types1_13_2.METADATA_LIST);
            handler(metaTypeHandler);
          }
        });
  }
}
