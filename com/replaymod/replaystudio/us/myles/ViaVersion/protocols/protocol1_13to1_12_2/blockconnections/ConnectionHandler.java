package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;

public abstract class ConnectionHandler {
  public abstract int connect(UserConnection paramUserConnection, Position paramPosition, int paramInt);
  
  public int getBlockData(UserConnection user, Position position) {
    return ((BlockConnectionProvider)Via.getManager().getProviders().get(BlockConnectionProvider.class)).getBlockdata(user, position);
  }
  
  public boolean canConnect(int id) {
    ConnectionHandler handler = ConnectionData.connectionHandlerMap.get(Integer.valueOf(id));
    return (handler != null && handler == this);
  }
}
