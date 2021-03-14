package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDoor extends Block {
  public static final PropertyDirection FACING = PropertyDirection.create("facing", (Predicate)EnumFacing.Plane.HORIZONTAL);
  
  public static final PropertyBool OPEN = PropertyBool.create("open");
  
  public static final PropertyEnum<EnumHingePosition> HINGE = PropertyEnum.create("hinge", EnumHingePosition.class);
  
  public static final PropertyBool POWERED = PropertyBool.create("powered");
  
  public static final PropertyEnum<EnumDoorHalf> HALF = PropertyEnum.create("half", EnumDoorHalf.class);
  
  protected BlockDoor(Material materialIn) {
    super(materialIn);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH).withProperty((IProperty)OPEN, Boolean.valueOf(false)).withProperty((IProperty)HINGE, EnumHingePosition.LEFT).withProperty((IProperty)POWERED, Boolean.valueOf(false)).withProperty((IProperty)HALF, EnumDoorHalf.LOWER));
  }
  
  public String getLocalizedName() {
    return StatCollector.translateToLocal((getUnlocalizedName() + ".name").replaceAll("tile", "item"));
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
    return isOpen(combineMetadata(worldIn, pos));
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
    setBlockBoundsBasedOnState((IBlockAccess)worldIn, pos);
    return super.getSelectedBoundingBox(worldIn, pos);
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    setBlockBoundsBasedOnState((IBlockAccess)worldIn, pos);
    return super.getCollisionBoundingBox(worldIn, pos, state);
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    setBoundBasedOnMeta(combineMetadata(worldIn, pos));
  }
  
  private void setBoundBasedOnMeta(int combinedMeta) {
    float f = 0.1875F;
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
    EnumFacing enumfacing = getFacing(combinedMeta);
    boolean flag = isOpen(combinedMeta);
    boolean flag1 = isHingeLeft(combinedMeta);
    if (flag) {
      if (enumfacing == EnumFacing.EAST) {
        if (!flag1) {
          setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
        } else {
          setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
        } 
      } else if (enumfacing == EnumFacing.SOUTH) {
        if (!flag1) {
          setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        } else {
          setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
        } 
      } else if (enumfacing == EnumFacing.WEST) {
        if (!flag1) {
          setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
        } else {
          setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
        } 
      } else if (enumfacing == EnumFacing.NORTH) {
        if (!flag1) {
          setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
        } else {
          setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        } 
      } 
    } else if (enumfacing == EnumFacing.EAST) {
      setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
    } else if (enumfacing == EnumFacing.SOUTH) {
      setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
    } else if (enumfacing == EnumFacing.WEST) {
      setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    } else if (enumfacing == EnumFacing.NORTH) {
      setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
    } 
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (this.blockMaterial == Material.iron)
      return true; 
    BlockPos blockpos = (state.getValue((IProperty)HALF) == EnumDoorHalf.LOWER) ? pos : pos.down();
    IBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);
    if (iblockstate.getBlock() != this)
      return false; 
    state = iblockstate.cycleProperty((IProperty)OPEN);
    worldIn.setBlockState(blockpos, state, 2);
    worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
    worldIn.playAuxSFXAtEntity(playerIn, ((Boolean)state.getValue((IProperty)OPEN)).booleanValue() ? 1003 : 1006, pos, 0);
    return true;
  }
  
  public void toggleDoor(World worldIn, BlockPos pos, boolean open) {
    IBlockState iblockstate = worldIn.getBlockState(pos);
    if (iblockstate.getBlock() == this) {
      BlockPos blockpos = (iblockstate.getValue((IProperty)HALF) == EnumDoorHalf.LOWER) ? pos : pos.down();
      IBlockState iblockstate1 = (pos == blockpos) ? iblockstate : worldIn.getBlockState(blockpos);
      if (iblockstate1.getBlock() == this && ((Boolean)iblockstate1.getValue((IProperty)OPEN)).booleanValue() != open) {
        worldIn.setBlockState(blockpos, iblockstate1.withProperty((IProperty)OPEN, Boolean.valueOf(open)), 2);
        worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
        worldIn.playAuxSFXAtEntity((EntityPlayer)null, open ? 1003 : 1006, pos, 0);
      } 
    } 
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    if (state.getValue((IProperty)HALF) == EnumDoorHalf.UPPER) {
      BlockPos blockpos = pos.down();
      IBlockState iblockstate = worldIn.getBlockState(blockpos);
      if (iblockstate.getBlock() != this) {
        worldIn.setBlockToAir(pos);
      } else if (neighborBlock != this) {
        onNeighborBlockChange(worldIn, blockpos, iblockstate, neighborBlock);
      } 
    } else {
      boolean flag1 = false;
      BlockPos blockpos1 = pos.up();
      IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);
      if (iblockstate1.getBlock() != this) {
        worldIn.setBlockToAir(pos);
        flag1 = true;
      } 
      if (!World.doesBlockHaveSolidTopSurface((IBlockAccess)worldIn, pos.down())) {
        worldIn.setBlockToAir(pos);
        flag1 = true;
        if (iblockstate1.getBlock() == this)
          worldIn.setBlockToAir(blockpos1); 
      } 
      if (flag1) {
        if (!worldIn.isRemote)
          dropBlockAsItem(worldIn, pos, state, 0); 
      } else {
        boolean flag = (worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos1));
        if ((flag || neighborBlock.canProvidePower()) && neighborBlock != this && flag != ((Boolean)iblockstate1.getValue((IProperty)POWERED)).booleanValue()) {
          worldIn.setBlockState(blockpos1, iblockstate1.withProperty((IProperty)POWERED, Boolean.valueOf(flag)), 2);
          if (flag != ((Boolean)state.getValue((IProperty)OPEN)).booleanValue()) {
            worldIn.setBlockState(pos, state.withProperty((IProperty)OPEN, Boolean.valueOf(flag)), 2);
            worldIn.markBlockRangeForRenderUpdate(pos, pos);
            worldIn.playAuxSFXAtEntity((EntityPlayer)null, flag ? 1003 : 1006, pos, 0);
          } 
        } 
      } 
    } 
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return (state.getValue((IProperty)HALF) == EnumDoorHalf.UPPER) ? null : getItem();
  }
  
  public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
    setBlockBoundsBasedOnState((IBlockAccess)worldIn, pos);
    return super.collisionRayTrace(worldIn, pos, start, end);
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return (pos.getY() >= 255) ? false : ((World.doesBlockHaveSolidTopSurface((IBlockAccess)worldIn, pos.down()) && super.canPlaceBlockAt(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos.up())));
  }
  
  public int getMobilityFlag() {
    return 1;
  }
  
  public static int combineMetadata(IBlockAccess worldIn, BlockPos pos) {
    IBlockState iblockstate = worldIn.getBlockState(pos);
    int i = iblockstate.getBlock().getMetaFromState(iblockstate);
    boolean flag = isTop(i);
    IBlockState iblockstate1 = worldIn.getBlockState(pos.down());
    int j = iblockstate1.getBlock().getMetaFromState(iblockstate1);
    int k = flag ? j : i;
    IBlockState iblockstate2 = worldIn.getBlockState(pos.up());
    int l = iblockstate2.getBlock().getMetaFromState(iblockstate2);
    int i1 = flag ? i : l;
    boolean flag1 = ((i1 & 0x1) != 0);
    boolean flag2 = ((i1 & 0x2) != 0);
    return removeHalfBit(k) | (flag ? 8 : 0) | (flag1 ? 16 : 0) | (flag2 ? 32 : 0);
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return getItem();
  }
  
  private Item getItem() {
    return (this == Blocks.iron_door) ? Items.iron_door : ((this == Blocks.spruce_door) ? Items.spruce_door : ((this == Blocks.birch_door) ? Items.birch_door : ((this == Blocks.jungle_door) ? Items.jungle_door : ((this == Blocks.acacia_door) ? Items.acacia_door : ((this == Blocks.dark_oak_door) ? Items.dark_oak_door : Items.oak_door)))));
  }
  
  public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
    BlockPos blockpos = pos.down();
    if (player.capabilities.isCreativeMode && state.getValue((IProperty)HALF) == EnumDoorHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this)
      worldIn.setBlockToAir(blockpos); 
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.CUTOUT;
  }
  
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    if (state.getValue((IProperty)HALF) == EnumDoorHalf.LOWER) {
      IBlockState iblockstate = worldIn.getBlockState(pos.up());
      if (iblockstate.getBlock() == this)
        state = state.withProperty((IProperty)HINGE, iblockstate.getValue((IProperty)HINGE)).withProperty((IProperty)POWERED, iblockstate.getValue((IProperty)POWERED)); 
    } else {
      IBlockState iblockstate1 = worldIn.getBlockState(pos.down());
      if (iblockstate1.getBlock() == this)
        state = state.withProperty((IProperty)FACING, iblockstate1.getValue((IProperty)FACING)).withProperty((IProperty)OPEN, iblockstate1.getValue((IProperty)OPEN)); 
    } 
    return state;
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return ((meta & 0x8) > 0) ? getDefaultState().withProperty((IProperty)HALF, EnumDoorHalf.UPPER).withProperty((IProperty)HINGE, ((meta & 0x1) > 0) ? EnumHingePosition.RIGHT : EnumHingePosition.LEFT).withProperty((IProperty)POWERED, Boolean.valueOf(((meta & 0x2) > 0))) : getDefaultState().withProperty((IProperty)HALF, EnumDoorHalf.LOWER).withProperty((IProperty)FACING, (Comparable)EnumFacing.getHorizontal(meta & 0x3).rotateYCCW()).withProperty((IProperty)OPEN, Boolean.valueOf(((meta & 0x4) > 0)));
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    if (state.getValue((IProperty)HALF) == EnumDoorHalf.UPPER) {
      i |= 0x8;
      if (state.getValue((IProperty)HINGE) == EnumHingePosition.RIGHT)
        i |= 0x1; 
      if (((Boolean)state.getValue((IProperty)POWERED)).booleanValue())
        i |= 0x2; 
    } else {
      i |= ((EnumFacing)state.getValue((IProperty)FACING)).rotateY().getHorizontalIndex();
      if (((Boolean)state.getValue((IProperty)OPEN)).booleanValue())
        i |= 0x4; 
    } 
    return i;
  }
  
  protected static int removeHalfBit(int meta) {
    return meta & 0x7;
  }
  
  public static boolean isOpen(IBlockAccess worldIn, BlockPos pos) {
    return isOpen(combineMetadata(worldIn, pos));
  }
  
  public static EnumFacing getFacing(IBlockAccess worldIn, BlockPos pos) {
    return getFacing(combineMetadata(worldIn, pos));
  }
  
  public static EnumFacing getFacing(int combinedMeta) {
    return EnumFacing.getHorizontal(combinedMeta & 0x3).rotateYCCW();
  }
  
  protected static boolean isOpen(int combinedMeta) {
    return ((combinedMeta & 0x4) != 0);
  }
  
  protected static boolean isTop(int meta) {
    return ((meta & 0x8) != 0);
  }
  
  protected static boolean isHingeLeft(int combinedMeta) {
    return ((combinedMeta & 0x10) != 0);
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)HALF, (IProperty)FACING, (IProperty)OPEN, (IProperty)HINGE, (IProperty)POWERED });
  }
  
  public enum EnumDoorHalf implements IStringSerializable {
    UPPER, LOWER;
    
    public String toString() {
      return getName();
    }
    
    public String getName() {
      return (this == UPPER) ? "upper" : "lower";
    }
  }
  
  public enum EnumHingePosition implements IStringSerializable {
    LEFT, RIGHT;
    
    public String toString() {
      return getName();
    }
    
    public String getName() {
      return (this == LEFT) ? "left" : "right";
    }
  }
}
