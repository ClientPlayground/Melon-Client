package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.google.common.collect.Sets;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import java.util.List;
import java.util.Set;

public class ClientChunks extends StoredObject {
  private final Set<Long> loadedChunks = Sets.newConcurrentHashSet();
  
  public Set<Long> getLoadedChunks() {
    return this.loadedChunks;
  }
  
  private final Set<Long> bulkChunks = Sets.newConcurrentHashSet();
  
  public Set<Long> getBulkChunks() {
    return this.bulkChunks;
  }
  
  public ClientChunks(UserConnection user) {
    super(user);
  }
  
  public static long toLong(int msw, int lsw) {
    return (msw << 32L) + lsw - -2147483648L;
  }
  
  public List<Object> transformMapChunkBulk(Object packet) throws Exception {
    return ((BulkChunkTranslatorProvider)Via.getManager().getProviders().get(BulkChunkTranslatorProvider.class)).transformMapChunkBulk(packet, this);
  }
}
