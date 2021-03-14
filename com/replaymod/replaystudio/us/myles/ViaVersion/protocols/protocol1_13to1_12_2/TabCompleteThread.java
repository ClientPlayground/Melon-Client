package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.TabCompleteTracker;

public class TabCompleteThread implements Runnable {
  public void run() {
    for (UserConnection info : Via.getManager().getPortedPlayers().values()) {
      if (info.has(ProtocolInfo.class) && ((ProtocolInfo)info.get(ProtocolInfo.class)).getPipeline().contains(Protocol1_13To1_12_2.class) && 
        info.getChannel().isOpen())
        ((TabCompleteTracker)info.get(TabCompleteTracker.class)).sendPacketToServer(); 
    } 
  }
}
