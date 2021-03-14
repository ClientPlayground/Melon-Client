package com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.exception.InformativeException;
import java.util.ArrayList;
import java.util.List;

public abstract class PacketRemapper {
  private final List<Pair<ValueReader, ValueWriter>> valueRemappers = new ArrayList<>();
  
  public PacketRemapper() {
    registerMap();
  }
  
  public void map(Type<?> type) {
    TypeRemapper<?> remapper = new TypeRemapper(type);
    map(remapper, remapper);
  }
  
  public void map(Type<?> oldType, Type<?> newType) {
    map(new TypeRemapper(oldType), new TypeRemapper(newType));
  }
  
  public <T1, T2> void map(Type<T1> oldType, ValueTransformer<T1, T2> transformer) {
    map(new TypeRemapper<>(oldType), transformer);
  }
  
  public <T> void map(ValueReader<T> inputReader, ValueWriter<T> outputWriter) {
    this.valueRemappers.add(new Pair(inputReader, outputWriter));
  }
  
  public void create(ValueCreator creator) {
    map(new TypeRemapper(Type.NOTHING), creator);
  }
  
  public void handler(PacketHandler handler) {
    map(new TypeRemapper(Type.NOTHING), handler);
  }
  
  public abstract void registerMap();
  
  public void remap(PacketWrapper packetWrapper) throws Exception {
    try {
      for (Pair<ValueReader, ValueWriter> valueRemapper : this.valueRemappers) {
        Object object = ((ValueReader)valueRemapper.getKey()).read(packetWrapper);
        ((ValueWriter<Object>)valueRemapper.getValue()).write(packetWrapper, object);
      } 
    } catch (InformativeException e) {
      e.addSource(getClass());
      throw e;
    } 
  }
}
