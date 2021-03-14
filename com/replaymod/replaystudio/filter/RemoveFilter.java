package com.replaymod.replaystudio.filter;

import com.google.gson.JsonObject;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.stream.PacketStream;
import java.util.function.Predicate;

public class RemoveFilter implements StreamFilter {
  private Predicate<PacketData> filter = packetData -> true;
  
  public String getName() {
    return "remove";
  }
  
  public void init(Studio studio, JsonObject config) {
    if (config.has("type")) {
      String name = config.get("type").getAsString();
      PacketType type = PacketType.valueOf(name);
      this.filter = (d -> (d.getPacket().getType() == type));
    } 
  }
  
  public void onStart(PacketStream stream) {}
  
  public boolean onPacket(PacketStream stream, PacketData data) {
    return !this.filter.test(data);
  }
  
  public void onEnd(PacketStream stream, long timestamp) {}
}
