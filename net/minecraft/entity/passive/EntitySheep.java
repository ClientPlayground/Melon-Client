package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntitySheep extends EntityAnimal {
  private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new Container() {
        public boolean canInteractWith(EntityPlayer playerIn) {
          return false;
        }
      },  2, 1);
  
  private static final Map<EnumDyeColor, float[]> DYE_TO_RGB = Maps.newEnumMap(EnumDyeColor.class);
  
  private int sheepTimer;
  
  private EntityAIEatGrass entityAIEatGrass = new EntityAIEatGrass((EntityLiving)this);
  
  public static float[] getDyeRgb(EnumDyeColor dyeColor) {
    return DYE_TO_RGB.get(dyeColor);
  }
  
  public EntitySheep(World worldIn) {
    super(worldIn);
    setSize(0.9F, 1.3F);
    ((PathNavigateGround)getNavigator()).setAvoidsWater(true);
    this.tasks.addTask(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
    this.tasks.addTask(1, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.25D));
    this.tasks.addTask(2, (EntityAIBase)new EntityAIMate(this, 1.0D));
    this.tasks.addTask(3, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.1D, Items.wheat, false));
    this.tasks.addTask(4, (EntityAIBase)new EntityAIFollowParent(this, 1.1D));
    this.tasks.addTask(5, (EntityAIBase)this.entityAIEatGrass);
    this.tasks.addTask(6, (EntityAIBase)new EntityAIWander((EntityCreature)this, 1.0D));
    this.tasks.addTask(7, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0F));
    this.tasks.addTask(8, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
    this.inventoryCrafting.setInventorySlotContents(0, new ItemStack(Items.dye, 1, 0));
    this.inventoryCrafting.setInventorySlotContents(1, new ItemStack(Items.dye, 1, 0));
  }
  
  protected void updateAITasks() {
    this.sheepTimer = this.entityAIEatGrass.getEatingGrassTimer();
    super.updateAITasks();
  }
  
  public void onLivingUpdate() {
    if (this.worldObj.isRemote)
      this.sheepTimer = Math.max(0, this.sheepTimer - 1); 
    super.onLivingUpdate();
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
  }
  
  protected void entityInit() {
    super.entityInit();
    this.dataWatcher.addObject(16, new Byte((byte)0));
  }
  
  protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    if (!getSheared())
      entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, getFleeceColor().getMetadata()), 0.0F); 
    int i = this.rand.nextInt(2) + 1 + this.rand.nextInt(1 + lootingModifier);
    for (int j = 0; j < i; j++) {
      if (isBurning()) {
        dropItem(Items.cooked_mutton, 1);
      } else {
        dropItem(Items.mutton, 1);
      } 
    } 
  }
  
  protected Item getDropItem() {
    return Item.getItemFromBlock(Blocks.wool);
  }
  
  public void handleHealthUpdate(byte id) {
    if (id == 10) {
      this.sheepTimer = 40;
    } else {
      super.handleHealthUpdate(id);
    } 
  }
  
  public float getHeadRotationPointY(float p_70894_1_) {
    return (this.sheepTimer <= 0) ? 0.0F : ((this.sheepTimer >= 4 && this.sheepTimer <= 36) ? 1.0F : ((this.sheepTimer < 4) ? ((this.sheepTimer - p_70894_1_) / 4.0F) : (-((this.sheepTimer - 40) - p_70894_1_) / 4.0F)));
  }
  
  public float getHeadRotationAngleX(float p_70890_1_) {
    if (this.sheepTimer > 4 && this.sheepTimer <= 36) {
      float f = ((this.sheepTimer - 4) - p_70890_1_) / 32.0F;
      return 0.62831855F + 0.2199115F * MathHelper.sin(f * 28.7F);
    } 
    return (this.sheepTimer > 0) ? 0.62831855F : (this.rotationPitch / 57.295776F);
  }
  
  public boolean interact(EntityPlayer player) {
    ItemStack itemstack = player.inventory.getCurrentItem();
    if (itemstack != null && itemstack.getItem() == Items.shears && !getSheared() && !isChild()) {
      if (!this.worldObj.isRemote) {
        setSheared(true);
        int i = 1 + this.rand.nextInt(3);
        for (int j = 0; j < i; j++) {
          EntityItem entityitem = entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, getFleeceColor().getMetadata()), 1.0F);
          entityitem.motionY += (this.rand.nextFloat() * 0.05F);
          entityitem.motionX += ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
          entityitem.motionZ += ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
        } 
      } 
      itemstack.damageItem(1, (EntityLivingBase)player);
      playSound("mob.sheep.shear", 1.0F, 1.0F);
    } 
    return super.interact(player);
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setBoolean("Sheared", getSheared());
    tagCompound.setByte("Color", (byte)getFleeceColor().getMetadata());
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    setSheared(tagCompund.getBoolean("Sheared"));
    setFleeceColor(EnumDyeColor.byMetadata(tagCompund.getByte("Color")));
  }
  
  protected String getLivingSound() {
    return "mob.sheep.say";
  }
  
  protected String getHurtSound() {
    return "mob.sheep.say";
  }
  
  protected String getDeathSound() {
    return "mob.sheep.say";
  }
  
  protected void playStepSound(BlockPos pos, Block blockIn) {
    playSound("mob.sheep.step", 0.15F, 1.0F);
  }
  
  public EnumDyeColor getFleeceColor() {
    return EnumDyeColor.byMetadata(this.dataWatcher.getWatchableObjectByte(16) & 0xF);
  }
  
  public void setFleeceColor(EnumDyeColor color) {
    byte b0 = this.dataWatcher.getWatchableObjectByte(16);
    this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & 0xF0 | color.getMetadata() & 0xF)));
  }
  
  public boolean getSheared() {
    return ((this.dataWatcher.getWatchableObjectByte(16) & 0x10) != 0);
  }
  
  public void setSheared(boolean sheared) {
    byte b0 = this.dataWatcher.getWatchableObjectByte(16);
    if (sheared) {
      this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 0x10)));
    } else {
      this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & 0xFFFFFFEF)));
    } 
  }
  
  public static EnumDyeColor getRandomSheepColor(Random random) {
    int i = random.nextInt(100);
    return (i < 5) ? EnumDyeColor.BLACK : ((i < 10) ? EnumDyeColor.GRAY : ((i < 15) ? EnumDyeColor.SILVER : ((i < 18) ? EnumDyeColor.BROWN : ((random.nextInt(500) == 0) ? EnumDyeColor.PINK : EnumDyeColor.WHITE))));
  }
  
  public EntitySheep createChild(EntityAgeable ageable) {
    EntitySheep entitysheep = (EntitySheep)ageable;
    EntitySheep entitysheep1 = new EntitySheep(this.worldObj);
    entitysheep1.setFleeceColor(getDyeColorMixFromParents(this, entitysheep));
    return entitysheep1;
  }
  
  public void eatGrassBonus() {
    setSheared(false);
    if (isChild())
      addGrowth(60); 
  }
  
  public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
    livingdata = super.onInitialSpawn(difficulty, livingdata);
    setFleeceColor(getRandomSheepColor(this.worldObj.rand));
    return livingdata;
  }
  
  private EnumDyeColor getDyeColorMixFromParents(EntityAnimal father, EntityAnimal mother) {
    int k, i = ((EntitySheep)father).getFleeceColor().getDyeDamage();
    int j = ((EntitySheep)mother).getFleeceColor().getDyeDamage();
    this.inventoryCrafting.getStackInSlot(0).setItemDamage(i);
    this.inventoryCrafting.getStackInSlot(1).setItemDamage(j);
    ItemStack itemstack = CraftingManager.getInstance().findMatchingRecipe(this.inventoryCrafting, ((EntitySheep)father).worldObj);
    if (itemstack != null && itemstack.getItem() == Items.dye) {
      k = itemstack.getMetadata();
    } else {
      k = this.worldObj.rand.nextBoolean() ? i : j;
    } 
    return EnumDyeColor.byDyeDamage(k);
  }
  
  public float getEyeHeight() {
    return 0.95F * this.height;
  }
  
  static {
    DYE_TO_RGB.put(EnumDyeColor.WHITE, new float[] { 1.0F, 1.0F, 1.0F });
    DYE_TO_RGB.put(EnumDyeColor.ORANGE, new float[] { 0.85F, 0.5F, 0.2F });
    DYE_TO_RGB.put(EnumDyeColor.MAGENTA, new float[] { 0.7F, 0.3F, 0.85F });
    DYE_TO_RGB.put(EnumDyeColor.LIGHT_BLUE, new float[] { 0.4F, 0.6F, 0.85F });
    DYE_TO_RGB.put(EnumDyeColor.YELLOW, new float[] { 0.9F, 0.9F, 0.2F });
    DYE_TO_RGB.put(EnumDyeColor.LIME, new float[] { 0.5F, 0.8F, 0.1F });
    DYE_TO_RGB.put(EnumDyeColor.PINK, new float[] { 0.95F, 0.5F, 0.65F });
    DYE_TO_RGB.put(EnumDyeColor.GRAY, new float[] { 0.3F, 0.3F, 0.3F });
    DYE_TO_RGB.put(EnumDyeColor.SILVER, new float[] { 0.6F, 0.6F, 0.6F });
    DYE_TO_RGB.put(EnumDyeColor.CYAN, new float[] { 0.3F, 0.5F, 0.6F });
    DYE_TO_RGB.put(EnumDyeColor.PURPLE, new float[] { 0.5F, 0.25F, 0.7F });
    DYE_TO_RGB.put(EnumDyeColor.BLUE, new float[] { 0.2F, 0.3F, 0.7F });
    DYE_TO_RGB.put(EnumDyeColor.BROWN, new float[] { 0.4F, 0.3F, 0.2F });
    DYE_TO_RGB.put(EnumDyeColor.GREEN, new float[] { 0.4F, 0.5F, 0.2F });
    DYE_TO_RGB.put(EnumDyeColor.RED, new float[] { 0.6F, 0.2F, 0.2F });
    DYE_TO_RGB.put(EnumDyeColor.BLACK, new float[] { 0.1F, 0.1F, 0.1F });
  }
}
