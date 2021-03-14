package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemFirework extends Item {
  public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!worldIn.isRemote) {
      EntityFireworkRocket entityfireworkrocket = new EntityFireworkRocket(worldIn, (pos.getX() + hitX), (pos.getY() + hitY), (pos.getZ() + hitZ), stack);
      worldIn.spawnEntityInWorld((Entity)entityfireworkrocket);
      if (!playerIn.capabilities.isCreativeMode)
        stack.stackSize--; 
      return true;
    } 
    return false;
  }
  
  public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
    if (stack.hasTagCompound()) {
      NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("Fireworks");
      if (nbttagcompound != null) {
        if (nbttagcompound.hasKey("Flight", 99))
          tooltip.add(StatCollector.translateToLocal("item.fireworks.flight") + " " + nbttagcompound.getByte("Flight")); 
        NBTTagList nbttaglist = nbttagcompound.getTagList("Explosions", 10);
        if (nbttaglist != null && nbttaglist.tagCount() > 0)
          for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            List<String> list = Lists.newArrayList();
            ItemFireworkCharge.addExplosionInfo(nbttagcompound1, list);
            if (list.size() > 0) {
              for (int j = 1; j < list.size(); j++)
                list.set(j, "  " + (String)list.get(j)); 
              tooltip.addAll(list);
            } 
          }  
      } 
    } 
  }
}
