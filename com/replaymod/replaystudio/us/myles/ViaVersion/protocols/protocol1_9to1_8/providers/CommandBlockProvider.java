package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.CommandBlockStorage;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class CommandBlockProvider implements Provider {
  public void addOrUpdateBlock(UserConnection user, Position position, CompoundTag tag) throws Exception {
    checkPermission(user);
    if (isEnabled())
      getStorage(user).addOrUpdateBlock(position, tag); 
  }
  
  public Optional<CompoundTag> get(UserConnection user, Position position) throws Exception {
    checkPermission(user);
    if (isEnabled())
      return getStorage(user).getCommandBlock(position); 
    return Optional.absent();
  }
  
  public void unloadChunk(UserConnection user, int x, int z) throws Exception {
    checkPermission(user);
    if (isEnabled())
      getStorage(user).unloadChunk(x, z); 
  }
  
  private CommandBlockStorage getStorage(UserConnection connection) {
    return (CommandBlockStorage)connection.get(CommandBlockStorage.class);
  }
  
  public void sendPermission(UserConnection user) throws Exception {
    if (!isEnabled())
      return; 
    PacketWrapper wrapper = new PacketWrapper(27, null, user);
    wrapper.write(Type.INT, Integer.valueOf(((EntityTracker)user.get(EntityTracker.class)).getProvidedEntityId()));
    wrapper.write(Type.BYTE, Byte.valueOf((byte)26));
    wrapper.send(Protocol1_9To1_8.class);
    ((CommandBlockStorage)user.get(CommandBlockStorage.class)).setPermissions(true);
  }
  
  private void checkPermission(UserConnection user) throws Exception {
    if (!isEnabled())
      return; 
    CommandBlockStorage storage = getStorage(user);
    if (!storage.isPermissions())
      sendPermission(user); 
  }
  
  public boolean isEnabled() {
    return true;
  }
  
  public void unloadChunks(UserConnection userConnection) {
    if (isEnabled())
      getStorage(userConnection).unloadChunks(); 
  }
}
