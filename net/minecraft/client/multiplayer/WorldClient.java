package net.minecraft.client.multiplayer;

import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.imp.ServerEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.CustomGuis;
import net.optifine.DynamicLights;
import net.optifine.override.PlayerControllerOF;
import net.optifine.reflect.Reflector;

public class WorldClient extends World {
  private NetHandlerPlayClient sendQueue;
  
  public ChunkProviderClient clientChunkProvider;
  
  private final Set<Entity> entityList = Sets.newHashSet();
  
  private final Set<Entity> entitySpawnQueue = Sets.newHashSet();
  
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final Set<ChunkCoordIntPair> previousActiveChunkSet = Sets.newHashSet();
  
  private boolean playerUpdate = false;
  
  public WorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
    super((ISaveHandler)new SaveHandlerMP(), new WorldInfo(settings, "MpServer"), WorldProvider.getProviderForDimension(dimension), profilerIn, true);
    this.sendQueue = netHandler;
    getWorldInfo().setDifficulty(difficulty);
    this.provider.registerWorld(this);
    setSpawnPoint(new BlockPos(8, 64, 8));
    this.chunkProvider = createChunkProvider();
    this.mapStorage = (MapStorage)new SaveDataMemoryStorage();
    calculateInitialSkylight();
    calculateInitialWeather();
    Reflector.postForgeBusEvent(Reflector.WorldEvent_Load_Constructor, new Object[] { this });
    if (this.mc.playerController != null && this.mc.playerController.getClass() == PlayerControllerMP.class) {
      this.mc.playerController = (PlayerControllerMP)new PlayerControllerOF(this.mc, netHandler);
      CustomGuis.setPlayerControllerOF((PlayerControllerOF)this.mc.playerController);
    } 
  }
  
  public void tick() {
    super.tick();
    setTotalWorldTime(getTotalWorldTime() + 1L);
    if (getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
      setWorldTime(getWorldTime() + 1L); 
    this.theProfiler.startSection("reEntryProcessing");
    for (int i = 0; i < 10 && !this.entitySpawnQueue.isEmpty(); i++) {
      Entity entity = this.entitySpawnQueue.iterator().next();
      this.entitySpawnQueue.remove(entity);
      if (!this.loadedEntityList.contains(entity))
        spawnEntityInWorld(entity); 
    } 
    this.theProfiler.endStartSection("chunkCache");
    this.clientChunkProvider.unloadQueuedChunks();
    this.theProfiler.endStartSection("blocks");
    updateBlocks();
    this.theProfiler.endSection();
  }
  
  public void invalidateBlockReceiveRegion(int x1, int y1, int z1, int x2, int y2, int z2) {}
  
  protected IChunkProvider createChunkProvider() {
    this.clientChunkProvider = new ChunkProviderClient(this);
    return this.clientChunkProvider;
  }
  
  protected void updateBlocks() {
    super.updateBlocks();
    this.previousActiveChunkSet.retainAll(this.activeChunkSet);
    if (this.previousActiveChunkSet.size() == this.activeChunkSet.size())
      this.previousActiveChunkSet.clear(); 
    int i = 0;
    for (ChunkCoordIntPair chunkcoordintpair : this.activeChunkSet) {
      if (!this.previousActiveChunkSet.contains(chunkcoordintpair)) {
        int j = chunkcoordintpair.chunkXPos * 16;
        int k = chunkcoordintpair.chunkZPos * 16;
        this.theProfiler.startSection("getChunk");
        Chunk chunk = getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
        playMoodSoundAndCheckLight(j, k, chunk);
        this.theProfiler.endSection();
        this.previousActiveChunkSet.add(chunkcoordintpair);
        i++;
        if (i >= 10)
          return; 
      } 
    } 
  }
  
  public void doPreChunk(int chuncX, int chuncZ, boolean loadChunk) {
    if (loadChunk) {
      this.clientChunkProvider.loadChunk(chuncX, chuncZ);
    } else {
      this.clientChunkProvider.unloadChunk(chuncX, chuncZ);
    } 
    if (!loadChunk)
      markBlockRangeForRenderUpdate(chuncX * 16, 0, chuncZ * 16, chuncX * 16 + 15, 256, chuncZ * 16 + 15); 
  }
  
  public boolean spawnEntityInWorld(Entity entityIn) {
    boolean flag = super.spawnEntityInWorld(entityIn);
    this.entityList.add(entityIn);
    if (!flag) {
      this.entitySpawnQueue.add(entityIn);
    } else if (entityIn instanceof EntityMinecart) {
      this.mc.getSoundHandler().playSound((ISound)new MovingSoundMinecart((EntityMinecart)entityIn));
    } 
    return flag;
  }
  
  public void removeEntity(Entity entityIn) {
    super.removeEntity(entityIn);
    this.entityList.remove(entityIn);
  }
  
  public void onEntityAdded(Entity entityIn) {
    super.onEntityAdded(entityIn);
    if (this.entitySpawnQueue.contains(entityIn))
      this.entitySpawnQueue.remove(entityIn); 
  }
  
  public void onEntityRemoved(Entity entityIn) {
    super.onEntityRemoved(entityIn);
    boolean flag = false;
    if (this.entityList.contains(entityIn))
      if (entityIn.isEntityAlive()) {
        this.entitySpawnQueue.add(entityIn);
        flag = true;
      } else {
        this.entityList.remove(entityIn);
      }  
  }
  
  public void addEntityToWorld(int entityID, Entity entityToSpawn) {
    Entity entity = getEntityByID(entityID);
    if (entity != null)
      removeEntity(entity); 
    this.entityList.add(entityToSpawn);
    entityToSpawn.setEntityId(entityID);
    if (!spawnEntityInWorld(entityToSpawn))
      this.entitySpawnQueue.add(entityToSpawn); 
    this.entitiesById.addKey(entityID, entityToSpawn);
  }
  
  public Entity getEntityByID(int id) {
    return (id == this.mc.thePlayer.getEntityId()) ? (Entity)this.mc.thePlayer : super.getEntityByID(id);
  }
  
  public Entity removeEntityFromWorld(int entityID) {
    Entity entity = (Entity)this.entitiesById.removeObject(entityID);
    if (entity != null) {
      this.entityList.remove(entity);
      removeEntity(entity);
    } 
    return entity;
  }
  
  public boolean invalidateRegionAndSetBlock(BlockPos pos, IBlockState state) {
    int i = pos.getX();
    int j = pos.getY();
    int k = pos.getZ();
    invalidateBlockReceiveRegion(i, j, k, i, j, k);
    return super.setBlockState(pos, state, 3);
  }
  
  public void sendQuittingDisconnectingPacket() {
    EventHandler.call((Event)new ServerEvent.Leave());
    this.sendQueue.getNetworkManager().closeChannel((IChatComponent)new ChatComponentText("Quitting"));
  }
  
  protected void updateWeather() {}
  
  protected int getRenderDistanceChunks() {
    return this.mc.gameSettings.renderDistanceChunks;
  }
  
  public void doVoidFogParticles(int posX, int posY, int posZ) {
    int i = 16;
    Random random = new Random();
    ItemStack itemstack = this.mc.thePlayer.getHeldItem();
    boolean flag = (this.mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE && itemstack != null && Block.getBlockFromItem(itemstack.getItem()) == Blocks.barrier);
    BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
    for (int j = 0; j < 1000; j++) {
      int k = posX + this.rand.nextInt(i) - this.rand.nextInt(i);
      int l = posY + this.rand.nextInt(i) - this.rand.nextInt(i);
      int i1 = posZ + this.rand.nextInt(i) - this.rand.nextInt(i);
      blockpos$mutableblockpos.set(k, l, i1);
      IBlockState iblockstate = getBlockState((BlockPos)blockpos$mutableblockpos);
      iblockstate.getBlock().randomDisplayTick(this, (BlockPos)blockpos$mutableblockpos, iblockstate, random);
      if (flag && iblockstate.getBlock() == Blocks.barrier)
        spawnParticle(EnumParticleTypes.BARRIER, (k + 0.5F), (l + 0.5F), (i1 + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]); 
    } 
  }
  
  public void removeAllEntities() {
    this.loadedEntityList.removeAll(this.unloadedEntityList);
    for (int i = 0; i < this.unloadedEntityList.size(); i++) {
      Entity entity = this.unloadedEntityList.get(i);
      int j = entity.chunkCoordX;
      int k = entity.chunkCoordZ;
      if (entity.addedToChunk && isChunkLoaded(j, k, true))
        getChunkFromChunkCoords(j, k).removeEntity(entity); 
    } 
    for (int l = 0; l < this.unloadedEntityList.size(); l++)
      onEntityRemoved(this.unloadedEntityList.get(l)); 
    this.unloadedEntityList.clear();
    for (int i1 = 0; i1 < this.loadedEntityList.size(); i1++) {
      Entity entity1 = this.loadedEntityList.get(i1);
      if (entity1.ridingEntity != null) {
        if (!entity1.ridingEntity.isDead && entity1.ridingEntity.riddenByEntity == entity1)
          continue; 
        entity1.ridingEntity.riddenByEntity = null;
        entity1.ridingEntity = null;
      } 
      if (entity1.isDead) {
        int j1 = entity1.chunkCoordX;
        int k1 = entity1.chunkCoordZ;
        if (entity1.addedToChunk && isChunkLoaded(j1, k1, true))
          getChunkFromChunkCoords(j1, k1).removeEntity(entity1); 
        this.loadedEntityList.remove(i1--);
        onEntityRemoved(entity1);
      } 
      continue;
    } 
  }
  
  public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
    CrashReportCategory crashreportcategory = super.addWorldInfoToCrashReport(report);
    crashreportcategory.addCrashSectionCallable("Forced entities", new Callable<String>() {
          public String call() {
            return WorldClient.this.entityList.size() + " total; " + WorldClient.this.entityList.toString();
          }
        });
    crashreportcategory.addCrashSectionCallable("Retry entities", new Callable<String>() {
          public String call() {
            return WorldClient.this.entitySpawnQueue.size() + " total; " + WorldClient.this.entitySpawnQueue.toString();
          }
        });
    crashreportcategory.addCrashSectionCallable("Server brand", new Callable<String>() {
          public String call() throws Exception {
            return WorldClient.this.mc.thePlayer.getClientBrand();
          }
        });
    crashreportcategory.addCrashSectionCallable("Server type", new Callable<String>() {
          public String call() throws Exception {
            return (WorldClient.this.mc.getIntegratedServer() == null) ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
          }
        });
    return crashreportcategory;
  }
  
  public void playSoundAtPos(BlockPos pos, String soundName, float volume, float pitch, boolean distanceDelay) {
    playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, soundName, volume, pitch, distanceDelay);
  }
  
  public void playSound(double x, double y, double z, String soundName, float volume, float pitch, boolean distanceDelay) {
    double d0 = this.mc.getRenderViewEntity().getDistanceSq(x, y, z);
    PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(new ResourceLocation(soundName), volume, pitch, (float)x, (float)y, (float)z);
    if (distanceDelay && d0 > 100.0D) {
      double d1 = Math.sqrt(d0) / 40.0D;
      this.mc.getSoundHandler().playDelayedSound((ISound)positionedsoundrecord, (int)(d1 * 20.0D));
    } else {
      this.mc.getSoundHandler().playSound((ISound)positionedsoundrecord);
    } 
  }
  
  public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compund) {
    this.mc.effectRenderer.addEffect((EntityFX)new EntityFirework.StarterFX(this, x, y, z, motionX, motionY, motionZ, this.mc.effectRenderer, compund));
  }
  
  public void setWorldScoreboard(Scoreboard scoreboardIn) {
    this.worldScoreboard = scoreboardIn;
  }
  
  public void setWorldTime(long time) {
    if (time < 0L) {
      time = -time;
      getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
    } else {
      getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
    } 
    super.setWorldTime(time);
  }
  
  public int getCombinedLight(BlockPos pos, int lightValue) {
    int i = super.getCombinedLight(pos, lightValue);
    if (Config.isDynamicLights())
      i = DynamicLights.getCombinedLight(pos, i); 
    return i;
  }
  
  public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
    this.playerUpdate = isPlayerActing();
    boolean flag = super.setBlockState(pos, newState, flags);
    this.playerUpdate = false;
    return flag;
  }
  
  private boolean isPlayerActing() {
    if (this.mc.playerController instanceof PlayerControllerOF) {
      PlayerControllerOF playercontrollerof = (PlayerControllerOF)this.mc.playerController;
      return playercontrollerof.isActing();
    } 
    return false;
  }
  
  public boolean isPlayerUpdate() {
    return this.playerUpdate;
  }
}
