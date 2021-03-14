package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDragonEgg extends Block {
  public BlockDragonEgg() {
    super(Material.dragonEgg, MapColor.blackColor);
    setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
  }
  
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    checkFall(worldIn, pos);
  }
  
  private void checkFall(World worldIn, BlockPos pos) {
    if (BlockFalling.canFallInto(worldIn, pos.down()) && pos.getY() >= 0) {
      int i = 32;
      if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-i, -i, -i), pos.add(i, i, i))) {
        worldIn.spawnEntityInWorld((Entity)new EntityFallingBlock(worldIn, (pos.getX() + 0.5F), pos.getY(), (pos.getZ() + 0.5F), getDefaultState()));
      } else {
        worldIn.setBlockToAir(pos);
        BlockPos blockpos;
        for (blockpos = pos; BlockFalling.canFallInto(worldIn, blockpos) && blockpos.getY() > 0; blockpos = blockpos.down());
        if (blockpos.getY() > 0)
          worldIn.setBlockState(blockpos, getDefaultState(), 2); 
      } 
    } 
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    teleport(worldIn, pos);
    return true;
  }
  
  public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
    teleport(worldIn, pos);
  }
  
  private void teleport(World worldIn, BlockPos pos) {
    IBlockState iblockstate = worldIn.getBlockState(pos);
    if (iblockstate.getBlock() == this)
      for (int i = 0; i < 1000; i++) {
        BlockPos blockpos = pos.add(worldIn.rand.nextInt(16) - worldIn.rand.nextInt(16), worldIn.rand.nextInt(8) - worldIn.rand.nextInt(8), worldIn.rand.nextInt(16) - worldIn.rand.nextInt(16));
        if ((worldIn.getBlockState(blockpos).getBlock()).blockMaterial == Material.air) {
          if (worldIn.isRemote) {
            for (int j = 0; j < 128; j++) {
              double d0 = worldIn.rand.nextDouble();
              float f = (worldIn.rand.nextFloat() - 0.5F) * 0.2F;
              float f1 = (worldIn.rand.nextFloat() - 0.5F) * 0.2F;
              float f2 = (worldIn.rand.nextFloat() - 0.5F) * 0.2F;
              double d1 = blockpos.getX() + (pos.getX() - blockpos.getX()) * d0 + (worldIn.rand.nextDouble() - 0.5D) * 1.0D + 0.5D;
              double d2 = blockpos.getY() + (pos.getY() - blockpos.getY()) * d0 + worldIn.rand.nextDouble() * 1.0D - 0.5D;
              double d3 = blockpos.getZ() + (pos.getZ() - blockpos.getZ()) * d0 + (worldIn.rand.nextDouble() - 0.5D) * 1.0D + 0.5D;
              worldIn.spawnParticle(EnumParticleTypes.PORTAL, d1, d2, d3, f, f1, f2, new int[0]);
            } 
          } else {
            worldIn.setBlockState(blockpos, iblockstate, 2);
            worldIn.setBlockToAir(pos);
          } 
          return;
        } 
      }  
  }
  
  public int tickRate(World worldIn) {
    return 5;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return true;
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return null;
  }
}
