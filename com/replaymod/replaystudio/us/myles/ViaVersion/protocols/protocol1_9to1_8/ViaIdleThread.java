package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;

public class ViaIdleThread implements Runnable {
  public void run() {
    for (UserConnection info : Via.getManager().getPortedPlayers().values()) {
      ProtocolInfo protocolInfo = (ProtocolInfo)info.get(ProtocolInfo.class);
      if (protocolInfo != null && protocolInfo.getPipeline().contains(Protocol1_9To1_8.class)) {
        long nextIdleUpdate = ((MovementTracker)info.get(MovementTracker.class)).getNextIdlePacket();
        if (nextIdleUpdate <= System.currentTimeMillis() && 
          info.getChannel().isOpen())
          ((MovementTransmitterProvider)Via.getManager().getProviders().get(MovementTransmitterProvider.class)).sendPlayer(info); 
      } 
    } 
  }
}
