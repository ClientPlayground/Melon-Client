package com.replaymod.replaystudio.protocol;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;

public enum PacketType {
  UnknownLogin(ProtocolVersion.v1_7_6, -1, State.LOGIN),
  UnknownPlay(ProtocolVersion.v1_7_6, -1),
  LoginSuccess(ProtocolVersion.v1_7_6, 2, State.LOGIN),
  KeepAlive(ProtocolVersion.v1_7_6, 0),
  JoinGame(ProtocolVersion.v1_7_6, 1),
  Chat(ProtocolVersion.v1_7_6, 2),
  UpdateTime(ProtocolVersion.v1_7_6, 3),
  EntityEquipment(ProtocolVersion.v1_7_6, 4),
  SpawnPosition(ProtocolVersion.v1_7_6, 5),
  UpdateHealth(ProtocolVersion.v1_7_6, 6),
  Respawn(ProtocolVersion.v1_7_6, 7),
  PlayerPositionRotation(ProtocolVersion.v1_7_6, 8),
  ChangeHeldItem(ProtocolVersion.v1_7_6, 9),
  PlayerUseBed(ProtocolVersion.v1_7_6, 10),
  EntityAnimation(ProtocolVersion.v1_7_6, 11),
  SpawnPlayer(ProtocolVersion.v1_7_6, 12),
  EntityCollectItem(ProtocolVersion.v1_7_6, 13),
  SpawnObject(ProtocolVersion.v1_7_6, 14),
  SpawnMob(ProtocolVersion.v1_7_6, 15),
  SpawnPainting(ProtocolVersion.v1_7_6, 16),
  SpawnExpOrb(ProtocolVersion.v1_7_6, 17),
  EntityVelocity(ProtocolVersion.v1_7_6, 18),
  DestroyEntities(ProtocolVersion.v1_7_6, 19),
  EntityMovement(ProtocolVersion.v1_7_6, 20),
  EntityPosition(ProtocolVersion.v1_7_6, 21),
  EntityRotation(ProtocolVersion.v1_7_6, 22),
  EntityPositionRotation(ProtocolVersion.v1_7_6, 23),
  EntityTeleport(ProtocolVersion.v1_7_6, 24),
  EntityHeadLook(ProtocolVersion.v1_7_6, 25),
  EntityStatus(ProtocolVersion.v1_7_6, 26),
  EntityAttach(ProtocolVersion.v1_7_6, 27),
  EntityMetadata(ProtocolVersion.v1_7_6, 28),
  EntityEffect(ProtocolVersion.v1_7_6, 29),
  EntityRemoveEffect(ProtocolVersion.v1_7_6, 30),
  SetExperience(ProtocolVersion.v1_7_6, 31),
  EntityProperties(ProtocolVersion.v1_7_6, 32),
  ChunkData(ProtocolVersion.v1_7_6, 33),
  MultiBlockChange(ProtocolVersion.v1_7_6, 34),
  BlockChange(ProtocolVersion.v1_7_6, 35),
  BlockValue(ProtocolVersion.v1_7_6, 36),
  BlockBreakAnim(ProtocolVersion.v1_7_6, 37),
  BulkChunkData(ProtocolVersion.v1_7_6, 38),
  Explosion(ProtocolVersion.v1_7_6, 39),
  PlayEffect(ProtocolVersion.v1_7_6, 40),
  PlaySound(ProtocolVersion.v1_7_6, 41),
  SpawnParticle(ProtocolVersion.v1_7_6, 42),
  NotifyClient(ProtocolVersion.v1_7_6, 43),
  SpawnGlobalEntity(ProtocolVersion.v1_7_6, 44),
  OpenWindow(ProtocolVersion.v1_7_6, 45),
  CloseWindow(ProtocolVersion.v1_7_6, 46),
  SetSlot(ProtocolVersion.v1_7_6, 47),
  WindowItems(ProtocolVersion.v1_7_6, 48),
  WindowProperty(ProtocolVersion.v1_7_6, 49),
  ConfirmTransaction(ProtocolVersion.v1_7_6, 50),
  UpdateSign(ProtocolVersion.v1_7_6, 51),
  MapData(ProtocolVersion.v1_7_6, 52),
  UpdateTileEntity(ProtocolVersion.v1_7_6, 53),
  OpenTileEntityEditor(ProtocolVersion.v1_7_6, 54),
  Statistics(ProtocolVersion.v1_7_6, 55),
  PlayerListEntry(ProtocolVersion.v1_7_6, 56),
  PlayerAbilities(ProtocolVersion.v1_7_6, 57),
  TabComplete(ProtocolVersion.v1_7_6, 58),
  ScoreboardObjective(ProtocolVersion.v1_7_6, 59),
  UpdateScore(ProtocolVersion.v1_7_6, 60),
  DisplayScoreboard(ProtocolVersion.v1_7_6, 61),
  Team(ProtocolVersion.v1_7_6, 62),
  PluginMessage(ProtocolVersion.v1_7_6, 63),
  Disconnect(ProtocolVersion.v1_7_6, 64),
  Difficulty(ProtocolVersion.v1_8, 65),
  Combat(ProtocolVersion.v1_8, 66),
  SwitchCamera(ProtocolVersion.v1_8, 67),
  WorldBorder(ProtocolVersion.v1_8, 68),
  EntityNBTUpdate(ProtocolVersion.v1_8, 73),
  UnloadChunk(ProtocolVersion.v1_9, 29),
  SetPassengers(ProtocolVersion.v1_9, 64),
  OpenHorseWindow(ProtocolVersion.v1_14, 31),
  UpdateLight(ProtocolVersion.v1_14, 36),
  TradeList(ProtocolVersion.v1_14, 39),
  UpdateViewPosition(ProtocolVersion.v1_14, 64),
  UpdateViewDistance(ProtocolVersion.v1_14, 65),
  EntitySoundEffect(ProtocolVersion.v1_14, 80),
  PlayerActionAck(ProtocolVersion.v1_14, 92);
  
  private final State state;
  
  private final ProtocolVersion initialVersion;
  
  private final int initialId;
  
  PacketType(ProtocolVersion initialVersion, int initialId, State state) {
    this.state = state;
    this.initialVersion = initialVersion;
    this.initialId = initialId;
  }
  
  public State getState() {
    return this.state;
  }
  
  public ProtocolVersion getInitialVersion() {
    return this.initialVersion;
  }
  
  public int getInitialId() {
    return this.initialId;
  }
  
  public boolean isUnknown() {
    return (this.initialId == -1);
  }
}
