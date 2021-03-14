package net.minecraft.network.play.server;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

public class S38PacketPlayerListItem implements Packet<INetHandlerPlayClient> {
  public Action action;
  
  private final List<AddPlayerData> players = Lists.newArrayList();
  
  public S38PacketPlayerListItem(Action actionIn, EntityPlayerMP... players) {
    this.action = actionIn;
    for (EntityPlayerMP entityplayermp : players)
      this.players.add(new AddPlayerData(entityplayermp.getGameProfile(), entityplayermp.ping, entityplayermp.theItemInWorldManager.getGameType(), entityplayermp.getTabListDisplayName())); 
  }
  
  public S38PacketPlayerListItem(Action actionIn, Iterable<EntityPlayerMP> players) {
    this.action = actionIn;
    for (EntityPlayerMP entityplayermp : players)
      this.players.add(new AddPlayerData(entityplayermp.getGameProfile(), entityplayermp.ping, entityplayermp.theItemInWorldManager.getGameType(), entityplayermp.getTabListDisplayName())); 
  }
  
  public void readPacketData(PacketBuffer buf) throws IOException {
    this.action = (Action)buf.readEnumValue(Action.class);
    int i = buf.readVarIntFromBuffer();
    for (int j = 0; j < i; j++) {
      int l, i1;
      GameProfile gameprofile = null;
      int k = 0;
      WorldSettings.GameType worldsettings$gametype = null;
      IChatComponent ichatcomponent = null;
      switch (this.action) {
        case ADD_PLAYER:
          gameprofile = new GameProfile(buf.readUuid(), buf.readStringFromBuffer(16));
          l = buf.readVarIntFromBuffer();
          i1 = 0;
          for (; i1 < l; i1++) {
            String s = buf.readStringFromBuffer(32767);
            String s1 = buf.readStringFromBuffer(32767);
            if (buf.readBoolean()) {
              gameprofile.getProperties().put(s, new Property(s, s1, buf.readStringFromBuffer(32767)));
            } else {
              gameprofile.getProperties().put(s, new Property(s, s1));
            } 
          } 
          worldsettings$gametype = WorldSettings.GameType.getByID(buf.readVarIntFromBuffer());
          k = buf.readVarIntFromBuffer();
          if (buf.readBoolean())
            ichatcomponent = buf.readChatComponent(); 
          break;
        case UPDATE_GAME_MODE:
          gameprofile = new GameProfile(buf.readUuid(), (String)null);
          worldsettings$gametype = WorldSettings.GameType.getByID(buf.readVarIntFromBuffer());
          break;
        case UPDATE_LATENCY:
          gameprofile = new GameProfile(buf.readUuid(), (String)null);
          k = buf.readVarIntFromBuffer();
          break;
        case UPDATE_DISPLAY_NAME:
          gameprofile = new GameProfile(buf.readUuid(), (String)null);
          if (buf.readBoolean())
            ichatcomponent = buf.readChatComponent(); 
          break;
        case REMOVE_PLAYER:
          gameprofile = new GameProfile(buf.readUuid(), (String)null);
          break;
      } 
      this.players.add(new AddPlayerData(gameprofile, k, worldsettings$gametype, ichatcomponent));
    } 
  }
  
  public void writePacketData(PacketBuffer buf) throws IOException {
    buf.writeEnumValue(this.action);
    buf.writeVarIntToBuffer(this.players.size());
    for (AddPlayerData s38packetplayerlistitem$addplayerdata : this.players) {
      switch (this.action) {
        case ADD_PLAYER:
          buf.writeUuid(s38packetplayerlistitem$addplayerdata.getProfile().getId());
          buf.writeString(s38packetplayerlistitem$addplayerdata.getProfile().getName());
          buf.writeVarIntToBuffer(s38packetplayerlistitem$addplayerdata.getProfile().getProperties().size());
          for (Property property : s38packetplayerlistitem$addplayerdata.getProfile().getProperties().values()) {
            buf.writeString(property.getName());
            buf.writeString(property.getValue());
            if (property.hasSignature()) {
              buf.writeBoolean(true);
              buf.writeString(property.getSignature());
              continue;
            } 
            buf.writeBoolean(false);
          } 
          buf.writeVarIntToBuffer(s38packetplayerlistitem$addplayerdata.getGameMode().getID());
          buf.writeVarIntToBuffer(s38packetplayerlistitem$addplayerdata.getPing());
          if (s38packetplayerlistitem$addplayerdata.getDisplayName() == null) {
            buf.writeBoolean(false);
            continue;
          } 
          buf.writeBoolean(true);
          buf.writeChatComponent(s38packetplayerlistitem$addplayerdata.getDisplayName());
        case UPDATE_GAME_MODE:
          buf.writeUuid(s38packetplayerlistitem$addplayerdata.getProfile().getId());
          buf.writeVarIntToBuffer(s38packetplayerlistitem$addplayerdata.getGameMode().getID());
        case UPDATE_LATENCY:
          buf.writeUuid(s38packetplayerlistitem$addplayerdata.getProfile().getId());
          buf.writeVarIntToBuffer(s38packetplayerlistitem$addplayerdata.getPing());
        case UPDATE_DISPLAY_NAME:
          buf.writeUuid(s38packetplayerlistitem$addplayerdata.getProfile().getId());
          if (s38packetplayerlistitem$addplayerdata.getDisplayName() == null) {
            buf.writeBoolean(false);
            continue;
          } 
          buf.writeBoolean(true);
          buf.writeChatComponent(s38packetplayerlistitem$addplayerdata.getDisplayName());
        case REMOVE_PLAYER:
          buf.writeUuid(s38packetplayerlistitem$addplayerdata.getProfile().getId());
      } 
    } 
  }
  
  public void processPacket(INetHandlerPlayClient handler) {
    handler.handlePlayerListItem(this);
  }
  
  public List<AddPlayerData> getEntries() {
    return this.players;
  }
  
  public Action getAction() {
    return this.action;
  }
  
  public String toString() {
    return Objects.toStringHelper(this).add("action", this.action).add("entries", this.players).toString();
  }
  
  public S38PacketPlayerListItem() {}
  
  public enum Action {
    ADD_PLAYER, UPDATE_GAME_MODE, UPDATE_LATENCY, UPDATE_DISPLAY_NAME, REMOVE_PLAYER;
  }
  
  public static class AddPlayerData {
    private final int ping;
    
    private final WorldSettings.GameType gamemode;
    
    private final GameProfile profile;
    
    private final IChatComponent displayName;
    
    public AddPlayerData(GameProfile profile, int pingIn, WorldSettings.GameType gamemodeIn, IChatComponent displayNameIn) {
      this.profile = profile;
      this.ping = pingIn;
      this.gamemode = gamemodeIn;
      this.displayName = displayNameIn;
    }
    
    public GameProfile getProfile() {
      return this.profile;
    }
    
    public int getPing() {
      return this.ping;
    }
    
    public WorldSettings.GameType getGameMode() {
      return this.gamemode;
    }
    
    public IChatComponent getDisplayName() {
      return this.displayName;
    }
    
    public String toString() {
      return Objects.toStringHelper(this).add("latency", this.ping).add("gameMode", this.gamemode).add("profile", this.profile).add("displayName", (this.displayName == null) ? null : IChatComponent.Serializer.componentToJson(this.displayName)).toString();
    }
  }
}
