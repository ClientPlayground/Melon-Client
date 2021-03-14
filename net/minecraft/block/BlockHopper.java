package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockHopper extends BlockContainer {
  public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>() {
        public boolean apply(EnumFacing p_apply_1_) {
          return (p_apply_1_ != EnumFacing.UP);
        }
      });
  
  public static final PropertyBool ENABLED = PropertyBool.create("enabled");
  
  public BlockHopper() {
    super(Material.iron, MapColor.stoneColor);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.DOWN).withProperty((IProperty)ENABLED, Boolean.valueOf(true)));
    setCreativeTab(CreativeTabs.tabRedstone);
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
  }
  
  public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);
    super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    float f = 0.125F;
    setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
    super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
    super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
    super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
  }
  
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    EnumFacing enumfacing = facing.getOpposite();
    if (enumfacing == EnumFacing.UP)
      enumfacing = EnumFacing.DOWN; 
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)enumfacing).withProperty((IProperty)ENABLED, Boolean.valueOf(true));
  }
  
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return (TileEntity)new TileEntityHopper();
  }
  
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    if (stack.hasDisplayName()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityHopper)
        ((TileEntityHopper)tileentity).setCustomName(stack.getDisplayName()); 
    } 
  }
  
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    updateState(worldIn, pos, state);
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote)
      return true; 
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityHopper) {
      playerIn.displayGUIChest((IInventory)tileentity);
      playerIn.triggerAchievement(StatList.field_181732_P);
    } 
    return true;
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    updateState(worldIn, pos, state);
  }
  
  private void updateState(World worldIn, BlockPos pos, IBlockState state) {
    boolean flag = !worldIn.isBlockPowered(pos);
    if (flag != ((Boolean)state.getValue((IProperty)ENABLED)).booleanValue())
      worldIn.setBlockState(pos, state.withProperty((IProperty)ENABLED, Boolean.valueOf(flag)), 4); 
  }
  
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityHopper) {
      InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
      worldIn.updateComparatorOutputLevel(pos, this);
    } 
    super.breakBlock(worldIn, pos, state);
  }
  
  public int getRenderType() {
    return 3;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return true;
  }
  
  public static EnumFacing getFacing(int meta) {
    return EnumFacing.getFront(meta & 0x7);
  }
  
  public static boolean isEnabled(int meta) {
    return ((meta & 0x8) != 8);
  }
  
  public boolean hasComparatorInputOverride() {
    return true;
  }
  
  public int getComparatorInputOverride(World worldIn, BlockPos pos) {
    return Container.calcRedstone(worldIn.getTileEntity(pos));
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.CUTOUT_MIPPED;
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)getFacing(meta)).withProperty((IProperty)ENABLED, Boolean.valueOf(isEnabled(meta)));
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    i |= ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
    if (!((Boolean)state.getValue((IProperty)ENABLED)).booleanValue())
      i |= 0x8; 
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING, (IProperty)ENABLED });
  }
}
