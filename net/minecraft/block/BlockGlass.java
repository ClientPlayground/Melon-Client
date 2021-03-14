package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumWorldBlockLayer;

public class BlockGlass extends BlockBreakable {
  public BlockGlass(Material materialIn, boolean ignoreSimilarity) {
    super(materialIn, ignoreSimilarity);
    setCreativeTab(CreativeTabs.tabBlock);
  }
  
  public int quantityDropped(Random random) {
    return 0;
  }
  
  public EnumWorldBlockLayer getBlockLayer() {
    return EnumWorldBlockLayer.CUTOUT;
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  protected boolean canSilkHarvest() {
    return true;
  }
}
