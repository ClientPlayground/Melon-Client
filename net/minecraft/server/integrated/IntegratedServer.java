package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.ClearWater;
import net.optifine.reflect.Reflector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IntegratedServer extends MinecraftServer {
  private static final Logger logger = LogManager.getLogger();
  
  private final Minecraft mc;
  
  private final WorldSettings theWorldSettings;
  
  private boolean isGamePaused;
  
  private boolean isPublic;
  
  private ThreadLanServerPing lanServerPing;
  
  private long ticksSaveLast = 0L;
  
  public World difficultyUpdateWorld = null;
  
  public BlockPos difficultyUpdatePos = null;
  
  public DifficultyInstance difficultyLast = null;
  
  public IntegratedServer(Minecraft mcIn) {
    super(mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
    this.mc = mcIn;
    this.theWorldSettings = null;
  }
  
  public IntegratedServer(Minecraft mcIn, String folderName, String worldName, WorldSettings settings) {
    super(new File(mcIn.mcDataDir, "saves"), mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
    setServerOwner(mcIn.getSession().getUsername());
    setFolderName(folderName);
    setWorldName(worldName);
    setDemo(mcIn.isDemo());
    canCreateBonusChest(settings.isBonusChestEnabled());
    setBuildLimit(256);
    setConfigManager(new IntegratedPlayerList(this));
    this.mc = mcIn;
    this.theWorldSettings = isDemo() ? DemoWorldServer.demoWorldSettings : settings;
    ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(folderName, false);
    WorldInfo worldinfo = isavehandler.loadWorldInfo();
    if (worldinfo != null) {
      NBTTagCompound nbttagcompound = worldinfo.getPlayerNBTTagCompound();
      if (nbttagcompound != null && nbttagcompound.hasKey("Dimension")) {
        int i = nbttagcompound.getInteger("Dimension");
        PacketThreadUtil.lastDimensionId = i;
        this.mc.loadingScreen.setLoadingProgress(-1);
      } 
    } 
  }
  
  protected ServerCommandManager createNewCommandManager() {
    return new IntegratedServerCommandManager();
  }
  
  protected void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String worldNameIn2) {
    convertMapIfNeeded(saveName);
    boolean flag = Reflector.DimensionManager.exists();
    if (!flag) {
      this.worldServers = new WorldServer[3];
      this.timeOfLastDimensionTick = new long[this.worldServers.length][100];
    } 
    ISaveHandler isavehandler = getActiveAnvilConverter().getSaveLoader(saveName, true);
    setResourcePackFromWorld(getFolderName(), isavehandler);
    WorldInfo worldinfo = isavehandler.loadWorldInfo();
    if (worldinfo == null) {
      worldinfo = new WorldInfo(this.theWorldSettings, worldNameIn);
    } else {
      worldinfo.setWorldName(worldNameIn);
    } 
    if (flag) {
      WorldServer worldserver = isDemo() ? (WorldServer)(new DemoWorldServer(this, isavehandler, worldinfo, 0, this.theProfiler)).init() : (WorldServer)(new WorldServer(this, isavehandler, worldinfo, 0, this.theProfiler)).init();
      worldserver.initialize(this.theWorldSettings);
      Integer[] ainteger = (Integer[])Reflector.call(Reflector.DimensionManager_getStaticDimensionIDs, new Object[0]);
      Integer[] ainteger1 = ainteger;
      int i = ainteger.length;
      for (int j = 0; j < i; j++) {
        int k = ainteger1[j].intValue();
        WorldServer worldserver1 = (k == 0) ? worldserver : (WorldServer)(new WorldServerMulti(this, isavehandler, k, worldserver, this.theProfiler)).init();
        worldserver1.addWorldAccess((IWorldAccess)new WorldManager(this, worldserver1));
        if (!isSinglePlayer())
          worldserver1.getWorldInfo().setGameType(getGameType()); 
        if (Reflector.EventBus.exists())
          Reflector.postForgeBusEvent(Reflector.WorldEvent_Load_Constructor, new Object[] { worldserver1 }); 
      } 
      getConfigurationManager().setPlayerManager(new WorldServer[] { worldserver });
      if (worldserver.getWorldInfo().getDifficulty() == null)
        setDifficultyForAllWorlds(this.mc.gameSettings.difficulty); 
    } else {
      for (int l = 0; l < this.worldServers.length; l++) {
        int i1 = 0;
        if (l == 1)
          i1 = -1; 
        if (l == 2)
          i1 = 1; 
        if (l == 0) {
          if (isDemo()) {
            this.worldServers[l] = (WorldServer)(new DemoWorldServer(this, isavehandler, worldinfo, i1, this.theProfiler)).init();
          } else {
            this.worldServers[l] = (WorldServer)(new WorldServer(this, isavehandler, worldinfo, i1, this.theProfiler)).init();
          } 
          this.worldServers[l].initialize(this.theWorldSettings);
        } else {
          this.worldServers[l] = (WorldServer)(new WorldServerMulti(this, isavehandler, i1, this.worldServers[0], this.theProfiler)).init();
        } 
        this.worldServers[l].addWorldAccess((IWorldAccess)new WorldManager(this, this.worldServers[l]));
      } 
      getConfigurationManager().setPlayerManager(this.worldServers);
      if (this.worldServers[0].getWorldInfo().getDifficulty() == null)
        setDifficultyForAllWorlds(this.mc.gameSettings.difficulty); 
    } 
    initialWorldChunkLoad();
  }
  
  protected boolean startServer() throws IOException {
    logger.info("Starting integrated minecraft server version 1.9");
    setOnlineMode(true);
    setCanSpawnAnimals(true);
    setCanSpawnNPCs(true);
    setAllowPvp(true);
    setAllowFlight(true);
    logger.info("Generating keypair");
    setKeyPair(CryptManager.generateKeyPair());
    if (Reflector.FMLCommonHandler_handleServerAboutToStart.exists()) {
      Object object = Reflector.call(Reflector.FMLCommonHandler_instance, new Object[0]);
      if (!Reflector.callBoolean(object, Reflector.FMLCommonHandler_handleServerAboutToStart, new Object[] { this }))
        return false; 
    } 
    loadAllWorlds(getFolderName(), getWorldName(), this.theWorldSettings.getSeed(), this.theWorldSettings.getTerrainType(), this.theWorldSettings.getWorldName());
    setMOTD(getServerOwner() + " - " + this.worldServers[0].getWorldInfo().getWorldName());
    if (Reflector.FMLCommonHandler_handleServerStarting.exists()) {
      Object object1 = Reflector.call(Reflector.FMLCommonHandler_instance, new Object[0]);
      if (Reflector.FMLCommonHandler_handleServerStarting.getReturnType() == boolean.class)
        return Reflector.callBoolean(object1, Reflector.FMLCommonHandler_handleServerStarting, new Object[] { this }); 
      Reflector.callVoid(object1, Reflector.FMLCommonHandler_handleServerStarting, new Object[] { this });
    } 
    return true;
  }
  
  public void tick() {
    onTick();
    boolean flag = this.isGamePaused;
    this.isGamePaused = (Minecraft.getMinecraft().getNetHandler() != null && Minecraft.getMinecraft().isGamePaused());
    if (!flag && this.isGamePaused) {
      logger.info("Saving and pausing game...");
      getConfigurationManager().saveAllPlayerData();
      saveAllWorlds(false);
    } 
    if (this.isGamePaused) {
      synchronized (this.futureTaskQueue) {
        while (!this.futureTaskQueue.isEmpty())
          Util.runTask(this.futureTaskQueue.poll(), logger); 
      } 
    } else {
      super.tick();
      if (this.mc.gameSettings.renderDistanceChunks != getConfigurationManager().getViewDistance()) {
        logger.info("Changing view distance to {}, from {}", new Object[] { Integer.valueOf(this.mc.gameSettings.renderDistanceChunks), Integer.valueOf(getConfigurationManager().getViewDistance()) });
        getConfigurationManager().setViewDistance(this.mc.gameSettings.renderDistanceChunks);
      } 
      if (this.mc.theWorld != null) {
        WorldInfo worldinfo1 = this.worldServers[0].getWorldInfo();
        WorldInfo worldinfo = this.mc.theWorld.getWorldInfo();
        if (!worldinfo1.isDifficultyLocked() && worldinfo.getDifficulty() != worldinfo1.getDifficulty()) {
          logger.info("Changing difficulty to {}, from {}", new Object[] { worldinfo.getDifficulty(), worldinfo1.getDifficulty() });
          setDifficultyForAllWorlds(worldinfo.getDifficulty());
        } else if (worldinfo.isDifficultyLocked() && !worldinfo1.isDifficultyLocked()) {
          logger.info("Locking difficulty to {}", new Object[] { worldinfo.getDifficulty() });
          for (WorldServer worldserver : this.worldServers) {
            if (worldserver != null)
              worldserver.getWorldInfo().setDifficultyLocked(true); 
          } 
        } 
      } 
    } 
  }
  
  public boolean canStructuresSpawn() {
    return false;
  }
  
  public WorldSettings.GameType getGameType() {
    return this.theWorldSettings.getGameType();
  }
  
  public EnumDifficulty getDifficulty() {
    return (this.mc.theWorld == null) ? this.mc.gameSettings.difficulty : this.mc.theWorld.getWorldInfo().getDifficulty();
  }
  
  public boolean isHardcore() {
    return this.theWorldSettings.getHardcoreEnabled();
  }
  
  public boolean shouldBroadcastRconToOps() {
    return true;
  }
  
  public boolean shouldBroadcastConsoleToOps() {
    return true;
  }
  
  public void saveAllWorlds(boolean dontLog) {
    if (dontLog) {
      int i = getTickCounter();
      int j = this.mc.gameSettings.ofAutoSaveTicks;
      if (i < this.ticksSaveLast + j)
        return; 
      this.ticksSaveLast = i;
    } 
    super.saveAllWorlds(dontLog);
  }
  
  public File getDataDirectory() {
    return this.mc.mcDataDir;
  }
  
  public boolean isDedicatedServer() {
    return false;
  }
  
  public boolean shouldUseNativeTransport() {
    return false;
  }
  
  protected void finalTick(CrashReport report) {
    this.mc.crashed(report);
  }
  
  public CrashReport addServerInfoToCrashReport(CrashReport report) {
    report = super.addServerInfoToCrashReport(report);
    report.getCategory().addCrashSectionCallable("Type", new Callable<String>() {
          public String call() throws Exception {
            return "Integrated Server (map_client.txt)";
          }
        });
    report.getCategory().addCrashSectionCallable("Is Modded", new Callable<String>() {
          public String call() throws Exception {
            String s = ClientBrandRetriever.getClientModName();
            if (!s.equals("vanilla"))
              return "Definitely; Client brand changed to '" + s + "'"; 
            s = IntegratedServer.this.getServerModName();
            return !s.equals("vanilla") ? ("Definitely; Server brand changed to '" + s + "'") : ((Minecraft.class.getSigners() == null) ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.");
          }
        });
    return report;
  }
  
  public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
    super.setDifficultyForAllWorlds(difficulty);
    if (this.mc.theWorld != null)
      this.mc.theWorld.getWorldInfo().setDifficulty(difficulty); 
  }
  
  public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper) {
    super.addServerStatsToSnooper(playerSnooper);
    playerSnooper.addClientStat("snooper_partner", this.mc.getPlayerUsageSnooper().getUniqueID());
  }
  
  public boolean isSnooperEnabled() {
    return Minecraft.getMinecraft().isSnooperEnabled();
  }
  
  public String shareToLAN(WorldSettings.GameType type, boolean allowCheats) {
    try {
      int i = -1;
      try {
        i = HttpUtil.getSuitableLanPort();
      } catch (IOException iOException) {}
      if (i <= 0)
        i = 25564; 
      getNetworkSystem().addLanEndpoint((InetAddress)null, i);
      logger.info("Started on " + i);
      this.isPublic = true;
      this.lanServerPing = new ThreadLanServerPing(getMOTD(), i + "");
      this.lanServerPing.start();
      getConfigurationManager().setGameType(type);
      getConfigurationManager().setCommandsAllowedForAll(allowCheats);
      return i + "";
    } catch (IOException var6) {
      return null;
    } 
  }
  
  public void stopServer() {
    super.stopServer();
    if (this.lanServerPing != null) {
      this.lanServerPing.interrupt();
      this.lanServerPing = null;
    } 
  }
  
  public void initiateShutdown() {
    if (!Reflector.MinecraftForge.exists() || isServerRunning())
      Futures.getUnchecked((Future)addScheduledTask(new Runnable() {
              public void run() {
                for (EntityPlayerMP entityplayermp : Lists.newArrayList(IntegratedServer.this.getConfigurationManager().getPlayerList()))
                  IntegratedServer.this.getConfigurationManager().playerLoggedOut(entityplayermp); 
              }
            })); 
    super.initiateShutdown();
    if (this.lanServerPing != null) {
      this.lanServerPing.interrupt();
      this.lanServerPing = null;
    } 
  }
  
  public void setStaticInstance() {
    setInstance();
  }
  
  public boolean getPublic() {
    return this.isPublic;
  }
  
  public void setGameType(WorldSettings.GameType gameMode) {
    getConfigurationManager().setGameType(gameMode);
  }
  
  public boolean isCommandBlockEnabled() {
    return true;
  }
  
  public int getOpPermissionLevel() {
    return 4;
  }
  
  private void onTick() {
    for (WorldServer worldserver : Arrays.<WorldServer>asList(this.worldServers))
      onTick(worldserver); 
  }
  
  public DifficultyInstance getDifficultyAsync(World p_getDifficultyAsync_1_, BlockPos p_getDifficultyAsync_2_) {
    this.difficultyUpdateWorld = p_getDifficultyAsync_1_;
    this.difficultyUpdatePos = p_getDifficultyAsync_2_;
    return this.difficultyLast;
  }
  
  private void onTick(WorldServer p_onTick_1_) {
    if (!Config.isTimeDefault())
      fixWorldTime(p_onTick_1_); 
    if (!Config.isWeatherEnabled())
      fixWorldWeather(p_onTick_1_); 
    if (Config.waterOpacityChanged) {
      Config.waterOpacityChanged = false;
      ClearWater.updateWaterOpacity(Config.getGameSettings(), (World)p_onTick_1_);
    } 
    if (this.difficultyUpdateWorld == p_onTick_1_ && this.difficultyUpdatePos != null) {
      this.difficultyLast = p_onTick_1_.getDifficultyForLocation(this.difficultyUpdatePos);
      this.difficultyUpdateWorld = null;
      this.difficultyUpdatePos = null;
    } 
  }
  
  private void fixWorldWeather(WorldServer p_fixWorldWeather_1_) {
    WorldInfo worldinfo = p_fixWorldWeather_1_.getWorldInfo();
    if (worldinfo.isRaining() || worldinfo.isThundering()) {
      worldinfo.setRainTime(0);
      worldinfo.setRaining(false);
      p_fixWorldWeather_1_.setRainStrength(0.0F);
      worldinfo.setThunderTime(0);
      worldinfo.setThundering(false);
      p_fixWorldWeather_1_.setThunderStrength(0.0F);
      getConfigurationManager().sendPacketToAllPlayers((Packet)new S2BPacketChangeGameState(2, 0.0F));
      getConfigurationManager().sendPacketToAllPlayers((Packet)new S2BPacketChangeGameState(7, 0.0F));
      getConfigurationManager().sendPacketToAllPlayers((Packet)new S2BPacketChangeGameState(8, 0.0F));
    } 
  }
  
  private void fixWorldTime(WorldServer p_fixWorldTime_1_) {
    WorldInfo worldinfo = p_fixWorldTime_1_.getWorldInfo();
    if (worldinfo.getGameType().getID() == 1) {
      long i = p_fixWorldTime_1_.getWorldTime();
      long j = i % 24000L;
      if (Config.isTimeDayOnly()) {
        if (j <= 1000L)
          p_fixWorldTime_1_.setWorldTime(i - j + 1001L); 
        if (j >= 11000L)
          p_fixWorldTime_1_.setWorldTime(i - j + 24001L); 
      } 
      if (Config.isTimeNightOnly()) {
        if (j <= 14000L)
          p_fixWorldTime_1_.setWorldTime(i - j + 14001L); 
        if (j >= 22000L)
          p_fixWorldTime_1_.setWorldTime(i - j + 24000L + 14001L); 
      } 
    } 
  }
}
