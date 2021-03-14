package net.optifine;

import net.minecraft.world.World;

public class LightMapPack {
  private LightMap lightMap;
  
  private LightMap lightMapRain;
  
  private LightMap lightMapThunder;
  
  private int[] colorBuffer1 = new int[0];
  
  private int[] colorBuffer2 = new int[0];
  
  public LightMapPack(LightMap lightMap, LightMap lightMapRain, LightMap lightMapThunder) {
    if (lightMapRain != null || lightMapThunder != null) {
      if (lightMapRain == null)
        lightMapRain = lightMap; 
      if (lightMapThunder == null)
        lightMapThunder = lightMapRain; 
    } 
    this.lightMap = lightMap;
    this.lightMapRain = lightMapRain;
    this.lightMapThunder = lightMapThunder;
  }
  
  public boolean updateLightmap(World world, float torchFlickerX, int[] lmColors, boolean nightvision, float partialTicks) {
    if (this.lightMapRain == null && this.lightMapThunder == null)
      return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision); 
    int i = world.provider.getDimensionId();
    if (i != 1 && i != -1) {
      float f = world.getRainStrength(partialTicks);
      float f1 = world.getThunderStrength(partialTicks);
      float f2 = 1.0E-4F;
      boolean flag = (f > f2);
      boolean flag1 = (f1 > f2);
      if (!flag && !flag1)
        return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision); 
      if (f > 0.0F)
        f1 /= f; 
      float f3 = 1.0F - f;
      float f4 = f - f1;
      if (this.colorBuffer1.length != lmColors.length) {
        this.colorBuffer1 = new int[lmColors.length];
        this.colorBuffer2 = new int[lmColors.length];
      } 
      int j = 0;
      int[][] aint = { lmColors, this.colorBuffer1, this.colorBuffer2 };
      float[] afloat = new float[3];
      if (f3 > f2 && this.lightMap.updateLightmap(world, torchFlickerX, aint[j], nightvision)) {
        afloat[j] = f3;
        j++;
      } 
      if (f4 > f2 && this.lightMapRain != null && this.lightMapRain.updateLightmap(world, torchFlickerX, aint[j], nightvision)) {
        afloat[j] = f4;
        j++;
      } 
      if (f1 > f2 && this.lightMapThunder != null && this.lightMapThunder.updateLightmap(world, torchFlickerX, aint[j], nightvision)) {
        afloat[j] = f1;
        j++;
      } 
      return (j == 2) ? blend(aint[0], afloat[0], aint[1], afloat[1]) : ((j == 3) ? blend(aint[0], afloat[0], aint[1], afloat[1], aint[2], afloat[2]) : true);
    } 
    return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision);
  }
  
  private boolean blend(int[] cols0, float br0, int[] cols1, float br1) {
    if (cols1.length != cols0.length)
      return false; 
    for (int i = 0; i < cols0.length; i++) {
      int j = cols0[i];
      int k = j >> 16 & 0xFF;
      int l = j >> 8 & 0xFF;
      int i1 = j & 0xFF;
      int j1 = cols1[i];
      int k1 = j1 >> 16 & 0xFF;
      int l1 = j1 >> 8 & 0xFF;
      int i2 = j1 & 0xFF;
      int j2 = (int)(k * br0 + k1 * br1);
      int k2 = (int)(l * br0 + l1 * br1);
      int l2 = (int)(i1 * br0 + i2 * br1);
      cols0[i] = 0xFF000000 | j2 << 16 | k2 << 8 | l2;
    } 
    return true;
  }
  
  private boolean blend(int[] cols0, float br0, int[] cols1, float br1, int[] cols2, float br2) {
    if (cols1.length == cols0.length && cols2.length == cols0.length) {
      for (int i = 0; i < cols0.length; i++) {
        int j = cols0[i];
        int k = j >> 16 & 0xFF;
        int l = j >> 8 & 0xFF;
        int i1 = j & 0xFF;
        int j1 = cols1[i];
        int k1 = j1 >> 16 & 0xFF;
        int l1 = j1 >> 8 & 0xFF;
        int i2 = j1 & 0xFF;
        int j2 = cols2[i];
        int k2 = j2 >> 16 & 0xFF;
        int l2 = j2 >> 8 & 0xFF;
        int i3 = j2 & 0xFF;
        int j3 = (int)(k * br0 + k1 * br1 + k2 * br2);
        int k3 = (int)(l * br0 + l1 * br1 + l2 * br2);
        int l3 = (int)(i1 * br0 + i2 * br1 + i3 * br2);
        cols0[i] = 0xFF000000 | j3 << 16 | k3 << 8 | l3;
      } 
      return true;
    } 
    return false;
  }
}
