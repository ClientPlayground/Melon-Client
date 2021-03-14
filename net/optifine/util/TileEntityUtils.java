package net.optifine.util;

import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.optifine.reflect.Reflector;

public class TileEntityUtils {
  public static String getTileEntityName(IBlockAccess blockAccess, BlockPos blockPos) {
    TileEntity tileentity = blockAccess.getTileEntity(blockPos);
    return getTileEntityName(tileentity);
  }
  
  public static String getTileEntityName(TileEntity te) {
    if (!(te instanceof IWorldNameable))
      return null; 
    IWorldNameable iworldnameable = (IWorldNameable)te;
    updateTileEntityName(te);
    return !iworldnameable.hasCustomName() ? null : iworldnameable.getCommandSenderName();
  }
  
  public static void updateTileEntityName(TileEntity te) {
    BlockPos blockpos = te.getPos();
    String s = getTileEntityRawName(te);
    if (s == null) {
      String s1 = getServerTileEntityRawName(blockpos);
      s1 = Config.normalize(s1);
      setTileEntityRawName(te, s1);
    } 
  }
  
  public static String getServerTileEntityRawName(BlockPos blockPos) {
    TileEntity tileentity = IntegratedServerUtils.getTileEntity(blockPos);
    return (tileentity == null) ? null : getTileEntityRawName(tileentity);
  }
  
  public static String getTileEntityRawName(TileEntity te) {
    if (te instanceof net.minecraft.tileentity.TileEntityBeacon)
      return (String)Reflector.getFieldValue(te, Reflector.TileEntityBeacon_customName); 
    if (te instanceof net.minecraft.tileentity.TileEntityBrewingStand)
      return (String)Reflector.getFieldValue(te, Reflector.TileEntityBrewingStand_customName); 
    if (te instanceof net.minecraft.tileentity.TileEntityEnchantmentTable)
      return (String)Reflector.getFieldValue(te, Reflector.TileEntityEnchantmentTable_customName); 
    if (te instanceof net.minecraft.tileentity.TileEntityFurnace)
      return (String)Reflector.getFieldValue(te, Reflector.TileEntityFurnace_customName); 
    if (te instanceof IWorldNameable) {
      IWorldNameable iworldnameable = (IWorldNameable)te;
      if (iworldnameable.hasCustomName())
        return iworldnameable.getCommandSenderName(); 
    } 
    return null;
  }
  
  public static boolean setTileEntityRawName(TileEntity te, String name) {
    if (te instanceof net.minecraft.tileentity.TileEntityBeacon)
      return Reflector.setFieldValue(te, Reflector.TileEntityBeacon_customName, name); 
    if (te instanceof net.minecraft.tileentity.TileEntityBrewingStand)
      return Reflector.setFieldValue(te, Reflector.TileEntityBrewingStand_customName, name); 
    if (te instanceof net.minecraft.tileentity.TileEntityEnchantmentTable)
      return Reflector.setFieldValue(te, Reflector.TileEntityEnchantmentTable_customName, name); 
    if (te instanceof net.minecraft.tileentity.TileEntityFurnace)
      return Reflector.setFieldValue(te, Reflector.TileEntityFurnace_customName, name); 
    if (te instanceof TileEntityChest) {
      ((TileEntityChest)te).setCustomName(name);
      return true;
    } 
    if (te instanceof TileEntityDispenser) {
      ((TileEntityDispenser)te).setCustomName(name);
      return true;
    } 
    if (te instanceof TileEntityHopper) {
      ((TileEntityHopper)te).setCustomName(name);
      return true;
    } 
    return false;
  }
}
