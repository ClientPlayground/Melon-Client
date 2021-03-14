package com.replaymod.lib.de.johni0702.minecraft.gui;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;

public class RenderInfo {
  public final float partialTick;
  
  public final int mouseX;
  
  public final int mouseY;
  
  public final int layer;
  
  public RenderInfo(float partialTick, int mouseX, int mouseY, int layer) {
    this.partialTick = partialTick;
    this.mouseX = mouseX;
    this.mouseY = mouseY;
    this.layer = layer;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof RenderInfo))
      return false; 
    RenderInfo other = (RenderInfo)o;
    return !other.canEqual(this) ? false : ((Float.compare(getPartialTick(), other.getPartialTick()) != 0) ? false : ((getMouseX() != other.getMouseX()) ? false : ((getMouseY() != other.getMouseY()) ? false : (!(getLayer() != other.getLayer())))));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof RenderInfo;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + Float.floatToIntBits(getPartialTick());
    result = result * 59 + getMouseX();
    result = result * 59 + getMouseY();
    return result * 59 + getLayer();
  }
  
  public String toString() {
    return "RenderInfo(partialTick=" + getPartialTick() + ", mouseX=" + getMouseX() + ", mouseY=" + getMouseY() + ", layer=" + getLayer() + ")";
  }
  
  public float getPartialTick() {
    return this.partialTick;
  }
  
  public int getMouseX() {
    return this.mouseX;
  }
  
  public int getMouseY() {
    return this.mouseY;
  }
  
  public int getLayer() {
    return this.layer;
  }
  
  public RenderInfo offsetMouse(int minusX, int minusY) {
    return new RenderInfo(this.partialTick, this.mouseX - minusX, this.mouseY - minusY, this.layer);
  }
  
  public RenderInfo layer(int layer) {
    return (this.layer == layer) ? this : new RenderInfo(this.partialTick, this.mouseX, this.mouseY, layer);
  }
  
  public void addTo(CrashReport crashReport) {
    CrashReportCategory category = crashReport.func_85058_a("Render info details");
    MCVer.addDetail(category, "Partial Tick", () -> "" + this.partialTick);
    MCVer.addDetail(category, "Mouse X", () -> "" + this.mouseX);
    MCVer.addDetail(category, "Mouse Y", () -> "" + this.mouseY);
    MCVer.addDetail(category, "Layer", () -> "" + this.layer);
  }
}
