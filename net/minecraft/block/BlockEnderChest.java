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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockEnderChest extends BlockContainer {
  public static final PropertyDirection FACING = PropertyDirection.create("facing", (Predicate)EnumFacing.Plane.HORIZONTAL);
  
  protected BlockEnderChest() {
    super(Material.rock);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH));
    setCreativeTab(CreativeTabs.tabDecorations);
    setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public int getRenderType() {
    return 2;
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Item.getItemFromBlock(Blocks.obsidian);
  }
  
  public int quantityDropped(Random random) {
    return 8;
  }
  
  protected boolean canSilkHarvest() {
    return true;
  }
  
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)placer.getHorizontalFacing().getOpposite());
  }
  
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    worldIn.setBlockState(pos, state.withProperty((IProperty)FACING, (Comparable)placer.getHorizontalFacing().getOpposite()), 2);
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    InventoryEnderChest inventoryenderchest = playerIn.getInventoryEnderChest();
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (inventoryenderchest != null && tileentity instanceof TileEntityEnderChest) {
      if (worldIn.getBlockState(pos.up()).getBlock().isNormalCube())
        return true; 
      if (worldIn.isRemote)
        return true; 
      inventoryenderchest.setChestTileEntity((TileEntityEnderChest)tileentity);
      playerIn.displayGUIChest((IInventory)inventoryenderchest);
      playerIn.triggerAchievement(StatList.field_181738_V);
      return true;
    } 
    return true;
  }
  
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return (TileEntity)new TileEntityEnderChest();
  }
  
  public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    for (int i = 0; i < 3; i++) {
      int j = rand.nextInt(2) * 2 - 1;
      int k = rand.nextInt(2) * 2 - 1;
      double d0 = pos.getX() + 0.5D + 0.25D * j;
      double d1 = (pos.getY() + rand.nextFloat());
      double d2 = pos.getZ() + 0.5D + 0.25D * k;
      double d3 = (rand.nextFloat() * j);
      double d4 = (rand.nextFloat() - 0.5D) * 0.125D;
      double d5 = (rand.nextFloat() * k);
      worldIn.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5, new int[0]);
    } 
  }
  
  public IBlockState getStateFromMeta(int meta) {
    EnumFacing enumfacing = EnumFacing.getFront(meta);
    if (enumfacing.getAxis() == EnumFacing.Axis.Y)
      enumfacing = EnumFacing.NORTH; 
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)enumfacing);
  }
  
  public int getMetaFromState(IBlockState state) {
    return ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING });
  }
}
