package com.replaymod.replaystudio.studio;

import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.stream.AbstractPacketStream;
import java.io.IOException;

public class StudioPacketStream extends AbstractPacketStream {
  private final ReplayInputStream in;
  
  public StudioPacketStream(ReplayInputStream in) {
    this.in = in;
  }
  
  protected PacketData nextInput() {
    try {
      return this.in.readPacket();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public void start() {}
  
  protected void cleanup() {
    try {
      this.in.close();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
}
