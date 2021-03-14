package net.minecraft.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPistonExtension extends Block {
  public static final PropertyDirection FACING = PropertyDirection.create("facing");
  
  public static final PropertyEnum<EnumPistonType> TYPE = PropertyEnum.create("type", EnumPistonType.class);
  
  public static final PropertyBool SHORT = PropertyBool.create("short");
  
  public BlockPistonExtension() {
    super(Material.piston);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH).withProperty((IProperty)TYPE, EnumPistonType.DEFAULT).withProperty((IProperty)SHORT, Boolean.valueOf(false)));
    setStepSound(soundTypePiston);
    setHardness(0.5F);
  }
  
  public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
    if (player.capabilities.isCreativeMode) {
      EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
      if (enumfacing != null) {
        BlockPos blockpos = pos.offset(enumfacing.getOpposite());
        Block block = worldIn.getBlockState(blockpos).getBlock();
        if (block == Blocks.piston || block == Blocks.sticky_piston)
          worldIn.setBlockToAir(blockpos); 
      } 
    } 
    super.onBlockHarvested(worldIn, pos, state, player);
  }
  
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    super.breakBlock(worldIn, pos, state);
    EnumFacing enumfacing = ((EnumFacing)state.getValue((IProperty)FACING)).getOpposite();
    pos = pos.offset(enumfacing);
    IBlockState iblockstate = worldIn.getBlockState(pos);
    if ((iblockstate.getBlock() == Blocks.piston || iblockstate.getBlock() == Blocks.sticky_piston) && ((Boolean)iblockstate.getValue((IProperty)BlockPistonBase.EXTENDED)).booleanValue()) {
      iblockstate.getBlock().dropBlockAsItem(worldIn, pos, iblockstate, 0);
      worldIn.setBlockToAir(pos);
    } 
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return false;
  }
  
  public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
    return false;
  }
  
  public int quantityDropped(Random random) {
    return 0;
  }
  
  public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
    applyHeadBounds(state);
    super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    applyCoreBounds(state);
    super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
  }
  
  private void applyCoreBounds(IBlockState state) {
    float f = 0.25F;
    float f1 = 0.375F;
    float f2 = 0.625F;
    float f3 = 0.25F;
    float f4 = 0.75F;
    switch ((EnumFacing)state.getValue((IProperty)FACING)) {
      case DOWN:
        setBlockBounds(0.375F, 0.25F, 0.375F, 0.625F, 1.0F, 0.625F);
        break;
      case UP:
        setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 0.75F, 0.625F);
        break;
      case NORTH:
        setBlockBounds(0.25F, 0.375F, 0.25F, 0.75F, 0.625F, 1.0F);
        break;
      case SOUTH:
        setBlockBounds(0.25F, 0.375F, 0.0F, 0.75F, 0.625F, 0.75F);
        break;
      case WEST:
        setBlockBounds(0.375F, 0.25F, 0.25F, 0.625F, 0.75F, 1.0F);
        break;
      case EAST:
        setBlockBounds(0.0F, 0.375F, 0.25F, 0.75F, 0.625F, 0.75F);
        break;
    } 
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    applyHeadBounds(worldIn.getBlockState(pos));
  }
  
  public void applyHeadBounds(IBlockState state) {
    float f = 0.25F;
    EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
    if (enumfacing != null)
      switch (enumfacing) {
        case DOWN:
          setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
          break;
        case UP:
          setBlockBounds(0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F);
          break;
        case NORTH:
          setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.25F);
          break;
        case SOUTH:
          setBlockBounds(0.0F, 0.0F, 0.75F, 1.0F, 1.0F, 1.0F);
          break;
        case WEST:
          setBlockBounds(0.0F, 0.0F, 0.0F, 0.25F, 1.0F, 1.0F);
          break;
        case EAST:
          setBlockBounds(0.75F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
          break;
      }  
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
    BlockPos blockpos = pos.offset(enumfacing.getOpposite());
    IBlockState iblockstate = worldIn.getBlockState(blockpos);
    if (iblockstate.getBlock() != Blocks.piston && iblockstate.getBlock() != Blocks.sticky_piston) {
      worldIn.setBlockToAir(pos);
    } else {
      iblockstate.getBlock().onNeighborBlockChange(worldIn, blockpos, iblockstate, neighborBlock);
    } 
  }
  
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return true;
  }
  
  public static EnumFacing getFacing(int meta) {
    int i = meta & 0x7;
    return (i > 5) ? null : EnumFacing.getFront(i);
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return (worldIn.getBlockState(pos).getValue((IProperty)TYPE) == EnumPistonType.STICKY) ? Item.getItemFromBlock(Blocks.sticky_piston) : Item.getItemFromBlock(Blocks.piston);
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)getFacing(meta)).withProperty((IProperty)TYPE, ((meta & 0x8) > 0) ? EnumPistonType.STICKY : EnumPistonType.DEFAULT);
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    i |= ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
    if (state.getValue((IProperty)TYPE) == EnumPistonType.STICKY)
      i |= 0x8; 
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING, (IProperty)TYPE, (IProperty)SHORT });
  }
  
  public enum EnumPistonType implements IStringSerializable {
    DEFAULT("normal"),
    STICKY("sticky");
    
    private final String VARIANT;
    
    EnumPistonType(String name) {
      this.VARIANT = name;
    }
    
    public String toString() {
      return this.VARIANT;
    }
    
    public String getName() {
      return this.VARIANT;
    }
  }
}
