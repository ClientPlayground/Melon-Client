package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.Protocol1_9_1_2To1_9_3_4;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockEntity {
  public static Map<String, Integer> getTypes() {
    return types;
  }
  
  private static final Map<String, Integer> types = new ConcurrentHashMap<>();
  
  static {
    types.put("MobSpawner", Integer.valueOf(1));
    types.put("Control", Integer.valueOf(2));
    types.put("Beacon", Integer.valueOf(3));
    types.put("Skull", Integer.valueOf(4));
    types.put("FlowerPot", Integer.valueOf(5));
    types.put("Banner", Integer.valueOf(6));
    types.put("UNKNOWN", Integer.valueOf(7));
    types.put("EndGateway", Integer.valueOf(8));
    types.put("Sign", Integer.valueOf(9));
  }
  
  public static void handle(List<CompoundTag> tags, UserConnection connection) {
    for (CompoundTag tag : tags) {
      try {
        if (!tag.contains("id"))
          throw new Exception("NBT tag not handled because the id key is missing"); 
        String id = (String)tag.get("id").getValue();
        if (!types.containsKey(id))
          throw new Exception("Not handled id: " + id); 
        int newId = ((Integer)types.get(id)).intValue();
        if (newId == -1)
          continue; 
        int x = ((Integer)tag.get("x").getValue()).intValue();
        int y = ((Integer)tag.get("y").getValue()).intValue();
        int z = ((Integer)tag.get("z").getValue()).intValue();
        Position pos = new Position(Long.valueOf(x), Long.valueOf(y), Long.valueOf(z));
        updateBlockEntity(pos, (short)newId, tag, connection);
      } catch (Exception e) {
        if (Via.getManager().isDebug())
          Via.getPlatform().getLogger().warning("Block Entity: " + e.getMessage() + ": " + tag); 
      } 
    } 
  }
  
  private static void updateBlockEntity(Position pos, short id, CompoundTag tag, UserConnection connection) throws Exception {
    PacketWrapper wrapper = new PacketWrapper(9, null, connection);
    wrapper.write(Type.POSITION, pos);
    wrapper.write(Type.UNSIGNED_BYTE, Short.valueOf(id));
    wrapper.write(Type.NBT, tag);
    wrapper.send(Protocol1_9_1_2To1_9_3_4.class, false);
  }
}
