package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPistonMoving extends BlockContainer {
  public static final PropertyDirection FACING = BlockPistonExtension.FACING;
  
  public static final PropertyEnum<BlockPistonExtension.EnumPistonType> TYPE = BlockPistonExtension.TYPE;
  
  public BlockPistonMoving() {
    super(Material.piston);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH).withProperty((IProperty)TYPE, BlockPistonExtension.EnumPistonType.DEFAULT));
    setHardness(-1.0F);
  }
  
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return null;
  }
  
  public static TileEntity newTileEntity(IBlockState state, EnumFacing facing, boolean extending, boolean renderHead) {
    return (TileEntity)new TileEntityPiston(state, facing, extending, renderHead);
  }
  
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityPiston) {
      ((TileEntityPiston)tileentity).clearPistonTileEntity();
    } else {
      super.breakBlock(worldIn, pos, state);
    } 
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return false;
  }
  
  public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
    return false;
  }
  
  public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
    BlockPos blockpos = pos.offset(((EnumFacing)state.getValue((IProperty)FACING)).getOpposite());
    IBlockState iblockstate = worldIn.getBlockState(blockpos);
    if (iblockstate.getBlock() instanceof BlockPistonBase && ((Boolean)iblockstate.getValue((IProperty)BlockPistonBase.EXTENDED)).booleanValue())
      worldIn.setBlockToAir(blockpos); 
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!worldIn.isRemote && worldIn.getTileEntity(pos) == null) {
      worldIn.setBlockToAir(pos);
      return true;
    } 
    return false;
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return null;
  }
  
  public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    if (!worldIn.isRemote) {
      TileEntityPiston tileentitypiston = getTileEntity((IBlockAccess)worldIn, pos);
      if (tileentitypiston != null) {
        IBlockState iblockstate = tileentitypiston.getPistonState();
        iblockstate.getBlock().dropBlockAsItem(worldIn, pos, iblockstate, 0);
      } 
    } 
  }
  
  public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
    return null;
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    if (!worldIn.isRemote)
      worldIn.getTileEntity(pos); 
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    TileEntityPiston tileentitypiston = getTileEntity((IBlockAccess)worldIn, pos);
    if (tileentitypiston == null)
      return null; 
    float f = tileentitypiston.getProgress(0.0F);
    if (tileentitypiston.isExtending())
      f = 1.0F - f; 
    return getBoundingBox(worldIn, pos, tileentitypiston.getPistonState(), f, tileentitypiston.getFacing());
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    TileEntityPiston tileentitypiston = getTileEntity(worldIn, pos);
    if (tileentitypiston != null) {
      IBlockState iblockstate = tileentitypiston.getPistonState();
      Block block = iblockstate.getBlock();
      if (block == this || block.getMaterial() == Material.air)
        return; 
      float f = tileentitypiston.getProgress(0.0F);
      if (tileentitypiston.isExtending())
        f = 1.0F - f; 
      block.setBlockBoundsBasedOnState(worldIn, pos);
      if (block == Blocks.piston || block == Blocks.sticky_piston)
        f = 0.0F; 
      EnumFacing enumfacing = tileentitypiston.getFacing();
      this.minX = block.getBlockBoundsMinX() - (enumfacing.getFrontOffsetX() * f);
      this.minY = block.getBlockBoundsMinY() - (enumfacing.getFrontOffsetY() * f);
      this.minZ = block.getBlockBoundsMinZ() - (enumfacing.getFrontOffsetZ() * f);
      this.maxX = block.getBlockBoundsMaxX() - (enumfacing.getFrontOffsetX() * f);
      this.maxY = block.getBlockBoundsMaxY() - (enumfacing.getFrontOffsetY() * f);
      this.maxZ = block.getBlockBoundsMaxZ() - (enumfacing.getFrontOffsetZ() * f);
    } 
  }
  
  public AxisAlignedBB getBoundingBox(World worldIn, BlockPos pos, IBlockState extendingBlock, float progress, EnumFacing direction) {
    if (extendingBlock.getBlock() != this && extendingBlock.getBlock().getMaterial() != Material.air) {
      AxisAlignedBB axisalignedbb = extendingBlock.getBlock().getCollisionBoundingBox(worldIn, pos, extendingBlock);
      if (axisalignedbb == null)
        return null; 
      double d0 = axisalignedbb.minX;
      double d1 = axisalignedbb.minY;
      double d2 = axisalignedbb.minZ;
      double d3 = axisalignedbb.maxX;
      double d4 = axisalignedbb.maxY;
      double d5 = axisalignedbb.maxZ;
      if (direction.getFrontOffsetX() < 0) {
        d0 -= (direction.getFrontOffsetX() * progress);
      } else {
        d3 -= (direction.getFrontOffsetX() * progress);
      } 
      if (direction.getFrontOffsetY() < 0) {
        d1 -= (direction.getFrontOffsetY() * progress);
      } else {
        d4 -= (direction.getFrontOffsetY() * progress);
      } 
      if (direction.getFrontOffsetZ() < 0) {
        d2 -= (direction.getFrontOffsetZ() * progress);
      } else {
        d5 -= (direction.getFrontOffsetZ() * progress);
      } 
      return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    } 
    return null;
  }
  
  private TileEntityPiston getTileEntity(IBlockAccess worldIn, BlockPos pos) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    return (tileentity instanceof TileEntityPiston) ? (TileEntityPiston)tileentity : null;
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return null;
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)BlockPistonExtension.getFacing(meta)).withProperty((IProperty)TYPE, ((meta & 0x8) > 0) ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    i |= ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
    if (state.getValue((IProperty)TYPE) == BlockPistonExtension.EnumPistonType.STICKY)
      i |= 0x8; 
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING, (IProperty)TYPE });
  }
}
