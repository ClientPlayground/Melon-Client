package net.minecraft.dispenser;

import net.minecraft.util.BlockPos;

public interface IBlockSource extends ILocatableSource {
  double getX();
  
  double getY();
  
  double getZ();
  
  BlockPos getBlockPos();
  
  int getBlockMetadata();
  
  <T extends net.minecraft.tileentity.TileEntity> T getBlockTileEntity();
}
