package net.minecraft.entity;

import java.util.UUID;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.scoreboard.Team;
import net.minecraft.src.Config;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.optifine.reflect.Reflector;

public abstract class EntityLiving extends EntityLivingBase {
  public int livingSoundTime;
  
  protected int experienceValue;
  
  private EntityLookHelper lookHelper;
  
  protected EntityMoveHelper moveHelper;
  
  protected EntityJumpHelper jumpHelper;
  
  private EntityBodyHelper bodyHelper;
  
  protected PathNavigate navigator;
  
  protected final EntityAITasks tasks;
  
  protected final EntityAITasks targetTasks;
  
  private EntityLivingBase attackTarget;
  
  private EntitySenses senses;
  
  private ItemStack[] equipment = new ItemStack[5];
  
  protected float[] equipmentDropChances = new float[5];
  
  private boolean canPickUpLoot;
  
  private boolean persistenceRequired;
  
  private boolean isLeashed;
  
  private Entity leashedToEntity;
  
  private NBTTagCompound leashNBTTag;
  
  private UUID teamUuid = null;
  
  private String teamUuidString = null;
  
  public EntityLiving(World worldIn) {
    super(worldIn);
    this.tasks = new EntityAITasks((worldIn != null && worldIn.theProfiler != null) ? worldIn.theProfiler : null);
    this.targetTasks = new EntityAITasks((worldIn != null && worldIn.theProfiler != null) ? worldIn.theProfiler : null);
    this.lookHelper = new EntityLookHelper(this);
    this.moveHelper = new EntityMoveHelper(this);
    this.jumpHelper = new EntityJumpHelper(this);
    this.bodyHelper = new EntityBodyHelper(this);
    this.navigator = getNewNavigator(worldIn);
    this.senses = new EntitySenses(this);
    for (int i = 0; i < this.equipmentDropChances.length; i++)
      this.equipmentDropChances[i] = 0.085F; 
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getAttributeMap().registerAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
  }
  
  protected PathNavigate getNewNavigator(World worldIn) {
    return (PathNavigate)new PathNavigateGround(this, worldIn);
  }
  
  public EntityLookHelper getLookHelper() {
    return this.lookHelper;
  }
  
  public EntityMoveHelper getMoveHelper() {
    return this.moveHelper;
  }
  
  public EntityJumpHelper getJumpHelper() {
    return this.jumpHelper;
  }
  
  public PathNavigate getNavigator() {
    return this.navigator;
  }
  
  public EntitySenses getEntitySenses() {
    return this.senses;
  }
  
  public EntityLivingBase getAttackTarget() {
    return this.attackTarget;
  }
  
  public void setAttackTarget(EntityLivingBase entitylivingbaseIn) {
    this.attackTarget = entitylivingbaseIn;
    Reflector.callVoid(Reflector.ForgeHooks_onLivingSetAttackTarget, new Object[] { this, entitylivingbaseIn });
  }
  
