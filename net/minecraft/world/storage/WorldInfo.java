package net.minecraft.world.storage;

import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class WorldInfo {
  public static final EnumDifficulty DEFAULT_DIFFICULTY = EnumDifficulty.NORMAL;
  
  private long randomSeed;
  
  private WorldType terrainType = WorldType.DEFAULT;
  
  private String generatorOptions = "";
  
  private int spawnX;
  
  private int spawnY;
  
  private int spawnZ;
  
  private long totalTime;
  
  private long worldTime;
  
  private long lastTimePlayed;
  
  private long sizeOnDisk;
  
  private NBTTagCompound playerTag;
  
  private int dimension;
  
  private String levelName;
  
  private int saveVersion;
  
  private int cleanWeatherTime;
  
  private boolean raining;
  
  private int rainTime;
  
  private boolean thundering;
  
  private int thunderTime;
  
  private WorldSettings.GameType theGameType;
  
  private boolean mapFeaturesEnabled;
  
  private boolean hardcore;
  
  private boolean allowCommands;
  
  private boolean initialized;
  
  private EnumDifficulty difficulty;
  
  private boolean difficultyLocked;
  
  private double borderCenterX = 0.0D;
  
  private double borderCenterZ = 0.0D;
  
  private double borderSize = 6.0E7D;
  
  private long borderSizeLerpTime = 0L;
  
  private double borderSizeLerpTarget = 0.0D;
  
  private double borderSafeZone = 5.0D;
  
  private double borderDamagePerBlock = 0.2D;
  
  private int borderWarningDistance = 5;
  
  private int borderWarningTime = 15;
  
  private GameRules theGameRules = new GameRules();
  
  public WorldInfo(NBTTagCompound nbt) {
    this.randomSeed = nbt.getLong("RandomSeed");
    if (nbt.hasKey("generatorName", 8)) {
      String s = nbt.getString("generatorName");
      this.terrainType = WorldType.parseWorldType(s);
      if (this.terrainType == null) {
        this.terrainType = WorldType.DEFAULT;
      } else if (this.terrainType.isVersioned()) {
        int i = 0;
        if (nbt.hasKey("generatorVersion", 99))
          i = nbt.getInteger("generatorVersion"); 
        this.terrainType = this.terrainType.getWorldTypeForGeneratorVersion(i);
      } 
      if (nbt.hasKey("generatorOptions", 8))
        this.generatorOptions = nbt.getString("generatorOptions"); 
    } 
    this.theGameType = WorldSettings.GameType.getByID(nbt.getInteger("GameType"));
    if (nbt.hasKey("MapFeatures", 99)) {
      this.mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
    } else {
      this.mapFeaturesEnabled = true;
    } 
    this.spawnX = nbt.getInteger("SpawnX");
    this.spawnY = nbt.getInteger("SpawnY");
    this.spawnZ = nbt.getInteger("SpawnZ");
    this.totalTime = nbt.getLong("Time");
    if (nbt.hasKey("DayTime", 99)) {
      this.worldTime = nbt.getLong("DayTime");
    } else {
      this.worldTime = this.totalTime;
    } 
    this.lastTimePlayed = nbt.getLong("LastPlayed");
    this.sizeOnDisk = nbt.getLong("SizeOnDisk");
    this.levelName = nbt.getString("LevelName");
    this.saveVersion = nbt.getInteger("version");
    this.cleanWeatherTime = nbt.getInteger("clearWeatherTime");
    this.rainTime = nbt.getInteger("rainTime");
    this.raining = nbt.getBoolean("raining");
    this.thunderTime = nbt.getInteger("thunderTime");
    this.thundering = nbt.getBoolean("thundering");
    this.hardcore = nbt.getBoolean("hardcore");
    if (nbt.hasKey("initialized", 99)) {
      this.initialized = nbt.getBoolean("initialized");
    } else {
      this.initialized = true;
    } 
    if (nbt.hasKey("allowCommands", 99)) {
      this.allowCommands = nbt.getBoolean("allowCommands");
    } else {
      this.allowCommands = (this.theGameType == WorldSettings.GameType.CREATIVE);
    } 
    if (nbt.hasKey("Player", 10)) {
      this.playerTag = nbt.getCompoundTag("Player");
      this.dimension = this.playerTag.getInteger("Dimension");
    } 
    if (nbt.hasKey("GameRules", 10))
      this.theGameRules.readFromNBT(nbt.getCompoundTag("GameRules")); 
    if (nbt.hasKey("Difficulty", 99))
      this.difficulty = EnumDifficulty.getDifficultyEnum(nbt.getByte("Difficulty")); 
    if (nbt.hasKey("DifficultyLocked", 1))
      this.difficultyLocked = nbt.getBoolean("DifficultyLocked"); 
    if (nbt.hasKey("BorderCenterX", 99))
      this.borderCenterX = nbt.getDouble("BorderCenterX"); 
    if (nbt.hasKey("BorderCenterZ", 99))
      this.borderCenterZ = nbt.getDouble("BorderCenterZ"); 
    if (nbt.hasKey("BorderSize", 99))
      this.borderSize = nbt.getDouble("BorderSize"); 
    if (nbt.hasKey("BorderSizeLerpTime", 99))
      this.borderSizeLerpTime = nbt.getLong("BorderSizeLerpTime"); 
    if (nbt.hasKey("BorderSizeLerpTarget", 99))
      this.borderSizeLerpTarget = nbt.getDouble("BorderSizeLerpTarget"); 
    if (nbt.hasKey("BorderSafeZone", 99))
      this.borderSafeZone = nbt.getDouble("BorderSafeZone"); 
    if (nbt.hasKey("BorderDamagePerBlock", 99))
      this.borderDamagePerBlock = nbt.getDouble("BorderDamagePerBlock"); 
    if (nbt.hasKey("BorderWarningBlocks", 99))
      this.borderWarningDistance = nbt.getInteger("BorderWarningBlocks"); 
    if (nbt.hasKey("BorderWarningTime", 99))
      this.borderWarningTime = nbt.getInteger("BorderWarningTime"); 
  }
  
  public WorldInfo(WorldSettings settings, String name) {
    populateFromWorldSettings(settings);
    this.levelName = name;
    this.difficulty = DEFAULT_DIFFICULTY;
    this.initialized = false;
  }
  
  public void populateFromWorldSettings(WorldSettings settings) {
    this.randomSeed = settings.getSeed();
    this.theGameType = settings.getGameType();
    this.mapFeaturesEnabled = settings.isMapFeaturesEnabled();
    this.hardcore = settings.getHardcoreEnabled();
    this.terrainType = settings.getTerrainType();
    this.generatorOptions = settings.getWorldName();
    this.allowCommands = settings.areCommandsAllowed();
  }
  
  public WorldInfo(WorldInfo worldInformation) {
    this.randomSeed = worldInformation.randomSeed;
    this.terrainType = worldInformation.terrainType;
    this.generatorOptions = worldInformation.generatorOptions;
    this.theGameType = worldInformation.theGameType;
    this.mapFeaturesEnabled = worldInformation.mapFeaturesEnabled;
    this.spawnX = worldInformation.spawnX;
    this.spawnY = worldInformation.spawnY;
    this.spawnZ = worldInformation.spawnZ;
    this.totalTime = worldInformation.totalTime;
    this.worldTime = worldInformation.worldTime;
    this.lastTimePlayed = worldInformation.lastTimePlayed;
    this.sizeOnDisk = worldInformation.sizeOnDisk;
    this.playerTag = worldInformation.playerTag;
    this.dimension = worldInformation.dimension;
    this.levelName = worldInformation.levelName;
    this.saveVersion = worldInformation.saveVersion;
    this.rainTime = worldInformation.rainTime;
    this.raining = worldInformation.raining;
    this.thunderTime = worldInformation.thunderTime;
    this.thundering = worldInformation.thundering;
    this.hardcore = worldInformation.hardcore;
    this.allowCommands = worldInformation.allowCommands;
    this.initialized = worldInformation.initialized;
    this.theGameRules = worldInformation.theGameRules;
    this.difficulty = worldInformation.difficulty;
    this.difficultyLocked = worldInformation.difficultyLocked;
    this.borderCenterX = worldInformation.borderCenterX;
    this.borderCenterZ = worldInformation.borderCenterZ;
    this.borderSize = worldInformation.borderSize;
    this.borderSizeLerpTime = worldInformation.borderSizeLerpTime;
    this.borderSizeLerpTarget = worldInformation.borderSizeLerpTarget;
    this.borderSafeZone = worldInformation.borderSafeZone;
    this.borderDamagePerBlock = worldInformation.borderDamagePerBlock;
    this.borderWarningTime = worldInformation.borderWarningTime;
    this.borderWarningDistance = worldInformation.borderWarningDistance;
  }
  
  public NBTTagCompound getNBTTagCompound() {
    NBTTagCompound nbttagcompound = new NBTTagCompound();
    updateTagCompound(nbttagcompound, this.playerTag);
    return nbttagcompound;
  }
  
  public NBTTagCompound cloneNBTCompound(NBTTagCompound nbt) {
    NBTTagCompound nbttagcompound = new NBTTagCompound();
    updateTagCompound(nbttagcompound, nbt);
    return nbttagcompound;
  }
  
  private void updateTagCompound(NBTTagCompound nbt, NBTTagCompound playerNbt) {
    nbt.setLong("RandomSeed", this.randomSeed);
    nbt.setString("generatorName", this.terrainType.getWorldTypeName());
    nbt.setInteger("generatorVersion", this.terrainType.getGeneratorVersion());
    nbt.setString("generatorOptions", this.generatorOptions);
    nbt.setInteger("GameType", this.theGameType.getID());
    nbt.setBoolean("MapFeatures", this.mapFeaturesEnabled);
    nbt.setInteger("SpawnX", this.spawnX);
    nbt.setInteger("SpawnY", this.spawnY);
    nbt.setInteger("SpawnZ", this.spawnZ);
    nbt.setLong("Time", this.totalTime);
    nbt.setLong("DayTime", this.worldTime);
    nbt.setLong("SizeOnDisk", this.sizeOnDisk);
    nbt.setLong("LastPlayed", MinecraftServer.getCurrentTimeMillis());
    nbt.setString("LevelName", this.levelName);
    nbt.setInteger("version", this.saveVersion);
    nbt.setInteger("clearWeatherTime", this.cleanWeatherTime);
    nbt.setInteger("rainTime", this.rainTime);
    nbt.setBoolean("raining", this.raining);
    nbt.setInteger("thunderTime", this.thunderTime);
    nbt.setBoolean("thundering", this.thundering);
    nbt.setBoolean("hardcore", this.hardcore);
    nbt.setBoolean("allowCommands", this.allowCommands);
    nbt.setBoolean("initialized", this.initialized);
    nbt.setDouble("BorderCenterX", this.borderCenterX);
    nbt.setDouble("BorderCenterZ", this.borderCenterZ);
    nbt.setDouble("BorderSize", this.borderSize);
    nbt.setLong("BorderSizeLerpTime", this.borderSizeLerpTime);
    nbt.setDouble("BorderSafeZone", this.borderSafeZone);
    nbt.setDouble("BorderDamagePerBlock", this.borderDamagePerBlock);
    nbt.setDouble("BorderSizeLerpTarget", this.borderSizeLerpTarget);
    nbt.setDouble("BorderWarningBlocks", this.borderWarningDistance);
    nbt.setDouble("BorderWarningTime", this.borderWarningTime);
    if (this.difficulty != null)
      nbt.setByte("Difficulty", (byte)this.difficulty.getDifficultyId()); 
    nbt.setBoolean("DifficultyLocked", this.difficultyLocked);
    nbt.setTag("GameRules", (NBTBase)this.theGameRules.writeToNBT());
    if (playerNbt != null)
      nbt.setTag("Player", (NBTBase)playerNbt); 
  }
  
  public long getSeed() {
    return this.randomSeed;
  }
  
  public int getSpawnX() {
    return this.spawnX;
  }
  
  public int getSpawnY() {
    return this.spawnY;
  }
  
  public int getSpawnZ() {
    return this.spawnZ;
  }
  
  public long getWorldTotalTime() {
    return this.totalTime;
  }
  
  public long getWorldTime() {
    return this.worldTime;
  }
  
  public long getSizeOnDisk() {
    return this.sizeOnDisk;
  }
  
  public NBTTagCompound getPlayerNBTTagCompound() {
    return this.playerTag;
  }
  
  public void setSpawnX(int x) {
    this.spawnX = x;
  }
  
  public void setSpawnY(int y) {
    this.spawnY = y;
  }
  
  public void setSpawnZ(int z) {
    this.spawnZ = z;
  }
  
  public void setWorldTotalTime(long time) {
    this.totalTime = time;
  }
  
  public void setWorldTime(long time) {
    this.worldTime = time;
  }
  
  public void setSpawn(BlockPos spawnPoint) {
    this.spawnX = spawnPoint.getX();
    this.spawnY = spawnPoint.getY();
    this.spawnZ = spawnPoint.getZ();
  }
  
  public String getWorldName() {
    return this.levelName;
  }
  
  public void setWorldName(String worldName) {
    this.levelName = worldName;
  }
  
  public int getSaveVersion() {
    return this.saveVersion;
  }
  
  public void setSaveVersion(int version) {
    this.saveVersion = version;
  }
  
  public long getLastTimePlayed() {
    return this.lastTimePlayed;
  }
  
  public int getCleanWeatherTime() {
    return this.cleanWeatherTime;
  }
  
  public void setCleanWeatherTime(int cleanWeatherTimeIn) {
    this.cleanWeatherTime = cleanWeatherTimeIn;
  }
  
  public boolean isThundering() {
    return this.thundering;
  }
  
  public void setThundering(boolean thunderingIn) {
    this.thundering = thunderingIn;
  }
  
  public int getThunderTime() {
    return this.thunderTime;
  }
  
  public void setThunderTime(int time) {
    this.thunderTime = time;
  }
  
  public boolean isRaining() {
    return this.raining;
  }
  
  public void setRaining(boolean isRaining) {
    this.raining = isRaining;
  }
  
  public int getRainTime() {
    return this.rainTime;
  }
  
  public void setRainTime(int time) {
    this.rainTime = time;
  }
  
  public WorldSettings.GameType getGameType() {
    return this.theGameType;
  }
  
  public boolean isMapFeaturesEnabled() {
    return this.mapFeaturesEnabled;
  }
  
  public void setMapFeaturesEnabled(boolean enabled) {
    this.mapFeaturesEnabled = enabled;
  }
  
  public void setGameType(WorldSettings.GameType type) {
    this.theGameType = type;
  }
  
  public boolean isHardcoreModeEnabled() {
    return this.hardcore;
  }
  
  public void setHardcore(boolean hardcoreIn) {
    this.hardcore = hardcoreIn;
  }
  
  public WorldType getTerrainType() {
    return this.terrainType;
  }
  
  public void setTerrainType(WorldType type) {
    this.terrainType = type;
  }
  
  public String getGeneratorOptions() {
    return this.generatorOptions;
  }
  
  public boolean areCommandsAllowed() {
    return this.allowCommands;
  }
  
  public void setAllowCommands(boolean allow) {
    this.allowCommands = allow;
  }
  
  public boolean isInitialized() {
    return this.initialized;
  }
  
  public void setServerInitialized(boolean initializedIn) {
    this.initialized = initializedIn;
  }
  
  public GameRules getGameRulesInstance() {
    return this.theGameRules;
  }
  
  public double getBorderCenterX() {
    return this.borderCenterX;
  }
  
  public double getBorderCenterZ() {
    return this.borderCenterZ;
  }
  
  public double getBorderSize() {
    return this.borderSize;
  }
  
  public void setBorderSize(double size) {
    this.borderSize = size;
  }
  
  public long getBorderLerpTime() {
    return this.borderSizeLerpTime;
  }
  
  public void setBorderLerpTime(long time) {
    this.borderSizeLerpTime = time;
  }
  
  public double getBorderLerpTarget() {
    return this.borderSizeLerpTarget;
  }
  
  public void setBorderLerpTarget(double lerpSize) {
    this.borderSizeLerpTarget = lerpSize;
  }
  
  public void getBorderCenterZ(double posZ) {
    this.borderCenterZ = posZ;
  }
  
  public void getBorderCenterX(double posX) {
    this.borderCenterX = posX;
  }
  
  public double getBorderSafeZone() {
    return this.borderSafeZone;
  }
  
  public void setBorderSafeZone(double amount) {
    this.borderSafeZone = amount;
  }
  
  public double getBorderDamagePerBlock() {
    return this.borderDamagePerBlock;
  }
  
  public void setBorderDamagePerBlock(double damage) {
    this.borderDamagePerBlock = damage;
  }
  
  public int getBorderWarningDistance() {
    return this.borderWarningDistance;
  }
  
  public int getBorderWarningTime() {
    return this.borderWarningTime;
  }
  
  public void setBorderWarningDistance(int amountOfBlocks) {
    this.borderWarningDistance = amountOfBlocks;
  }
  
  public void setBorderWarningTime(int ticks) {
    this.borderWarningTime = ticks;
  }
  
  public EnumDifficulty getDifficulty() {
    return this.difficulty;
  }
  
  public void setDifficulty(EnumDifficulty newDifficulty) {
    this.difficulty = newDifficulty;
  }
  
  public boolean isDifficultyLocked() {
    return this.difficultyLocked;
  }
  
  public void setDifficultyLocked(boolean locked) {
    this.difficultyLocked = locked;
  }
  
  public void addToCrashReport(CrashReportCategory category) {
    category.addCrashSectionCallable("Level seed", new Callable<String>() {
          public String call() throws Exception {
            return String.valueOf(WorldInfo.this.getSeed());
          }
        });
    category.addCrashSectionCallable("Level generator", new Callable<String>() {
          public String call() throws Exception {
            return String.format("ID %02d - %s, ver %d. Features enabled: %b", new Object[] { Integer.valueOf(WorldInfo.access$000(this.this$0).getWorldTypeID()), WorldInfo.access$000(this.this$0).getWorldTypeName(), Integer.valueOf(WorldInfo.access$000(this.this$0).getGeneratorVersion()), Boolean.valueOf(WorldInfo.access$100(this.this$0)) });
          }
        });
    category.addCrashSectionCallable("Level generator options", new Callable<String>() {
          public String call() throws Exception {
            return WorldInfo.this.generatorOptions;
          }
        });
    category.addCrashSectionCallable("Level spawn location", new Callable<String>() {
          public String call() throws Exception {
            return CrashReportCategory.getCoordinateInfo(WorldInfo.this.spawnX, WorldInfo.this.spawnY, WorldInfo.this.spawnZ);
          }
        });
    category.addCrashSectionCallable("Level time", new Callable<String>() {
          public String call() throws Exception {
            return String.format("%d game time, %d day time", new Object[] { Long.valueOf(WorldInfo.access$600(this.this$0)), Long.valueOf(WorldInfo.access$700(this.this$0)) });
          }
        });
    category.addCrashSectionCallable("Level dimension", new Callable<String>() {
          public String call() throws Exception {
            return String.valueOf(WorldInfo.this.dimension);
          }
        });
    category.addCrashSectionCallable("Level storage version", new Callable<String>() {
          public String call() throws Exception {
            String s = "Unknown?";
            try {
              switch (WorldInfo.this.saveVersion) {
                case 19132:
                  s = "McRegion";
                  break;
                case 19133:
                  s = "Anvil";
                  break;
              } 
            } catch (Throwable throwable) {}
            return String.format("0x%05X - %s", new Object[] { Integer.valueOf(WorldInfo.access$900(this.this$0)), s });
          }
        });
    category.addCrashSectionCallable("Level weather", new Callable<String>() {
          public String call() throws Exception {
            return String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", new Object[] { Integer.valueOf(WorldInfo.access$1000(this.this$0)), Boolean.valueOf(WorldInfo.access$1100(this.this$0)), Integer.valueOf(WorldInfo.access$1200(this.this$0)), Boolean.valueOf(WorldInfo.access$1300(this.this$0)) });
          }
        });
    category.addCrashSectionCallable("Level game mode", new Callable<String>() {
          public String call() throws Exception {
            return String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", new Object[] { WorldInfo.access$1400(this.this$0).getName(), Integer.valueOf(WorldInfo.access$1400(this.this$0).getID()), Boolean.valueOf(WorldInfo.access$1500(this.this$0)), Boolean.valueOf(WorldInfo.access$1600(this.this$0)) });
          }
        });
  }
  
  protected WorldInfo() {}
}
