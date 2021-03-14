package net.minecraft.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BehaviorDefaultDispenseItem implements IBehaviorDispenseItem {
  public final ItemStack dispense(IBlockSource source, ItemStack stack) {
    ItemStack itemstack = dispenseStack(source, stack);
    playDispenseSound(source);
    spawnDispenseParticles(source, BlockDispenser.getFacing(source.getBlockMetadata()));
    return itemstack;
  }
  
  protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
    EnumFacing enumfacing = BlockDispenser.getFacing(source.getBlockMetadata());
    IPosition iposition = BlockDispenser.getDispensePosition(source);
    ItemStack itemstack = stack.splitStack(1);
    doDispense(source.getWorld(), itemstack, 6, enumfacing, iposition);
    return stack;
  }
  
  public static void doDispense(World worldIn, ItemStack stack, int speed, EnumFacing facing, IPosition position) {
    double d0 = position.getX();
    double d1 = position.getY();
    double d2 = position.getZ();
    if (facing.getAxis() == EnumFacing.Axis.Y) {
      d1 -= 0.125D;
    } else {
      d1 -= 0.15625D;
    } 
    EntityItem entityitem = new EntityItem(worldIn, d0, d1, d2, stack);
    double d3 = worldIn.rand.nextDouble() * 0.1D + 0.2D;
    entityitem.motionX = facing.getFrontOffsetX() * d3;
    entityitem.motionY = 0.20000000298023224D;
    entityitem.motionZ = facing.getFrontOffsetZ() * d3;
    entityitem.motionX += worldIn.rand.nextGaussian() * 0.007499999832361937D * speed;
    entityitem.motionY += worldIn.rand.nextGaussian() * 0.007499999832361937D * speed;
    entityitem.motionZ += worldIn.rand.nextGaussian() * 0.007499999832361937D * speed;
    worldIn.spawnEntityInWorld((Entity)entityitem);
  }
  
  protected void playDispenseSound(IBlockSource source) {
    source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
  }
  
  protected void spawnDispenseParticles(IBlockSource source, EnumFacing facingIn) {
    source.getWorld().playAuxSFX(2000, source.getBlockPos(), func_82488_a(facingIn));
  }
  
  private int func_82488_a(EnumFacing facingIn) {
    return facingIn.getFrontOffsetX() + 1 + (facingIn.getFrontOffsetZ() + 1) * 3;
  }
}
