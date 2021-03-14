package net.minecraft.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEndPortal extends BlockContainer {
  protected BlockEndPortal(Material materialIn) {
    super(materialIn);
    setLightLevel(1.0F);
  }
  
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return (TileEntity)new TileEntityEndPortal();
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    float f = 0.0625F;
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
  }
  
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return (side == EnumFacing.DOWN) ? super.shouldSideBeRendered(worldIn, pos, side) : false;
  }
  
  public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {}
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public int quantityDropped(Random random) {
    return 0;
  }
  
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    if (entityIn.ridingEntity == null && entityIn.riddenByEntity == null && !worldIn.isRemote)
      entityIn.travelToDimension(1); 
  }
  
  public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    double d0 = (pos.getX() + rand.nextFloat());
    double d1 = (pos.getY() + 0.8F);
    double d2 = (pos.getZ() + rand.nextFloat());
    double d3 = 0.0D;
    double d4 = 0.0D;
    double d5 = 0.0D;
    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return null;
  }
  
  public MapColor getMapColor(IBlockState state) {
    return MapColor.blackColor;
  }
}
