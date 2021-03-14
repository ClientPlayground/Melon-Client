package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector;

import java.nio.FloatBuffer;

public interface ReadableVector {
  float length();
  
  float lengthSquared();
  
  Vector store(FloatBuffer paramFloatBuffer);
}
