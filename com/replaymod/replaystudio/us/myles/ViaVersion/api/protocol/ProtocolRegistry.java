package com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.BaseProtocol;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.base.BaseProtocol1_7;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_10to1_9_3.Protocol1_10To1_9_3_4;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11_1to1_11.Protocol1_11_1To1_11;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_11to1_10.Protocol1_11To1_10;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12_1to1_12.Protocol1_12_1To1_12;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12_2to1_12_1.Protocol1_12_2To1_12_1;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_1to1_13.Protocol1_13_1To1_13;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.Protocol1_13_2To1_13_1;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_1to1_14.Protocol1_14_1To1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_2to1_14_1.Protocol1_14_2To1_14_1;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_3to1_14_2.Protocol1_14_3To1_14_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14_4to1_14_3.Protocol1_14_4To1_14_3;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15_1to1_15.Protocol1_15_1To1_15;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15_2to1_15_1.Protocol1_15_2To1_15_1;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.Protocol1_9_1_2To1_9_3_4;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1to1_9.Protocol1_9_1To1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.Protocol1_9_3To1_9_1_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_9_1.Protocol1_9To1_9_1;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolRegistry {
  public static final Protocol BASE_PROTOCOL = (Protocol)new BaseProtocol();
  
  public static int SERVER_PROTOCOL = -1;
  
  private static final Map<Integer, Map<Integer, Protocol>> registryMap = new ConcurrentHashMap<>();
  
  private static final Map<Pair<Integer, Integer>, List<Pair<Integer, Protocol>>> pathCache = new ConcurrentHashMap<>();
  
  private static final List<Protocol> registerList = Lists.newCopyOnWriteArrayList();
  
  private static final Set<Integer> supportedVersions = Sets.newConcurrentHashSet();
  
  private static final List<Pair<Range<Integer>, Protocol>> baseProtocols = Lists.newCopyOnWriteArrayList();
  
  static {
    registerBaseProtocol(BASE_PROTOCOL, Range.lessThan(Integer.valueOf(-2147483648)));
    registerBaseProtocol((Protocol)new BaseProtocol1_7(), Range.all());
    registerProtocol((Protocol)new Protocol1_9To1_8(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_9.getId())), Integer.valueOf(ProtocolVersion.v1_8.getId()));
    registerProtocol((Protocol)new Protocol1_9_1To1_9(), Arrays.asList(new Integer[] { Integer.valueOf(ProtocolVersion.v1_9_1.getId()), Integer.valueOf(ProtocolVersion.v1_9_2.getId()) }, ), Integer.valueOf(ProtocolVersion.v1_9.getId()));
    registerProtocol((Protocol)new Protocol1_9_3To1_9_1_2(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_9_3.getId())), Integer.valueOf(ProtocolVersion.v1_9_2.getId()));
    registerProtocol((Protocol)new Protocol1_9To1_9_1(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_9.getId())), Integer.valueOf(ProtocolVersion.v1_9_2.getId()));
    registerProtocol((Protocol)new Protocol1_9_1_2To1_9_3_4(), Arrays.asList(new Integer[] { Integer.valueOf(ProtocolVersion.v1_9_1.getId()), Integer.valueOf(ProtocolVersion.v1_9_2.getId()) }, ), Integer.valueOf(ProtocolVersion.v1_9_3.getId()));
    registerProtocol((Protocol)new Protocol1_10To1_9_3_4(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_10.getId())), Integer.valueOf(ProtocolVersion.v1_9_3.getId()));
    registerProtocol((Protocol)new Protocol1_11To1_10(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_11.getId())), Integer.valueOf(ProtocolVersion.v1_10.getId()));
    registerProtocol((Protocol)new Protocol1_11_1To1_11(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_11_1.getId())), Integer.valueOf(ProtocolVersion.v1_11.getId()));
    registerProtocol((Protocol)new Protocol1_12To1_11_1(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_12.getId())), Integer.valueOf(ProtocolVersion.v1_11_1.getId()));
    registerProtocol((Protocol)new Protocol1_12_1To1_12(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_12_1.getId())), Integer.valueOf(ProtocolVersion.v1_12.getId()));
    registerProtocol((Protocol)new Protocol1_12_2To1_12_1(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_12_2.getId())), Integer.valueOf(ProtocolVersion.v1_12_1.getId()));
    registerProtocol((Protocol)new Protocol1_13To1_12_2(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_13.getId())), Integer.valueOf(ProtocolVersion.v1_12_2.getId()));
    registerProtocol((Protocol)new Protocol1_13_1To1_13(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_13_1.getId())), Integer.valueOf(ProtocolVersion.v1_13.getId()));
    registerProtocol((Protocol)new Protocol1_13_2To1_13_1(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_13_2.getId())), Integer.valueOf(ProtocolVersion.v1_13_1.getId()));
    registerProtocol((Protocol)new Protocol1_14To1_13_2(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_14.getId())), Integer.valueOf(ProtocolVersion.v1_13_2.getId()));
    registerProtocol((Protocol)new Protocol1_14_1To1_14(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_14_1.getId())), Integer.valueOf(ProtocolVersion.v1_14.getId()));
    registerProtocol((Protocol)new Protocol1_14_2To1_14_1(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_14_2.getId())), Integer.valueOf(ProtocolVersion.v1_14_1.getId()));
    registerProtocol((Protocol)new Protocol1_14_3To1_14_2(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_14_3.getId())), Integer.valueOf(ProtocolVersion.v1_14_2.getId()));
    registerProtocol((Protocol)new Protocol1_14_4To1_14_3(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_14_4.getId())), Integer.valueOf(ProtocolVersion.v1_14_3.getId()));
    registerProtocol((Protocol)new Protocol1_15To1_14_4(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_15.getId())), Integer.valueOf(ProtocolVersion.v1_14_4.getId()));
    registerProtocol((Protocol)new Protocol1_15_1To1_15(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_15_1.getId())), Integer.valueOf(ProtocolVersion.v1_15.getId()));
    registerProtocol((Protocol)new Protocol1_15_2To1_15_1(), Collections.singletonList(Integer.valueOf(ProtocolVersion.v1_15_2.getId())), Integer.valueOf(ProtocolVersion.v1_15_1.getId()));
  }
  
  public static void registerProtocol(Protocol protocol, List<Integer> supported, Integer output) {
    if (pathCache.size() > 0)
      pathCache.clear(); 
    for (Integer version : supported) {
      if (!registryMap.containsKey(version))
        registryMap.put(version, new HashMap<>()); 
      ((Map<Integer, Protocol>)registryMap.get(version)).put(output, protocol);
    } 
    if (Via.getPlatform().isPluginEnabled()) {
      protocol.registerListeners();
      protocol.register(Via.getManager().getProviders());
      refreshVersions();
    } else {
      registerList.add(protocol);
    } 
  }
  
  public static void registerBaseProtocol(Protocol baseProtocol, Range<Integer> supportedProtocols) {
    baseProtocols.add(new Pair(supportedProtocols, baseProtocol));
    if (Via.getPlatform().isPluginEnabled()) {
      baseProtocol.registerListeners();
      baseProtocol.register(Via.getManager().getProviders());
      refreshVersions();
    } else {
      registerList.add(baseProtocol);
    } 
  }
  
  public static void refreshVersions() {
    supportedVersions.clear();
    supportedVersions.add(Integer.valueOf(SERVER_PROTOCOL));
    for (ProtocolVersion versions : ProtocolVersion.getProtocols()) {
      List<Pair<Integer, Protocol>> paths = getProtocolPath(versions.getId(), SERVER_PROTOCOL);
      if (paths == null)
        continue; 
      supportedVersions.add(Integer.valueOf(versions.getId()));
      for (Pair<Integer, Protocol> path : paths)
        supportedVersions.add(path.getKey()); 
    } 
  }
  
  public static SortedSet<Integer> getSupportedVersions() {
    return Collections.unmodifiableSortedSet(new TreeSet<>(supportedVersions));
  }
  
  public static boolean isWorkingPipe() {
    for (Map<Integer, Protocol> maps : registryMap.values()) {
      if (maps.containsKey(Integer.valueOf(SERVER_PROTOCOL)))
        return true; 
    } 
    return false;
  }
  
  public static void onServerLoaded() {
    for (Protocol protocol : registerList) {
      protocol.registerListeners();
      protocol.register(Via.getManager().getProviders());
    } 
    registerList.clear();
  }
  
  private static List<Pair<Integer, Protocol>> getProtocolPath(List<Pair<Integer, Protocol>> current, int clientVersion, int serverVersion) {
    if (clientVersion == serverVersion)
      return null; 
    if (current.size() > 50)
      return null; 
    Map<Integer, Protocol> inputMap = registryMap.get(Integer.valueOf(clientVersion));
    if (inputMap == null)
      return null; 
    Protocol protocol = inputMap.get(Integer.valueOf(serverVersion));
    if (protocol != null) {
      current.add(new Pair(Integer.valueOf(serverVersion), protocol));
      return current;
    } 
    List<Pair<Integer, Protocol>> shortest = null;
    for (Map.Entry<Integer, Protocol> entry : inputMap.entrySet()) {
      if (!((Integer)entry.getKey()).equals(Integer.valueOf(serverVersion))) {
        Pair<Integer, Protocol> pair = new Pair(entry.getKey(), entry.getValue());
        if (!current.contains(pair)) {
          List<Pair<Integer, Protocol>> newCurrent = new ArrayList<>(current);
          newCurrent.add(pair);
          newCurrent = getProtocolPath(newCurrent, ((Integer)entry.getKey()).intValue(), serverVersion);
          if (newCurrent != null)
            if (shortest == null || shortest.size() > newCurrent.size())
              shortest = newCurrent;  
        } 
      } 
    } 
    return shortest;
  }
  
  public static List<Pair<Integer, Protocol>> getProtocolPath(int clientVersion, int serverVersion) {
    Pair<Integer, Integer> protocolKey = new Pair(Integer.valueOf(clientVersion), Integer.valueOf(serverVersion));
    List<Pair<Integer, Protocol>> protocolList = pathCache.get(protocolKey);
    if (protocolList != null)
      return protocolList; 
    List<Pair<Integer, Protocol>> outputPath = getProtocolPath(new ArrayList<>(), clientVersion, serverVersion);
    if (outputPath != null)
      pathCache.put(protocolKey, outputPath); 
    return outputPath;
  }
  
  public static Protocol getBaseProtocol(int serverVersion) {
    for (Pair<Range<Integer>, Protocol> rangeProtocol : (Iterable<Pair<Range<Integer>, Protocol>>)Lists.reverse(baseProtocols)) {
      if (((Range)rangeProtocol.getKey()).contains(Integer.valueOf(serverVersion)))
        return (Protocol)rangeProtocol.getValue(); 
    } 
    throw new IllegalStateException("No Base Protocol for " + serverVersion);
  }
  
  public static boolean isBaseProtocol(Protocol protocol) {
    for (Pair<Range<Integer>, Protocol> p : baseProtocols) {
      if (p.getValue() == protocol)
        return true; 
    } 
    return false;
  }
}
