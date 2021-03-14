package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntityId {
  public static List<Integer> getEntityIds(Packet packet) throws IOException {
    switch (packet.getType()) {
      case EntityCollectItem:
        try (Packet.Reader in = packet.reader()) {
          if (packet.atLeast(ProtocolVersion.v1_8))
            return Arrays.asList(new Integer[] { Integer.valueOf(in.readVarInt()), Integer.valueOf(in.readVarInt()) }); 
          return Arrays.asList(new Integer[] { Integer.valueOf(in.readInt()), Integer.valueOf(in.readInt()) });
        } 
      case DestroyEntities:
        return PacketDestroyEntities.getEntityIds(packet);
      case SetPassengers:
        try (Packet.Reader in = packet.reader()) {
          int j = in.readVarInt();
          int len = in.readVarInt();
          List<Integer> result = new ArrayList<>(len + 1);
          result.add(Integer.valueOf(j));
          for (int i = 0; i < len; i++)
            result.add(Integer.valueOf(in.readVarInt())); 
          return result;
        } 
      case EntityAttach:
        try (Packet.Reader in = packet.reader()) {
          return Arrays.asList(new Integer[] { Integer.valueOf(in.readInt()), Integer.valueOf(in.readInt()) });
        } 
      case Combat:
        try (Packet.Reader in = packet.reader()) {
          int event = in.readVarInt();
          if (event == 1) {
            in.readVarInt();
            return Collections.singletonList(Integer.valueOf(in.readInt()));
          } 
          if (event == 2)
            return Arrays.asList(new Integer[] { Integer.valueOf(in.readVarInt()), Integer.valueOf(in.readInt()) }); 
          return (List)Collections.emptyList();
        } 
    } 
    Integer entityId = getEntityId(packet);
    if (entityId != null)
      return Collections.singletonList(entityId); 
    return Collections.emptyList();
  }
  
  public static Integer getEntityId(Packet packet) throws IOException {
    switch (packet.getType()) {
      case OpenHorseWindow:
        try (Packet.Reader in = packet.reader()) {
          in.readByte();
          in.readVarInt();
          return Integer.valueOf(in.readInt());
        } 
      case EntitySoundEffect:
        try (Packet.Reader in = packet.reader()) {
          in.readVarInt();
          in.readVarInt();
          return Integer.valueOf(in.readVarInt());
        } 
      case EntityEffect:
      case EntityRemoveEffect:
      case EntityEquipment:
      case EntityHeadLook:
      case EntityMetadata:
      case EntityMovement:
      case EntityPosition:
      case EntityRotation:
      case EntityPositionRotation:
      case EntityAnimation:
      case EntityNBTUpdate:
      case EntityProperties:
      case EntityTeleport:
      case EntityVelocity:
      case SwitchCamera:
      case PlayerUseBed:
        try (Packet.Reader in = packet.reader()) {
          if (packet.atLeast(ProtocolVersion.v1_8))
            return Integer.valueOf(in.readVarInt()); 
          return Integer.valueOf(in.readInt());
        } 
      case BlockBreakAnim:
      case SpawnPlayer:
      case SpawnObject:
      case SpawnPainting:
      case SpawnMob:
      case SpawnGlobalEntity:
      case SpawnExpOrb:
        try (Packet.Reader in = packet.reader()) {
          return Integer.valueOf(in.readVarInt());
        } 
      case EntityStatus:
        try (Packet.Reader in = packet.reader()) {
          return Integer.valueOf(in.readInt());
        } 
    } 
    return null;
  }
}
