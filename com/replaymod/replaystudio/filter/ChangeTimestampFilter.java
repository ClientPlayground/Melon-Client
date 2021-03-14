package com.replaymod.replaystudio.filter;

import com.google.gson.JsonObject;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.stream.PacketStream;

public class ChangeTimestampFilter implements StreamFilter {
  private long offset;
  
  public String getName() {
    return "timestamp";
  }
  
  public void init(Studio studio, JsonObject config) {
    this.offset = config.get("offset").getAsLong();
  }
  
  public void onStart(PacketStream stream) {}
  
  public boolean onPacket(PacketStream stream, PacketData data) {
    stream.insert(new PacketData(data.getTime() + this.offset, data.getPacket()));
    return false;
  }
  
  public void onEnd(PacketStream stream, long timestamp) {}
}
