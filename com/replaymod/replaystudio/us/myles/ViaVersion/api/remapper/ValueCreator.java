package com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.InformativeException;

public abstract class ValueCreator implements ValueWriter {
  public abstract void write(PacketWrapper paramPacketWrapper) throws Exception;
  
  public void write(PacketWrapper writer, Object inputValue) throws Exception {
    try {
      write(writer);
    } catch (InformativeException e) {
      e.addSource(getClass());
      throw e;
    } 
  }
}
