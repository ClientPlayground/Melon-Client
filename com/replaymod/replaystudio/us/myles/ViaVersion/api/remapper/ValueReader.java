package com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;

public interface ValueReader<T> {
  T read(PacketWrapper paramPacketWrapper) throws Exception;
}
