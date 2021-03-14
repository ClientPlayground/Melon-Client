package com.replaymod.replaystudio.util;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.packets.EntityId;
import com.replaymod.replaystudio.protocol.packets.PacketEntityMovement;
import com.replaymod.replaystudio.protocol.packets.PacketEntityTeleport;
import com.replaymod.replaystudio.protocol.packets.SpawnEntity;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Triple;
import java.io.IOException;
import java.util.List;

public class PacketUtils {
  public static boolean isSpawnEntityPacket(Packet packet) {
    switch (packet.getType()) {
      case SpawnPlayer:
      case SpawnMob:
      case SpawnObject:
      case SpawnExpOrb:
      case SpawnPainting:
      case SpawnGlobalEntity:
        return true;
    } 
    return false;
  }
  
  public static Integer getEntityId(Packet packet) throws IOException {
    return EntityId.getEntityId(packet);
  }
  
  public static List<Integer> getEntityIds(Packet packet) throws IOException {
    return EntityId.getEntityIds(packet);
  }
  
  public static Location updateLocation(Location loc, Packet packet) throws IOException {
    Triple<DPosition, Pair<Float, Float>, Boolean> movement;
    DPosition deltaPos;
    Pair<Float, Float> yawPitch;
    double x, y, z;
    float yaw, pitch;
    Location spawnLocation = SpawnEntity.getLocation(packet);
    if (spawnLocation != null)
      return spawnLocation; 
    switch (packet.getType()) {
      case EntityMovement:
      case EntityPosition:
      case EntityRotation:
      case EntityPositionRotation:
        if (loc == null)
          loc = Location.NULL; 
        movement = PacketEntityMovement.getMovement(packet);
        deltaPos = (DPosition)movement.getFirst();
        yawPitch = (Pair<Float, Float>)movement.getSecond();
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        if (deltaPos != null) {
          x += deltaPos.getX();
          y += deltaPos.getY();
          z += deltaPos.getZ();
        } 
        yaw = (yawPitch != null) ? ((Float)yawPitch.getKey()).floatValue() : loc.getYaw();
        pitch = (yawPitch != null) ? ((Float)yawPitch.getValue()).floatValue() : loc.getPitch();
        return new Location(x, y, z, yaw, pitch);
      case EntityTeleport:
        return PacketEntityTeleport.getLocation(packet);
    } 
    return null;
  }
}
