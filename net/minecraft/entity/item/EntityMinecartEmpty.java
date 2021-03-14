package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityMinecartEmpty extends EntityMinecart {
  public EntityMinecartEmpty(World worldIn) {
    super(worldIn);
  }
  
  public EntityMinecartEmpty(World worldIn, double x, double y, double z) {
    super(worldIn, x, y, z);
  }
  
  public boolean interactFirst(EntityPlayer playerIn) {
    if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != playerIn)
      return true; 
    if (this.riddenByEntity != null && this.riddenByEntity != playerIn)
      return false; 
    if (!this.worldObj.isRemote)
      playerIn.mountEntity(this); 
    return true;
  }
  
  public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
    if (receivingPower) {
      if (this.riddenByEntity != null)
        this.riddenByEntity.mountEntity((Entity)null); 
      if (getRollingAmplitude() == 0) {
        setRollingDirection(-getRollingDirection());
        setRollingAmplitude(10);
        setDamage(50.0F);
        setBeenAttacked();
      } 
    } 
  }
  
  public EntityMinecart.EnumMinecartType getMinecartType() {
    return EntityMinecart.EnumMinecartType.RIDEABLE;
  }
}
