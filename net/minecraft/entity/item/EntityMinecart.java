package net.minecraft.entity.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class EntityMinecart extends Entity implements IWorldNameable {
  private boolean isInReverse;
  
  private String entityName;
  
  private static final int[][][] matrix = new int[][][] { { { 0, 0, -1 }, { 0, 0, 1 } }, { { -1, 0, 0 }, { 1, 0, 0 } }, { { -1, -1, 0 }, { 1, 0, 0 } }, { { -1, 0, 0 }, { 1, -1, 0 } }, { { 0, 0, -1 }, { 0, -1, 1 } }, { { 0, -1, -1 }, { 0, 0, 1 } }, { { 0, 0, 1 }, { 1, 0, 0 } }, { { 0, 0, 1 }, { -1, 0, 0 } }, { { 0, 0, -1 }, { -1, 0, 0 } }, { { 0, 0, -1 }, { 1, 0, 0 } } };
  
  private int turnProgress;
  
  private double minecartX;
  
  private double minecartY;
  
  private double minecartZ;
  
  private double minecartYaw;
  
  private double minecartPitch;
  
  private double velocityX;
  
  private double velocityY;
  
  private double velocityZ;
  
  public EntityMinecart(World worldIn) {
    super(worldIn);
    this.preventEntitySpawning = true;
    setSize(0.98F, 0.7F);
  }
  
  public static EntityMinecart getMinecart(World worldIn, double x, double y, double z, EnumMinecartType type) {
    switch (type) {
      case ASCENDING_EAST:
        return new EntityMinecartChest(worldIn, x, y, z);
      case ASCENDING_WEST:
        return new EntityMinecartFurnace(worldIn, x, y, z);
      case ASCENDING_NORTH:
        return new EntityMinecartTNT(worldIn, x, y, z);
      case ASCENDING_SOUTH:
        return (EntityMinecart)new EntityMinecartMobSpawner(worldIn, x, y, z);
      case null:
        return new EntityMinecartHopper(worldIn, x, y, z);
      case null:
        return (EntityMinecart)new EntityMinecartCommandBlock(worldIn, x, y, z);
    } 
    return new EntityMinecartEmpty(worldIn, x, y, z);
  }
  
  protected boolean canTriggerWalking() {
    return false;
  }
  
  protected void entityInit() {
    this.dataWatcher.addObject(17, new Integer(0));
    this.dataWatcher.addObject(18, new Integer(1));
    this.dataWatcher.addObject(19, new Float(0.0F));
    this.dataWatcher.addObject(20, new Integer(0));
    this.dataWatcher.addObject(21, new Integer(6));
    this.dataWatcher.addObject(22, Byte.valueOf((byte)0));
  }
  
  public AxisAlignedBB getCollisionBox(Entity entityIn) {
    return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
  }
  
  public AxisAlignedBB getCollisionBoundingBox() {
    return null;
  }
  
  public boolean canBePushed() {
    return true;
  }
  
  public EntityMinecart(World worldIn, double x, double y, double z) {
    this(worldIn);
    setPosition(x, y, z);
    this.motionX = 0.0D;
    this.motionY = 0.0D;
    this.motionZ = 0.0D;
    this.prevPosX = x;
    this.prevPosY = y;
    this.prevPosZ = z;
  }
  
  public double getMountedYOffset() {
    return 0.0D;
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (!this.worldObj.isRemote && !this.isDead) {
      if (isEntityInvulnerable(source))
        return false; 
      setRollingDirection(-getRollingDirection());
      setRollingAmplitude(10);
      setBeenAttacked();
      setDamage(getDamage() + amount * 10.0F);
      boolean flag = (source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode);
      if (flag || getDamage() > 40.0F) {
        if (this.riddenByEntity != null)
          this.riddenByEntity.mountEntity((Entity)null); 
        if (flag && !hasCustomName()) {
          setDead();
        } else {
          killMinecart(source);
        } 
      } 
      return true;
    } 
    return true;
  }
  
  public void killMinecart(DamageSource source) {
    setDead();
    if (this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops")) {
      ItemStack itemstack = new ItemStack(Items.minecart, 1);
      if (this.entityName != null)
        itemstack.setStackDisplayName(this.entityName); 
      entityDropItem(itemstack, 0.0F);
    } 
  }
  
  public void performHurtAnimation() {
    setRollingDirection(-getRollingDirection());
    setRollingAmplitude(10);
    setDamage(getDamage() + getDamage() * 10.0F);
  }
  
  public boolean canBeCollidedWith() {
    return !this.isDead;
  }
  
  public void setDead() {
    super.setDead();
  }
  
  public void onUpdate() {
    if (getRollingAmplitude() > 0)
      setRollingAmplitude(getRollingAmplitude() - 1); 
    if (getDamage() > 0.0F)
      setDamage(getDamage() - 1.0F); 
    if (this.posY < -64.0D)
      kill(); 
    if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer) {
      this.worldObj.theProfiler.startSection("portal");
      MinecraftServer minecraftserver = ((WorldServer)this.worldObj).getMinecraftServer();
      int i = getMaxInPortalTime();
      if (this.inPortal) {
        if (minecraftserver.getAllowNether()) {
          if (this.ridingEntity == null && this.portalCounter++ >= i) {
            int j;
            this.portalCounter = i;
            this.timeUntilPortal = getPortalCooldown();
            if (this.worldObj.provider.getDimensionId() == -1) {
              j = 0;
            } else {
              j = -1;
            } 
            travelToDimension(j);
          } 
          this.inPortal = false;
        } 
      } else {
        if (this.portalCounter > 0)
          this.portalCounter -= 4; 
        if (this.portalCounter < 0)
          this.portalCounter = 0; 
      } 
      if (this.timeUntilPortal > 0)
        this.timeUntilPortal--; 
      this.worldObj.theProfiler.endSection();
    } 
    if (this.worldObj.isRemote) {
      if (this.turnProgress > 0) {
        double d4 = this.posX + (this.minecartX - this.posX) / this.turnProgress;
        double d5 = this.posY + (this.minecartY - this.posY) / this.turnProgress;
        double d6 = this.posZ + (this.minecartZ - this.posZ) / this.turnProgress;
        double d1 = MathHelper.wrapAngleTo180_double(this.minecartYaw - this.rotationYaw);
        this.rotationYaw = (float)(this.rotationYaw + d1 / this.turnProgress);
        this.rotationPitch = (float)(this.rotationPitch + (this.minecartPitch - this.rotationPitch) / this.turnProgress);
        this.turnProgress--;
        setPosition(d4, d5, d6);
        setRotation(this.rotationYaw, this.rotationPitch);
      } else {
        setPosition(this.posX, this.posY, this.posZ);
        setRotation(this.rotationYaw, this.rotationPitch);
      } 
    } else {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.motionY -= 0.03999999910593033D;
      int k = MathHelper.floor_double(this.posX);
      int l = MathHelper.floor_double(this.posY);
      int i1 = MathHelper.floor_double(this.posZ);
      if (BlockRailBase.isRailBlock(this.worldObj, new BlockPos(k, l - 1, i1)))
        l--; 
      BlockPos blockpos = new BlockPos(k, l, i1);
      IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
      if (BlockRailBase.isRailBlock(iblockstate)) {
        func_180460_a(blockpos, iblockstate);
        if (iblockstate.getBlock() == Blocks.activator_rail)
          onActivatorRailPass(k, l, i1, ((Boolean)iblockstate.getValue((IProperty)BlockRailPowered.POWERED)).booleanValue()); 
      } else {
        moveDerailedMinecart();
      } 
      doBlockCollisions();
      this.rotationPitch = 0.0F;
      double d0 = this.prevPosX - this.posX;
      double d2 = this.prevPosZ - this.posZ;
      if (d0 * d0 + d2 * d2 > 0.001D) {
        this.rotationYaw = (float)(MathHelper.atan2(d2, d0) * 180.0D / Math.PI);
        if (this.isInReverse)
          this.rotationYaw += 180.0F; 
      } 
      double d3 = MathHelper.wrapAngleTo180_float(this.rotationYaw - this.prevRotationYaw);
      if (d3 < -170.0D || d3 >= 170.0D) {
        this.rotationYaw += 180.0F;
        this.isInReverse = !this.isInReverse;
      } 
      setRotation(this.rotationYaw, this.rotationPitch);
      for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D))) {
        if (entity != this.riddenByEntity && entity.canBePushed() && entity instanceof EntityMinecart)
          entity.applyEntityCollision(this); 
      } 
      if (this.riddenByEntity != null && this.riddenByEntity.isDead) {
        if (this.riddenByEntity.ridingEntity == this)
          this.riddenByEntity.ridingEntity = null; 
        this.riddenByEntity = null;
      } 
      handleWaterMovement();
    } 
  }
  
  protected double getMaximumSpeed() {
    return 0.4D;
  }
  
  public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {}
  
  protected void moveDerailedMinecart() {
    double d0 = getMaximumSpeed();
    this.motionX = MathHelper.clamp_double(this.motionX, -d0, d0);
    this.motionZ = MathHelper.clamp_double(this.motionZ, -d0, d0);
    if (this.onGround) {
      this.motionX *= 0.5D;
      this.motionY *= 0.5D;
      this.motionZ *= 0.5D;
    } 
    moveEntity(this.motionX, this.motionY, this.motionZ);
    if (!this.onGround) {
      this.motionX *= 0.949999988079071D;
      this.motionY *= 0.949999988079071D;
      this.motionZ *= 0.949999988079071D;
    } 
  }
  
  protected void func_180460_a(BlockPos p_180460_1_, IBlockState p_180460_2_) {
    this.fallDistance = 0.0F;
    Vec3 vec3 = func_70489_a(this.posX, this.posY, this.posZ);
    this.posY = p_180460_1_.getY();
    boolean flag = false;
    boolean flag1 = false;
    BlockRailBase blockrailbase = (BlockRailBase)p_180460_2_.getBlock();
    if (blockrailbase == Blocks.golden_rail) {
      flag = ((Boolean)p_180460_2_.getValue((IProperty)BlockRailPowered.POWERED)).booleanValue();
      flag1 = !flag;
    } 
    double d0 = 0.0078125D;
    BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)p_180460_2_.getValue(blockrailbase.getShapeProperty());
    switch (blockrailbase$enumraildirection) {
      case ASCENDING_EAST:
        this.motionX -= 0.0078125D;
        this.posY++;
        break;
      case ASCENDING_WEST:
        this.motionX += 0.0078125D;
        this.posY++;
        break;
      case ASCENDING_NORTH:
        this.motionZ += 0.0078125D;
        this.posY++;
        break;
      case ASCENDING_SOUTH:
        this.motionZ -= 0.0078125D;
        this.posY++;
        break;
    } 
    int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
    double d1 = (aint[1][0] - aint[0][0]);
    double d2 = (aint[1][2] - aint[0][2]);
    double d3 = Math.sqrt(d1 * d1 + d2 * d2);
    double d4 = this.motionX * d1 + this.motionZ * d2;
    if (d4 < 0.0D) {
      d1 = -d1;
      d2 = -d2;
    } 
    double d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
    if (d5 > 2.0D)
      d5 = 2.0D; 
    this.motionX = d5 * d1 / d3;
    this.motionZ = d5 * d2 / d3;
    if (this.riddenByEntity instanceof EntityLivingBase) {
      double d6 = ((EntityLivingBase)this.riddenByEntity).moveForward;
      if (d6 > 0.0D) {
        double d7 = -Math.sin((this.riddenByEntity.rotationYaw * 3.1415927F / 180.0F));
        double d8 = Math.cos((this.riddenByEntity.rotationYaw * 3.1415927F / 180.0F));
        double d9 = this.motionX * this.motionX + this.motionZ * this.motionZ;
        if (d9 < 0.01D) {
          this.motionX += d7 * 0.1D;
          this.motionZ += d8 * 0.1D;
          flag1 = false;
        } 
      } 
    } 
    if (flag1) {
      double d17 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      if (d17 < 0.03D) {
        this.motionX *= 0.0D;
        this.motionY *= 0.0D;
        this.motionZ *= 0.0D;
      } else {
        this.motionX *= 0.5D;
        this.motionY *= 0.0D;
        this.motionZ *= 0.5D;
      } 
    } 
    double d18 = 0.0D;
    double d19 = p_180460_1_.getX() + 0.5D + aint[0][0] * 0.5D;
    double d20 = p_180460_1_.getZ() + 0.5D + aint[0][2] * 0.5D;
    double d21 = p_180460_1_.getX() + 0.5D + aint[1][0] * 0.5D;
    double d10 = p_180460_1_.getZ() + 0.5D + aint[1][2] * 0.5D;
    d1 = d21 - d19;
    d2 = d10 - d20;
    if (d1 == 0.0D) {
      this.posX = p_180460_1_.getX() + 0.5D;
      d18 = this.posZ - p_180460_1_.getZ();
    } else if (d2 == 0.0D) {
      this.posZ = p_180460_1_.getZ() + 0.5D;
      d18 = this.posX - p_180460_1_.getX();
    } else {
      double d11 = this.posX - d19;
      double d12 = this.posZ - d20;
      d18 = (d11 * d1 + d12 * d2) * 2.0D;
    } 
    this.posX = d19 + d1 * d18;
    this.posZ = d20 + d2 * d18;
    setPosition(this.posX, this.posY, this.posZ);
    double d22 = this.motionX;
    double d23 = this.motionZ;
    if (this.riddenByEntity != null) {
      d22 *= 0.75D;
      d23 *= 0.75D;
    } 
    double d13 = getMaximumSpeed();
    d22 = MathHelper.clamp_double(d22, -d13, d13);
    d23 = MathHelper.clamp_double(d23, -d13, d13);
    moveEntity(d22, 0.0D, d23);
    if (aint[0][1] != 0 && MathHelper.floor_double(this.posX) - p_180460_1_.getX() == aint[0][0] && MathHelper.floor_double(this.posZ) - p_180460_1_.getZ() == aint[0][2]) {
      setPosition(this.posX, this.posY + aint[0][1], this.posZ);
    } else if (aint[1][1] != 0 && MathHelper.floor_double(this.posX) - p_180460_1_.getX() == aint[1][0] && MathHelper.floor_double(this.posZ) - p_180460_1_.getZ() == aint[1][2]) {
      setPosition(this.posX, this.posY + aint[1][1], this.posZ);
    } 
    applyDrag();
    Vec3 vec31 = func_70489_a(this.posX, this.posY, this.posZ);
    if (vec31 != null && vec3 != null) {
      double d14 = (vec3.yCoord - vec31.yCoord) * 0.05D;
      d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      if (d5 > 0.0D) {
        this.motionX = this.motionX / d5 * (d5 + d14);
        this.motionZ = this.motionZ / d5 * (d5 + d14);
      } 
      setPosition(this.posX, vec31.yCoord, this.posZ);
    } 
    int j = MathHelper.floor_double(this.posX);
    int i = MathHelper.floor_double(this.posZ);
    if (j != p_180460_1_.getX() || i != p_180460_1_.getZ()) {
      d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.motionX = d5 * (j - p_180460_1_.getX());
      this.motionZ = d5 * (i - p_180460_1_.getZ());
    } 
    if (flag) {
      double d15 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      if (d15 > 0.01D) {
        double d16 = 0.06D;
        this.motionX += this.motionX / d15 * d16;
        this.motionZ += this.motionZ / d15 * d16;
      } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST) {
        if (this.worldObj.getBlockState(p_180460_1_.west()).getBlock().isNormalCube()) {
          this.motionX = 0.02D;
        } else if (this.worldObj.getBlockState(p_180460_1_.east()).getBlock().isNormalCube()) {
          this.motionX = -0.02D;
        } 
      } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
        if (this.worldObj.getBlockState(p_180460_1_.north()).getBlock().isNormalCube()) {
          this.motionZ = 0.02D;
        } else if (this.worldObj.getBlockState(p_180460_1_.south()).getBlock().isNormalCube()) {
          this.motionZ = -0.02D;
        } 
      } 
    } 
  }
  
  protected void applyDrag() {
    if (this.riddenByEntity != null) {
      this.motionX *= 0.996999979019165D;
      this.motionY *= 0.0D;
      this.motionZ *= 0.996999979019165D;
    } else {
      this.motionX *= 0.9599999785423279D;
      this.motionY *= 0.0D;
      this.motionZ *= 0.9599999785423279D;
    } 
  }
  
  public void setPosition(double x, double y, double z) {
    this.posX = x;
    this.posY = y;
    this.posZ = z;
    float f = this.width / 2.0F;
    float f1 = this.height;
    setEntityBoundingBox(new AxisAlignedBB(x - f, y, z - f, x + f, y + f1, z + f));
  }
  
  public Vec3 func_70495_a(double p_70495_1_, double p_70495_3_, double p_70495_5_, double p_70495_7_) {
    int i = MathHelper.floor_double(p_70495_1_);
    int j = MathHelper.floor_double(p_70495_3_);
    int k = MathHelper.floor_double(p_70495_5_);
    if (BlockRailBase.isRailBlock(this.worldObj, new BlockPos(i, j - 1, k)))
      j--; 
    IBlockState iblockstate = this.worldObj.getBlockState(new BlockPos(i, j, k));
    if (BlockRailBase.isRailBlock(iblockstate)) {
      BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)iblockstate.getValue(((BlockRailBase)iblockstate.getBlock()).getShapeProperty());
      p_70495_3_ = j;
      if (blockrailbase$enumraildirection.isAscending())
        p_70495_3_ = (j + 1); 
      int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
      double d0 = (aint[1][0] - aint[0][0]);
      double d1 = (aint[1][2] - aint[0][2]);
      double d2 = Math.sqrt(d0 * d0 + d1 * d1);
      d0 /= d2;
      d1 /= d2;
      p_70495_1_ += d0 * p_70495_7_;
      p_70495_5_ += d1 * p_70495_7_;
      if (aint[0][1] != 0 && MathHelper.floor_double(p_70495_1_) - i == aint[0][0] && MathHelper.floor_double(p_70495_5_) - k == aint[0][2]) {
        p_70495_3_ += aint[0][1];
      } else if (aint[1][1] != 0 && MathHelper.floor_double(p_70495_1_) - i == aint[1][0] && MathHelper.floor_double(p_70495_5_) - k == aint[1][2]) {
        p_70495_3_ += aint[1][1];
      } 
      return func_70489_a(p_70495_1_, p_70495_3_, p_70495_5_);
    } 
    return null;
  }
  
  public Vec3 func_70489_a(double p_70489_1_, double p_70489_3_, double p_70489_5_) {
    int i = MathHelper.floor_double(p_70489_1_);
    int j = MathHelper.floor_double(p_70489_3_);
    int k = MathHelper.floor_double(p_70489_5_);
    if (BlockRailBase.isRailBlock(this.worldObj, new BlockPos(i, j - 1, k)))
      j--; 
    IBlockState iblockstate = this.worldObj.getBlockState(new BlockPos(i, j, k));
    if (BlockRailBase.isRailBlock(iblockstate)) {
      BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)iblockstate.getValue(((BlockRailBase)iblockstate.getBlock()).getShapeProperty());
      int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
      double d0 = 0.0D;
      double d1 = i + 0.5D + aint[0][0] * 0.5D;
      double d2 = j + 0.0625D + aint[0][1] * 0.5D;
      double d3 = k + 0.5D + aint[0][2] * 0.5D;
      double d4 = i + 0.5D + aint[1][0] * 0.5D;
      double d5 = j + 0.0625D + aint[1][1] * 0.5D;
      double d6 = k + 0.5D + aint[1][2] * 0.5D;
      double d7 = d4 - d1;
      double d8 = (d5 - d2) * 2.0D;
      double d9 = d6 - d3;
      if (d7 == 0.0D) {
        p_70489_1_ = i + 0.5D;
        d0 = p_70489_5_ - k;
      } else if (d9 == 0.0D) {
        p_70489_5_ = k + 0.5D;
        d0 = p_70489_1_ - i;
      } else {
        double d10 = p_70489_1_ - d1;
        double d11 = p_70489_5_ - d3;
        d0 = (d10 * d7 + d11 * d9) * 2.0D;
      } 
      p_70489_1_ = d1 + d7 * d0;
      p_70489_3_ = d2 + d8 * d0;
      p_70489_5_ = d3 + d9 * d0;
      if (d8 < 0.0D)
        p_70489_3_++; 
      if (d8 > 0.0D)
        p_70489_3_ += 0.5D; 
      return new Vec3(p_70489_1_, p_70489_3_, p_70489_5_);
    } 
    return null;
  }
  
  protected void readEntityFromNBT(NBTTagCompound tagCompund) {
    if (tagCompund.getBoolean("CustomDisplayTile")) {
      int i = tagCompund.getInteger("DisplayData");
      if (tagCompund.hasKey("DisplayTile", 8)) {
        Block block = Block.getBlockFromName(tagCompund.getString("DisplayTile"));
        if (block == null) {
          func_174899_a(Blocks.air.getDefaultState());
        } else {
          func_174899_a(block.getStateFromMeta(i));
        } 
      } else {
        Block block1 = Block.getBlockById(tagCompund.getInteger("DisplayTile"));
        if (block1 == null) {
          func_174899_a(Blocks.air.getDefaultState());
        } else {
          func_174899_a(block1.getStateFromMeta(i));
        } 
      } 
      setDisplayTileOffset(tagCompund.getInteger("DisplayOffset"));
    } 
    if (tagCompund.hasKey("CustomName", 8) && tagCompund.getString("CustomName").length() > 0)
      this.entityName = tagCompund.getString("CustomName"); 
  }
  
  protected void writeEntityToNBT(NBTTagCompound tagCompound) {
    if (hasDisplayTile()) {
      tagCompound.setBoolean("CustomDisplayTile", true);
      IBlockState iblockstate = getDisplayTile();
      ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(iblockstate.getBlock());
      tagCompound.setString("DisplayTile", (resourcelocation == null) ? "" : resourcelocation.toString());
      tagCompound.setInteger("DisplayData", iblockstate.getBlock().getMetaFromState(iblockstate));
      tagCompound.setInteger("DisplayOffset", getDisplayTileOffset());
    } 
    if (this.entityName != null && this.entityName.length() > 0)
      tagCompound.setString("CustomName", this.entityName); 
  }
  
  public void applyEntityCollision(Entity entityIn) {
    if (!this.worldObj.isRemote)
      if (!entityIn.noClip && !this.noClip)
        if (entityIn != this.riddenByEntity) {
          if (entityIn instanceof EntityLivingBase && !(entityIn instanceof EntityPlayer) && !(entityIn instanceof net.minecraft.entity.monster.EntityIronGolem) && getMinecartType() == EnumMinecartType.RIDEABLE && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D && this.riddenByEntity == null && entityIn.ridingEntity == null)
            entityIn.mountEntity(this); 
          double d0 = entityIn.posX - this.posX;
          double d1 = entityIn.posZ - this.posZ;
          double d2 = d0 * d0 + d1 * d1;
          if (d2 >= 9.999999747378752E-5D) {
            d2 = MathHelper.sqrt_double(d2);
            d0 /= d2;
            d1 /= d2;
            double d3 = 1.0D / d2;
            if (d3 > 1.0D)
              d3 = 1.0D; 
            d0 *= d3;
            d1 *= d3;
            d0 *= 0.10000000149011612D;
            d1 *= 0.10000000149011612D;
            d0 *= (1.0F - this.entityCollisionReduction);
            d1 *= (1.0F - this.entityCollisionReduction);
            d0 *= 0.5D;
            d1 *= 0.5D;
            if (entityIn instanceof EntityMinecart) {
              double d4 = entityIn.posX - this.posX;
              double d5 = entityIn.posZ - this.posZ;
              Vec3 vec3 = (new Vec3(d4, 0.0D, d5)).normalize();
              Vec3 vec31 = (new Vec3(MathHelper.cos(this.rotationYaw * 3.1415927F / 180.0F), 0.0D, MathHelper.sin(this.rotationYaw * 3.1415927F / 180.0F))).normalize();
              double d6 = Math.abs(vec3.dotProduct(vec31));
              if (d6 < 0.800000011920929D)
                return; 
              double d7 = entityIn.motionX + this.motionX;
              double d8 = entityIn.motionZ + this.motionZ;
              if (((EntityMinecart)entityIn).getMinecartType() == EnumMinecartType.FURNACE && getMinecartType() != EnumMinecartType.FURNACE) {
                this.motionX *= 0.20000000298023224D;
                this.motionZ *= 0.20000000298023224D;
                addVelocity(entityIn.motionX - d0, 0.0D, entityIn.motionZ - d1);
                entityIn.motionX *= 0.949999988079071D;
                entityIn.motionZ *= 0.949999988079071D;
              } else if (((EntityMinecart)entityIn).getMinecartType() != EnumMinecartType.FURNACE && getMinecartType() == EnumMinecartType.FURNACE) {
                entityIn.motionX *= 0.20000000298023224D;
                entityIn.motionZ *= 0.20000000298023224D;
                entityIn.addVelocity(this.motionX + d0, 0.0D, this.motionZ + d1);
                this.motionX *= 0.949999988079071D;
                this.motionZ *= 0.949999988079071D;
              } else {
                d7 /= 2.0D;
                d8 /= 2.0D;
                this.motionX *= 0.20000000298023224D;
                this.motionZ *= 0.20000000298023224D;
                addVelocity(d7 - d0, 0.0D, d8 - d1);
                entityIn.motionX *= 0.20000000298023224D;
                entityIn.motionZ *= 0.20000000298023224D;
                entityIn.addVelocity(d7 + d0, 0.0D, d8 + d1);
              } 
            } else {
              addVelocity(-d0, 0.0D, -d1);
              entityIn.addVelocity(d0 / 4.0D, 0.0D, d1 / 4.0D);
            } 
          } 
        }   
  }
  
  public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
    this.minecartX = x;
    this.minecartY = y;
    this.minecartZ = z;
    this.minecartYaw = yaw;
    this.minecartPitch = pitch;
    this.turnProgress = posRotationIncrements + 2;
    this.motionX = this.velocityX;
    this.motionY = this.velocityY;
    this.motionZ = this.velocityZ;
  }
  
  public void setVelocity(double x, double y, double z) {
    this.velocityX = this.motionX = x;
    this.velocityY = this.motionY = y;
    this.velocityZ = this.motionZ = z;
  }
  
  public void setDamage(float p_70492_1_) {
    this.dataWatcher.updateObject(19, Float.valueOf(p_70492_1_));
  }
  
  public float getDamage() {
    return this.dataWatcher.getWatchableObjectFloat(19);
  }
  
  public void setRollingAmplitude(int p_70497_1_) {
    this.dataWatcher.updateObject(17, Integer.valueOf(p_70497_1_));
  }
  
  public int getRollingAmplitude() {
    return this.dataWatcher.getWatchableObjectInt(17);
  }
  
  public void setRollingDirection(int p_70494_1_) {
    this.dataWatcher.updateObject(18, Integer.valueOf(p_70494_1_));
  }
  
  public int getRollingDirection() {
    return this.dataWatcher.getWatchableObjectInt(18);
  }
  
  public abstract EnumMinecartType getMinecartType();
  
  public IBlockState getDisplayTile() {
    return !hasDisplayTile() ? getDefaultDisplayTile() : Block.getStateById(getDataWatcher().getWatchableObjectInt(20));
  }
  
  public IBlockState getDefaultDisplayTile() {
    return Blocks.air.getDefaultState();
  }
  
  public int getDisplayTileOffset() {
    return !hasDisplayTile() ? getDefaultDisplayTileOffset() : getDataWatcher().getWatchableObjectInt(21);
  }
  
  public int getDefaultDisplayTileOffset() {
    return 6;
  }
  
  public void func_174899_a(IBlockState p_174899_1_) {
    getDataWatcher().updateObject(20, Integer.valueOf(Block.getStateId(p_174899_1_)));
    setHasDisplayTile(true);
  }
  
  public void setDisplayTileOffset(int p_94086_1_) {
    getDataWatcher().updateObject(21, Integer.valueOf(p_94086_1_));
    setHasDisplayTile(true);
  }
  
  public boolean hasDisplayTile() {
    return (getDataWatcher().getWatchableObjectByte(22) == 1);
  }
  
  public void setHasDisplayTile(boolean p_94096_1_) {
    getDataWatcher().updateObject(22, Byte.valueOf((byte)(p_94096_1_ ? 1 : 0)));
  }
  
  public void setCustomNameTag(String name) {
    this.entityName = name;
  }
  
  public String getCommandSenderName() {
    return (this.entityName != null) ? this.entityName : super.getCommandSenderName();
  }
  
  public boolean hasCustomName() {
    return (this.entityName != null);
  }
  
  public String getCustomNameTag() {
    return this.entityName;
  }
  
  public IChatComponent getDisplayName() {
    if (hasCustomName()) {
      ChatComponentText chatcomponenttext = new ChatComponentText(this.entityName);
      chatcomponenttext.getChatStyle().setChatHoverEvent(getHoverEvent());
      chatcomponenttext.getChatStyle().setInsertion(getUniqueID().toString());
      return (IChatComponent)chatcomponenttext;
    } 
    ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(getCommandSenderName(), new Object[0]);
    chatcomponenttranslation.getChatStyle().setChatHoverEvent(getHoverEvent());
    chatcomponenttranslation.getChatStyle().setInsertion(getUniqueID().toString());
    return (IChatComponent)chatcomponenttranslation;
  }
  
  public enum EnumMinecartType {
    RIDEABLE(0, "MinecartRideable"),
    CHEST(1, "MinecartChest"),
    FURNACE(2, "MinecartFurnace"),
    TNT(3, "MinecartTNT"),
    SPAWNER(4, "MinecartSpawner"),
    HOPPER(5, "MinecartHopper"),
    COMMAND_BLOCK(6, "MinecartCommandBlock");
    
    private static final Map<Integer, EnumMinecartType> ID_LOOKUP = Maps.newHashMap();
    
    private final int networkID;
    
    private final String name;
    
    static {
      for (EnumMinecartType entityminecart$enumminecarttype : values())
        ID_LOOKUP.put(Integer.valueOf(entityminecart$enumminecarttype.getNetworkID()), entityminecart$enumminecarttype); 
    }
    
    EnumMinecartType(int networkID, String name) {
      this.networkID = networkID;
      this.name = name;
    }
    
    public int getNetworkID() {
      return this.networkID;
    }
    
    public String getName() {
      return this.name;
    }
    
    public static EnumMinecartType byNetworkID(int id) {
      EnumMinecartType entityminecart$enumminecarttype = ID_LOOKUP.get(Integer.valueOf(id));
      return (entityminecart$enumminecarttype == null) ? RIDEABLE : entityminecart$enumminecarttype;
    }
  }
}
