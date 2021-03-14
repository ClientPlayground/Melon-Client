package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockRedstoneOre extends Block {
  private final boolean isOn;
  
  public BlockRedstoneOre(boolean isOn) {
    super(Material.rock);
    if (isOn)
      setTickRandomly(true); 
    this.isOn = isOn;
  }
  
  public int tickRate(World worldIn) {
    return 30;
  }
  
  public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
    activate(worldIn, pos);
    super.onBlockClicked(worldIn, pos, playerIn);
  }
  
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn) {
    activate(worldIn, pos);
    super.onEntityCollidedWithBlock(worldIn, pos, entityIn);
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    activate(worldIn, pos);
    return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
  }
  
  private void activate(World worldIn, BlockPos pos) {
    spawnParticles(worldIn, pos);
    if (this == Blocks.redstone_ore)
      worldIn.setBlockState(pos, Blocks.lit_redstone_ore.getDefaultState()); 
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (this == Blocks.lit_redstone_ore)
      worldIn.setBlockState(pos, Blocks.redstone_ore.getDefaultState()); 
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Items.redstone;
  }
  
  public int quantityDroppedWithBonus(int fortune, Random random) {
    return quantityDropped(random) + random.nextInt(fortune + 1);
  }
  
  public int quantityDropped(Random random) {
    return 4 + random.nextInt(2);
  }
  
  public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    if (getItemDropped(state, worldIn.rand, fortune) != Item.getItemFromBlock(this)) {
      int i = 1 + worldIn.rand.nextInt(5);
      dropXpOnBlockBreak(worldIn, pos, i);
    } 
  }
  
  public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (this.isOn)
      spawnParticles(worldIn, pos); 
  }
  
  private void spawnParticles(World worldIn, BlockPos pos) {
    Random random = worldIn.rand;
    double d0 = 0.0625D;
    for (int i = 0; i < 6; i++) {
      double d1 = (pos.getX() + random.nextFloat());
      double d2 = (pos.getY() + random.nextFloat());
      double d3 = (pos.getZ() + random.nextFloat());
      if (i == 0 && !worldIn.getBlockState(pos.up()).getBlock().isOpaqueCube())
        d2 = pos.getY() + d0 + 1.0D; 
      if (i == 1 && !worldIn.getBlockState(pos.down()).getBlock().isOpaqueCube())
        d2 = pos.getY() - d0; 
      if (i == 2 && !worldIn.getBlockState(pos.south()).getBlock().isOpaqueCube())
        d3 = pos.getZ() + d0 + 1.0D; 
      if (i == 3 && !worldIn.getBlockState(pos.north()).getBlock().isOpaqueCube())
        d3 = pos.getZ() - d0; 
      if (i == 4 && !worldIn.getBlockState(pos.east()).getBlock().isOpaqueCube())
        d1 = pos.getX() + d0 + 1.0D; 
      if (i == 5 && !worldIn.getBlockState(pos.west()).getBlock().isOpaqueCube())
        d1 = pos.getX() - d0; 
      if (d1 < pos.getX() || d1 > (pos.getX() + 1) || d2 < 0.0D || d2 > (pos.getY() + 1) || d3 < pos.getZ() || d3 > (pos.getZ() + 1))
        worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D, new int[0]); 
    } 
  }
  
  protected ItemStack createStackedBlock(IBlockState state) {
    return new ItemStack(Blocks.redstone_ore);
  }
}
