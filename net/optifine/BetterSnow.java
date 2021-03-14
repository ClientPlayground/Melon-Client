package net.optifine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class BetterSnow {
  private static IBakedModel modelSnowLayer = null;
  
  public static void update() {
    modelSnowLayer = Config.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(Blocks.snow_layer.getDefaultState());
  }
  
  public static IBakedModel getModelSnowLayer() {
    return modelSnowLayer;
  }
  
  public static IBlockState getStateSnowLayer() {
    return Blocks.snow_layer.getDefaultState();
  }
  
  public static boolean shouldRender(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos) {
    Block block = blockState.getBlock();
    return !checkBlock(block, blockState) ? false : hasSnowNeighbours(blockAccess, blockPos);
  }
  
  private static boolean hasSnowNeighbours(IBlockAccess blockAccess, BlockPos pos) {
    Block block = Blocks.snow_layer;
    return (blockAccess.getBlockState(pos.north()).getBlock() != block && blockAccess.getBlockState(pos.south()).getBlock() != block && blockAccess.getBlockState(pos.west()).getBlock() != block && blockAccess.getBlockState(pos.east()).getBlock() != block) ? false : blockAccess.getBlockState(pos.down()).getBlock().isOpaqueCube();
  }
  
  private static boolean checkBlock(Block block, IBlockState blockState) {
    if (block.isFullCube())
      return false; 
    if (block.isOpaqueCube())
      return false; 
    if (block instanceof net.minecraft.block.BlockSnow)
      return false; 
    if (!(block instanceof net.minecraft.block.BlockBush) || (!(block instanceof net.minecraft.block.BlockDoublePlant) && !(block instanceof net.minecraft.block.BlockFlower) && !(block instanceof net.minecraft.block.BlockMushroom) && !(block instanceof net.minecraft.block.BlockSapling) && !(block instanceof net.minecraft.block.BlockTallGrass))) {
      if (!(block instanceof net.minecraft.block.BlockFence) && !(block instanceof net.minecraft.block.BlockFenceGate) && !(block instanceof net.minecraft.block.BlockFlowerPot) && !(block instanceof net.minecraft.block.BlockPane) && !(block instanceof net.minecraft.block.BlockReed) && !(block instanceof net.minecraft.block.BlockWall)) {
        if (block instanceof net.minecraft.block.BlockRedstoneTorch && blockState.getValue((IProperty)BlockTorch.FACING) == EnumFacing.UP)
          return true; 
        if (block instanceof BlockLever) {
          Object object = blockState.getValue((IProperty)BlockLever.FACING);
          if (object == BlockLever.EnumOrientation.UP_X || object == BlockLever.EnumOrientation.UP_Z)
            return true; 
        } 
        return false;
      } 
      return true;
    } 
    return true;
  }
}
