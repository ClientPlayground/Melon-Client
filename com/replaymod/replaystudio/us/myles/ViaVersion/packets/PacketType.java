package com.replaymod.replaystudio.us.myles.ViaVersion.packets;

import java.util.HashMap;

@Deprecated
public enum PacketType {
  HANDSHAKE(State.HANDSHAKE, Direction.INCOMING, 0),
  LOGIN_START(State.LOGIN, Direction.INCOMING, 0),
  LOGIN_ENCRYPTION_RESPONSE(State.LOGIN, Direction.INCOMING, 1),
  LOGIN_DISCONNECT(State.LOGIN, Direction.OUTGOING, 0),
  LOGIN_ENCRYPTION_REQUEST(State.LOGIN, Direction.OUTGOING, 1),
  LOGIN_SUCCESS(State.LOGIN, Direction.OUTGOING, 2),
  LOGIN_SETCOMPRESSION(State.LOGIN, Direction.OUTGOING, 3),
  STATUS_REQUEST(State.STATUS, Direction.INCOMING, 0),
  STATUS_PING(State.STATUS, Direction.INCOMING, 1),
  STATUS_RESPONSE(State.STATUS, Direction.OUTGOING, 0),
  STATUS_PONG(State.STATUS, Direction.OUTGOING, 1),
  PLAY_TP_CONFIRM(State.PLAY, Direction.INCOMING, -1, 0),
  PLAY_TAB_COMPLETE_REQUEST(State.PLAY, Direction.INCOMING, 20, 1),
  PLAY_CHAT_MESSAGE_CLIENT(State.PLAY, Direction.INCOMING, 1, 2),
  PLAY_CLIENT_STATUS(State.PLAY, Direction.INCOMING, 22, 3),
  PLAY_CLIENT_SETTINGS(State.PLAY, Direction.INCOMING, 21, 4),
  PLAY_CONFIRM_TRANS(State.PLAY, Direction.INCOMING, 15, 5),
  PLAY_ENCHANT_ITEM(State.PLAY, Direction.INCOMING, 17, 6),
  PLAY_CLICK_WINDOW(State.PLAY, Direction.INCOMING, 14, 7),
  PLAY_CLOSE_WINDOW_REQUEST(State.PLAY, Direction.INCOMING, 13, 8),
  PLAY_PLUGIN_MESSAGE_REQUEST(State.PLAY, Direction.INCOMING, 23, 9),
  PLAY_USE_ENTITY(State.PLAY, Direction.INCOMING, 2, 10),
  PLAY_KEEP_ALIVE_REQUEST(State.PLAY, Direction.INCOMING, 0, 11),
  PLAY_PLAYER_POSITION_REQUEST(State.PLAY, Direction.INCOMING, 4, 12),
  PLAY_PLAYER_POSITION_LOOK_REQUEST(State.PLAY, Direction.INCOMING, 6, 13),
  PLAY_PLAYER_LOOK_REQUEST(State.PLAY, Direction.INCOMING, 5, 14),
  PLAY_PLAYER(State.PLAY, Direction.INCOMING, 3, 15),
  PLAY_VEHICLE_MOVE_REQUEST(State.PLAY, Direction.INCOMING, -1, 16),
  PLAY_STEER_BOAT(State.PLAY, Direction.INCOMING, -1, 17),
  PLAY_PLAYER_ABILITIES_REQUEST(State.PLAY, Direction.INCOMING, 19, 18),
  PLAY_PLAYER_DIGGING(State.PLAY, Direction.INCOMING, 7, 19),
  PLAY_ENTITY_ACTION(State.PLAY, Direction.INCOMING, 11, 20),
  PLAY_STEER_VEHICLE(State.PLAY, Direction.INCOMING, 12, 21),
  PLAY_RESOURCE_PACK_STATUS(State.PLAY, Direction.INCOMING, 25, 22),
  PLAY_HELD_ITEM_CHANGE_REQUEST(State.PLAY, Direction.INCOMING, 9, 23),
  PLAY_CREATIVE_INVENTORY_ACTION(State.PLAY, Direction.INCOMING, 16, 24),
  PLAY_UPDATE_SIGN_REQUEST(State.PLAY, Direction.INCOMING, 18, 25),
  PLAY_ANIMATION_REQUEST(State.PLAY, Direction.INCOMING, 10, 26),
  PLAY_SPECTATE(State.PLAY, Direction.INCOMING, 24, 27),
  PLAY_PLAYER_BLOCK_PLACEMENT(State.PLAY, Direction.INCOMING, 8, 28),
  PLAY_USE_ITEM(State.PLAY, Direction.INCOMING, -1, 29),
  PLAY_SPAWN_OBJECT(State.PLAY, Direction.OUTGOING, 14, 0),
  PLAY_SPAWN_XP_ORB(State.PLAY, Direction.OUTGOING, 17, 1),
  PLAY_SPAWN_GLOBAL_ENTITY(State.PLAY, Direction.OUTGOING, 44, 2),
  PLAY_SPAWN_MOB(State.PLAY, Direction.OUTGOING, 15, 3),
  PLAY_SPAWN_PAINTING(State.PLAY, Direction.OUTGOING, 16, 4),
  PLAY_SPAWN_PLAYER(State.PLAY, Direction.OUTGOING, 12, 5),
  PLAY_ANIMATION(State.PLAY, Direction.OUTGOING, 11, 6),
  PLAY_STATS(State.PLAY, Direction.OUTGOING, 55, 7),
  PLAY_BLOCK_BREAK_ANIMATION(State.PLAY, Direction.OUTGOING, 37, 8),
  PLAY_UPDATE_BLOCK_ENTITY(State.PLAY, Direction.OUTGOING, 53, 9),
  PLAY_BLOCK_ACTION(State.PLAY, Direction.OUTGOING, 36, 10),
  PLAY_BLOCK_CHANGE(State.PLAY, Direction.OUTGOING, 35, 11),
  PLAY_BOSS_BAR(State.PLAY, Direction.OUTGOING, -1, 12),
  PLAY_SERVER_DIFFICULTY(State.PLAY, Direction.OUTGOING, 65, 13),
  PLAY_TAB_COMPLETE(State.PLAY, Direction.OUTGOING, 58, 14),
  PLAY_CHAT_MESSAGE(State.PLAY, Direction.OUTGOING, 2, 15),
  PLAY_MULTI_BLOCK_CHANGE(State.PLAY, Direction.OUTGOING, 34, 16),
  PLAY_CONFIRM_TRANSACTION(State.PLAY, Direction.OUTGOING, 50, 17),
  PLAY_CLOSE_WINDOW(State.PLAY, Direction.OUTGOING, 46, 18),
  PLAY_OPEN_WINDOW(State.PLAY, Direction.OUTGOING, 45, 19),
  PLAY_WINDOW_ITEMS(State.PLAY, Direction.OUTGOING, 48, 20),
  PLAY_WINDOW_PROPERTY(State.PLAY, Direction.OUTGOING, 49, 21),
  PLAY_SET_SLOT(State.PLAY, Direction.OUTGOING, 47, 22),
  PLAY_SET_COOLDOWN(State.PLAY, Direction.OUTGOING, -1, 23),
  PLAY_PLUGIN_MESSAGE(State.PLAY, Direction.OUTGOING, 63, 24),
  PLAY_NAMED_SOUND_EFFECT(State.PLAY, Direction.OUTGOING, 41, 25),
  PLAY_DISCONNECT(State.PLAY, Direction.OUTGOING, 64, 26),
  PLAY_ENTITY_STATUS(State.PLAY, Direction.OUTGOING, 26, 27),
  PLAY_EXPLOSION(State.PLAY, Direction.OUTGOING, 39, 28),
  PLAY_UNLOAD_CHUNK(State.PLAY, Direction.OUTGOING, -1, 29),
  PLAY_CHANGE_GAME_STATE(State.PLAY, Direction.OUTGOING, 43, 30),
  PLAY_KEEP_ALIVE(State.PLAY, Direction.OUTGOING, 0, 31),
  PLAY_CHUNK_DATA(State.PLAY, Direction.OUTGOING, 33, 32),
  PLAY_EFFECT(State.PLAY, Direction.OUTGOING, 40, 33),
  PLAY_PARTICLE(State.PLAY, Direction.OUTGOING, 42, 34),
  PLAY_JOIN_GAME(State.PLAY, Direction.OUTGOING, 1, 35),
  PLAY_MAP(State.PLAY, Direction.OUTGOING, 52, 36),
  PLAY_ENTITY_RELATIVE_MOVE(State.PLAY, Direction.OUTGOING, 21, 37),
  PLAY_ENTITY_LOOK_MOVE(State.PLAY, Direction.OUTGOING, 23, 38),
  PLAY_ENTITY_LOOK(State.PLAY, Direction.OUTGOING, 22, 39),
  PLAY_ENTITY(State.PLAY, Direction.OUTGOING, 20, 40),
  PLAY_VEHICLE_MOVE(State.PLAY, Direction.OUTGOING, -1, 41),
  PLAY_OPEN_SIGN_EDITOR(State.PLAY, Direction.OUTGOING, 54, 42),
  PLAY_PLAYER_ABILITIES(State.PLAY, Direction.OUTGOING, 57, 43),
  PLAY_COMBAT_EVENT(State.PLAY, Direction.OUTGOING, 66, 44),
  PLAY_PLAYER_LIST_ITEM(State.PLAY, Direction.OUTGOING, 56, 45),
  PLAY_PLAYER_POSITION_LOOK(State.PLAY, Direction.OUTGOING, 8, 46),
  PLAY_USE_BED(State.PLAY, Direction.OUTGOING, 10, 47),
  PLAY_DESTROY_ENTITIES(State.PLAY, Direction.OUTGOING, 19, 48),
  PLAY_REMOVE_ENTITY_EFFECT(State.PLAY, Direction.OUTGOING, 30, 49),
  PLAY_RESOURCE_PACK_SEND(State.PLAY, Direction.OUTGOING, 72, 50),
  PLAY_RESPAWN(State.PLAY, Direction.OUTGOING, 7, 51),
  PLAY_ENTITY_HEAD_LOOK(State.PLAY, Direction.OUTGOING, 25, 52),
  PLAY_WORLD_BORDER(State.PLAY, Direction.OUTGOING, 68, 53),
  PLAY_CAMERA(State.PLAY, Direction.OUTGOING, 67, 54),
  PLAY_HELD_ITEM_CHANGE(State.PLAY, Direction.OUTGOING, 9, 55),
  PLAY_DISPLAY_SCOREBOARD(State.PLAY, Direction.OUTGOING, 61, 56),
  PLAY_ENTITY_METADATA(State.PLAY, Direction.OUTGOING, 28, 57),
  PLAY_ATTACH_ENTITY(State.PLAY, Direction.OUTGOING, 27, 58),
  PLAY_ENTITY_VELOCITY(State.PLAY, Direction.OUTGOING, 18, 59),
  PLAY_ENTITY_EQUIPMENT(State.PLAY, Direction.OUTGOING, 4, 60),
  PLAY_SET_XP(State.PLAY, Direction.OUTGOING, 31, 61),
  PLAY_UPDATE_HEALTH(State.PLAY, Direction.OUTGOING, 6, 62),
  PLAY_SCOREBOARD_OBJ(State.PLAY, Direction.OUTGOING, 59, 63),
  PLAY_SET_PASSENGERS(State.PLAY, Direction.OUTGOING, -1, 64),
  PLAY_TEAM(State.PLAY, Direction.OUTGOING, 62, 65),
  PLAY_UPDATE_SCORE(State.PLAY, Direction.OUTGOING, 60, 66),
  PLAY_SPAWN_POSITION(State.PLAY, Direction.OUTGOING, 5, 67),
  PLAY_TIME_UPDATE(State.PLAY, Direction.OUTGOING, 3, 68),
  PLAY_TITLE(State.PLAY, Direction.OUTGOING, 69, 69),
  PLAY_UPDATE_SIGN(State.PLAY, Direction.OUTGOING, 51, 70),
  PLAY_SOUND_EFFECT(State.PLAY, Direction.OUTGOING, -1, 71),
  PLAY_PLAYER_LIST_HEADER_FOOTER(State.PLAY, Direction.OUTGOING, 71, 72),
  PLAY_COLLECT_ITEM(State.PLAY, Direction.OUTGOING, 13, 73),
  PLAY_ENTITY_TELEPORT(State.PLAY, Direction.OUTGOING, 24, 74),
  PLAY_ENTITY_PROPERTIES(State.PLAY, Direction.OUTGOING, 32, 75),
  PLAY_ENTITY_EFFECT(State.PLAY, Direction.OUTGOING, 29, 76),
  PLAY_MAP_CHUNK_BULK(State.PLAY, Direction.OUTGOING, 38, -1),
  PLAY_SET_COMPRESSION(State.PLAY, Direction.OUTGOING, 70, -1),
  PLAY_UPDATE_ENTITY_NBT(State.PLAY, Direction.OUTGOING, 73, -1);
  
