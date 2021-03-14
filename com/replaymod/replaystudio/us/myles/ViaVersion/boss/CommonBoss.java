package com.replaymod.replaystudio.us.myles.ViaVersion.boss;

import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossBar;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossColor;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossFlag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.boss.BossStyle;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;

public abstract class CommonBoss<T> extends BossBar<T> {
  private UUID uuid;
  
  private String title;
  
  private float health;
  
  private BossColor color;
  
  private BossStyle style;
  
  private Set<UUID> players;
  
  private boolean visible;
  
  private Set<BossFlag> flags;
  
  public UUID getUuid() {
    return this.uuid;
  }
  
  public String getTitle() {
    return this.title;
  }
  
  public float getHealth() {
    return this.health;
  }
  
  public BossStyle getStyle() {
    return this.style;
  }
  
  public Set<BossFlag> getFlags() {
    return this.flags;
  }
  
  public CommonBoss(String title, float health, BossColor color, BossStyle style) {
    Preconditions.checkNotNull(title, "Title cannot be null");
    Preconditions.checkArgument((health >= 0.0F && health <= 1.0F), "Health must be between 0 and 1");
    this.uuid = UUID.randomUUID();
    this.title = title;
    this.health = health;
    this.color = (color == null) ? BossColor.PURPLE : color;
    this.style = (style == null) ? BossStyle.SOLID : style;
    this.players = new HashSet<>();
    this.flags = new HashSet<>();
    this.visible = true;
  }
  
  public BossBar setTitle(@NonNull String title) {
    if (title == null)
      throw new NullPointerException("title is marked @NonNull but is null"); 
    this.title = title;
    sendPacket(UpdateAction.UPDATE_TITLE);
    return this;
  }
  
  public BossBar setHealth(float health) {
    Preconditions.checkArgument((health >= 0.0F && health <= 1.0F), "Health must be between 0 and 1");
    this.health = health;
    sendPacket(UpdateAction.UPDATE_HEALTH);
    return this;
  }
  
  public BossColor getColor() {
    return this.color;
  }
  
  public BossBar setColor(@NonNull BossColor color) {
    if (color == null)
      throw new NullPointerException("color is marked @NonNull but is null"); 
    this.color = color;
    sendPacket(UpdateAction.UPDATE_STYLE);
    return this;
  }
  
  public BossBar setStyle(@NonNull BossStyle style) {
    if (style == null)
      throw new NullPointerException("style is marked @NonNull but is null"); 
    this.style = style;
    sendPacket(UpdateAction.UPDATE_STYLE);
    return this;
  }
  
  public BossBar addPlayer(UUID player) {
    if (!this.players.contains(player)) {
      this.players.add(player);
      if (this.visible) {
        UserConnection user = Via.getManager().getConnection(player);
        sendPacket(player, getPacket(UpdateAction.ADD, user));
      } 
    } 
    return this;
  }
  
  public BossBar removePlayer(UUID uuid) {
    if (this.players.contains(uuid)) {
      this.players.remove(uuid);
      UserConnection user = Via.getManager().getConnection(uuid);
      sendPacket(uuid, getPacket(UpdateAction.REMOVE, user));
    } 
    return this;
  }
  
  public BossBar addFlag(@NonNull BossFlag flag) {
    if (flag == null)
      throw new NullPointerException("flag is marked @NonNull but is null"); 
    if (!hasFlag(flag))
      this.flags.add(flag); 
    sendPacket(UpdateAction.UPDATE_FLAGS);
    return this;
  }
  
  public BossBar removeFlag(@NonNull BossFlag flag) {
    if (flag == null)
      throw new NullPointerException("flag is marked @NonNull but is null"); 
    if (hasFlag(flag))
      this.flags.remove(flag); 
    sendPacket(UpdateAction.UPDATE_FLAGS);
    return this;
  }
  
  public boolean hasFlag(@NonNull BossFlag flag) {
    if (flag == null)
      throw new NullPointerException("flag is marked @NonNull but is null"); 
    return this.flags.contains(flag);
  }
  
  public Set<UUID> getPlayers() {
    return Collections.unmodifiableSet(this.players);
  }
  
  public BossBar show() {
    setVisible(true);
    return this;
  }
  
  public BossBar hide() {
    setVisible(false);
    return this;
  }
  
  public boolean isVisible() {
    return this.visible;
  }
  
  public UUID getId() {
    return this.uuid;
  }
  
  private void setVisible(boolean value) {
    if (this.visible != value) {
      this.visible = value;
      sendPacket(value ? UpdateAction.ADD : UpdateAction.REMOVE);
    } 
  }
  
  private void sendPacket(UpdateAction action) {
    for (UUID uuid : new ArrayList(this.players)) {
      UserConnection connection = Via.getManager().getConnection(uuid);
      PacketWrapper wrapper = getPacket(action, connection);
      sendPacket(uuid, wrapper);
    } 
  }
  
  private void sendPacket(UUID uuid, PacketWrapper wrapper) {
    if (!Via.getAPI().isPorted(uuid) || Via.getAPI().getPlayerVersion(uuid) < ProtocolVersion.v1_9.getId()) {
      this.players.remove(uuid);
      return;
    } 
    try {
      wrapper.send(Protocol1_9To1_8.class);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  private PacketWrapper getPacket(UpdateAction action, UserConnection connection) {
    try {
      PacketWrapper wrapper = new PacketWrapper(12, null, connection);
      wrapper.write(Type.UUID, this.uuid);
      wrapper.write(Type.VAR_INT, Integer.valueOf(action.getId()));
      switch (action) {
        case ADD:
          Protocol1_9To1_8.FIX_JSON.write(wrapper, this.title);
          wrapper.write(Type.FLOAT, Float.valueOf(this.health));
          wrapper.write(Type.VAR_INT, Integer.valueOf(this.color.getId()));
          wrapper.write(Type.VAR_INT, Integer.valueOf(this.style.getId()));
          wrapper.write(Type.BYTE, Byte.valueOf((byte)flagToBytes()));
          break;
        case UPDATE_HEALTH:
          wrapper.write(Type.FLOAT, Float.valueOf(this.health));
          break;
        case UPDATE_TITLE:
          Protocol1_9To1_8.FIX_JSON.write(wrapper, this.title);
          break;
        case UPDATE_STYLE:
          wrapper.write(Type.VAR_INT, Integer.valueOf(this.color.getId()));
          wrapper.write(Type.VAR_INT, Integer.valueOf(this.style.getId()));
          break;
        case UPDATE_FLAGS:
          wrapper.write(Type.BYTE, Byte.valueOf((byte)flagToBytes()));
          break;
      } 
      return wrapper;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  private int flagToBytes() {
    int bitmask = 0;
    for (BossFlag flag : this.flags)
      bitmask |= flag.getId(); 
    return bitmask;
  }
  
  private enum UpdateAction {
    ADD(0),
    REMOVE(1),
    UPDATE_HEALTH(2),
    UPDATE_TITLE(3),
    UPDATE_STYLE(4),
    UPDATE_FLAGS(5);
    
    UpdateAction(int id) {
      this.id = id;
    }
    
    private final int id;
    
    public int getId() {
      return this.id;
    }
  }
}