  public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
    return (cls != EntityGhast.class);
  }
  
  public void eatGrassBonus() {}
  
  protected void entityInit() {
    super.entityInit();
    this.dataWatcher.addObject(15, Byte.valueOf((byte)0));
  }
  
  public int getTalkInterval() {
    return 80;
  }
  
  public void playLivingSound() {
    String s = getLivingSound();
    if (s != null)
      playSound(s, getSoundVolume(), getSoundPitch()); 
  }
  
  public void onEntityUpdate() {
    super.onEntityUpdate();
    this.worldObj.theProfiler.startSection("mobBaseTick");
    if (isEntityAlive() && this.rand.nextInt(1000) < this.livingSoundTime++) {
      this.livingSoundTime = -getTalkInterval();
      playLivingSound();
    } 
    this.worldObj.theProfiler.endSection();
  }
  
  protected int getExperiencePoints(EntityPlayer player) {
    if (this.experienceValue > 0) {
      int i = this.experienceValue;
      ItemStack[] aitemstack = getInventory();
      for (int j = 0; j < aitemstack.length; j++) {
        if (aitemstack[j] != null && this.equipmentDropChances[j] <= 1.0F)
          i += 1 + this.rand.nextInt(3); 
      } 
      return i;
    } 
    return this.experienceValue;
  }
  
  public void spawnExplosionParticle() {
    if (this.worldObj.isRemote) {
      for (int i = 0; i < 20; i++) {
        double d0 = this.rand.nextGaussian() * 0.02D;
        double d1 = this.rand.nextGaussian() * 0.02D;
        double d2 = this.rand.nextGaussian() * 0.02D;
        double d3 = 10.0D;
        this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (this.rand.nextFloat() * this.width * 2.0F) - this.width - d0 * d3, this.posY + (this.rand.nextFloat() * this.height) - d1 * d3, this.posZ + (this.rand.nextFloat() * this.width * 2.0F) - this.width - d2 * d3, d0, d1, d2, new int[0]);
      } 
    } else {
      this.worldObj.setEntityState(this, (byte)20);
    } 
  }
  
  public void handleHealthUpdate(byte id) {
    if (id == 20) {
      spawnExplosionParticle();
    } else {
      super.handleHealthUpdate(id);
    } 
  }
  
  public void onUpdate() {
    if (Config.isSmoothWorld() && canSkipUpdate()) {
      onUpdateMinimal();
    } else {
      super.onUpdate();
      if (!this.worldObj.isRemote)
        updateLeashedState(); 
    } 
  }
  
  protected float updateDistance(float p_110146_1_, float p_110146_2_) {
    this.bodyHelper.updateRenderAngles();
    return p_110146_2_;
  }
  
  protected String getLivingSound() {
    return null;
  }
  
  protected Item getDropItem() {
    return null;
  }
  
  protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    Item item = getDropItem();
    if (item != null) {
      int i = this.rand.nextInt(3);
      if (lootingModifier > 0)
        i += this.rand.nextInt(lootingModifier + 1); 
      for (int j = 0; j < i; j++)
        dropItem(item, 1); 
    } 
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setBoolean("CanPickUpLoot", canPickUpLoot());
    tagCompound.setBoolean("PersistenceRequired", this.persistenceRequired);
    NBTTagList nbttaglist = new NBTTagList();
    for (int i = 0; i < this.equipment.length; i++) {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      if (this.equipment[i] != null)
        this.equipment[i].writeToNBT(nbttagcompound); 
      nbttaglist.appendTag((NBTBase)nbttagcompound);
    } 
    tagCompound.setTag("Equipment", (NBTBase)nbttaglist);
    NBTTagList nbttaglist1 = new NBTTagList();
    for (int j = 0; j < this.equipmentDropChances.length; j++)
      nbttaglist1.appendTag((NBTBase)new NBTTagFloat(this.equipmentDropChances[j])); 
    tagCompound.setTag("DropChances", (NBTBase)nbttaglist1);
    tagCompound.setBoolean("Leashed", this.isLeashed);
    if (this.leashedToEntity != null) {
      NBTTagCompound nbttagcompound1 = new NBTTagCompound();
      if (this.leashedToEntity instanceof EntityLivingBase) {
        nbttagcompound1.setLong("UUIDMost", this.leashedToEntity.getUniqueID().getMostSignificantBits());
        nbttagcompound1.setLong("UUIDLeast", this.leashedToEntity.getUniqueID().getLeastSignificantBits());
      } else if (this.leashedToEntity instanceof EntityHanging) {
        BlockPos blockpos = ((EntityHanging)this.leashedToEntity).getHangingPosition();
        nbttagcompound1.setInteger("X", blockpos.getX());
        nbttagcompound1.setInteger("Y", blockpos.getY());
        nbttagcompound1.setInteger("Z", blockpos.getZ());
      } 
      tagCompound.setTag("Leash", (NBTBase)nbttagcompound1);
    } 
    if (isAIDisabled())
      tagCompound.setBoolean("NoAI", isAIDisabled()); 
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    if (tagCompund.hasKey("CanPickUpLoot", 1))
      setCanPickUpLoot(tagCompund.getBoolean("CanPickUpLoot")); 
    this.persistenceRequired = tagCompund.getBoolean("PersistenceRequired");
    if (tagCompund.hasKey("Equipment", 9)) {
      NBTTagList nbttaglist = tagCompund.getTagList("Equipment", 10);
      for (int i = 0; i < this.equipment.length; i++)
        this.equipment[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i)); 
    } 
    if (tagCompund.hasKey("DropChances", 9)) {
      NBTTagList nbttaglist1 = tagCompund.getTagList("DropChances", 5);
      for (int j = 0; j < nbttaglist1.tagCount(); j++)
        this.equipmentDropChances[j] = nbttaglist1.getFloatAt(j); 
    } 
    this.isLeashed = tagCompund.getBoolean("Leashed");
    if (this.isLeashed && tagCompund.hasKey("Leash", 10))
      this.leashNBTTag = tagCompund.getCompoundTag("Leash"); 
    setNoAI(tagCompund.getBoolean("NoAI"));
  }
  
  public void setMoveForward(float p_70657_1_) {
    this.moveForward = p_70657_1_;
  }
  
  public void setAIMoveSpeed(float speedIn) {
    super.setAIMoveSpeed(speedIn);
    setMoveForward(speedIn);
  }
  
  public void onLivingUpdate() {
    super.onLivingUpdate();
    this.worldObj.theProfiler.startSection("looting");
    if (!this.worldObj.isRemote && canPickUpLoot() && !this.dead && this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
      for (EntityItem entityitem : this.worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D))) {
        if (!entityitem.isDead && entityitem.getEntityItem() != null && !entityitem.cannotPickup())
          updateEquipmentIfNeeded(entityitem); 
      }  
    this.worldObj.theProfiler.endSection();
  }
  
  protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
    ItemStack itemstack = itemEntity.getEntityItem();
    int i = getArmorPosition(itemstack);
    if (i > -1) {
      boolean flag = true;
      ItemStack itemstack1 = getEquipmentInSlot(i);
      if (itemstack1 != null)
        if (i == 0) {
          if (itemstack.getItem() instanceof ItemSword && !(itemstack1.getItem() instanceof ItemSword)) {
            flag = true;
          } else if (itemstack.getItem() instanceof ItemSword && itemstack1.getItem() instanceof ItemSword) {
            ItemSword itemsword = (ItemSword)itemstack.getItem();
            ItemSword itemsword1 = (ItemSword)itemstack1.getItem();
            if (itemsword.getDamageVsEntity() != itemsword1.getDamageVsEntity()) {
              flag = (itemsword.getDamageVsEntity() > itemsword1.getDamageVsEntity());
            } else {
              flag = (itemstack.getMetadata() > itemstack1.getMetadata() || (itemstack.hasTagCompound() && !itemstack1.hasTagCompound()));
            } 
          } else if (itemstack.getItem() instanceof net.minecraft.item.ItemBow && itemstack1.getItem() instanceof net.minecraft.item.ItemBow) {
            flag = (itemstack.hasTagCompound() && !itemstack1.hasTagCompound());
          } else {
            flag = false;
          } 
        } else if (itemstack.getItem() instanceof ItemArmor && !(itemstack1.getItem() instanceof ItemArmor)) {
          flag = true;
        } else if (itemstack.getItem() instanceof ItemArmor && itemstack1.getItem() instanceof ItemArmor) {
          ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
          ItemArmor itemarmor1 = (ItemArmor)itemstack1.getItem();
          if (itemarmor.damageReduceAmount != itemarmor1.damageReduceAmount) {
            flag = (itemarmor.damageReduceAmount > itemarmor1.damageReduceAmount);
          } else {
            flag = (itemstack.getMetadata() > itemstack1.getMetadata() || (itemstack.hasTagCompound() && !itemstack1.hasTagCompound()));
          } 
        } else {
          flag = false;
        }  
      if (flag && func_175448_a(itemstack)) {
        if (itemstack1 != null && this.rand.nextFloat() - 0.1F < this.equipmentDropChances[i])
          entityDropItem(itemstack1, 0.0F); 
        if (itemstack.getItem() == Items.diamond && itemEntity.getThrower() != null) {
          EntityPlayer entityplayer = this.worldObj.getPlayerEntityByName(itemEntity.getThrower());
          if (entityplayer != null)
            entityplayer.triggerAchievement((StatBase)AchievementList.diamondsToYou); 
        } 
        setCurrentItemOrArmor(i, itemstack);
        this.equipmentDropChances[i] = 2.0F;
        this.persistenceRequired = true;
        onItemPickup((Entity)itemEntity, 1);
        itemEntity.setDead();
      } 
    } 
  }
  
  protected boolean func_175448_a(ItemStack stack) {
    return true;
  }
  
  protected boolean canDespawn() {
    return true;
  }
  
  protected void despawnEntity() {
    Object object = null;
    Object object1 = Reflector.getFieldValue(Reflector.Event_Result_DEFAULT);
    Object object2 = Reflector.getFieldValue(Reflector.Event_Result_DENY);
    if (this.persistenceRequired) {
      this.entityAge = 0;
    } else if ((this.entityAge & 0x1F) == 31 && (object = Reflector.call(Reflector.ForgeEventFactory_canEntityDespawn, new Object[] { this })) != object1) {
      if (object == object2) {
        this.entityAge = 0;
      } else {
        setDead();
      } 
    } else {
      EntityPlayer entityPlayer = this.worldObj.getClosestPlayerToEntity(this, -1.0D);
      if (entityPlayer != null) {
        double d0 = ((Entity)entityPlayer).posX - this.posX;
        double d1 = ((Entity)entityPlayer).posY - this.posY;
        double d2 = ((Entity)entityPlayer).posZ - this.posZ;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        if (canDespawn() && d3 > 16384.0D)
          setDead(); 
        if (this.entityAge > 600 && this.rand.nextInt(800) == 0 && d3 > 1024.0D && canDespawn()) {
          setDead();
        } else if (d3 < 1024.0D) {
          this.entityAge = 0;
        } 
      } 
    } 
  }
  
  protected final void updateEntityActionState() {
    this.entityAge++;
    this.worldObj.theProfiler.startSection("checkDespawn");
    despawnEntity();
    this.worldObj.theProfiler.endSection();
    this.worldObj.theProfiler.startSection("sensing");
    this.senses.clearSensingCache();
    this.worldObj.theProfiler.endSection();
    this.worldObj.theProfiler.startSection("targetSelector");
    this.targetTasks.onUpdateTasks();
    this.worldObj.theProfiler.endSection();
    this.worldObj.theProfiler.startSection("goalSelector");
    this.tasks.onUpdateTasks();
    this.worldObj.theProfiler.endSection();
    this.worldObj.theProfiler.startSection("navigation");
    this.navigator.onUpdateNavigation();
    this.worldObj.theProfiler.endSection();
    this.worldObj.theProfiler.startSection("mob tick");
    updateAITasks();
    this.worldObj.theProfiler.endSection();
    this.worldObj.theProfiler.startSection("controls");
    this.worldObj.theProfiler.startSection("move");
    this.moveHelper.onUpdateMoveHelper();
    this.worldObj.theProfiler.endStartSection("look");
    this.lookHelper.onUpdateLook();
    this.worldObj.theProfiler.endStartSection("jump");
    this.jumpHelper.doJump();
    this.worldObj.theProfiler.endSection();
    this.worldObj.theProfiler.endSection();
  }
  
  protected void updateAITasks() {}
  
  public int getVerticalFaceSpeed() {
    return 40;
  }
  
  public void faceEntity(Entity entityIn, float p_70625_2_, float p_70625_3_) {
    double d2, d0 = entityIn.posX - this.posX;
    double d1 = entityIn.posZ - this.posZ;
    if (entityIn instanceof EntityLivingBase) {
      EntityLivingBase entitylivingbase = (EntityLivingBase)entityIn;
      d2 = entitylivingbase.posY + entitylivingbase.getEyeHeight() - this.posY + getEyeHeight();
    } else {
      d2 = ((entityIn.getEntityBoundingBox()).minY + (entityIn.getEntityBoundingBox()).maxY) / 2.0D - this.posY + getEyeHeight();
    } 
    double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
    float f = (float)(MathHelper.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
    float f1 = (float)-(MathHelper.atan2(d2, d3) * 180.0D / Math.PI);
    this.rotationPitch = updateRotation(this.rotationPitch, f1, p_70625_3_);
    this.rotationYaw = updateRotation(this.rotationYaw, f, p_70625_2_);
  }
  
  private float updateRotation(float p_70663_1_, float p_70663_2_, float p_70663_3_) {
    float f = MathHelper.wrapAngleTo180_float(p_70663_2_ - p_70663_1_);
    if (f > p_70663_3_)
      f = p_70663_3_; 
    if (f < -p_70663_3_)
      f = -p_70663_3_; 
    return p_70663_1_ + f;
  }
  
  public boolean getCanSpawnHere() {
    return true;
  }
  
  public boolean isNotColliding() {
    return (this.worldObj.checkNoEntityCollision(getEntityBoundingBox(), this) && this.worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty() && !this.worldObj.isAnyLiquid(getEntityBoundingBox()));
  }
  
  public float getRenderSizeModifier() {
    return 1.0F;
  }
  
  public int getMaxSpawnedInChunk() {
    return 4;
  }
  
  public int getMaxFallHeight() {
    if (getAttackTarget() == null)
      return 3; 
    int i = (int)(getHealth() - getMaxHealth() * 0.33F);
    i -= (3 - this.worldObj.getDifficulty().getDifficultyId()) * 4;
    if (i < 0)
      i = 0; 
    return i + 3;
  }
  
  public ItemStack getHeldItem() {
    return this.equipment[0];
  }
  
  public ItemStack getEquipmentInSlot(int slotIn) {
    return this.equipment[slotIn];
  }
  
  public ItemStack getCurrentArmor(int slotIn) {
    return this.equipment[slotIn + 1];
  }
  
  public void setCurrentItemOrArmor(int slotIn, ItemStack stack) {
    this.equipment[slotIn] = stack;
  }
  
  public ItemStack[] getInventory() {
    return this.equipment;
  }
  
  protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
    for (int i = 0; i < (getInventory()).length; i++) {
      ItemStack itemstack = getEquipmentInSlot(i);
      boolean flag = (this.equipmentDropChances[i] > 1.0F);
      if (itemstack != null && (wasRecentlyHit || flag) && this.rand.nextFloat() - lootingModifier * 0.01F < this.equipmentDropChances[i]) {
        if (!flag && itemstack.isItemStackDamageable()) {
          int j = Math.max(itemstack.getMaxDamage() - 25, 1);
          int k = itemstack.getMaxDamage() - this.rand.nextInt(this.rand.nextInt(j) + 1);
          if (k > j)
            k = j; 
          if (k < 1)
            k = 1; 
          itemstack.setItemDamage(k);
        } 
        entityDropItem(itemstack, 0.0F);
      } 
    } 
  }
  
  protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
    if (this.rand.nextFloat() < 0.15F * difficulty.getClampedAdditionalDifficulty()) {
      int i = this.rand.nextInt(2);
      float f = (this.worldObj.getDifficulty() == EnumDifficulty.HARD) ? 0.1F : 0.25F;
      if (this.rand.nextFloat() < 0.095F)
        i++; 
      if (this.rand.nextFloat() < 0.095F)
        i++; 
      if (this.rand.nextFloat() < 0.095F)
        i++; 
      for (int j = 3; j >= 0; j--) {
        ItemStack itemstack = getCurrentArmor(j);
        if (j < 3 && this.rand.nextFloat() < f)
          break; 
        if (itemstack == null) {
          Item item = getArmorItemForSlot(j + 1, i);
          if (item != null)
            setCurrentItemOrArmor(j + 1, new ItemStack(item)); 
        } 
      } 
    } 
  }
  
  public static int getArmorPosition(ItemStack stack) {
    if (stack.getItem() != Item.getItemFromBlock(Blocks.pumpkin) && stack.getItem() != Items.skull) {
      if (stack.getItem() instanceof ItemArmor)
        switch (((ItemArmor)stack.getItem()).armorType) {
          case 0:
            return 4;
          case 1:
            return 3;
          case 2:
            return 2;
          case 3:
            return 1;
        }  
      return 0;
    } 
    return 4;
  }
  
  public static Item getArmorItemForSlot(int armorSlot, int itemTier) {
    switch (armorSlot) {
      case 4:
        if (itemTier == 0)
          return (Item)Items.leather_helmet; 
        if (itemTier == 1)
          return (Item)Items.golden_helmet; 
        if (itemTier == 2)
          return (Item)Items.chainmail_helmet; 
        if (itemTier == 3)
          return (Item)Items.iron_helmet; 
        if (itemTier == 4)
          return (Item)Items.diamond_helmet; 
      case 3:
        if (itemTier == 0)
          return (Item)Items.leather_chestplate; 
        if (itemTier == 1)
          return (Item)Items.golden_chestplate; 
        if (itemTier == 2)
          return (Item)Items.chainmail_chestplate; 
        if (itemTier == 3)
          return (Item)Items.iron_chestplate; 
        if (itemTier == 4)
          return (Item)Items.diamond_chestplate; 
      case 2:
        if (itemTier == 0)
          return (Item)Items.leather_leggings; 
        if (itemTier == 1)
          return (Item)Items.golden_leggings; 
        if (itemTier == 2)
          return (Item)Items.chainmail_leggings; 
        if (itemTier == 3)
          return (Item)Items.iron_leggings; 
        if (itemTier == 4)
          return (Item)Items.diamond_leggings; 
      case 1:
        if (itemTier == 0)
          return (Item)Items.leather_boots; 
        if (itemTier == 1)
          return (Item)Items.golden_boots; 
        if (itemTier == 2)
          return (Item)Items.chainmail_boots; 
        if (itemTier == 3)
          return (Item)Items.iron_boots; 
        if (itemTier == 4)
          return (Item)Items.diamond_boots; 
        break;
    } 
    return null;
  }
  
  protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty) {
    float f = difficulty.getClampedAdditionalDifficulty();
    if (getHeldItem() != null && this.rand.nextFloat() < 0.25F * f)
      EnchantmentHelper.addRandomEnchantment(this.rand, getHeldItem(), (int)(5.0F + f * this.rand.nextInt(18))); 
    for (int i = 0; i < 4; i++) {
      ItemStack itemstack = getCurrentArmor(i);
      if (itemstack != null && this.rand.nextFloat() < 0.5F * f)
        EnchantmentHelper.addRandomEnchantment(this.rand, itemstack, (int)(5.0F + f * this.rand.nextInt(18))); 
    } 
  }
  
  public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
    getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, 1));
    return livingdata;
  }
  
  public boolean canBeSteered() {
    return false;
  }
  
  public void enablePersistence() {
    this.persistenceRequired = true;
  }
  
  public void setEquipmentDropChance(int slotIn, float chance) {
    this.equipmentDropChances[slotIn] = chance;
  }
  
  public boolean canPickUpLoot() {
    return this.canPickUpLoot;
  }
  
  public void setCanPickUpLoot(boolean canPickup) {
    this.canPickUpLoot = canPickup;
  }
  
  public boolean isNoDespawnRequired() {
    return this.persistenceRequired;
  }
  
  public final boolean interactFirst(EntityPlayer playerIn) {
    if (getLeashed() && getLeashedToEntity() == playerIn) {
      clearLeashed(true, !playerIn.capabilities.isCreativeMode);
      return true;
    } 
    ItemStack itemstack = playerIn.inventory.getCurrentItem();
    if (itemstack != null && itemstack.getItem() == Items.lead && allowLeashing()) {
      if (!(this instanceof EntityTameable) || !((EntityTameable)this).isTamed()) {
        setLeashedToEntity((Entity)playerIn, true);
        itemstack.stackSize--;
        return true;
      } 
      if (((EntityTameable)this).isOwner((EntityLivingBase)playerIn)) {
        setLeashedToEntity((Entity)playerIn, true);
        itemstack.stackSize--;
        return true;
      } 
    } 
    if (interact(playerIn))
      return true; 
    return super.interactFirst(playerIn);
  }
  
  protected boolean interact(EntityPlayer player) {
    return false;
  }
  
  protected void updateLeashedState() {
    if (this.leashNBTTag != null)
      recreateLeash(); 
    if (this.isLeashed) {
      if (!isEntityAlive())
        clearLeashed(true, true); 
      if (this.leashedToEntity == null || this.leashedToEntity.isDead)
        clearLeashed(true, true); 
    } 
  }
  
  public void clearLeashed(boolean sendPacket, boolean dropLead) {
    if (this.isLeashed) {
      this.isLeashed = false;
      this.leashedToEntity = null;
      if (!this.worldObj.isRemote && dropLead)
        dropItem(Items.lead, 1); 
      if (!this.worldObj.isRemote && sendPacket && this.worldObj instanceof WorldServer)
        ((WorldServer)this.worldObj).getEntityTracker().sendToAllTrackingEntity(this, (Packet)new S1BPacketEntityAttach(1, this, (Entity)null)); 
    } 
  }
  
  public boolean allowLeashing() {
    return (!getLeashed() && !(this instanceof net.minecraft.entity.monster.IMob));
  }
  
  public boolean getLeashed() {
    return this.isLeashed;
  }
  
  public Entity getLeashedToEntity() {
    return this.leashedToEntity;
  }
  
  public void setLeashedToEntity(Entity entityIn, boolean sendAttachNotification) {
    this.isLeashed = true;
    this.leashedToEntity = entityIn;
    if (!this.worldObj.isRemote && sendAttachNotification && this.worldObj instanceof WorldServer)
      ((WorldServer)this.worldObj).getEntityTracker().sendToAllTrackingEntity(this, (Packet)new S1BPacketEntityAttach(1, this, this.leashedToEntity)); 
  }
  
  private void recreateLeash() {
    if (this.isLeashed && this.leashNBTTag != null)
      if (this.leashNBTTag.hasKey("UUIDMost", 4) && this.leashNBTTag.hasKey("UUIDLeast", 4)) {
        UUID uuid = new UUID(this.leashNBTTag.getLong("UUIDMost"), this.leashNBTTag.getLong("UUIDLeast"));
        for (EntityLivingBase entitylivingbase : this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().expand(10.0D, 10.0D, 10.0D))) {
          if (entitylivingbase.getUniqueID().equals(uuid)) {
            this.leashedToEntity = entitylivingbase;
            break;
          } 
        } 
      } else if (this.leashNBTTag.hasKey("X", 99) && this.leashNBTTag.hasKey("Y", 99) && this.leashNBTTag.hasKey("Z", 99)) {
        BlockPos blockpos = new BlockPos(this.leashNBTTag.getInteger("X"), this.leashNBTTag.getInteger("Y"), this.leashNBTTag.getInteger("Z"));
        EntityLeashKnot entityleashknot = EntityLeashKnot.getKnotForPosition(this.worldObj, blockpos);
        if (entityleashknot == null)
          entityleashknot = EntityLeashKnot.createKnot(this.worldObj, blockpos); 
        this.leashedToEntity = entityleashknot;
      } else {
        clearLeashed(false, true);
      }  
    this.leashNBTTag = null;
  }
  
  public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
    int i;
    if (inventorySlot == 99) {
      i = 0;
    } else {
      i = inventorySlot - 100 + 1;
      if (i < 0 || i >= this.equipment.length)
        return false; 
    } 
    if (itemStackIn == null || getArmorPosition(itemStackIn) == i || (i == 4 && itemStackIn.getItem() instanceof net.minecraft.item.ItemBlock)) {
      setCurrentItemOrArmor(i, itemStackIn);
      return true;
    } 
    return false;
  }
  
  public boolean isServerWorld() {
    return (super.isServerWorld() && !isAIDisabled());
  }
  
  public void setNoAI(boolean disable) {
    this.dataWatcher.updateObject(15, Byte.valueOf((byte)(disable ? 1 : 0)));
  }
  
  public boolean isAIDisabled() {
    return (this.dataWatcher.getWatchableObjectByte(15) != 0);
  }
  
  private boolean canSkipUpdate() {
    if (isChild())
      return false; 
    if (this.hurtTime > 0)
      return false; 
    if (this.ticksExisted < 20)
      return false; 
    World world = getEntityWorld();
    if (world == null)
      return false; 
    if (world.playerEntities.size() != 1)
      return false; 
    Entity entity = world.playerEntities.get(0);
    double d0 = Math.max(Math.abs(this.posX - entity.posX) - 16.0D, 0.0D);
    double d1 = Math.max(Math.abs(this.posZ - entity.posZ) - 16.0D, 0.0D);
    double d2 = d0 * d0 + d1 * d1;
    return !isInRangeToRenderDist(d2);
  }
  
  private void onUpdateMinimal() {
    this.entityAge++;
    if (this instanceof net.minecraft.entity.monster.EntityMob) {
      float f = getBrightness(1.0F);
      if (f > 0.5F)
        this.entityAge += 2; 
    } 
    despawnEntity();
  }
  
  public Team getTeam() {
    UUID uuid = getUniqueID();
    if (this.teamUuid != uuid) {
      this.teamUuid = uuid;
      this.teamUuidString = uuid.toString();
    } 
    return (Team)this.worldObj.getScoreboard().getPlayersTeam(this.teamUuidString);
  }
  
  public enum SpawnPlacementType {
    ON_GROUND, IN_AIR, IN_WATER;
  }
}
