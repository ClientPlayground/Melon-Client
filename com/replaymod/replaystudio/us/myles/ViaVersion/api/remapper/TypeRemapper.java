package com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class TypeRemapper<T> implements ValueReader<T>, ValueWriter<T> {
  private final Type<T> type;
  
  public TypeRemapper(Type<T> type) {
    this.type = type;
  }
  
  public T read(PacketWrapper wrapper) throws Exception {
    return (T)wrapper.read(this.type);
  }
  
  public void write(PacketWrapper output, T inputValue) {
    output.write(this.type, inputValue);
  }
}
