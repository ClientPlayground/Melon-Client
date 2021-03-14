package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.entities.Entity1_10Types;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketHandler;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueCreator;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.ProtocolInfo;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.PlayerMovementMapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.chat.ChatRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.chat.GameMode;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MainHandProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class PlayerPackets {
  public static void register(Protocol protocol) {
    protocol.registerOutgoing(State.PLAY, 2, 15, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    try {
                      JsonObject obj = (JsonObject)(new JsonParser()).parse((String)wrapper.get(Type.STRING, 0));
                      ChatRewriter.toClient(obj, wrapper.user());
                      wrapper.set(Type.STRING, 0, obj.toString());
                    } catch (Exception e) {
                      e.printStackTrace();
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 71, 72, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
            map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
          }
        });
    protocol.registerOutgoing(State.PLAY, 64, 26, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
          }
        });
    protocol.registerOutgoing(State.PLAY, 69, 69, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int action = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (action == 0 || action == 1)
                      Protocol1_9To1_8.FIX_JSON.write(wrapper, wrapper.read(Type.STRING)); 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 8, 46, new PacketRemapper() {
          public void registerMap() {
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.BYTE);
            create(new ValueCreator() {
                  public void write(PacketWrapper wrapper) {
                    wrapper.write(Type.VAR_INT, Integer.valueOf(0));
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 62, 65, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    byte mode = ((Byte)wrapper.get(Type.BYTE, 0)).byteValue();
                    if (mode == 0 || mode == 2) {
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.BYTE);
                      wrapper.passthrough(Type.STRING);
                      wrapper.write(Type.STRING, Via.getConfig().isPreventCollision() ? "never" : "");
                      wrapper.passthrough(Type.BYTE);
                    } 
                    if (mode == 0 || mode == 3 || mode == 4) {
                      String[] players = (String[])wrapper.passthrough(Type.STRING_ARRAY);
                      EntityTracker entityTracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                      String myName = ((ProtocolInfo)wrapper.user().get(ProtocolInfo.class)).getUsername();
                      String teamName = (String)wrapper.get(Type.STRING, 0);
                      for (String player : players) {
                        if (entityTracker.isAutoTeam() && player.equalsIgnoreCase(myName))
                          if (mode == 4) {
                            wrapper.send(Protocol1_9To1_8.class, true, true);
                            wrapper.cancel();
                            entityTracker.sendTeamPacket(true, true);
                            entityTracker.setCurrentTeam("viaversion");
                          } else {
                            entityTracker.sendTeamPacket(false, true);
                            entityTracker.setCurrentTeam(teamName);
                          }  
                      } 
                    } 
                    if (mode == 1) {
                      EntityTracker entityTracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                      String teamName = (String)wrapper.get(Type.STRING, 0);
                      if (entityTracker.isAutoTeam() && teamName
                        .equals(entityTracker.getCurrentTeam())) {
                        wrapper.send(Protocol1_9To1_8.class, true, true);
                        wrapper.cancel();
                        entityTracker.sendTeamPacket(true, true);
                        entityTracker.setCurrentTeam("viaversion");
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 1, 35, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int entityID = ((Integer)wrapper.get(Type.INT, 0)).intValue();
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.getClientEntityTypes().put(Integer.valueOf(entityID), Entity1_10Types.EntityType.PLAYER);
                    tracker.setEntityID(entityID);
                  }
                });
            map(Type.UNSIGNED_BYTE);
            map(Type.BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.STRING);
            map(Type.BOOLEAN);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    tracker.setGameMode(GameMode.getById(((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue()));
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    CommandBlockProvider provider = (CommandBlockProvider)Via.getManager().getProviders().get(CommandBlockProvider.class);
                    provider.sendPermission(wrapper.user());
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    EntityTracker entityTracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                    if (Via.getConfig().isAutoTeam()) {
                      entityTracker.setAutoTeam(true);
                      wrapper.send(Protocol1_9To1_8.class, true, true);
                      wrapper.cancel();
                      entityTracker.sendTeamPacket(true, true);
                      entityTracker.setCurrentTeam("viaversion");
                    } else {
                      entityTracker.setAutoTeam(false);
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 56, 45, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int action = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    int count = ((Integer)wrapper.get(Type.VAR_INT, 1)).intValue();
                    for (int i = 0; i < count; i++) {
                      wrapper.passthrough(Type.UUID);
                      if (action == 0) {
                        wrapper.passthrough(Type.STRING);
                        int properties = ((Integer)wrapper.passthrough(Type.VAR_INT)).intValue();
                        for (int j = 0; j < properties; j++) {
                          wrapper.passthrough(Type.STRING);
                          wrapper.passthrough(Type.STRING);
                          boolean isSigned = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                          if (isSigned)
                            wrapper.passthrough(Type.STRING); 
                        } 
                        wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.VAR_INT);
                        boolean hasDisplayName = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (hasDisplayName)
                          Protocol1_9To1_8.FIX_JSON.write(wrapper, wrapper.read(Type.STRING)); 
                      } else if (action == 1 || action == 2) {
                        wrapper.passthrough(Type.VAR_INT);
                      } else if (action == 3) {
                        boolean hasDisplayName = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (hasDisplayName)
                          Protocol1_9To1_8.FIX_JSON.write(wrapper, wrapper.read(Type.STRING)); 
                      } else if (action == 4) {
                      
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 63, 24, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String name = (String)wrapper.get(Type.STRING, 0);
                    if (name.equalsIgnoreCase("MC|BOpen")) {
                      wrapper.read(Type.REMAINING_BYTES);
                      wrapper.write(Type.VAR_INT, Integer.valueOf(0));
                    } 
                    if (name.equalsIgnoreCase("MC|TrList")) {
                      wrapper.passthrough(Type.INT);
                      Short size = (Short)wrapper.passthrough(Type.UNSIGNED_BYTE);
                      for (int i = 0; i < size.shortValue(); i++) {
                        Item item1 = (Item)wrapper.passthrough(Type.ITEM);
                        ItemRewriter.toClient(item1);
                        Item item2 = (Item)wrapper.passthrough(Type.ITEM);
                        ItemRewriter.toClient(item2);
                        boolean present = ((Boolean)wrapper.passthrough(Type.BOOLEAN)).booleanValue();
                        if (present) {
                          Item item3 = (Item)wrapper.passthrough(Type.ITEM);
                          ItemRewriter.toClient(item3);
                        } 
                        wrapper.passthrough(Type.BOOLEAN);
                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.INT);
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 6, 62, new PacketRemapper() {
          public void registerMap() {
            map(Type.FLOAT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    float health = ((Float)wrapper.get(Type.FLOAT, 0)).floatValue();
                    if (health <= 0.0F) {
                      ClientChunks cc = (ClientChunks)wrapper.user().get(ClientChunks.class);
                      cc.getBulkChunks().clear();
                      cc.getLoadedChunks().clear();
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 7, 51, new PacketRemapper() {
          public void registerMap() {
            map(Type.INT);
            map(Type.UNSIGNED_BYTE);
            map(Type.UNSIGNED_BYTE);
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    ClientChunks cc = (ClientChunks)wrapper.user().get(ClientChunks.class);
                    cc.getBulkChunks().clear();
                    cc.getLoadedChunks().clear();
                    int gamemode = ((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue();
                    ((EntityTracker)wrapper.user().get(EntityTracker.class)).setGameMode(GameMode.getById(gamemode));
                  }
                });
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    CommandBlockProvider provider = (CommandBlockProvider)Via.getManager().getProviders().get(CommandBlockProvider.class);
                    provider.sendPermission(wrapper.user());
                    provider.unloadChunks(wrapper.user());
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 43, 30, new PacketRemapper() {
          public void registerMap() {
            map(Type.UNSIGNED_BYTE);
            map(Type.FLOAT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    if (((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).shortValue() == 3) {
                      int gamemode = ((Float)wrapper.get(Type.FLOAT, 0)).intValue();
                      ((EntityTracker)wrapper.user().get(EntityTracker.class)).setGameMode(GameMode.getById(gamemode));
                    } 
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 70, 70, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    protocol.registerOutgoing(State.PLAY, 58, 14);
    protocol.registerOutgoing(State.PLAY, 11, 6);
    protocol.registerOutgoing(State.PLAY, 55, 7);
    protocol.registerOutgoing(State.PLAY, 54, 42);
    protocol.registerOutgoing(State.PLAY, 57, 43);
    protocol.registerOutgoing(State.PLAY, 0, 31);
    protocol.registerOutgoing(State.PLAY, 72, 50);
    protocol.registerOutgoing(State.PLAY, 67, 54);
    protocol.registerOutgoing(State.PLAY, 61, 56);
    protocol.registerOutgoing(State.PLAY, 59, 63);
    protocol.registerOutgoing(State.PLAY, 60, 66);
    protocol.registerOutgoing(State.PLAY, 5, 67);
    protocol.registerOutgoing(State.PLAY, 31, 61);
    protocol.registerOutgoing(State.PLAY, 13, 73);
    protocol.registerIncoming(State.PLAY, 20, 1, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.BOOLEAN, Type.NOTHING);
          }
        });
    protocol.registerIncoming(State.PLAY, 21, 4, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            map(Type.BYTE);
            map(Type.VAR_INT, Type.BYTE);
            map(Type.BOOLEAN);
            map(Type.UNSIGNED_BYTE);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int hand = ((Integer)wrapper.read(Type.VAR_INT)).intValue();
                    if (Via.getConfig().isLeftHandedHandling())
                      if (hand == 0)
                        wrapper.set(Type.UNSIGNED_BYTE, 0, 
                            Short.valueOf((short)(((Short)wrapper.get(Type.UNSIGNED_BYTE, 0)).intValue() | 0x80)));  
                    wrapper.sendToServer(Protocol1_9To1_8.class, true, true);
                    wrapper.cancel();
                    ((MainHandProvider)Via.getManager().getProviders().get(MainHandProvider.class)).setMainHand(wrapper.user(), hand);
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 10, 26, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT, Type.NOTHING);
          }
        });
    protocol.registerIncoming(State.PLAY, -1, 0, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, -1, 16, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, -1, 17, new PacketRemapper() {
          public void registerMap() {
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    wrapper.cancel();
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 23, 9, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    String name = (String)wrapper.get(Type.STRING, 0);
                    if (name.equalsIgnoreCase("MC|BSign")) {
                      Item item = (Item)wrapper.passthrough(Type.ITEM);
                      if (item != null) {
                        item.setIdentifier(387);
                        ItemRewriter.rewriteBookToServer(item);
                      } 
                    } 
                    if (name.equalsIgnoreCase("MC|AutoCmd")) {
                      wrapper.set(Type.STRING, 0, "MC|AdvCdm");
                      wrapper.write(Type.BYTE, Byte.valueOf((byte)0));
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.INT);
                      wrapper.passthrough(Type.STRING);
                      wrapper.passthrough(Type.BOOLEAN);
                      wrapper.clearInputBuffer();
                    } 
                    if (name.equalsIgnoreCase("MC|AdvCmd"))
                      wrapper.set(Type.STRING, 0, "MC|AdvCdm"); 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 22, 3, new PacketRemapper() {
          public void registerMap() {
            map(Type.VAR_INT);
            handler(new PacketHandler() {
                  public void handle(PacketWrapper wrapper) throws Exception {
                    int action = ((Integer)wrapper.get(Type.VAR_INT, 0)).intValue();
                    if (action == 2) {
                      EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
                      if (tracker.isBlocking()) {
                        tracker.setSecondHand(null);
                        tracker.setBlocking(false);
                      } 
                    } 
                  }
                });
          }
        });
    protocol.registerIncoming(State.PLAY, 4, 12, new PacketRemapper() {
          public void registerMap() {
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.BOOLEAN);
            handler((PacketHandler)new PlayerMovementMapper());
          }
        });
    protocol.registerIncoming(State.PLAY, 6, 13, new PacketRemapper() {
          public void registerMap() {
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.DOUBLE);
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.BOOLEAN);
            handler((PacketHandler)new PlayerMovementMapper());
          }
        });
    protocol.registerIncoming(State.PLAY, 5, 14, new PacketRemapper() {
          public void registerMap() {
            map(Type.FLOAT);
            map(Type.FLOAT);
            map(Type.BOOLEAN);
            handler((PacketHandler)new PlayerMovementMapper());
          }
        });
    protocol.registerIncoming(State.PLAY, 3, 15, new PacketRemapper() {
          public void registerMap() {
            map(Type.BOOLEAN);
            handler((PacketHandler)new PlayerMovementMapper());
          }
        });
    protocol.registerIncoming(State.PLAY, 1, 2);
    protocol.registerIncoming(State.PLAY, 19, 18);
    protocol.registerIncoming(State.PLAY, 25, 22);
    protocol.registerIncoming(State.PLAY, 0, 11);
  }
}
