package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities.BannerHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities.BedHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities.CommandBlockHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities.FlowerPotHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities.SkullHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities.SpawnerHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockEntityProvider implements Provider {
  private final Map<String, BlockEntityHandler> handlers = new ConcurrentHashMap<>();
  
  public BlockEntityProvider() {
    this.handlers.put("minecraft:flower_pot", new FlowerPotHandler());
    this.handlers.put("minecraft:bed", new BedHandler());
    this.handlers.put("minecraft:banner", new BannerHandler());
    this.handlers.put("minecraft:skull", new SkullHandler());
    this.handlers.put("minecraft:mob_spawner", new SpawnerHandler());
    this.handlers.put("minecraft:command_block", new CommandBlockHandler());
  }
  
  public int transform(UserConnection user, Position position, CompoundTag tag, boolean sendUpdate) throws Exception {
    if (!tag.contains("id"))
      return -1; 
    String id = (String)tag.get("id").getValue();
    BlockEntityHandler handler = this.handlers.get(id);
    if (handler == null) {
      if (Via.getManager().isDebug())
        Via.getPlatform().getLogger().warning("Unhandled BlockEntity " + id + " full tag: " + tag); 
      return -1;
    } 
    int newBlock = handler.transform(user, tag);
    if (sendUpdate && newBlock != -1)
      sendBlockChange(user, position, newBlock); 
    return newBlock;
  }
  
  private void sendBlockChange(UserConnection user, Position position, int blockId) throws Exception {
    PacketWrapper wrapper = new PacketWrapper(11, null, user);
    wrapper.write(Type.POSITION, position);
    wrapper.write(Type.VAR_INT, Integer.valueOf(blockId));
    wrapper.send(Protocol1_13To1_12_2.class, true, true);
  }
  
  public static interface BlockEntityHandler {
    int transform(UserConnection param1UserConnection, CompoundTag param1CompoundTag);
  }
}
