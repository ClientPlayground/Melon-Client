package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.glu;

public interface GLUtessellatorCallback {
  void begin(int paramInt);
  
  void beginData(int paramInt, Object paramObject);
  
  void edgeFlag(boolean paramBoolean);
  
  void edgeFlagData(boolean paramBoolean, Object paramObject);
  
  void vertex(Object paramObject);
  
  void vertexData(Object paramObject1, Object paramObject2);
  
  void end();
  
  void endData(Object paramObject);
  
  void combine(double[] paramArrayOfdouble, Object[] paramArrayOfObject1, float[] paramArrayOffloat, Object[] paramArrayOfObject2);
  
  void combineData(double[] paramArrayOfdouble, Object[] paramArrayOfObject1, float[] paramArrayOffloat, Object[] paramArrayOfObject2, Object paramObject);
  
  void error(int paramInt);
  
  void errorData(int paramInt, Object paramObject);
}
