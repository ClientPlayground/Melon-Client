package com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.InformativeException;

public abstract class ValueTransformer<T1, T2> implements ValueWriter<T1> {
  private final Type<T2> outputType;
  
  public ValueTransformer(Type<T2> outputType) {
    this.outputType = outputType;
  }
  
  public abstract T2 transform(PacketWrapper paramPacketWrapper, T1 paramT1) throws Exception;
  
  public void write(PacketWrapper writer, T1 inputValue) throws Exception {
    try {
      writer.write(this.outputType, transform(writer, inputValue));
    } catch (InformativeException e) {
      e.addSource(getClass());
      throw e;
    } 
  }
}
