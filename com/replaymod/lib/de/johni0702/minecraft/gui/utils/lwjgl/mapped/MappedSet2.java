package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.mapped;

public class MappedSet2 {
  private final MappedObject a;
  
  private final MappedObject b;
  
  public int view;
  
  MappedSet2(MappedObject a, MappedObject b) {
    this.a = a;
    this.b = b;
  }
  
  void view(int view) {
    this.a.setViewAddress(this.a.getViewAddress(view));
    this.b.setViewAddress(this.b.getViewAddress(view));
  }
  
  public void next() {
    this.a.next();
    this.b.next();
  }
}
