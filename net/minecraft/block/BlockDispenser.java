package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.PositionImpl;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.RegistryDefaulted;
import net.minecraft.world.World;

public class BlockDispenser extends BlockContainer {
  public static final PropertyDirection FACING = PropertyDirection.create("facing");
  
  public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");
  
  public static final RegistryDefaulted<Item, IBehaviorDispenseItem> dispenseBehaviorRegistry = new RegistryDefaulted(new BehaviorDefaultDispenseItem());
  
  protected Random rand = new Random();
  
  protected BlockDispenser() {
    super(Material.rock);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH).withProperty((IProperty)TRIGGERED, Boolean.valueOf(false)));
    setCreativeTab(CreativeTabs.tabRedstone);
  }
  
  public int tickRate(World worldIn) {
    return 4;
  }
  
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    super.onBlockAdded(worldIn, pos, state);
    setDefaultDirection(worldIn, pos, state);
  }
  
  private void setDefaultDirection(World worldIn, BlockPos pos, IBlockState state) {
    if (!worldIn.isRemote) {
      EnumFacing enumfacing = (EnumFacing)state.getValue((IProperty)FACING);
      boolean flag = worldIn.getBlockState(pos.north()).getBlock().isFullBlock();
      boolean flag1 = worldIn.getBlockState(pos.south()).getBlock().isFullBlock();
      if (enumfacing == EnumFacing.NORTH && flag && !flag1) {
        enumfacing = EnumFacing.SOUTH;
      } else if (enumfacing == EnumFacing.SOUTH && flag1 && !flag) {
        enumfacing = EnumFacing.NORTH;
      } else {
        boolean flag2 = worldIn.getBlockState(pos.west()).getBlock().isFullBlock();
        boolean flag3 = worldIn.getBlockState(pos.east()).getBlock().isFullBlock();
        if (enumfacing == EnumFacing.WEST && flag2 && !flag3) {
          enumfacing = EnumFacing.EAST;
        } else if (enumfacing == EnumFacing.EAST && flag3 && !flag2) {
          enumfacing = EnumFacing.WEST;
        } 
      } 
      worldIn.setBlockState(pos, state.withProperty((IProperty)FACING, (Comparable)enumfacing).withProperty((IProperty)TRIGGERED, Boolean.valueOf(false)), 2);
    } 
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote)
      return true; 
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityDispenser) {
      playerIn.displayGUIChest((IInventory)tileentity);
      if (tileentity instanceof net.minecraft.tileentity.TileEntityDropper) {
        playerIn.triggerAchievement(StatList.field_181731_O);
      } else {
        playerIn.triggerAchievement(StatList.field_181733_Q);
      } 
    } 
    return true;
  }
  
  protected void dispense(World worldIn, BlockPos pos) {
    BlockSourceImpl blocksourceimpl = new BlockSourceImpl(worldIn, pos);
    TileEntityDispenser tileentitydispenser = blocksourceimpl.<TileEntityDispenser>getBlockTileEntity();
    if (tileentitydispenser != null) {
      int i = tileentitydispenser.getDispenseSlot();
      if (i < 0) {
        worldIn.playAuxSFX(1001, pos, 0);
      } else {
        ItemStack itemstack = tileentitydispenser.getStackInSlot(i);
        IBehaviorDispenseItem ibehaviordispenseitem = getBehavior(itemstack);
        if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider) {
          ItemStack itemstack1 = ibehaviordispenseitem.dispense(blocksourceimpl, itemstack);
          tileentitydispenser.setInventorySlotContents(i, (itemstack1.stackSize <= 0) ? null : itemstack1);
        } 
      } 
    } 
  }
  
  protected IBehaviorDispenseItem getBehavior(ItemStack stack) {
    return (IBehaviorDispenseItem)dispenseBehaviorRegistry.getObject((stack == null) ? null : stack.getItem());
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    boolean flag = (worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up()));
    boolean flag1 = ((Boolean)state.getValue((IProperty)TRIGGERED)).booleanValue();
    if (flag && !flag1) {
      worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
      worldIn.setBlockState(pos, state.withProperty((IProperty)TRIGGERED, Boolean.valueOf(true)), 4);
    } else if (!flag && flag1) {
      worldIn.setBlockState(pos, state.withProperty((IProperty)TRIGGERED, Boolean.valueOf(false)), 4);
    } 
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (!worldIn.isRemote)
      dispense(worldIn, pos); 
  }
  
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return (TileEntity)new TileEntityDispenser();
  }
  
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)BlockPistonBase.getFacingFromEntity(worldIn, pos, placer)).withProperty((IProperty)TRIGGERED, Boolean.valueOf(false));
  }
  
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    worldIn.setBlockState(pos, state.withProperty((IProperty)FACING, (Comparable)BlockPistonBase.getFacingFromEntity(worldIn, pos, placer)), 2);
    if (stack.hasDisplayName()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityDispenser)
        ((TileEntityDispenser)tileentity).setCustomName(stack.getDisplayName()); 
    } 
  }
  
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileEntityDispenser) {
      InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
      worldIn.updateComparatorOutputLevel(pos, this);
    } 
    super.breakBlock(worldIn, pos, state);
  }
  
  public static IPosition getDispensePosition(IBlockSource coords) {
    EnumFacing enumfacing = getFacing(coords.getBlockMetadata());
    double d0 = coords.getX() + 0.7D * enumfacing.getFrontOffsetX();
    double d1 = coords.getY() + 0.7D * enumfacing.getFrontOffsetY();
    double d2 = coords.getZ() + 0.7D * enumfacing.getFrontOffsetZ();
    return (IPosition)new PositionImpl(d0, d1, d2);
  }
  
  public static EnumFacing getFacing(int meta) {
    return EnumFacing.getFront(meta & 0x7);
  }
  
  public boolean hasComparatorInputOverride() {
    return true;
  }
  
  public int getComparatorInputOverride(World worldIn, BlockPos pos) {
    return Container.calcRedstone(worldIn.getTileEntity(pos));
  }
  
  public int getRenderType() {
    return 3;
  }
  
  public IBlockState getStateForEntityRender(IBlockState state) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)EnumFacing.SOUTH);
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)getFacing(meta)).withProperty((IProperty)TRIGGERED, Boolean.valueOf(((meta & 0x8) > 0)));
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    i |= ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
    if (((Boolean)state.getValue((IProperty)TRIGGERED)).booleanValue())
      i |= 0x8; 
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING, (IProperty)TRIGGERED });
  }
}
