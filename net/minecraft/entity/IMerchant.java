package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public interface IMerchant {
  void setCustomer(EntityPlayer paramEntityPlayer);
  
  EntityPlayer getCustomer();
  
  MerchantRecipeList getRecipes(EntityPlayer paramEntityPlayer);
  
  void setRecipes(MerchantRecipeList paramMerchantRecipeList);
  
  void useRecipe(MerchantRecipe paramMerchantRecipe);
  
  void verifySellingItem(ItemStack paramItemStack);
  
  IChatComponent getDisplayName();
}
