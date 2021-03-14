package net.minecraft.world.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerFileData {
  void writePlayerData(EntityPlayer paramEntityPlayer);
  
  NBTTagCompound readPlayerData(EntityPlayer paramEntityPlayer);
  
  String[] getAvailablePlayerDat();
}
