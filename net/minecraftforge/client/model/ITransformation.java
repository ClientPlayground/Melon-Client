package net.minecraftforge.client.model;

import javax.vecmath.Matrix4f;
import net.minecraft.util.EnumFacing;

public interface ITransformation {
  Matrix4f getMatrix();
  
  EnumFacing rotate(EnumFacing paramEnumFacing);
  
  int rotate(EnumFacing paramEnumFacing, int paramInt);
}
