package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

public class BlockWeb extends Block {
  public BlockWeb() {
    super(Material.web);
    setCreativeTab(CreativeTabs.tabDecorations);
  }
  
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    entityIn.setInWeb();
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return null;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Items.string;
  }
  
  protected boolean canSilkHarvest() {
    return true;
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.CUTOUT;
  }
}
