package net.minecraft.item;

import net.minecraft.block.Block;

public class ItemCloth extends ItemBlock {
  public ItemCloth(Block block) {
    super(block);
    setMaxDamage(0);
    setHasSubtypes(true);
  }
  
  public int getMetadata(int damage) {
    return damage;
  }
  
  public String getUnlocalizedName(ItemStack stack) {
    return getUnlocalizedName() + "." + EnumDyeColor.byMetadata(stack.getMetadata()).getUnlocalizedName();
  }
}
