package net.optifine.util;

import net.minecraft.util.MathHelper;

public class MathUtils {
  public static final float PI = 3.1415927F;
  
  public static final float PI2 = 6.2831855F;
  
  public static final float PId2 = 1.5707964F;
  
  private static final float[] ASIN_TABLE = new float[65536];
  
  public static float asin(float value) {
    return ASIN_TABLE[(int)((value + 1.0F) * 32767.5D) & 0xFFFF];
  }
  
  public static float acos(float value) {
    return 1.5707964F - ASIN_TABLE[(int)((value + 1.0F) * 32767.5D) & 0xFFFF];
  }
  
  public static int getAverage(int[] vals) {
    if (vals.length <= 0)
      return 0; 
    int i = getSum(vals);
    int j = i / vals.length;
    return j;
  }
  
  public static int getSum(int[] vals) {
    if (vals.length <= 0)
      return 0; 
    int i = 0;
    for (int j = 0; j < vals.length; j++) {
      int k = vals[j];
      i += k;
    } 
    return i;
  }
  
  public static int roundDownToPowerOfTwo(int val) {
    int i = MathHelper.roundUpToPowerOfTwo(val);
    return (val == i) ? i : (i / 2);
  }
  
  public static boolean equalsDelta(float f1, float f2, float delta) {
    return (Math.abs(f1 - f2) <= delta);
  }
  
  public static float toDeg(float angle) {
    return angle * 180.0F / MathHelper.PI;
  }
  
  public static float toRad(float angle) {
    return angle / 180.0F * MathHelper.PI;
  }
  
  public static float roundToFloat(double d) {
    return (float)(Math.round(d * 1.0E8D) / 1.0E8D);
  }
  
  static {
    for (int i = 0; i < 65536; i++)
      ASIN_TABLE[i] = (float)Math.asin(i / 32767.5D - 1.0D); 
    for (int j = -1; j < 2; j++)
      ASIN_TABLE[(int)((j + 1.0D) * 32767.5D) & 0xFFFF] = (float)Math.asin(j); 
  }
}
