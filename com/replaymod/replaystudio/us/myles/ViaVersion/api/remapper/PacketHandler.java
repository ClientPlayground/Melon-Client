package com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.InformativeException;

public abstract class PacketHandler implements ValueWriter {
  public abstract void handle(PacketWrapper paramPacketWrapper) throws Exception;
  
  public void write(PacketWrapper writer, Object inputValue) throws Exception {
    try {
      handle(writer);
    } catch (InformativeException e) {
      e.addSource(getClass());
      throw e;
    } 
  }
}
