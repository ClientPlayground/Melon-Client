package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.glu.tessellation;

class GLUhalfEdge {
  public GLUhalfEdge next;
  
  public GLUhalfEdge Sym;
  
  public GLUhalfEdge Onext;
  
  public GLUhalfEdge Lnext;
  
  public GLUvertex Org;
  
  public GLUface Lface;
  
  public ActiveRegion activeRegion;
  
  public int winding;
  
  public boolean first;
  
  GLUhalfEdge(boolean first) {
    this.first = first;
  }
}
