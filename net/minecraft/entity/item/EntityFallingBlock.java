package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFallingBlock extends Entity {
  private IBlockState fallTile;
  
  public int fallTime;
  
  public boolean shouldDropItem = true;
  
  private boolean canSetAsBlock;
  
  private boolean hurtEntities;
  
  private int fallHurtMax = 40;
  
  private float fallHurtAmount = 2.0F;
  
  public NBTTagCompound tileEntityData;
  
  public EntityFallingBlock(World worldIn) {
    super(worldIn);
  }
  
  public EntityFallingBlock(World worldIn, double x, double y, double z, IBlockState fallingBlockState) {
    super(worldIn);
    this.fallTile = fallingBlockState;
    this.preventEntitySpawning = true;
    setSize(0.98F, 0.98F);
    setPosition(x, y, z);
    this.motionX = 0.0D;
    this.motionY = 0.0D;
    this.motionZ = 0.0D;
    this.prevPosX = x;
    this.prevPosY = y;
    this.prevPosZ = z;
  }
  
  protected boolean canTriggerWalking() {
    return false;
  }
  
  protected void entityInit() {}
  
  public boolean canBeCollidedWith() {
    return !this.isDead;
  }
  
  public void onUpdate() {
    Block block = this.fallTile.getBlock();
    if (block.getMaterial() == Material.air) {
      setDead();
    } else {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.fallTime++ == 0) {
        BlockPos blockpos = new BlockPos(this);
        if (this.worldObj.getBlockState(blockpos).getBlock() == block) {
          this.worldObj.setBlockToAir(blockpos);
        } else if (!this.worldObj.isRemote) {
          setDead();
          return;
        } 
      } 
      this.motionY -= 0.03999999910593033D;
      moveEntity(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863D;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= 0.9800000190734863D;
      if (!this.worldObj.isRemote) {
        BlockPos blockpos1 = new BlockPos(this);
        if (this.onGround) {
          this.motionX *= 0.699999988079071D;
          this.motionZ *= 0.699999988079071D;
          this.motionY *= -0.5D;
          if (this.worldObj.getBlockState(blockpos1).getBlock() != Blocks.piston_extension) {
            setDead();
            if (!this.canSetAsBlock)
              if (this.worldObj.canBlockBePlaced(block, blockpos1, true, EnumFacing.UP, (Entity)null, (ItemStack)null) && !BlockFalling.canFallInto(this.worldObj, blockpos1.down()) && this.worldObj.setBlockState(blockpos1, this.fallTile, 3)) {
                if (block instanceof BlockFalling)
                  ((BlockFalling)block).onEndFalling(this.worldObj, blockpos1); 
                if (this.tileEntityData != null && block instanceof net.minecraft.block.ITileEntityProvider) {
                  TileEntity tileentity = this.worldObj.getTileEntity(blockpos1);
                  if (tileentity != null) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    tileentity.writeToNBT(nbttagcompound);
                    for (String s : this.tileEntityData.getKeySet()) {
                      NBTBase nbtbase = this.tileEntityData.getTag(s);
                      if (!s.equals("x") && !s.equals("y") && !s.equals("z"))
                        nbttagcompound.setTag(s, nbtbase.copy()); 
                    } 
                    tileentity.readFromNBT(nbttagcompound);
                    tileentity.markDirty();
                  } 
                } 
              } else if (this.shouldDropItem && this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops")) {
                entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0F);
              }  
          } 
        } else if ((this.fallTime > 100 && !this.worldObj.isRemote && (blockpos1.getY() < 1 || blockpos1.getY() > 256)) || this.fallTime > 600) {
          if (this.shouldDropItem && this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops"))
            entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0F); 
          setDead();
        } 
      } 
    } 
  }
  
  public void fall(float distance, float damageMultiplier) {
    Block block = this.fallTile.getBlock();
    if (this.hurtEntities) {
      int i = MathHelper.ceiling_float_int(distance - 1.0F);
      if (i > 0) {
        List<Entity> list = Lists.newArrayList(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox()));
        boolean flag = (block == Blocks.anvil);
        DamageSource damagesource = flag ? DamageSource.anvil : DamageSource.fallingBlock;
        for (Entity entity : list)
          entity.attackEntityFrom(damagesource, Math.min(MathHelper.floor_float(i * this.fallHurtAmount), this.fallHurtMax)); 
        if (flag && this.rand.nextFloat() < 0.05000000074505806D + i * 0.05D) {
          int j = ((Integer)this.fallTile.getValue((IProperty)BlockAnvil.DAMAGE)).intValue();
          j++;
          if (j > 2) {
            this.canSetAsBlock = true;
          } else {
            this.fallTile = this.fallTile.withProperty((IProperty)BlockAnvil.DAMAGE, Integer.valueOf(j));
          } 
        } 
      } 
    } 
  }
  
  protected void writeEntityToNBT(NBTTagCompound tagCompound) {
    Block block = (this.fallTile != null) ? this.fallTile.getBlock() : Blocks.air;
    ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(block);
    tagCompound.setString("Block", (resourcelocation == null) ? "" : resourcelocation.toString());
    tagCompound.setByte("Data", (byte)block.getMetaFromState(this.fallTile));
    tagCompound.setByte("Time", (byte)this.fallTime);
    tagCompound.setBoolean("DropItem", this.shouldDropItem);
    tagCompound.setBoolean("HurtEntities", this.hurtEntities);
    tagCompound.setFloat("FallHurtAmount", this.fallHurtAmount);
    tagCompound.setInteger("FallHurtMax", this.fallHurtMax);
    if (this.tileEntityData != null)
      tagCompound.setTag("TileEntityData", (NBTBase)this.tileEntityData); 
  }
  
  protected void readEntityFromNBT(NBTTagCompound tagCompund) {
    int i = tagCompund.getByte("Data") & 0xFF;
    if (tagCompund.hasKey("Block", 8)) {
      this.fallTile = Block.getBlockFromName(tagCompund.getString("Block")).getStateFromMeta(i);
    } else if (tagCompund.hasKey("TileID", 99)) {
      this.fallTile = Block.getBlockById(tagCompund.getInteger("TileID")).getStateFromMeta(i);
    } else {
      this.fallTile = Block.getBlockById(tagCompund.getByte("Tile") & 0xFF).getStateFromMeta(i);
    } 
    this.fallTime = tagCompund.getByte("Time") & 0xFF;
    Block block = this.fallTile.getBlock();
    if (tagCompund.hasKey("HurtEntities", 99)) {
      this.hurtEntities = tagCompund.getBoolean("HurtEntities");
      this.fallHurtAmount = tagCompund.getFloat("FallHurtAmount");
      this.fallHurtMax = tagCompund.getInteger("FallHurtMax");
    } else if (block == Blocks.anvil) {
      this.hurtEntities = true;
    } 
    if (tagCompund.hasKey("DropItem", 99))
      this.shouldDropItem = tagCompund.getBoolean("DropItem"); 
    if (tagCompund.hasKey("TileEntityData", 10))
      this.tileEntityData = tagCompund.getCompoundTag("TileEntityData"); 
    if (block == null || block.getMaterial() == Material.air)
      this.fallTile = Blocks.sand.getDefaultState(); 
  }
  
  public World getWorldObj() {
    return this.worldObj;
  }
  
  public void setHurtEntities(boolean p_145806_1_) {
    this.hurtEntities = p_145806_1_;
  }
  
  public boolean canRenderOnFire() {
    return false;
  }
  
  public void addEntityCrashInfo(CrashReportCategory category) {
    super.addEntityCrashInfo(category);
    if (this.fallTile != null) {
      Block block = this.fallTile.getBlock();
      category.addCrashSection("Immitating block ID", Integer.valueOf(Block.getIdFromBlock(block)));
      category.addCrashSection("Immitating block data", Integer.valueOf(block.getMetaFromState(this.fallTile)));
    } 
  }
  
  public IBlockState getBlock() {
    return this.fallTile;
  }
}
