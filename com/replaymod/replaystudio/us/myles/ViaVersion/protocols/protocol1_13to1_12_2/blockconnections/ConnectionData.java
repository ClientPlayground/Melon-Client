package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.PacketWrapper;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockFace;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.platform.providers.Provider;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.PacketBlockConnectionProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ConnectionData {
  static Map<Integer, String> idToKey = new HashMap<>();
  
  static Map<String, Integer> keyToId = new HashMap<>();
  
  static Map<Integer, ConnectionHandler> connectionHandlerMap = new HashMap<>();
  
  static Map<Integer, BlockData> blockConnectionData = new HashMap<>();
  
  static Set<Integer> occludingStates = new HashSet<>();
  
  public static void update(UserConnection user, Position position) {
    BlockConnectionProvider connectionProvider = (BlockConnectionProvider)Via.getManager().getProviders().get(BlockConnectionProvider.class);
    for (BlockFace face : BlockFace.values()) {
      Position pos = new Position(Long.valueOf(position.getX().longValue() + face.getModX()), Long.valueOf(position.getY().longValue() + face.getModY()), Long.valueOf(position.getZ().longValue() + face.getModZ()));
      int blockState = connectionProvider.getBlockdata(user, pos);
      ConnectionHandler handler = connectionHandlerMap.get(Integer.valueOf(blockState));
      if (handler != null) {
        int newBlockState = handler.connect(user, pos, blockState);
        PacketWrapper blockUpdatePacket = new PacketWrapper(11, null, user);
        blockUpdatePacket.write(Type.POSITION, pos);
        blockUpdatePacket.write(Type.VAR_INT, Integer.valueOf(newBlockState));
        try {
          blockUpdatePacket.send(Protocol1_13To1_12_2.class, true, true);
        } catch (Exception ex) {
          ex.printStackTrace();
        } 
      } 
    } 
  }
  
  public static void updateChunkSectionNeighbours(UserConnection user, int chunkX, int chunkZ, int chunkSectionY) {
    for (int chunkDeltaX = -1; chunkDeltaX <= 1; chunkDeltaX++) {
      for (int chunkDeltaZ = -1; chunkDeltaZ <= 1; chunkDeltaZ++) {
        if (Math.abs(chunkDeltaX) + Math.abs(chunkDeltaZ) != 0) {
          List<BlockChangeRecord> updates = new ArrayList<>();
          if (Math.abs(chunkDeltaX) + Math.abs(chunkDeltaZ) == 2) {
            for (int blockY = chunkSectionY * 16; blockY < chunkSectionY * 16 + 16; blockY++) {
              int blockPosX = (chunkDeltaX == 1) ? 0 : 15;
              int blockPosZ = (chunkDeltaZ == 1) ? 0 : 15;
              updateBlock(user, new Position(
                    
                    Long.valueOf((chunkX + chunkDeltaX << 4) + blockPosX), 
                    Long.valueOf(blockY), 
                    Long.valueOf((chunkZ + chunkDeltaZ << 4) + blockPosZ)), updates);
            } 
          } else {
            for (int blockY = chunkSectionY * 16; blockY < chunkSectionY * 16 + 16; blockY++) {
              int xStart;
              int xEnd;
              int zStart;
              int zEnd;
              if (chunkDeltaX == 1) {
                xStart = 0;
                xEnd = 2;
                zStart = 0;
                zEnd = 16;
              } else if (chunkDeltaX == -1) {
                xStart = 14;
                xEnd = 16;
                zStart = 0;
                zEnd = 16;
              } else if (chunkDeltaZ == 1) {
                xStart = 0;
                xEnd = 16;
                zStart = 0;
                zEnd = 2;
              } else {
                xStart = 0;
                xEnd = 16;
                zStart = 14;
                zEnd = 16;
              } 
              for (int blockX = xStart; blockX < xEnd; blockX++) {
                for (int blockZ = zStart; blockZ < zEnd; blockZ++)
                  updateBlock(user, new Position(
                        
                        Long.valueOf((chunkX + chunkDeltaX << 4) + blockX), 
                        Long.valueOf(blockY), 
                        Long.valueOf((chunkZ + chunkDeltaZ << 4) + blockZ)), updates); 
              } 
            } 
          } 
          if (!updates.isEmpty()) {
            PacketWrapper wrapper = new PacketWrapper(15, null, user);
            wrapper.write(Type.INT, Integer.valueOf(chunkX + chunkDeltaX));
            wrapper.write(Type.INT, Integer.valueOf(chunkZ + chunkDeltaZ));
            wrapper.write(Type.BLOCK_CHANGE_RECORD_ARRAY, updates.toArray(new BlockChangeRecord[0]));
            try {
              wrapper.send(Protocol1_13To1_12_2.class, true, true);
            } catch (Exception e) {
              e.printStackTrace();
            } 
          } 
        } 
      } 
    } 
  }
  
  public static void updateBlock(UserConnection user, Position pos, List<BlockChangeRecord> records) {
    int blockState = ((BlockConnectionProvider)Via.getManager().getProviders().get(BlockConnectionProvider.class)).getBlockdata(user, pos);
    ConnectionHandler handler = getConnectionHandler(blockState);
    if (handler == null)
      return; 
    int newBlockState = handler.connect(user, pos, blockState);
    records.add(new BlockChangeRecord((short)(int)((pos.getX().longValue() & 0xFL) << 4L | pos.getZ().longValue() & 0xFL), pos.getY().shortValue(), newBlockState));
  }
  
  public static BlockConnectionProvider getProvider() {
    return (BlockConnectionProvider)Via.getManager().getProviders().get(BlockConnectionProvider.class);
  }
  
  public static void updateBlockStorage(UserConnection userConnection, Position position, int blockState) {
    if (!needStoreBlocks())
      return; 
    if (isWelcome(blockState)) {
      getProvider().storeBlock(userConnection, position, blockState);
    } else {
      getProvider().removeBlock(userConnection, position);
    } 
  }
  
  public static void clearBlockStorage(UserConnection connection) {
    if (!needStoreBlocks())
      return; 
    getProvider().clearStorage(connection);
  }
  
  public static boolean needStoreBlocks() {
    return getProvider().storesBlocks();
  }
  
  public static void connectBlocks(UserConnection user, Chunk chunk) {
    long xOff = (chunk.getX() << 4);
    long zOff = (chunk.getZ() << 4);
    for (int i = 0; i < (chunk.getSections()).length; i++) {
      ChunkSection section = chunk.getSections()[i];
      if (section != null) {
        boolean willConnect = false;
        for (int p = 0; p < section.getPaletteSize(); p++) {
          int id = section.getPaletteEntry(p);
          if (connects(id)) {
            willConnect = true;
            break;
          } 
        } 
        if (willConnect) {
          long yOff = (i << 4);
          for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
              for (int x = 0; x < 16; x++) {
                int block = section.getFlatBlock(x, y, z);
                ConnectionHandler handler = getConnectionHandler(block);
                if (handler != null) {
                  block = handler.connect(user, new Position(Long.valueOf(xOff + x), Long.valueOf(yOff + y), Long.valueOf(zOff + z)), block);
                  section.setFlatBlock(x, y, z, block);
                } 
              } 
            } 
          } 
        } 
      } 
    } 
  }
  
  public static void init() {
    if (!Via.getConfig().isServersideBlockConnections())
      return; 
    Via.getPlatform().getLogger().info("Loading block connection mappings ...");
    JsonObject mapping1_13 = MappingData.loadData("mapping-1.13.json");
    JsonObject blocks1_13 = mapping1_13.getAsJsonObject("blocks");
    for (Map.Entry<String, JsonElement> blockState : (Iterable<Map.Entry<String, JsonElement>>)blocks1_13.entrySet()) {
      Integer id = Integer.valueOf(Integer.parseInt(blockState.getKey()));
      String key = ((JsonElement)blockState.getValue()).getAsString();
      idToKey.put(id, key);
      keyToId.put(key, id);
    } 
    if (!Via.getConfig().isReduceBlockStorageMemory()) {
      JsonObject mappingBlockConnections = MappingData.loadData("blockConnections.json");
      for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)mappingBlockConnections.entrySet()) {
        int id = ((Integer)keyToId.get(entry.getKey())).intValue();
        BlockData blockData1 = new BlockData();
        for (Map.Entry<String, JsonElement> type : (Iterable<Map.Entry<String, JsonElement>>)((JsonElement)entry.getValue()).getAsJsonObject().entrySet()) {
          String name = type.getKey();
          JsonObject object = ((JsonElement)type.getValue()).getAsJsonObject();
          boolean[] data = new boolean[6];
          for (BlockFace value : BlockFace.values()) {
            String face = value.toString().toLowerCase(Locale.ROOT);
            if (object.has(face))
              data[value.ordinal()] = object.getAsJsonPrimitive(face).getAsBoolean(); 
          } 
          blockData1.put(name, data);
        } 
        if (((String)entry.getKey()).contains("stairs"))
          blockData1.put("allFalseIfStairPre1_12", new boolean[6]); 
        blockConnectionData.put(Integer.valueOf(id), blockData1);
      } 
    } 
    JsonObject blockData = MappingData.loadData("blockData.json");
    JsonArray occluding = blockData.getAsJsonArray("occluding");
    for (JsonElement jsonElement : occluding)
      occludingStates.add(keyToId.get(jsonElement.getAsString())); 
    List<ConnectorInitAction> initActions = new ArrayList<>();
    initActions.add(PumpkinConnectionHandler.init());
    initActions.addAll(BasicFenceConnectionHandler.init());
    initActions.add(NetherFenceConnectionHandler.init());
    initActions.addAll(WallConnectionHandler.init());
    initActions.add(MelonConnectionHandler.init());
    initActions.addAll(GlassConnectionHandler.init());
    initActions.add(ChestConnectionHandler.init());
    initActions.add(DoorConnectionHandler.init());
    initActions.add(RedstoneConnectionHandler.init());
    initActions.add(StairConnectionHandler.init());
    initActions.add(FlowerConnectionHandler.init());
    initActions.addAll(ChorusPlantConnectionHandler.init());
    initActions.add(TripwireConnectionHandler.init());
    initActions.add(SnowyGrassConnectionHandler.init());
    if (Via.getConfig().isVineClimbFix())
      initActions.add(VineConnectionHandler.init()); 
    for (String key : keyToId.keySet()) {
      WrappedBlockData wrappedBlockData = WrappedBlockData.fromString(key);
      for (ConnectorInitAction action : initActions)
        action.check(wrappedBlockData); 
    } 
    if (Via.getConfig().getBlockConnectionMethod().equalsIgnoreCase("packet"))
      Via.getManager().getProviders().register(BlockConnectionProvider.class, (Provider)new PacketBlockConnectionProvider()); 
  }
  
  public static boolean isWelcome(int blockState) {
    return (blockConnectionData.containsKey(Integer.valueOf(blockState)) || connectionHandlerMap.containsKey(Integer.valueOf(blockState)));
  }
  
  public static boolean connects(int blockState) {
    return connectionHandlerMap.containsKey(Integer.valueOf(blockState));
  }
  
  public static int connect(UserConnection user, Position position, int blockState) {
    ConnectionHandler handler = connectionHandlerMap.get(Integer.valueOf(blockState));
    return (handler != null) ? handler.connect(user, position, blockState) : blockState;
  }
  
  public static ConnectionHandler getConnectionHandler(int blockstate) {
    return connectionHandlerMap.get(Integer.valueOf(blockstate));
  }
  
  public static int getId(String key) {
    return keyToId.containsKey(key) ? ((Integer)keyToId.get(key)).intValue() : -1;
  }
  
  public static String getKey(int id) {
    return idToKey.get(Integer.valueOf(id));
  }
  
  static interface ConnectorInitAction {
    void check(WrappedBlockData param1WrappedBlockData);
  }
}
