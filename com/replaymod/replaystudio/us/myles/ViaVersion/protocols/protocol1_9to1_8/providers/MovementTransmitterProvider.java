package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.PipelineUtil;

public abstract class MovementTransmitterProvider implements Provider {
  public abstract Object getFlyingPacket();
  
  public abstract Object getGroundPacket();
  
  public void sendPlayer(UserConnection userConnection) {
    ChannelHandlerContext context = PipelineUtil.getContextBefore("decoder", userConnection.getChannel().pipeline());
    if (context != null) {
      if (((MovementTracker)userConnection.get(MovementTracker.class)).isGround()) {
        context.fireChannelRead(getGroundPacket());
      } else {
        context.fireChannelRead(getFlyingPacket());
      } 
      ((MovementTracker)userConnection.get(MovementTracker.class)).incrementIdlePacket();
    } 
  }
}
