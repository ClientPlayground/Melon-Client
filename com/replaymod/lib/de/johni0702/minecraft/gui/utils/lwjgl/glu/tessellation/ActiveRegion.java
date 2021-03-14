package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.glu.tessellation;

class ActiveRegion {
  GLUhalfEdge eUp;
  
  DictNode nodeUp;
  
  int windingNumber;
  
  boolean inside;
  
  boolean sentinel;
  
  boolean dirty;
  
  boolean fixUpperEdge;
}
