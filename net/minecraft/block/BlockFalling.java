package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockFalling extends Block {
  public static boolean fallInstantly;
  
  public BlockFalling() {
    super(Material.sand);
    setCreativeTab(CreativeTabs.tabBlock);
  }
  
  public BlockFalling(Material materialIn) {
    super(materialIn);
  }
  
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (!worldIn.isRemote)
      checkFallable(worldIn, pos); 
  }
  
  private void checkFallable(World worldIn, BlockPos pos) {
    if (canFallInto(worldIn, pos.down()) && pos.getY() >= 0) {
      int i = 32;
      if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-i, -i, -i), pos.add(i, i, i))) {
        if (!worldIn.isRemote) {
          EntityFallingBlock entityfallingblock = new EntityFallingBlock(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, worldIn.getBlockState(pos));
          onStartFalling(entityfallingblock);
          worldIn.spawnEntityInWorld((Entity)entityfallingblock);
        } 
      } else {
        worldIn.setBlockToAir(pos);
        BlockPos blockpos;
        for (blockpos = pos.down(); canFallInto(worldIn, blockpos) && blockpos.getY() > 0; blockpos = blockpos.down());
        if (blockpos.getY() > 0)
          worldIn.setBlockState(blockpos.up(), getDefaultState()); 
      } 
    } 
  }
  
  protected void onStartFalling(EntityFallingBlock fallingEntity) {}
  
  public int tickRate(World worldIn) {
    return 2;
  }
  
  public static boolean canFallInto(World worldIn, BlockPos pos) {
    Block block = worldIn.getBlockState(pos).getBlock();
    Material material = block.blockMaterial;
    return (block == Blocks.fire || material == Material.air || material == Material.water || material == Material.lava);
  }
  
  public void onEndFalling(World worldIn, BlockPos pos) {}
}
