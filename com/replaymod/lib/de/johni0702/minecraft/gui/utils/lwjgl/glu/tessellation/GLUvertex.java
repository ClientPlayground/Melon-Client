package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.glu.tessellation;

class GLUvertex {
  public GLUvertex next;
  
  public GLUvertex prev;
  
  public GLUhalfEdge anEdge;
  
  public Object data;
  
  public double[] coords = new double[3];
  
  public double s;
  
  public double t;
  
  public int pqHandle;
}
