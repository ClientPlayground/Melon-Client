package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.ViaProviders;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.Direction;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import java.util.List;

public class BaseProtocol extends Protocol {
  protected void registerPackets() {
    registerIncoming(State.HANDSHAKE, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.STRING);
            map(Type.UNSIGNED_SHORT);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int protVer = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int state = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    ProtocolInfo info = (ProtocolInfo)wrapper.user().get(ProtocolInfo.class);
                    info.setProtocolVersion(protVer);
                    if (Via.getManager().getProviders().get(VersionProvider.class) == null) {
                      wrapper.user().setActive(false);
                      return;
                    } 
                    int protocol = ((VersionProvider)Via.getManager().getProviders().get(VersionProvider.class)).getServerProtocol(wrapper.user());
                    info.setServerProtocolVersion(protocol);
                    List<Pair<Integer, Protocol>> protocols = null;
                    if (info.getProtocolVersion() >= protocol || Via.getPlatform().isOldClientsAllowed())
                      protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocol); 
                    ProtocolPipeline pipeline = ((ProtocolInfo)wrapper.user().get(ProtocolInfo.class)).getPipeline();
                    if (protocols != null) {
                      for (Pair<Integer, Protocol> prot : protocols)
                        pipeline.add((Protocol)prot.getValue()); 
                      wrapper.set(Type.VAR_INT, 0, Integer.valueOf(protocol));
                    } 
                    pipeline.add(ProtocolRegistry.getBaseProtocol(protocol));
                    if (state == 1)
                      info.setState(State.STATUS); 
                    if (state == 2)
                      info.setState(State.LOGIN); 
                  }
                });
          }
        });
  }
  
  public void init(UserConnection userConnection) {}
  
  protected void register(ViaProviders providers) {
    providers.register(VersionProvider.class, new VersionProvider());
  }
  
  public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
    super.transform(direction, state, packetWrapper);
    if (direction == Direction.INCOMING && state == State.HANDSHAKE)
      if (packetWrapper.getId() != 0)
        packetWrapper.user().setActive(false);  
  }
}
