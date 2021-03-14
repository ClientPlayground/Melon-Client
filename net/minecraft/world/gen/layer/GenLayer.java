package net.minecraft.world.gen.layer;

import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.ChunkProviderSettings;

public abstract class GenLayer {
  private long worldGenSeed;
  
  protected GenLayer parent;
  
  private long chunkSeed;
  
  protected long baseSeed;
  
  public static GenLayer[] initializeAllBiomeGenerators(long seed, WorldType p_180781_2_, String p_180781_3_) {
    GenLayer genlayer = new GenLayerIsland(1L);
    genlayer = new GenLayerFuzzyZoom(2000L, genlayer);
    GenLayerAddIsland genlayeraddisland = new GenLayerAddIsland(1L, genlayer);
    GenLayerZoom genlayerzoom = new GenLayerZoom(2001L, genlayeraddisland);
    GenLayerAddIsland genlayeraddisland1 = new GenLayerAddIsland(2L, genlayerzoom);
    genlayeraddisland1 = new GenLayerAddIsland(50L, genlayeraddisland1);
    genlayeraddisland1 = new GenLayerAddIsland(70L, genlayeraddisland1);
    GenLayerRemoveTooMuchOcean genlayerremovetoomuchocean = new GenLayerRemoveTooMuchOcean(2L, genlayeraddisland1);
    GenLayerAddSnow genlayeraddsnow = new GenLayerAddSnow(2L, genlayerremovetoomuchocean);
    GenLayerAddIsland genlayeraddisland2 = new GenLayerAddIsland(3L, genlayeraddsnow);
    GenLayerEdge genlayeredge = new GenLayerEdge(2L, genlayeraddisland2, GenLayerEdge.Mode.COOL_WARM);
    genlayeredge = new GenLayerEdge(2L, genlayeredge, GenLayerEdge.Mode.HEAT_ICE);
    genlayeredge = new GenLayerEdge(3L, genlayeredge, GenLayerEdge.Mode.SPECIAL);
    GenLayerZoom genlayerzoom1 = new GenLayerZoom(2002L, genlayeredge);
    genlayerzoom1 = new GenLayerZoom(2003L, genlayerzoom1);
    GenLayerAddIsland genlayeraddisland3 = new GenLayerAddIsland(4L, genlayerzoom1);
    GenLayerAddMushroomIsland genlayeraddmushroomisland = new GenLayerAddMushroomIsland(5L, genlayeraddisland3);
    GenLayerDeepOcean genlayerdeepocean = new GenLayerDeepOcean(4L, genlayeraddmushroomisland);
    GenLayer genlayer4 = GenLayerZoom.magnify(1000L, genlayerdeepocean, 0);
    ChunkProviderSettings chunkprovidersettings = null;
    int i = 4;
    int j = i;
    if (p_180781_2_ == WorldType.CUSTOMIZED && p_180781_3_.length() > 0) {
      chunkprovidersettings = ChunkProviderSettings.Factory.jsonToFactory(p_180781_3_).func_177864_b();
      i = chunkprovidersettings.biomeSize;
      j = chunkprovidersettings.riverSize;
    } 
    if (p_180781_2_ == WorldType.LARGE_BIOMES)
      i = 6; 
    GenLayer lvt_8_1_ = GenLayerZoom.magnify(1000L, genlayer4, 0);
    GenLayerRiverInit genlayerriverinit = new GenLayerRiverInit(100L, lvt_8_1_);
    GenLayerBiome lvt_9_1_ = new GenLayerBiome(200L, genlayer4, p_180781_2_, p_180781_3_);
    GenLayer genlayer6 = GenLayerZoom.magnify(1000L, lvt_9_1_, 2);
    GenLayerBiomeEdge genlayerbiomeedge = new GenLayerBiomeEdge(1000L, genlayer6);
    GenLayer lvt_10_1_ = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
    GenLayer genlayerhills = new GenLayerHills(1000L, genlayerbiomeedge, lvt_10_1_);
    GenLayer genlayer5 = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
    genlayer5 = GenLayerZoom.magnify(1000L, genlayer5, j);
    GenLayerRiver genlayerriver = new GenLayerRiver(1L, genlayer5);
    GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerriver);
    genlayerhills = new GenLayerRareBiome(1001L, genlayerhills);
    for (int k = 0; k < i; k++) {
      genlayerhills = new GenLayerZoom((1000 + k), genlayerhills);
      if (k == 0)
        genlayerhills = new GenLayerAddIsland(3L, genlayerhills); 
      if (k == 1 || i == 1)
        genlayerhills = new GenLayerShore(1000L, genlayerhills); 
    } 
    GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerhills);
    GenLayerRiverMix genlayerrivermix = new GenLayerRiverMix(100L, genlayersmooth1, genlayersmooth);
    GenLayer genlayer3 = new GenLayerVoronoiZoom(10L, genlayerrivermix);
    genlayerrivermix.initWorldGenSeed(seed);
    genlayer3.initWorldGenSeed(seed);
    return new GenLayer[] { genlayerrivermix, genlayer3, genlayerrivermix };
  }
  
  public GenLayer(long p_i2125_1_) {
    this.baseSeed = p_i2125_1_;
    this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
    this.baseSeed += p_i2125_1_;
    this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
    this.baseSeed += p_i2125_1_;
    this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
    this.baseSeed += p_i2125_1_;
  }
  
  public void initWorldGenSeed(long seed) {
    this.worldGenSeed = seed;
    if (this.parent != null)
      this.parent.initWorldGenSeed(seed); 
    this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
    this.worldGenSeed += this.baseSeed;
    this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
    this.worldGenSeed += this.baseSeed;
    this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
    this.worldGenSeed += this.baseSeed;
  }
  
  public void initChunkSeed(long p_75903_1_, long p_75903_3_) {
    this.chunkSeed = this.worldGenSeed;
    this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
    this.chunkSeed += p_75903_1_;
    this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
    this.chunkSeed += p_75903_3_;
    this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
    this.chunkSeed += p_75903_1_;
    this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
    this.chunkSeed += p_75903_3_;
  }
  
  protected int nextInt(int p_75902_1_) {
    int i = (int)((this.chunkSeed >> 24L) % p_75902_1_);
    if (i < 0)
      i += p_75902_1_; 
    this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
    this.chunkSeed += this.worldGenSeed;
    return i;
  }
  
  public abstract int[] getInts(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  protected static boolean biomesEqualOrMesaPlateau(int biomeIDA, int biomeIDB) {
    if (biomeIDA == biomeIDB)
      return true; 
    if (biomeIDA != BiomeGenBase.mesaPlateau_F.biomeID && biomeIDA != BiomeGenBase.mesaPlateau.biomeID) {
      final BiomeGenBase biomegenbase = BiomeGenBase.getBiome(biomeIDA);
      final BiomeGenBase biomegenbase1 = BiomeGenBase.getBiome(biomeIDB);
      try {
        return (biomegenbase != null && biomegenbase1 != null) ? biomegenbase.isEqualTo(biomegenbase1) : false;
      } catch (Throwable throwable) {
        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Comparing biomes");
        CrashReportCategory crashreportcategory = crashreport.makeCategory("Biomes being compared");
        crashreportcategory.addCrashSection("Biome A ID", Integer.valueOf(biomeIDA));
        crashreportcategory.addCrashSection("Biome B ID", Integer.valueOf(biomeIDB));
        crashreportcategory.addCrashSectionCallable("Biome A", new Callable<String>() {
              public String call() throws Exception {
                return String.valueOf(biomegenbase);
              }
            });
        crashreportcategory.addCrashSectionCallable("Biome B", new Callable<String>() {
              public String call() throws Exception {
                return String.valueOf(biomegenbase1);
              }
            });
        throw new ReportedException(crashreport);
      } 
    } 
    return (biomeIDB == BiomeGenBase.mesaPlateau_F.biomeID || biomeIDB == BiomeGenBase.mesaPlateau.biomeID);
  }
  
  protected static boolean isBiomeOceanic(int p_151618_0_) {
    return (p_151618_0_ == BiomeGenBase.ocean.biomeID || p_151618_0_ == BiomeGenBase.deepOcean.biomeID || p_151618_0_ == BiomeGenBase.frozenOcean.biomeID);
  }
  
  protected int selectRandom(int... p_151619_1_) {
    return p_151619_1_[nextInt(p_151619_1_.length)];
  }
  
  protected int selectModeOrRandom(int p_151617_1_, int p_151617_2_, int p_151617_3_, int p_151617_4_) {
    return (p_151617_2_ == p_151617_3_ && p_151617_3_ == p_151617_4_) ? p_151617_2_ : ((p_151617_1_ == p_151617_2_ && p_151617_1_ == p_151617_3_) ? p_151617_1_ : ((p_151617_1_ == p_151617_2_ && p_151617_1_ == p_151617_4_) ? p_151617_1_ : ((p_151617_1_ == p_151617_3_ && p_151617_1_ == p_151617_4_) ? p_151617_1_ : ((p_151617_1_ == p_151617_2_ && p_151617_3_ != p_151617_4_) ? p_151617_1_ : ((p_151617_1_ == p_151617_3_ && p_151617_2_ != p_151617_4_) ? p_151617_1_ : ((p_151617_1_ == p_151617_4_ && p_151617_2_ != p_151617_3_) ? p_151617_1_ : ((p_151617_2_ == p_151617_3_ && p_151617_1_ != p_151617_4_) ? p_151617_2_ : ((p_151617_2_ == p_151617_4_ && p_151617_1_ != p_151617_3_) ? p_151617_2_ : ((p_151617_3_ == p_151617_4_ && p_151617_1_ != p_151617_2_) ? p_151617_3_ : selectRandom(new int[] { p_151617_1_, p_151617_2_, p_151617_3_, p_151617_4_ }))))))))));
  }
}
