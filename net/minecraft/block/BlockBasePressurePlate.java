package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockBasePressurePlate extends Block {
  protected BlockBasePressurePlate(Material materialIn) {
    this(materialIn, materialIn.getMaterialMapColor());
  }
  
  protected BlockBasePressurePlate(Material p_i46401_1_, MapColor p_i46401_2_) {
    super(p_i46401_1_, p_i46401_2_);
    setCreativeTab(CreativeTabs.tabRedstone);
    setTickRandomly(true);
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    setBlockBoundsBasedOnState0(worldIn.getBlockState(pos));
  }
  
  protected void setBlockBoundsBasedOnState0(IBlockState state) {
    boolean flag = (getRedstoneStrength(state) > 0);
    float f = 0.0625F;
    if (flag) {
      setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.03125F, 0.9375F);
    } else {
      setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.0625F, 0.9375F);
    } 
  }
  
  public int tickRate(World worldIn) {
    return 20;
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return null;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
    return true;
  }
  
  public boolean canSpawnInBlock() {
    return true;
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return canBePlacedOn(worldIn, pos.down());
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    if (!canBePlacedOn(worldIn, pos.down())) {
      dropBlockAsItem(worldIn, pos, state, 0);
      worldIn.setBlockToAir(pos);
    } 
  }
  
  private boolean canBePlacedOn(World worldIn, BlockPos pos) {
    return (World.doesBlockHaveSolidTopSurface((IBlockAccess)worldIn, pos) || worldIn.getBlockState(pos).getBlock() instanceof BlockFence);
  }
  
  public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {}
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (!worldIn.isRemote) {
      int i = getRedstoneStrength(state);
      if (i > 0)
        updateState(worldIn, pos, state, i); 
    } 
  }
  
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    if (!worldIn.isRemote) {
      int i = getRedstoneStrength(state);
      if (i == 0)
        updateState(worldIn, pos, state, i); 
    } 
  }
  
  protected void updateState(World worldIn, BlockPos pos, IBlockState state, int oldRedstoneStrength) {
    int i = computeRedstoneStrength(worldIn, pos);
    boolean flag = (oldRedstoneStrength > 0);
    boolean flag1 = (i > 0);
    if (oldRedstoneStrength != i) {
      state = setRedstoneStrength(state, i);
      worldIn.setBlockState(pos, state, 2);
      updateNeighbors(worldIn, pos);
      worldIn.markBlockRangeForRenderUpdate(pos, pos);
    } 
    if (!flag1 && flag) {
      worldIn.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, "random.click", 0.3F, 0.5F);
    } else if (flag1 && !flag) {
      worldIn.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, "random.click", 0.3F, 0.6F);
    } 
    if (flag1)
      worldIn.scheduleUpdate(pos, this, tickRate(worldIn)); 
  }
  
  protected AxisAlignedBB getSensitiveAABB(BlockPos pos) {
    float f = 0.125F;
    return new AxisAlignedBB((pos.getX() + 0.125F), pos.getY(), (pos.getZ() + 0.125F), ((pos.getX() + 1) - 0.125F), pos.getY() + 0.25D, ((pos.getZ() + 1) - 0.125F));
  }
  
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    if (getRedstoneStrength(state) > 0)
      updateNeighbors(worldIn, pos); 
    super.breakBlock(worldIn, pos, state);
  }
  
  protected void updateNeighbors(World worldIn, BlockPos pos) {
    worldIn.notifyNeighborsOfStateChange(pos, this);
    worldIn.notifyNeighborsOfStateChange(pos.down(), this);
  }
  
  public int isProvidingWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
    return getRedstoneStrength(state);
  }
  
  public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
    return (side == EnumFacing.UP) ? getRedstoneStrength(state) : 0;
  }
  
  public boolean canProvidePower() {
    return true;
  }
  
  public void setBlockBoundsForItemRender() {
    float f = 0.5F;
    float f1 = 0.125F;
    float f2 = 0.5F;
    setBlockBounds(0.0F, 0.375F, 0.0F, 1.0F, 0.625F, 1.0F);
  }
  
  public int getMobilityFlag() {
    return 1;
  }
  
  protected abstract int computeRedstoneStrength(World paramWorld, BlockPos paramBlockPos);
  
  protected abstract int getRedstoneStrength(IBlockState paramIBlockState);
  
  protected abstract IBlockState setRedstoneStrength(IBlockState paramIBlockState, int paramInt);
}
