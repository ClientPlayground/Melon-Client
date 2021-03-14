package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_10to1_9_3;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueTransformer;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Metadata1_9Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.MetadataList1_9Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_10to1_9_3.storage.ResourcePackTracker;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Protocol1_10To1_9_3_4 extends Protocol {
  @Deprecated
  public static final Type<List<Metadata>> METADATA_LIST = (Type<List<Metadata>>)new MetadataList1_9Type();
  
  @Deprecated
  public static final Type<Metadata> METADATA = (Type<Metadata>)new Metadata1_9Type();
  
  public static final ValueTransformer<Short, Float> toNewPitch = new ValueTransformer<Short, Float>(Type.FLOAT) {
      public Float transform(PacketWrapper wrapper, Short inputValue) throws Exception {
        return Float.valueOf(inputValue.shortValue() / 63.5F);
      }
    };
  
  public static final ValueTransformer<List<Metadata>, List<Metadata>> transformMetadata = new ValueTransformer<List<Metadata>, List<Metadata>>(Types1_9.METADATA_LIST) {
      public List<Metadata> transform(PacketWrapper wrapper, List<Metadata> inputValue) throws Exception {
        List<Metadata> metaList = new CopyOnWriteArrayList<>(inputValue);
        for (Metadata m : metaList) {
          if (m.getId() >= 5)
            m.setId(m.getId() + 1); 
        } 
        return metaList;
      }
    };
  
  protected void registerPackets() {
    registerOutgoing(State.PLAY, 25, 25, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.VAR_INT);
            map(Type.INT);
            map(Type.INT);
            map(Type.INT);
            map(Type.FLOAT);
            map(Type.UNSIGNED_BYTE, Protocol1_10To1_9_3_4.toNewPitch);
          }
        });
    registerOutgoing(State.PLAY, 70, 70, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            map(Type.INT);
            map(Type.INT);
            map(Type.INT);
            map(Type.FLOAT);
            map(Type.UNSIGNED_BYTE, Protocol1_10To1_9_3_4.toNewPitch);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int id = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    wrapper.set(Type.VAR_INT, 0, Integer.valueOf(Protocol1_10To1_9_3_4.this.getNewSoundId(id)));
                  }
                });
          }
        });
    registerOutgoing(State.PLAY, 57, 57, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Types1_9.METADATA_LIST, Protocol1_10To1_9_3_4.transformMetadata);
          }
        });
    registerOutgoing(State.PLAY, 3, 3, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.UNSIGNED_BYTE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Type.SHORT);
            map(Types1_9.METADATA_LIST, Protocol1_10To1_9_3_4.transformMetadata);
          }
        });
    registerOutgoing(State.PLAY, 5, 5, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.UUID);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BYTE);
            map(Type.BYTE);
            map(Types1_9.METADATA_LIST, Protocol1_10To1_9_3_4.transformMetadata);
          }
        });
    registerOutgoing(State.PLAY, 50, 50, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ResourcePackTracker tracker = (ResourcePackTracker)wrapper.user().get(ResourcePackTracker.class);
                    tracker.setLastHash((String)wrapper.get(Type.STRING, 1));
                  }
                });
          }
        });
    registerIncoming(State.PLAY, 22, 22, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ResourcePackTracker tracker = (ResourcePackTracker)wrapper.user().get(ResourcePackTracker.class);
                    wrapper.write(Type.STRING, tracker.getLastHash());
                    wrapper.write(Type.VAR_INT, wrapper.read(Type.VAR_INT));
                  }
                });
          }
        });
  }
  
  public int getNewSoundId(int id) {
    int newId = id;
    if (id >= 24)
      newId++; 
    if (id >= 248)
      newId += 4; 
    if (id >= 296)
      newId += 6; 
    if (id >= 354)
      newId += 4; 
    if (id >= 372)
      newId += 4; 
    return newId;
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new ResourcePackTracker(userConnection));
  }
}
