package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockReed extends Block {
  public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
  
  protected BlockReed() {
    super(Material.plants);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)AGE, Integer.valueOf(0)));
    float f = 0.375F;
    setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
    setTickRandomly(true);
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (worldIn.getBlockState(pos.down()).getBlock() == Blocks.reeds || checkForDrop(worldIn, pos, state))
      if (worldIn.isAirBlock(pos.up())) {
        int i;
        for (i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; i++);
        if (i < 3) {
          int j = ((Integer)state.getValue((IProperty)AGE)).intValue();
          if (j == 15) {
            worldIn.setBlockState(pos.up(), getDefaultState());
            worldIn.setBlockState(pos, state.withProperty((IProperty)AGE, Integer.valueOf(0)), 4);
          } else {
            worldIn.setBlockState(pos, state.withProperty((IProperty)AGE, Integer.valueOf(j + 1)), 4);
          } 
        } 
      }  
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    Block block = worldIn.getBlockState(pos.down()).getBlock();
    if (block == this)
      return true; 
    if (block != Blocks.grass && block != Blocks.dirt && block != Blocks.sand)
      return false; 
    for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
      if (worldIn.getBlockState(pos.offset(enumfacing).down()).getBlock().getMaterial() == Material.water)
        return true; 
    } 
    return false;
  }
  
  public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    checkForDrop(worldIn, pos, state);
  }
  
  protected final boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {
    if (canBlockStay(worldIn, pos))
      return true; 
    dropBlockAsItem(worldIn, pos, state, 0);
    worldIn.setBlockToAir(pos);
    return false;
  }
  
  public boolean canBlockStay(World worldIn, BlockPos pos) {
    return canPlaceBlockAt(worldIn, pos);
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return null;
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Items.reeds;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    return Items.reeds;
  }
  
  public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
    return worldIn.getBiomeGenForCoords(pos).getGrassColorAtPos(pos);
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.CUTOUT;
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)AGE, Integer.valueOf(meta));
  }
  
  public int getMetaFromState(IBlockState state) {
    return ((Integer)state.getValue((IProperty)AGE)).intValue();
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)AGE });
  }
}
