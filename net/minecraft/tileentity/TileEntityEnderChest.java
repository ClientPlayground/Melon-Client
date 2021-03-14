package net.minecraft.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;

public class TileEntityEnderChest extends TileEntity implements ITickable {
  public float lidAngle;
  
  public float prevLidAngle;
  
  public int numPlayersUsing;
  
  private int ticksSinceSync;
  
  public void update() {
    if (++this.ticksSinceSync % 20 * 4 == 0)
      this.worldObj.addBlockEvent(this.pos, Blocks.ender_chest, 1, this.numPlayersUsing); 
    this.prevLidAngle = this.lidAngle;
    int i = this.pos.getX();
    int j = this.pos.getY();
    int k = this.pos.getZ();
    float f = 0.1F;
    if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
      double d0 = i + 0.5D;
      double d1 = k + 0.5D;
      this.worldObj.playSoundEffect(d0, j + 0.5D, d1, "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
    } 
    if ((this.numPlayersUsing == 0 && this.lidAngle > 0.0F) || (this.numPlayersUsing > 0 && this.lidAngle < 1.0F)) {
      float f2 = this.lidAngle;
      if (this.numPlayersUsing > 0) {
        this.lidAngle += f;
      } else {
        this.lidAngle -= f;
      } 
      if (this.lidAngle > 1.0F)
        this.lidAngle = 1.0F; 
      float f1 = 0.5F;
      if (this.lidAngle < f1 && f2 >= f1) {
        double d3 = i + 0.5D;
        double d2 = k + 0.5D;
        this.worldObj.playSoundEffect(d3, j + 0.5D, d2, "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
      } 
      if (this.lidAngle < 0.0F)
        this.lidAngle = 0.0F; 
    } 
  }
  
  public boolean receiveClientEvent(int id, int type) {
    if (id == 1) {
      this.numPlayersUsing = type;
      return true;
    } 
    return super.receiveClientEvent(id, type);
  }
  
  public void invalidate() {
    updateContainingBlockInfo();
    super.invalidate();
  }
  
  public void openChest() {
    this.numPlayersUsing++;
    this.worldObj.addBlockEvent(this.pos, Blocks.ender_chest, 1, this.numPlayersUsing);
  }
  
  public void closeChest() {
    this.numPlayersUsing--;
    this.worldObj.addBlockEvent(this.pos, Blocks.ender_chest, 1, this.numPlayersUsing);
  }
  
  public boolean canBeUsed(EntityPlayer p_145971_1_) {
    return (this.worldObj.getTileEntity(this.pos) != this) ? false : ((p_145971_1_.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D));
  }
}
