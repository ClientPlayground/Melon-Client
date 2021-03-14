package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.ViaProviders;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.PacketRemapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.remapper.ValueTransformer;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets.EntityPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets.PlayerPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets.SpawnPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.packets.WorldPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MainHandProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.CommandBlockStorage;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.InventoryTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.MovementTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.PlaceBlockTracker;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.GsonUtil;
import java.util.List;

public class Protocol1_9To1_8 extends Protocol {
  public static final ValueTransformer<String, String> FIX_JSON = new ValueTransformer<String, String>(Type.STRING) {
      public String transform(PacketWrapper wrapper, String line) {
        return Protocol1_9To1_8.fixJson(line);
      }
    };
  
  public static String fixJson(String line) {
    if (line == null || line.equalsIgnoreCase("null")) {
      line = "{\"text\":\"\"}";
    } else {
      if ((!line.startsWith("\"") || !line.endsWith("\"")) && (!line.startsWith("{") || !line.endsWith("}")))
        return constructJson(line); 
      if (line.startsWith("\"") && line.endsWith("\""))
        line = "{\"text\":" + line + "}"; 
    } 
    try {
      GsonUtil.getGson().fromJson(line, JsonObject.class);
    } catch (Exception e) {
      if (Via.getConfig().isForceJsonTransform())
        return constructJson(line); 
      Via.getPlatform().getLogger().warning("Invalid JSON String: \"" + line + "\" Please report this issue to the ViaVersion Github: " + e.getMessage());
      return "{\"text\":\"\"}";
    } 
    return line;
  }
  
  private static String constructJson(String text) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("text", text);
    return GsonUtil.getGson().toJson((JsonElement)jsonObject);
  }
  
  public static Item getHandItem(UserConnection info) {
    return ((HandItemProvider)Via.getManager().getProviders().get(HandItemProvider.class)).getHandItem(info);
  }
  
  public static boolean isSword(int id) {
    if (id == 267)
      return true; 
    if (id == 268)
      return true; 
    if (id == 272)
      return true; 
    if (id == 276)
      return true; 
    if (id == 283)
      return true; 
    return false;
  }
  
  protected void registerPackets() {
    registerOutgoing(State.LOGIN, 0, 0, new PacketRemapper() {
          public void registerMap() {
            map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
          }
        });
    SpawnPackets.register(this);
    InventoryPackets.register(this);
    EntityPackets.register(this);
    PlayerPackets.register(this);
    WorldPackets.register(this);
  }
  
  protected void register(ViaProviders providers) {
    providers.register(HandItemProvider.class, (Provider)new HandItemProvider());
    providers.register(BulkChunkTranslatorProvider.class, (Provider)new BulkChunkTranslatorProvider());
    providers.register(CommandBlockProvider.class, (Provider)new CommandBlockProvider());
    providers.register(EntityIdProvider.class, (Provider)new EntityIdProvider());
    providers.register(BossBarProvider.class, (Provider)new BossBarProvider());
    providers.register(MainHandProvider.class, (Provider)new MainHandProvider());
    providers.require(MovementTransmitterProvider.class);
    if (Via.getConfig().isStimulatePlayerTick())
      Via.getPlatform().runRepeatingSync(new ViaIdleThread(), Long.valueOf(1L)); 
  }
  
  public boolean isFiltered(Class packetClass) {
    return ((BulkChunkTranslatorProvider)Via.getManager().getProviders().get(BulkChunkTranslatorProvider.class)).isFiltered(packetClass);
  }
  
  protected void filterPacket(UserConnection info, Object packet, List output) throws Exception {
    output.addAll(((ClientChunks)info.get(ClientChunks.class)).transformMapChunkBulk(packet));
  }
  
  public void init(UserConnection userConnection) {
    userConnection.put((StoredObject)new EntityTracker(userConnection));
    userConnection.put((StoredObject)new ClientChunks(userConnection));
    userConnection.put((StoredObject)new MovementTracker(userConnection));
    userConnection.put((StoredObject)new InventoryTracker(userConnection));
    userConnection.put((StoredObject)new PlaceBlockTracker(userConnection));
    userConnection.put((StoredObject)new CommandBlockStorage(userConnection));
  }
}
