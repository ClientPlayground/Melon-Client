package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.glu.tessellation;

class GLUface {
  public GLUface next;
  
  public GLUface prev;
  
  public GLUhalfEdge anEdge;
  
  public Object data;
  
  public GLUface trail;
  
  public boolean marked;
  
  public boolean inside;
}
