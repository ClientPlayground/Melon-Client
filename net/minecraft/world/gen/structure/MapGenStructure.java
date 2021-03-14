package net.minecraft.world.gen.structure;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public abstract class MapGenStructure extends MapGenBase {
  private MapGenStructureData structureData;
  
  protected Map<Long, StructureStart> structureMap = Maps.newHashMap();
  
  public abstract String getStructureName();
  
  protected final void recursiveGenerate(World worldIn, final int chunkX, final int chunkZ, int p_180701_4_, int p_180701_5_, ChunkPrimer chunkPrimerIn) {
    initializeStructureData(worldIn);
    if (!this.structureMap.containsKey(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)))) {
      this.rand.nextInt();
      try {
        if (canSpawnStructureAtCoords(chunkX, chunkZ)) {
          StructureStart structurestart = getStructureStart(chunkX, chunkZ);
          this.structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)), structurestart);
          setStructureStart(chunkX, chunkZ, structurestart);
        } 
      } catch (Throwable throwable) {
        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception preparing structure feature");
        CrashReportCategory crashreportcategory = crashreport.makeCategory("Feature being prepared");
        crashreportcategory.addCrashSectionCallable("Is feature chunk", new Callable<String>() {
              public String call() throws Exception {
                return MapGenStructure.this.canSpawnStructureAtCoords(chunkX, chunkZ) ? "True" : "False";
              }
            });
        crashreportcategory.addCrashSection("Chunk location", String.format("%d,%d", new Object[] { Integer.valueOf(chunkX), Integer.valueOf(chunkZ) }));
        crashreportcategory.addCrashSectionCallable("Chunk pos hash", new Callable<String>() {
              public String call() throws Exception {
                return String.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
              }
            });
        crashreportcategory.addCrashSectionCallable("Structure type", new Callable<String>() {
              public String call() throws Exception {
                return MapGenStructure.this.getClass().getCanonicalName();
              }
            });
        throw new ReportedException(crashreport);
      } 
    } 
  }
  
  public boolean generateStructure(World worldIn, Random randomIn, ChunkCoordIntPair chunkCoord) {
    initializeStructureData(worldIn);
    int i = (chunkCoord.chunkXPos << 4) + 8;
    int j = (chunkCoord.chunkZPos << 4) + 8;
    boolean flag = false;
    for (StructureStart structurestart : this.structureMap.values()) {
      if (structurestart.isSizeableStructure() && structurestart.func_175788_a(chunkCoord) && structurestart.getBoundingBox().intersectsWith(i, j, i + 15, j + 15)) {
        structurestart.generateStructure(worldIn, randomIn, new StructureBoundingBox(i, j, i + 15, j + 15));
        structurestart.func_175787_b(chunkCoord);
        flag = true;
        setStructureStart(structurestart.getChunkPosX(), structurestart.getChunkPosZ(), structurestart);
      } 
    } 
    return flag;
  }
  
  public boolean func_175795_b(BlockPos pos) {
    initializeStructureData(this.worldObj);
    return (func_175797_c(pos) != null);
  }
  
  protected StructureStart func_175797_c(BlockPos pos) {
    for (StructureStart structurestart : this.structureMap.values()) {
      if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside((Vec3i)pos)) {
        Iterator<StructureComponent> iterator = structurestart.getComponents().iterator();
        while (iterator.hasNext()) {
          StructureComponent structurecomponent = iterator.next();
          if (structurecomponent.getBoundingBox().isVecInside((Vec3i)pos))
            return structurestart; 
        } 
      } 
    } 
    return null;
  }
  
  public boolean isPositionInStructure(World worldIn, BlockPos pos) {
    initializeStructureData(worldIn);
    for (StructureStart structurestart : this.structureMap.values()) {
      if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside((Vec3i)pos))
        return true; 
    } 
    return false;
  }
  
  public BlockPos getClosestStrongholdPos(World worldIn, BlockPos pos) {
    this.worldObj = worldIn;
    initializeStructureData(worldIn);
    this.rand.setSeed(worldIn.getSeed());
    long i = this.rand.nextLong();
    long j = this.rand.nextLong();
    long k = (pos.getX() >> 4) * i;
    long l = (pos.getZ() >> 4) * j;
    this.rand.setSeed(k ^ l ^ worldIn.getSeed());
    recursiveGenerate(worldIn, pos.getX() >> 4, pos.getZ() >> 4, 0, 0, (ChunkPrimer)null);
    double d0 = Double.MAX_VALUE;
    BlockPos blockpos = null;
    for (StructureStart structurestart : this.structureMap.values()) {
      if (structurestart.isSizeableStructure()) {
        StructureComponent structurecomponent = structurestart.getComponents().get(0);
        BlockPos blockpos1 = structurecomponent.getBoundingBoxCenter();
        double d1 = blockpos1.distanceSq((Vec3i)pos);
        if (d1 < d0) {
          d0 = d1;
          blockpos = blockpos1;
        } 
      } 
    } 
    if (blockpos != null)
      return blockpos; 
    List<BlockPos> list = getCoordList();
    if (list != null) {
      BlockPos blockpos2 = null;
      for (BlockPos blockpos3 : list) {
        double d2 = blockpos3.distanceSq((Vec3i)pos);
        if (d2 < d0) {
          d0 = d2;
          blockpos2 = blockpos3;
        } 
      } 
      return blockpos2;
    } 
    return null;
  }
  
  protected List<BlockPos> getCoordList() {
    return null;
  }
  
  private void initializeStructureData(World worldIn) {
    if (this.structureData == null) {
      this.structureData = (MapGenStructureData)worldIn.loadItemData(MapGenStructureData.class, getStructureName());
      if (this.structureData == null) {
        this.structureData = new MapGenStructureData(getStructureName());
        worldIn.setItemData(getStructureName(), this.structureData);
      } else {
        NBTTagCompound nbttagcompound = this.structureData.getTagCompound();
        for (String s : nbttagcompound.getKeySet()) {
          NBTBase nbtbase = nbttagcompound.getTag(s);
          if (nbtbase.getId() == 10) {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbtbase;
            if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ")) {
              int i = nbttagcompound1.getInteger("ChunkX");
              int j = nbttagcompound1.getInteger("ChunkZ");
              StructureStart structurestart = MapGenStructureIO.getStructureStart(nbttagcompound1, worldIn);
              if (structurestart != null)
                this.structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, j)), structurestart); 
            } 
          } 
        } 
      } 
    } 
  }
  
  private void setStructureStart(int chunkX, int chunkZ, StructureStart start) {
    this.structureData.writeInstance(start.writeStructureComponentsToNBT(chunkX, chunkZ), chunkX, chunkZ);
    this.structureData.markDirty();
  }
  
  protected abstract boolean canSpawnStructureAtCoords(int paramInt1, int paramInt2);
  
  protected abstract StructureStart getStructureStart(int paramInt1, int paramInt2);
}
