package com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;

public interface ValueWriter<T> {
  void write(PacketWrapper paramPacketWrapper, T paramT) throws Exception;
}
