package com.replaymod.replaystudio.filter;

import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.JsonObject;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.stream.PacketStream;
import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;

public class PacketCountFilter implements StreamFilter {
  private final EnumMap<PacketType, MutableInt> count = new EnumMap<>(PacketType.class);
  
  public String getName() {
    return "packet_count";
  }
  
  public void init(Studio studio, JsonObject config) {}
  
  public void onStart(PacketStream stream) {
    this.count.clear();
  }
  
  public boolean onPacket(PacketStream stream, PacketData data) {
    PacketType type = data.getPacket().getType();
    ((MutableInt)this.count.computeIfAbsent(type, key -> new MutableInt())).increment();
    return true;
  }
  
  public void onEnd(PacketStream stream, long timestamp) {
    System.out.println();
    System.out.println();
    Ordering<Map.Entry<PacketType, MutableInt>> entryOrdering = Ordering.natural().reverse().onResultOf(Map.Entry::getValue);
    for (UnmodifiableIterator<Map.Entry<PacketType, MutableInt>> unmodifiableIterator = entryOrdering.immutableSortedCopy(this.count.entrySet()).iterator(); unmodifiableIterator.hasNext(); ) {
      Map.Entry<PacketType, MutableInt> e = unmodifiableIterator.next();
      System.out.println(String.format("[%dx] %s", new Object[] { Integer.valueOf(((MutableInt)e.getValue()).intValue()), ((PacketType)e.getKey()).toString() }));
    } 
    System.out.println();
  }
}
