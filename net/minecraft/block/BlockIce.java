package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class BlockIce extends BlockBreakable {
  public BlockIce() {
    super(Material.ice, false);
    this.slipperiness = 0.98F;
    setTickRandomly(true);
    setCreativeTab(CreativeTabs.tabBlock);
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.TRANSLUCENT;
  }
  
  public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
    player.triggerAchievement(StatList.mineBlockStatArray[Block.getIdFromBlock(this)]);
    player.addExhaustion(0.025F);
    if (canSilkHarvest() && EnchantmentHelper.getSilkTouchModifier((EntityLivingBase)player)) {
      ItemStack itemstack = createStackedBlock(state);
      if (itemstack != null)
        spawnAsEntity(worldIn, pos, itemstack); 
    } else {
      if (worldIn.provider.doesWaterVaporize()) {
        worldIn.setBlockToAir(pos);
        return;
      } 
      int i = EnchantmentHelper.getFortuneModifier((EntityLivingBase)player);
      dropBlockAsItem(worldIn, pos, state, i);
      Material material = worldIn.getBlockState(pos.down()).getBlock().getMaterial();
      if (material.blocksMovement() || material.isLiquid())
        worldIn.setBlockState(pos, Blocks.flowing_water.getDefaultState()); 
    } 
  }
  
  public int quantityDropped(Random random) {
    return 0;
  }
  
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11 - getLightOpacity())
      if (worldIn.provider.doesWaterVaporize()) {
        worldIn.setBlockToAir(pos);
      } else {
        dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
        worldIn.setBlockState(pos, Blocks.water.getDefaultState());
      }  
  }
  
  public int getMobilityFlag() {
    return 0;
  }
}
