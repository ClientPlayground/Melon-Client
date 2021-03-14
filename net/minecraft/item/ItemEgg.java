package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;

public class ItemEgg extends Item {
  public ItemEgg() {
    this.maxStackSize = 16;
    setCreativeTab(CreativeTabs.tabMaterials);
  }
  
  public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
    if (!playerIn.capabilities.isCreativeMode)
      itemStackIn.stackSize--; 
    worldIn.playSoundAtEntity((Entity)playerIn, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
    if (!worldIn.isRemote)
      worldIn.spawnEntityInWorld((Entity)new EntityEgg(worldIn, (EntityLivingBase)playerIn)); 
    playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
    return itemStackIn;
  }
}
