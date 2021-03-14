package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.GsonUtil;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;

public class BaseProtocol1_7 extends Protocol {
  protected void registerPackets() {
    registerOutgoing(State.STATUS, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ProtocolInfo info = (ProtocolInfo)wrapper.user().get(ProtocolInfo.class);
                    String originalStatus = (String)wrapper.get(Type.STRING, 0);
                    try {
                      JsonObject jsonObject1, version;
                      JsonElement json = (JsonElement)GsonUtil.getGson().fromJson(originalStatus, JsonElement.class);
                      int protocolVersion = 0;
                      if (json.isJsonObject()) {
                        if (json.getAsJsonObject().has("version")) {
                          version = json.getAsJsonObject().get("version").getAsJsonObject();
                          if (version.has("protocol"))
                            protocolVersion = Long.valueOf(version.get("protocol").getAsLong()).intValue(); 
                        } else {
                          version = new JsonObject();
                          json.getAsJsonObject().add("version", (JsonElement)version);
                        } 
                      } else {
                        jsonObject1 = new JsonObject();
                        version = new JsonObject();
                        jsonObject1.getAsJsonObject().add("version", (JsonElement)version);
                      } 
                      if (Via.getConfig().isSendSupportedVersions())
                        version.add("supportedVersions", GsonUtil.getGson().toJsonTree(Via.getAPI().getSupportedVersions())); 
                      if (ProtocolRegistry.SERVER_PROTOCOL == -1)
                        ProtocolRegistry.SERVER_PROTOCOL = protocolVersion; 
                      if (Via.getManager().getProviders().get(VersionProvider.class) == null) {
                        wrapper.user().setActive(false);
                        return;
                      } 
                      int protocol = ((VersionProvider)Via.getManager().getProviders().get(VersionProvider.class)).getServerProtocol(wrapper.user());
                      List<Pair<Integer, Protocol>> protocols = null;
                      if (info.getProtocolVersion() >= protocol || Via.getPlatform().isOldClientsAllowed())
                        protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocol); 
                      if (protocols != null) {
                        if (protocolVersion == protocol || protocolVersion == 0)
                          version.addProperty("protocol", Integer.valueOf(info.getProtocolVersion())); 
                      } else {
                        wrapper.user().setActive(false);
                      } 
                      if (Via.getConfig().getBlockedProtocols().contains(Integer.valueOf(info.getProtocolVersion())))
                        version.addProperty("protocol", Integer.valueOf(-1)); 
                      wrapper.set(Type.STRING, 0, GsonUtil.getGson().toJson((JsonElement)jsonObject1));
                    } catch (JsonParseException e) {
                      e.printStackTrace();
                    } 
                  }
                });
          }
        });
    registerOutgoing(State.STATUS, 1, 1);
    registerOutgoing(State.LOGIN, 0, 0);
    registerOutgoing(State.LOGIN, 1, 1);
    registerOutgoing(State.LOGIN, 2, 2, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ProtocolInfo info = (ProtocolInfo)wrapper.user().get(ProtocolInfo.class);
                    info.setState(State.PLAY);
                    String stringUUID = (String)wrapper.get(Type.STRING, 0);
                    if (stringUUID.length() == 32)
                      stringUUID = BaseProtocol1_7.addDashes(stringUUID); 
                    UUID uuid = UUID.fromString(stringUUID);
                    info.setUuid(uuid);
                    info.setUsername((String)wrapper.get(Type.STRING, 1));
                    Via.getManager().addPortedClient(wrapper.user());
                    if (info.getPipeline().pipes().size() == 2 && ((Protocol)info
                      .getPipeline().pipes().get(1)).getClass() == BaseProtocol1_7.class && ((Protocol)info
                      .getPipeline().pipes().get(0)).getClass() == BaseProtocol.class)
                      wrapper.user().setActive(false); 
                    if (Via.getManager().isDebug())
                      Via.getPlatform().getLogger().log(Level.INFO, "{0} logged in with protocol {1}, Route: {2}", new Object[] { wrapper
                            
                            .get(Type.STRING, 1), 
                            Integer.valueOf(info.getProtocolVersion()), 
                            Joiner.on(", ").join(info.getPipeline().pipes(), ", ", new Object[0]) }); 
                  }
                });
          }
        });
    registerOutgoing(State.LOGIN, 3, 3);
    registerIncoming(State.LOGIN, 4, 4);
    registerIncoming(State.STATUS, 0, 0);
    registerIncoming(State.STATUS, 1, 1);
    registerIncoming(State.LOGIN, 0, 0, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(final PacketWrapper wrapper) throws Exception {
                    int protocol = ((ProtocolInfo)wrapper.user().get(ProtocolInfo.class)).getProtocolVersion();
                    if (Via.getConfig().getBlockedProtocols().contains(Integer.valueOf(protocol))) {
                      if (!wrapper.user().getChannel().isOpen())
                        return; 
                      PacketWrapper disconnectPacket = new PacketWrapper(0, null, wrapper.user());
                      Protocol1_9To1_8.FIX_JSON.write(disconnectPacket, ChatColor.translateAlternateColorCodes('&', Via.getConfig().getBlockedDisconnectMsg()));
                      wrapper.cancel();
                      ChannelFuture future = disconnectPacket.sendFuture(BaseProtocol.class);
                      future.addListener(new GenericFutureListener<Future<? super Void>>() {
                            public void operationComplete(Future<? super Void> future) throws Exception {
                              wrapper.user().getChannel().close();
                            }
                          });
                    } 
                  }
                });
          }
        });
    registerIncoming(State.LOGIN, 1, 1);
    registerIncoming(State.LOGIN, 2, 2);
  }
  
  public void init(UserConnection userConnection) {}
  
  public static String addDashes(String trimmedUUID) {
    StringBuilder idBuff = new StringBuilder(trimmedUUID);
    idBuff.insert(20, '-');
    idBuff.insert(16, '-');
    idBuff.insert(12, '-');
    idBuff.insert(8, '-');
    return idBuff.toString();
  }
}
