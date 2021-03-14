package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Triple;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.util.DPosition;
import java.io.IOException;

public class PacketEntityMovement {
  public static Triple<DPosition, Pair<Float, Float>, Boolean> getMovement(Packet packet) throws IOException {
    PacketType type = packet.getType();
    boolean hasPos = (type == PacketType.EntityPosition || type == PacketType.EntityPositionRotation);
    boolean hasRot = (type == PacketType.EntityRotation || type == PacketType.EntityPositionRotation);
    try (Packet.Reader in = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        in.readVarInt();
      } else {
        in.readInt();
      } 
      DPosition pos = null;
      if (hasPos)
        if (packet.atLeast(ProtocolVersion.v1_9)) {
          pos = new DPosition(in.readShort() / 4096.0D, in.readShort() / 4096.0D, in.readShort() / 4096.0D);
        } else {
          pos = new DPosition(in.readByte() / 32.0D, in.readByte() / 32.0D, in.readByte() / 32.0D);
        }  
      Pair<Float, Float> yawPitch = null;
      if (hasRot)
        yawPitch = new Pair(Float.valueOf(in.readByte() / 256.0F * 360.0F), Float.valueOf(in.readByte() / 256.0F * 360.0F)); 
      boolean onGround = true;
      if (packet.atLeast(ProtocolVersion.v1_8) && (hasPos || hasRot))
        onGround = in.readBoolean(); 
      return new Triple(pos, yawPitch, Boolean.valueOf(onGround));
    } 
  }
  
  public static Packet write(PacketTypeRegistry registry, int entityId, DPosition deltaPos, Pair<Float, Float> yawPitch, boolean onGround) throws IOException {
    PacketType type;
    boolean hasPos = (deltaPos != null);
    boolean hasRot = (yawPitch != null);
    if (hasPos) {
      if (hasRot) {
        type = PacketType.EntityPositionRotation;
      } else {
        type = PacketType.EntityPosition;
      } 
    } else if (hasRot) {
      type = PacketType.EntityRotation;
    } else {
      type = PacketType.EntityMovement;
    } 
    Packet packet = new Packet(registry, type);
    try (Packet.Writer out = packet.overwrite()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        out.writeVarInt(entityId);
      } else {
        out.writeInt(entityId);
      } 
      if (hasPos)
        if (packet.atLeast(ProtocolVersion.v1_9)) {
          out.writeShort((int)(deltaPos.getX() * 4096.0D));
          out.writeShort((int)(deltaPos.getY() * 4096.0D));
          out.writeShort((int)(deltaPos.getZ() * 4096.0D));
        } else {
          out.writeByte((int)(deltaPos.getX() * 32.0D));
          out.writeByte((int)(deltaPos.getY() * 32.0D));
          out.writeByte((int)(deltaPos.getZ() * 32.0D));
        }  
      if (hasRot) {
        out.writeByte((int)(((Float)yawPitch.getKey()).floatValue() / 360.0F * 256.0F));
        out.writeByte((int)(((Float)yawPitch.getValue()).floatValue() / 360.0F * 256.0F));
      } 
      if (packet.atLeast(ProtocolVersion.v1_8) && (hasPos || hasRot))
        out.writeBoolean(onGround); 
    } 
    return packet;
  }
}
