package com.replaymod.replaystudio.protocol;

import com.google.common.collect.Lists;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.Protocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import com.replaymod.replaystudio.viaversion.CustomViaManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PacketTypeRegistry {
  private static Map<ProtocolVersion, EnumMap<State, PacketTypeRegistry>> forVersionAndState = new HashMap<>();
  
  private static Field outgoing;
  
  private static Field oldId;
  
  private static Field newId;
  
  private final ProtocolVersion version;
  
  private final State state;
  
  private final PacketType unknown;
  
  static {
    CustomViaManager.initialize();
    for (ProtocolVersion version : ProtocolVersion.getProtocols()) {
      if (ProtocolVersion.getIndex(version) < ProtocolVersion.getIndex(ProtocolVersion.v1_7_1))
        continue; 
      EnumMap<State, PacketTypeRegistry> forState = new EnumMap<>(State.class);
      for (State state : State.values())
        forState.put(state, new PacketTypeRegistry(version, state)); 
      forVersionAndState.put(version, forState);
    } 
  }
  
  public static PacketTypeRegistry get(ProtocolVersion version, State state) {
    EnumMap<State, PacketTypeRegistry> forState = forVersionAndState.get(version);
    return (forState != null) ? forState.get(state) : new PacketTypeRegistry(version, state);
  }
  
  private final Map<Integer, PacketType> typeForId = new HashMap<>();
  
  private final Map<PacketType, Integer> idForType = new HashMap<>();
  
  private PacketTypeRegistry(ProtocolVersion version, State state) {
    this.version = version;
    this.state = state;
    PacketType unknown = null;
    int versionIndex = ProtocolVersion.getIndex(version);
    for (PacketType packetType : PacketType.values()) {
      if (packetType.getState() == state)
        if (packetType.isUnknown()) {
          unknown = packetType;
        } else if (ProtocolVersion.getIndex(packetType.getInitialVersion()) <= versionIndex) {
          List<Pair<Integer, Protocol>> protocolPath = getProtocolPath(version.getId(), packetType.getInitialVersion().getId());
          if (protocolPath != null) {
            int id = packetType.getInitialId();
            Iterator<Pair<Integer, Protocol>> iterator = Lists.reverse(protocolPath).iterator();
            label44: while (true) {
              if (iterator.hasNext()) {
                Pair<Integer, Protocol> pair = iterator.next();
                Protocol protocol = (Protocol)pair.getValue();
                boolean wasReplaced = false;
                for (Pair<Integer, Integer> idMapping : getIdMappings(protocol, state)) {
                  int oldId = ((Integer)idMapping.getKey()).intValue();
                  int newId = ((Integer)idMapping.getValue()).intValue();
                  if (oldId == id) {
                    if (newId == -1)
                      break label44; 
                    id = newId;
                    wasReplaced = false;
                    break;
                  } 
                  if (newId == id)
                    wasReplaced = true; 
                } 
                if (protocol instanceof com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2 && packetType == PacketType.PlayerUseBed)
                  wasReplaced = true; 
                if (protocol instanceof com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8 && packetType == PacketType.EntityNBTUpdate)
                  wasReplaced = true; 
                if (wasReplaced)
                  break; 
                continue;
              } 
              this.typeForId.put(Integer.valueOf(id), packetType);
              this.idForType.put(packetType, Integer.valueOf(id));
              break;
            } 
          } 
        }  
    } 
    this.unknown = unknown;
  }
  
  private static List<Pair<Integer, Protocol>> getProtocolPath(int clientVersion, int serverVersion) {
    if (serverVersion == ProtocolVersion.v1_7_6.getId())
      return getProtocolPath(clientVersion, ProtocolVersion.v1_8.getId()); 
    if (clientVersion == ProtocolVersion.v1_7_6.getId())
      return getProtocolPath(ProtocolVersion.v1_8.getId(), serverVersion); 
    if (clientVersion == serverVersion)
      return Collections.emptyList(); 
    return ProtocolRegistry.getProtocolPath(clientVersion, serverVersion);
  }
  
  public ProtocolVersion getVersion() {
    return this.version;
  }
  
  public State getState() {
    return this.state;
  }
  
  public Integer getId(PacketType type) {
    return this.idForType.get(type);
  }
  
  public PacketType getType(int id) {
    return this.typeForId.getOrDefault(Integer.valueOf(id), this.unknown);
  }
  
  public boolean atLeast(ProtocolVersion protocolVersion) {
    return (this.version.getId() >= protocolVersion.getId());
  }
  
  public boolean atMost(ProtocolVersion protocolVersion) {
    return (this.version.getId() <= protocolVersion.getId());
  }
  
  private static List<Pair<Integer, Integer>> getIdMappings(Protocol protocol, State state) {
    List<Pair<Integer, Integer>> result = new ArrayList<>();
    try {
      if (outgoing == null) {
        outgoing = Protocol.class.getDeclaredField("outgoing");
        outgoing.setAccessible(true);
      } 
      for (Map.Entry<Pair<State, Integer>, Object> entry : (Iterable<Map.Entry<Pair<State, Integer>, Object>>)((Map)outgoing.get(protocol)).entrySet()) {
        if (((Pair)entry.getKey()).getKey() != state)
          continue; 
        Object mapping = entry.getValue();
        if (oldId == null || newId == null) {
          Class<?> mappingClass = mapping.getClass();
          oldId = mappingClass.getDeclaredField("oldID");
          newId = mappingClass.getDeclaredField("newID");
          oldId.setAccessible(true);
          newId.setAccessible(true);
        } 
        result.add(new Pair(oldId.get(mapping), newId.get(mapping)));
      } 
    } catch (NoSuchFieldException|IllegalAccessException e) {
      throw new RuntimeException(e);
    } 
    return result;
  }
}
