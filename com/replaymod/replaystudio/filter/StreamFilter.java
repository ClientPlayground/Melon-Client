package com.replaymod.replaystudio.filter;

import com.google.gson.JsonObject;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.stream.PacketStream;
import java.io.IOException;

public interface StreamFilter {
  String getName();
  
  void init(Studio paramStudio, JsonObject paramJsonObject);
  
  void onStart(PacketStream paramPacketStream) throws IOException;
  
  boolean onPacket(PacketStream paramPacketStream, PacketData paramPacketData) throws IOException;
  
  void onEnd(PacketStream paramPacketStream, long paramLong) throws IOException;
}
