package net.minecraft.world.gen.layer;

public class GenLayerZoom extends GenLayer {
  public GenLayerZoom(long p_i2134_1_, GenLayer p_i2134_3_) {
    super(p_i2134_1_);
    this.parent = p_i2134_3_;
  }
  
  public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
    int i = areaX >> 1;
    int j = areaY >> 1;
    int k = (areaWidth >> 1) + 2;
    int l = (areaHeight >> 1) + 2;
    int[] aint = this.parent.getInts(i, j, k, l);
    int i1 = k - 1 << 1;
    int j1 = l - 1 << 1;
    int[] aint1 = IntCache.getIntCache(i1 * j1);
    for (int k1 = 0; k1 < l - 1; k1++) {
      int l1 = (k1 << 1) * i1;
      int i2 = 0;
      int j2 = aint[i2 + 0 + (k1 + 0) * k];
      for (int k2 = aint[i2 + 0 + (k1 + 1) * k]; i2 < k - 1; i2++) {
        initChunkSeed((i2 + i << 1), (k1 + j << 1));
        int l2 = aint[i2 + 1 + (k1 + 0) * k];
        int i3 = aint[i2 + 1 + (k1 + 1) * k];
        aint1[l1] = j2;
        aint1[l1++ + i1] = selectRandom2(j2, k2);
        aint1[l1] = selectRandom2(j2, l2);
        aint1[l1++ + i1] = selectModeOrRandom(j2, l2, k2, i3);
        j2 = l2;
        k2 = i3;
      } 
    } 
    int[] aint2 = IntCache.getIntCache(areaWidth * areaHeight);
    for (int j3 = 0; j3 < areaHeight; j3++)
      System.arraycopy(aint1, (j3 + (areaY & 0x1)) * i1 + (areaX & 0x1), aint2, j3 * areaWidth, areaWidth); 
    return aint2;
  }
  
  public static GenLayer magnify(long p_75915_0_, GenLayer p_75915_2_, int p_75915_3_) {
    GenLayer genlayer = p_75915_2_;
    for (int i = 0; i < p_75915_3_; i++)
      genlayer = new GenLayerZoom(p_75915_0_ + i, genlayer); 
    return genlayer;
  }
  
  protected int selectRandom2(int p_selectRandom2_1_, int p_selectRandom2_2_) {
    int i = nextInt(2);
    return (i == 0) ? p_selectRandom2_1_ : p_selectRandom2_2_;
  }
}
