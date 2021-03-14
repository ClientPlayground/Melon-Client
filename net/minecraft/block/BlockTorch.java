package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTorch extends Block {
  public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>() {
        public boolean apply(EnumFacing p_apply_1_) {
          return (p_apply_1_ != EnumFacing.DOWN);
        }
      });
  
  protected BlockTorch() {
    super(Material.circuits);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.UP));
    setTickRandomly(true);
    setCreativeTab(CreativeTabs.tabDecorations);
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
  
  private boolean canPlaceOn(World worldIn, BlockPos pos) {
    if (World.doesBlockHaveSolidTopSurface((IBlockAccess)worldIn, pos))
      return true; 
    Block block = worldIn.getBlockState(pos).getBlock();
    return (block instanceof BlockFence || block == Blocks.glass || block == Blocks.cobblestone_wall || block == Blocks.stained_glass);
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    for (EnumFacing enumfacing : FACING.getAllowedValues()) {
      if (canPlaceAt(worldIn, pos, enumfacing))
        return true; 
    } 
    return false;
  }
  
  private boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing facing) {
    BlockPos blockpos = pos.offset(facing.getOpposite());
    boolean flag = facing.getAxis().isHorizontal();
    return ((flag && worldIn.isBlockNormalCube(blockpos, true)) || (facing.equals(EnumFacing.UP) && canPlaceOn(worldIn, blockpos)));
  }
  
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    if (canPlaceAt(worldIn, pos, facing))
      return getDefaultState().withProperty((IProperty)FACING, (Comparable)facing); 
    for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
      if (worldIn.isBlockNormalCube(pos.offset(enumfacing.getOpposite()), true))
        return getDefaultState().withProperty((IProperty)FACING, (Comparable)enumfacing); 
    } 
    return getDefaultState();
  }
  
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    checkForDrop(worldIn, pos, state);
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    onNeighborChangeInternal(worldIn, pos, state);
  }
  
  protected boolean onNeighborChangeInternal(World worldIn, BlockPos pos, IBlockState state) {
    if (!checkForDrop(worldIn, pos, state))
      return true; 
    EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
    EnumFacing.Axis enumfacing$axis = enumfacing.getAxis();
    EnumFacing enumfacing1 = enumfacing.getOpposite();
    boolean flag = false;
    if (enumfacing$axis.isHorizontal() && !worldIn.isBlockNormalCube(pos.offset(enumfacing1), true)) {
      flag = true;
    } else if (enumfacing$axis.isVertical() && !canPlaceOn(worldIn, pos.offset(enumfacing1))) {
      flag = true;
    } 
    if (flag) {
      dropBlockAsItem(worldIn, pos, state, 0);
      worldIn.setBlockToAir(pos);
      return true;
    } 
    return false;
  }
  
  protected boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {
    if (state.getBlock() == this && canPlaceAt(worldIn, pos, (EnumFacing)state.getValue((IProperty)FACING)))
      return true; 
    if (worldIn.getBlockState(pos).getBlock() == this) {
      dropBlockAsItem(worldIn, pos, state, 0);
      worldIn.setBlockToAir(pos);
    } 
    return false;
  }
  
  public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
    EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue((IProperty)FACING);
    float f = 0.15F;
    if (enumfacing == EnumFacing.EAST) {
      setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
    } else if (enumfacing == EnumFacing.WEST) {
      setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
    } else if (enumfacing == EnumFacing.SOUTH) {
      setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
    } else if (enumfacing == EnumFacing.NORTH) {
      setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
    } else {
      f = 0.1F;
      setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
    } 
    return super.collisionRayTrace(worldIn, pos, start, end);
  }
  
  public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
    double d0 = pos.getX() + 0.5D;
    double d1 = pos.getY() + 0.7D;
    double d2 = pos.getZ() + 0.5D;
    double d3 = 0.22D;
    double d4 = 0.27D;
    if (enumfacing.getAxis().isHorizontal()) {
      EnumFacing enumfacing1 = enumfacing.getOpposite();
      worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4 * enumfacing1.getFrontOffsetX(), d1 + d3, d2 + d4 * enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D, new int[0]);
      worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4 * enumfacing1.getFrontOffsetX(), d1 + d3, d2 + d4 * enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D, new int[0]);
    } else {
      worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
      worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
    } 
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.CUTOUT;
  }
  
  public IBlockState getStateFromMeta(int meta) {
    IBlockState iblockstate = getDefaultState();
    switch (meta) {
      case 1:
        iblockstate = iblockstate.withProperty((IProperty)FACING, (Comparable)EnumFacing.EAST);
        return iblockstate;
      case 2:
        iblockstate = iblockstate.withProperty((IProperty)FACING, (Comparable)EnumFacing.WEST);
        return iblockstate;
      case 3:
        iblockstate = iblockstate.withProperty((IProperty)FACING, (Comparable)EnumFacing.SOUTH);
        return iblockstate;
      case 4:
        iblockstate = iblockstate.withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH);
        return iblockstate;
    } 
    iblockstate = iblockstate.withProperty((IProperty)FACING, (Comparable)EnumFacing.UP);
    return iblockstate;
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    switch ((EnumFacing)state.getValue((IProperty)FACING)) {
      case EAST:
        i |= 0x1;
        return i;
      case WEST:
        i |= 0x2;
        return i;
      case SOUTH:
        i |= 0x3;
        return i;
      case NORTH:
        i |= 0x4;
        return i;
    } 
    i |= 0x5;
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING });
  }
}
