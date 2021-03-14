package net.minecraft.world;

import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderHell;

public class WorldProviderHell extends WorldProvider {
  public void registerWorldChunkManager() {
    this.worldChunkMgr = (WorldChunkManager)new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F);
    this.isHellWorld = true;
    this.hasNoSky = true;
    this.dimensionId = -1;
  }
  
  public Vec3 getFogColor(float p_76562_1_, float p_76562_2_) {
    return new Vec3(0.20000000298023224D, 0.029999999329447746D, 0.029999999329447746D);
  }
  
  protected void generateLightBrightnessTable() {
    float f = 0.1F;
    for (int i = 0; i <= 15; i++) {
      float f1 = 1.0F - i / 15.0F;
      this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
    } 
  }
  
  public IChunkProvider createChunkGenerator() {
    return (IChunkProvider)new ChunkProviderHell(this.worldObj, this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.worldObj.getSeed());
  }
  
  public boolean isSurfaceWorld() {
    return false;
  }
  
  public boolean canCoordinateBeSpawn(int x, int z) {
    return false;
  }
  
  public float calculateCelestialAngle(long worldTime, float partialTicks) {
    return 0.5F;
  }
  
  public boolean canRespawnHere() {
    return false;
  }
  
  public boolean doesXZShowFog(int x, int z) {
    return true;
  }
  
  public String getDimensionName() {
    return "Nether";
  }
  
  public String getInternalNameSuffix() {
    return "_nether";
  }
  
  public WorldBorder getWorldBorder() {
    return new WorldBorder() {
        public double getCenterX() {
          return super.getCenterX() / 8.0D;
        }
        
        public double getCenterZ() {
          return super.getCenterZ() / 8.0D;
        }
      };
  }
}
