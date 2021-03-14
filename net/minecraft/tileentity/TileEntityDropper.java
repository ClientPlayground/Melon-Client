package net.minecraft.tileentity;

public class TileEntityDropper extends TileEntityDispenser {
  public String getCommandSenderName() {
    return hasCustomName() ? this.customName : "container.dropper";
  }
  
  public String getGuiID() {
    return "minecraft:dropper";
  }
}