  static {
    oldids = new HashMap<>();
    newids = new HashMap<>();
    for (PacketType pt : values()) {
      oldids.put(Short.valueOf(toShort((short)pt.getPacketID(), (short)pt.getDirection().ordinal(), (short)pt.getState().ordinal())), pt);
      newids.put(Short.valueOf(toShort((short)pt.getNewPacketID(), (short)pt.getDirection().ordinal(), (short)pt.getState().ordinal())), pt);
    } 
  }
  
  private int newPacketID = -1;
  
  private static final HashMap<Short, PacketType> oldids;
  
  private static final HashMap<Short, PacketType> newids;
  
  private State state;
  
  private Direction direction;
  
  private int packetID;
  
  PacketType(State state, Direction direction, int packetID) {
    this.state = state;
    this.direction = direction;
    this.packetID = packetID;
    this.newPacketID = packetID;
  }
  
  PacketType(State state, Direction direction, int packetID, int newPacketID) {
    this.state = state;
    this.direction = direction;
    this.packetID = packetID;
    this.newPacketID = newPacketID;
  }
  
  public static PacketType findNewPacket(State state, Direction direction, int id) {
    return newids.get(Short.valueOf(toShort((short)id, (short)direction.ordinal(), (short)state.ordinal())));
  }
  
  public static PacketType findOldPacket(State state, Direction direction, int id) {
    return oldids.get(Short.valueOf(toShort((short)id, (short)direction.ordinal(), (short)state.ordinal())));
  }
  
  public static PacketType getIncomingPacket(State state, int id) {
    return findNewPacket(state, Direction.INCOMING, id);
  }
  
  public static PacketType getOutgoingPacket(State state, int id) {
    return findOldPacket(state, Direction.OUTGOING, id);
  }
  
  private static short toShort(short id, short direction, short state) {
    return (short)(id & 0xFF | direction << 8 & 0xF00 | state << 12 & 0xF000);
  }
  
  public State getState() {
    return this.state;
  }
  
  public Direction getDirection() {
    return this.direction;
  }
  
  public int getPacketID() {
    return this.packetID;
  }
  
  public int getNewPacketID() {
    return this.newPacketID;
  }
}
