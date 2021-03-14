package net.minecraft.client.renderer.culling;

import net.minecraft.util.AxisAlignedBB;

public interface ICamera {
  boolean isBoundingBoxInFrustum(AxisAlignedBB paramAxisAlignedBB);
  
  void setPosition(double paramDouble1, double paramDouble2, double paramDouble3);
}
