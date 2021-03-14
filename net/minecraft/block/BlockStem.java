package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStem extends BlockBush implements IGrowable {
  public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
  
  public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>() {
        public boolean apply(EnumFacing p_apply_1_) {
          return (p_apply_1_ != EnumFacing.DOWN);
        }
      });
  
  private final Block crop;
  
  protected BlockStem(Block crop) {
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)AGE, Integer.valueOf(0)).withProperty((IProperty)FACING, (Comparable)EnumFacing.UP));
    this.crop = crop;
    setTickRandomly(true);
    float f = 0.125F;
    setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
    setCreativeTab((CreativeTabs)null);
  }
  
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    state = state.withProperty((IProperty)FACING, (Comparable)EnumFacing.UP);
    for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
      if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() == this.crop) {
        state = state.withProperty((IProperty)FACING, (Comparable)enumfacing);
        break;
      } 
    } 
    return state;
  }
  
  protected boolean canPlaceBlockOn(Block ground) {
    return (ground == Blocks.farmland);
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    super.updateTick(worldIn, pos, state, rand);
    if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
      float f = BlockCrops.getGrowthChance(this, worldIn, pos);
      if (rand.nextInt((int)(25.0F / f) + 1) == 0) {
        int i = ((Integer)state.getValue((IProperty)AGE)).intValue();
        if (i < 7) {
          state = state.withProperty((IProperty)AGE, Integer.valueOf(i + 1));
          worldIn.setBlockState(pos, state, 2);
        } else {
          for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() == this.crop)
              return; 
          } 
          pos = pos.offset(EnumFacing.Plane.HORIZONTAL.random(rand));
          Block block = worldIn.getBlockState(pos.down()).getBlock();
          if ((worldIn.getBlockState(pos).getBlock()).blockMaterial == Material.air && (block == Blocks.farmland || block == Blocks.dirt || block == Blocks.grass))
            worldIn.setBlockState(pos, this.crop.getDefaultState()); 
        } 
      } 
    } 
  }
  
  public void growStem(World worldIn, BlockPos pos, IBlockState state) {
    int i = ((Integer)state.getValue((IProperty)AGE)).intValue() + MathHelper.getRandomIntegerInRange(worldIn.rand, 2, 5);
    worldIn.setBlockState(pos, state.withProperty((IProperty)AGE, Integer.valueOf(Math.min(7, i))), 2);
  }
  
  public int getRenderColor(IBlockState state) {
    if (state.getBlock() != this)
      return super.getRenderColor(state); 
    int i = ((Integer)state.getValue((IProperty)AGE)).intValue();
    int j = i * 32;
    int k = 255 - i * 8;
    int l = i * 4;
    return j << 16 | k << 8 | l;
  }
  
  public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
    return getRenderColor(worldIn.getBlockState(pos));
  }
  
  public void setBlockBoundsForItemRender() {
    float f = 0.125F;
    setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    this.maxY = ((((Integer)worldIn.getBlockState(pos).getValue((IProperty)AGE)).intValue() * 2 + 2) / 16.0F);
    float f = 0.125F;
    setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, (float)this.maxY, 0.5F + f);
  }
  
  public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    if (!worldIn.isRemote) {
      Item item = getSeedItem();
      if (item != null) {
        int i = ((Integer)state.getValue((IProperty)AGE)).intValue();
        for (int j = 0; j < 3; j++) {
          if (worldIn.rand.nextInt(15) <= i)
            spawnAsEntity(worldIn, pos, new ItemStack(item)); 
        } 
      } 
    } 
  }
  
  protected Item getSeedItem() {
    return (this.crop == Blocks.pumpkin) ? Items.pumpkin_seeds : ((this.crop == Blocks.melon_block) ? Items.melon_seeds : null);
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return null;
  }
  
  public Item getItem(World worldIn, BlockPos pos) {
    Item item = getSeedItem();
    return (item != null) ? item : null;
  }
  
  public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
    return (((Integer)state.getValue((IProperty)AGE)).intValue() != 7);
  }
  
  public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
    return true;
  }
  
  public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
    growStem(worldIn, pos, state);
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)AGE, Integer.valueOf(meta));
  }
  
  public int getMetaFromState(IBlockState state) {
    return ((Integer)state.getValue((IProperty)AGE)).intValue();
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)AGE, (IProperty)FACING });
  }
}
