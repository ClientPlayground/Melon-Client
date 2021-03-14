package net.minecraft.world;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderDebug;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.FlatGeneratorInfo;

public abstract class WorldProvider {
  public static final float[] moonPhaseFactors = new float[] { 1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F };
  
  protected World worldObj;
  
  private WorldType terrainType;
  
  private String generatorSettings;
  
  protected WorldChunkManager worldChunkMgr;
  
  protected boolean isHellWorld;
  
  protected boolean hasNoSky;
  
  protected final float[] lightBrightnessTable = new float[16];
  
  protected int dimensionId;
  
  private final float[] colorsSunriseSunset = new float[4];
  
  public final void registerWorld(World worldIn) {
    this.worldObj = worldIn;
    this.terrainType = worldIn.getWorldInfo().getTerrainType();
    this.generatorSettings = worldIn.getWorldInfo().getGeneratorOptions();
    registerWorldChunkManager();
    generateLightBrightnessTable();
  }
  
  protected void generateLightBrightnessTable() {
    float f = 0.0F;
    for (int i = 0; i <= 15; i++) {
      float f1 = 1.0F - i / 15.0F;
      this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
    } 
  }
  
  protected void registerWorldChunkManager() {
    WorldType worldtype = this.worldObj.getWorldInfo().getTerrainType();
    if (worldtype == WorldType.FLAT) {
      FlatGeneratorInfo flatgeneratorinfo = FlatGeneratorInfo.createFlatGeneratorFromString(this.worldObj.getWorldInfo().getGeneratorOptions());
      this.worldChunkMgr = (WorldChunkManager)new WorldChunkManagerHell(BiomeGenBase.getBiomeFromBiomeList(flatgeneratorinfo.getBiome(), BiomeGenBase.field_180279_ad), 0.5F);
    } else if (worldtype == WorldType.DEBUG_WORLD) {
      this.worldChunkMgr = (WorldChunkManager)new WorldChunkManagerHell(BiomeGenBase.plains, 0.0F);
    } else {
      this.worldChunkMgr = new WorldChunkManager(this.worldObj);
    } 
  }
  
  public IChunkProvider createChunkGenerator() {
    return (this.terrainType == WorldType.FLAT) ? (IChunkProvider)new ChunkProviderFlat(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings) : ((this.terrainType == WorldType.DEBUG_WORLD) ? (IChunkProvider)new ChunkProviderDebug(this.worldObj) : ((this.terrainType == WorldType.CUSTOMIZED) ? (IChunkProvider)new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings) : (IChunkProvider)new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings)));
  }
  
  public boolean canCoordinateBeSpawn(int x, int z) {
    return (this.worldObj.getGroundAboveSeaLevel(new BlockPos(x, 0, z)) == Blocks.grass);
  }
  
  public float calculateCelestialAngle(long worldTime, float partialTicks) {
    int i = (int)(worldTime % 24000L);
    float f = (i + partialTicks) / 24000.0F - 0.25F;
    if (f < 0.0F)
      f++; 
    if (f > 1.0F)
      f--; 
    f = 1.0F - (float)((Math.cos(f * Math.PI) + 1.0D) / 2.0D);
    f += (f - f) / 3.0F;
    return f;
  }
  
  public int getMoonPhase(long worldTime) {
    return (int)(worldTime / 24000L % 8L + 8L) % 8;
  }
  
  public boolean isSurfaceWorld() {
    return true;
  }
  
  public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
    float f = 0.4F;
    float f1 = MathHelper.cos(celestialAngle * 3.1415927F * 2.0F) - 0.0F;
    float f2 = -0.0F;
    if (f1 >= f2 - f && f1 <= f2 + f) {
      float f3 = (f1 - f2) / f * 0.5F + 0.5F;
      float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * 3.1415927F)) * 0.99F;
      f4 *= f4;
      this.colorsSunriseSunset[0] = f3 * 0.3F + 0.7F;
      this.colorsSunriseSunset[1] = f3 * f3 * 0.7F + 0.2F;
      this.colorsSunriseSunset[2] = f3 * f3 * 0.0F + 0.2F;
      this.colorsSunriseSunset[3] = f4;
      return this.colorsSunriseSunset;
    } 
    return null;
  }
  
  public Vec3 getFogColor(float p_76562_1_, float p_76562_2_) {
    float f = MathHelper.cos(p_76562_1_ * 3.1415927F * 2.0F) * 2.0F + 0.5F;
    f = MathHelper.clamp_float(f, 0.0F, 1.0F);
    float f1 = 0.7529412F;
    float f2 = 0.84705883F;
    float f3 = 1.0F;
    f1 *= f * 0.94F + 0.06F;
    f2 *= f * 0.94F + 0.06F;
    f3 *= f * 0.91F + 0.09F;
    return new Vec3(f1, f2, f3);
  }
  
  public boolean canRespawnHere() {
    return true;
  }
  
  public static WorldProvider getProviderForDimension(int dimension) {
    return (dimension == -1) ? new WorldProviderHell() : ((dimension == 0) ? new WorldProviderSurface() : ((dimension == 1) ? new WorldProviderEnd() : null));
  }
  
  public float getCloudHeight() {
    return 128.0F;
  }
  
  public boolean isSkyColored() {
    return true;
  }
  
  public BlockPos getSpawnCoordinate() {
    return null;
  }
  
  public int getAverageGroundLevel() {
    return (this.terrainType == WorldType.FLAT) ? 4 : (this.worldObj.getSeaLevel() + 1);
  }
  
  public double getVoidFogYFactor() {
    return (this.terrainType == WorldType.FLAT) ? 1.0D : 0.03125D;
  }
  
  public boolean doesXZShowFog(int x, int z) {
    return false;
  }
  
  public abstract String getDimensionName();
  
  public abstract String getInternalNameSuffix();
  
  public WorldChunkManager getWorldChunkManager() {
    return this.worldChunkMgr;
  }
  
  public boolean doesWaterVaporize() {
    return this.isHellWorld;
  }
  
  public boolean getHasNoSky() {
    return this.hasNoSky;
  }
  
  public float[] getLightBrightnessTable() {
    return this.lightBrightnessTable;
  }
  
  public int getDimensionId() {
    return this.dimensionId;
  }
  
  public WorldBorder getWorldBorder() {
    return new WorldBorder();
  }
}
