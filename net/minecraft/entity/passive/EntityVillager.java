package net.minecraft.entity.passive;

import java.util.Random;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowGolem;
import net.minecraft.entity.ai.EntityAIHarvestFarmland;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIPlay;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Tuple;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityVillager extends EntityAgeable implements IMerchant, INpc {
  private int randomTickDivider;
  
  private boolean isMating;
  
  private boolean isPlaying;
  
  Village villageObj;
  
  private EntityPlayer buyingPlayer;
  
  private MerchantRecipeList buyingList;
  
  private int timeUntilReset;
  
  private boolean needsInitilization;
  
  private boolean isWillingToTrade;
  
  private int wealth;
  
  private String lastBuyingPlayer;
  
  private int careerId;
  
  private int careerLevel;
  
  private boolean isLookingForHome;
  
  private boolean areAdditionalTasksSet;
  
  private InventoryBasic villagerInventory;
  
  private static final ITradeList[][][][] DEFAULT_TRADE_LIST_MAP = new ITradeList[][][][] { { { { new EmeraldForItems(Items.wheat, new PriceInfo(18, 22)), new EmeraldForItems(Items.potato, new PriceInfo(15, 19)), new EmeraldForItems(Items.carrot, new PriceInfo(15, 19)), new ListItemForEmeralds(Items.bread, new PriceInfo(-4, -2)) }, { new EmeraldForItems(Item.getItemFromBlock(Blocks.pumpkin), new PriceInfo(8, 13)), new ListItemForEmeralds(Items.pumpkin_pie, new PriceInfo(-3, -2)) }, { new EmeraldForItems(Item.getItemFromBlock(Blocks.melon_block), new PriceInfo(7, 12)), new ListItemForEmeralds(Items.apple, new PriceInfo(-5, -7)) }, { new ListItemForEmeralds(Items.cookie, new PriceInfo(-6, -10)), new ListItemForEmeralds(Items.cake, new PriceInfo(1, 1)) } }, { { new EmeraldForItems(Items.string, new PriceInfo(15, 20)), new EmeraldForItems(Items.coal, new PriceInfo(16, 24)), new ItemAndEmeraldToItem(Items.fish, new PriceInfo(6, 6), Items.cooked_fish, new PriceInfo(6, 6)) }, { new ListEnchantedItemForEmeralds((Item)Items.fishing_rod, new PriceInfo(7, 8)) } }, { { new EmeraldForItems(Item.getItemFromBlock(Blocks.wool), new PriceInfo(16, 22)), new ListItemForEmeralds((Item)Items.shears, new PriceInfo(3, 4)) }, { 
            new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 0), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 1), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 2), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 3), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 4), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 5), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 6), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 7), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 8), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 9), new PriceInfo(1, 2)), 
            new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 10), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 11), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 12), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 13), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 14), new PriceInfo(1, 2)), new ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, 15), new PriceInfo(1, 2)) } }, { { new EmeraldForItems(Items.string, new PriceInfo(15, 20)), new ListItemForEmeralds(Items.arrow, new PriceInfo(-12, -8)) }, { new ListItemForEmeralds((Item)Items.bow, new PriceInfo(2, 3)), new ItemAndEmeraldToItem(Item.getItemFromBlock(Blocks.gravel), new PriceInfo(10, 10), Items.flint, new PriceInfo(6, 10)) } } }, { { { new EmeraldForItems(Items.paper, new PriceInfo(24, 36)), new ListEnchantedBookForEmeralds() }, { new EmeraldForItems(Items.book, new PriceInfo(8, 10)), new ListItemForEmeralds(Items.compass, new PriceInfo(10, 12)), new ListItemForEmeralds(Item.getItemFromBlock(Blocks.bookshelf), new PriceInfo(3, 4)) }, { new EmeraldForItems(Items.written_book, new PriceInfo(2, 2)), new ListItemForEmeralds(Items.clock, new PriceInfo(10, 12)), new ListItemForEmeralds(Item.getItemFromBlock(Blocks.glass), new PriceInfo(-5, -3)) }, { new ListEnchantedBookForEmeralds() }, { new ListEnchantedBookForEmeralds() }, { new ListItemForEmeralds(Items.name_tag, new PriceInfo(20, 22)) } } }, { { { new EmeraldForItems(Items.rotten_flesh, new PriceInfo(36, 40)), new EmeraldForItems(Items.gold_ingot, new PriceInfo(8, 10)) }, { new ListItemForEmeralds(Items.redstone, new PriceInfo(-4, -1)), new ListItemForEmeralds(new ItemStack(Items.dye, 1, EnumDyeColor.BLUE.getDyeDamage()), new PriceInfo(-2, -1)) }, { new ListItemForEmeralds(Items.ender_eye, new PriceInfo(7, 11)), new ListItemForEmeralds(Item.getItemFromBlock(Blocks.glowstone), new PriceInfo(-3, -1)) }, { new ListItemForEmeralds(Items.experience_bottle, new PriceInfo(3, 11)) } } }, { { { new EmeraldForItems(Items.coal, new PriceInfo(16, 24)), new ListItemForEmeralds((Item)Items.iron_helmet, new PriceInfo(4, 6)) }, { new EmeraldForItems(Items.iron_ingot, new PriceInfo(7, 9)), new ListItemForEmeralds((Item)Items.iron_chestplate, new PriceInfo(10, 14)) }, { new EmeraldForItems(Items.diamond, new PriceInfo(3, 4)), new ListEnchantedItemForEmeralds((Item)Items.diamond_chestplate, new PriceInfo(16, 19)) }, { new ListItemForEmeralds((Item)Items.chainmail_boots, new PriceInfo(5, 7)), new ListItemForEmeralds((Item)Items.chainmail_leggings, new PriceInfo(9, 11)), new ListItemForEmeralds((Item)Items.chainmail_helmet, new PriceInfo(5, 7)), new ListItemForEmeralds((Item)Items.chainmail_chestplate, new PriceInfo(11, 15)) } }, { { new EmeraldForItems(Items.coal, new PriceInfo(16, 24)), new ListItemForEmeralds(Items.iron_axe, new PriceInfo(6, 8)) }, { new EmeraldForItems(Items.iron_ingot, new PriceInfo(7, 9)), new ListEnchantedItemForEmeralds(Items.iron_sword, new PriceInfo(9, 10)) }, { new EmeraldForItems(Items.diamond, new PriceInfo(3, 4)), new ListEnchantedItemForEmeralds(Items.diamond_sword, new PriceInfo(12, 15)), new ListEnchantedItemForEmeralds(Items.diamond_axe, new PriceInfo(9, 12)) } }, { { new EmeraldForItems(Items.coal, new PriceInfo(16, 24)), new ListEnchantedItemForEmeralds(Items.iron_shovel, new PriceInfo(5, 7)) }, { new EmeraldForItems(Items.iron_ingot, new PriceInfo(7, 9)), new ListEnchantedItemForEmeralds(Items.iron_pickaxe, new PriceInfo(9, 11)) }, { new EmeraldForItems(Items.diamond, new PriceInfo(3, 4)), new ListEnchantedItemForEmeralds(Items.diamond_pickaxe, new PriceInfo(12, 15)) } } }, { { { new EmeraldForItems(Items.porkchop, new PriceInfo(14, 18)), new EmeraldForItems(Items.chicken, new PriceInfo(14, 18)) }, { new EmeraldForItems(Items.coal, new PriceInfo(16, 24)), new ListItemForEmeralds(Items.cooked_porkchop, new PriceInfo(-7, -5)), new ListItemForEmeralds(Items.cooked_chicken, new PriceInfo(-8, -6)) } }, { { new EmeraldForItems(Items.leather, new PriceInfo(9, 12)), new ListItemForEmeralds((Item)Items.leather_leggings, new PriceInfo(2, 4)) }, { new ListEnchantedItemForEmeralds((Item)Items.leather_chestplate, new PriceInfo(7, 12)) }, { new ListItemForEmeralds(Items.saddle, new PriceInfo(8, 10)) } } } };
  
  public EntityVillager(World worldIn) {
    this(worldIn, 0);
  }
  
  public EntityVillager(World worldIn, int professionId) {
    super(worldIn);
    this.villagerInventory = new InventoryBasic("Items", false, 8);
    setProfession(professionId);
    setSize(0.6F, 1.8F);
    ((PathNavigateGround)getNavigator()).setBreakDoors(true);
    ((PathNavigateGround)getNavigator()).setAvoidsWater(true);
    this.tasks.addTask(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
    this.tasks.addTask(1, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
    this.tasks.addTask(1, (EntityAIBase)new EntityAITradePlayer(this));
    this.tasks.addTask(1, (EntityAIBase)new EntityAILookAtTradePlayer(this));
    this.tasks.addTask(2, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
    this.tasks.addTask(3, (EntityAIBase)new EntityAIRestrictOpenDoor((EntityCreature)this));
    this.tasks.addTask(4, (EntityAIBase)new EntityAIOpenDoor((EntityLiving)this, true));
    this.tasks.addTask(5, (EntityAIBase)new EntityAIMoveTowardsRestriction((EntityCreature)this, 0.6D));
    this.tasks.addTask(6, (EntityAIBase)new EntityAIVillagerMate(this));
    this.tasks.addTask(7, (EntityAIBase)new EntityAIFollowGolem(this));
    this.tasks.addTask(9, (EntityAIBase)new EntityAIWatchClosest2((EntityLiving)this, EntityPlayer.class, 3.0F, 1.0F));
    this.tasks.addTask(9, (EntityAIBase)new EntityAIVillagerInteract(this));
    this.tasks.addTask(9, (EntityAIBase)new EntityAIWander((EntityCreature)this, 0.6D));
    this.tasks.addTask(10, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityLiving.class, 8.0F));
    setCanPickUpLoot(true);
  }
  
  private void setAdditionalAItasks() {
    if (!this.areAdditionalTasksSet) {
      this.areAdditionalTasksSet = true;
      if (isChild()) {
        this.tasks.addTask(8, (EntityAIBase)new EntityAIPlay(this, 0.32D));
      } else if (getProfession() == 0) {
        this.tasks.addTask(6, (EntityAIBase)new EntityAIHarvestFarmland(this, 0.6D));
      } 
    } 
  }
  
  protected void onGrowingAdult() {
    if (getProfession() == 0)
      this.tasks.addTask(8, (EntityAIBase)new EntityAIHarvestFarmland(this, 0.6D)); 
    super.onGrowingAdult();
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
  }
  
  protected void updateAITasks() {
    if (--this.randomTickDivider <= 0) {
      BlockPos blockpos = new BlockPos((Entity)this);
      this.worldObj.getVillageCollection().addToVillagerPositionList(blockpos);
      this.randomTickDivider = 70 + this.rand.nextInt(50);
      this.villageObj = this.worldObj.getVillageCollection().getNearestVillage(blockpos, 32);
      if (this.villageObj == null) {
        detachHome();
      } else {
        BlockPos blockpos1 = this.villageObj.getCenter();
        setHomePosAndDistance(blockpos1, (int)(this.villageObj.getVillageRadius() * 1.0F));
        if (this.isLookingForHome) {
          this.isLookingForHome = false;
          this.villageObj.setDefaultPlayerReputation(5);
        } 
      } 
    } 
    if (!isTrading() && this.timeUntilReset > 0) {
      this.timeUntilReset--;
      if (this.timeUntilReset <= 0) {
        if (this.needsInitilization) {
          for (MerchantRecipe merchantrecipe : this.buyingList) {
            if (merchantrecipe.isRecipeDisabled())
              merchantrecipe.increaseMaxTradeUses(this.rand.nextInt(6) + this.rand.nextInt(6) + 2); 
          } 
          populateBuyingList();
          this.needsInitilization = false;
          if (this.villageObj != null && this.lastBuyingPlayer != null) {
            this.worldObj.setEntityState((Entity)this, (byte)14);
            this.villageObj.setReputationForPlayer(this.lastBuyingPlayer, 1);
          } 
        } 
        addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
      } 
    } 
    super.updateAITasks();
  }
  
  public boolean interact(EntityPlayer player) {
    ItemStack itemstack = player.inventory.getCurrentItem();
    boolean flag = (itemstack != null && itemstack.getItem() == Items.spawn_egg);
    if (!flag && isEntityAlive() && !isTrading() && !isChild()) {
      if (!this.worldObj.isRemote && (this.buyingList == null || this.buyingList.size() > 0)) {
        setCustomer(player);
        player.displayVillagerTradeGui(this);
      } 
      player.triggerAchievement(StatList.timesTalkedToVillagerStat);
      return true;
    } 
    return super.interact(player);
  }
  
  protected void entityInit() {
    super.entityInit();
    this.dataWatcher.addObject(16, Integer.valueOf(0));
  }
  
  public void writeEntityToNBT(NBTTagCompound tagCompound) {
    super.writeEntityToNBT(tagCompound);
    tagCompound.setInteger("Profession", getProfession());
    tagCompound.setInteger("Riches", this.wealth);
    tagCompound.setInteger("Career", this.careerId);
    tagCompound.setInteger("CareerLevel", this.careerLevel);
    tagCompound.setBoolean("Willing", this.isWillingToTrade);
    if (this.buyingList != null)
      tagCompound.setTag("Offers", (NBTBase)this.buyingList.getRecipiesAsTags()); 
    NBTTagList nbttaglist = new NBTTagList();
    for (int i = 0; i < this.villagerInventory.getSizeInventory(); i++) {
      ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
      if (itemstack != null)
        nbttaglist.appendTag((NBTBase)itemstack.writeToNBT(new NBTTagCompound())); 
    } 
    tagCompound.setTag("Inventory", (NBTBase)nbttaglist);
  }
  
  public void readEntityFromNBT(NBTTagCompound tagCompund) {
    super.readEntityFromNBT(tagCompund);
    setProfession(tagCompund.getInteger("Profession"));
    this.wealth = tagCompund.getInteger("Riches");
    this.careerId = tagCompund.getInteger("Career");
    this.careerLevel = tagCompund.getInteger("CareerLevel");
    this.isWillingToTrade = tagCompund.getBoolean("Willing");
    if (tagCompund.hasKey("Offers", 10)) {
      NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("Offers");
      this.buyingList = new MerchantRecipeList(nbttagcompound);
    } 
    NBTTagList nbttaglist = tagCompund.getTagList("Inventory", 10);
    for (int i = 0; i < nbttaglist.tagCount(); i++) {
      ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
      if (itemstack != null)
        this.villagerInventory.func_174894_a(itemstack); 
    } 
    setCanPickUpLoot(true);
    setAdditionalAItasks();
  }
  
  protected boolean canDespawn() {
    return false;
  }
  
  protected String getLivingSound() {
    return isTrading() ? "mob.villager.haggle" : "mob.villager.idle";
  }
  
  protected String getHurtSound() {
    return "mob.villager.hit";
  }
  
  protected String getDeathSound() {
    return "mob.villager.death";
  }
  
  public void setProfession(int professionId) {
    this.dataWatcher.updateObject(16, Integer.valueOf(professionId));
  }
  
  public int getProfession() {
    return Math.max(this.dataWatcher.getWatchableObjectInt(16) % 5, 0);
  }
  
  public boolean isMating() {
    return this.isMating;
  }
  
  public void setMating(boolean mating) {
    this.isMating = mating;
  }
  
  public void setPlaying(boolean playing) {
    this.isPlaying = playing;
  }
  
  public boolean isPlaying() {
    return this.isPlaying;
  }
  
  public void setRevengeTarget(EntityLivingBase livingBase) {
    super.setRevengeTarget(livingBase);
    if (this.villageObj != null && livingBase != null) {
      this.villageObj.addOrRenewAgressor(livingBase);
      if (livingBase instanceof EntityPlayer) {
        int i = -1;
        if (isChild())
          i = -3; 
        this.villageObj.setReputationForPlayer(livingBase.getCommandSenderName(), i);
        if (isEntityAlive())
          this.worldObj.setEntityState((Entity)this, (byte)13); 
      } 
    } 
  }
  
  public void onDeath(DamageSource cause) {
    if (this.villageObj != null) {
      Entity entity = cause.getEntity();
      if (entity != null) {
        if (entity instanceof EntityPlayer) {
          this.villageObj.setReputationForPlayer(entity.getCommandSenderName(), -2);
        } else if (entity instanceof net.minecraft.entity.monster.IMob) {
          this.villageObj.endMatingSeason();
        } 
      } else {
        EntityPlayer entityplayer = this.worldObj.getClosestPlayerToEntity((Entity)this, 16.0D);
        if (entityplayer != null)
          this.villageObj.endMatingSeason(); 
      } 
    } 
    super.onDeath(cause);
  }
  
  public void setCustomer(EntityPlayer p_70932_1_) {
    this.buyingPlayer = p_70932_1_;
  }
  
  public EntityPlayer getCustomer() {
    return this.buyingPlayer;
  }
  
  public boolean isTrading() {
    return (this.buyingPlayer != null);
  }
  
  public boolean getIsWillingToTrade(boolean updateFirst) {
    if (!this.isWillingToTrade && updateFirst && func_175553_cp()) {
      boolean flag = false;
      for (int i = 0; i < this.villagerInventory.getSizeInventory(); i++) {
        ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
        if (itemstack != null)
          if (itemstack.getItem() == Items.bread && itemstack.stackSize >= 3) {
            flag = true;
            this.villagerInventory.decrStackSize(i, 3);
          } else if ((itemstack.getItem() == Items.potato || itemstack.getItem() == Items.carrot) && itemstack.stackSize >= 12) {
            flag = true;
            this.villagerInventory.decrStackSize(i, 12);
          }  
        if (flag) {
          this.worldObj.setEntityState((Entity)this, (byte)18);
          this.isWillingToTrade = true;
          break;
        } 
      } 
    } 
    return this.isWillingToTrade;
  }
  
  public void setIsWillingToTrade(boolean willingToTrade) {
    this.isWillingToTrade = willingToTrade;
  }
  
  public void useRecipe(MerchantRecipe recipe) {
    recipe.incrementToolUses();
    this.livingSoundTime = -getTalkInterval();
    playSound("mob.villager.yes", getSoundVolume(), getSoundPitch());
    int i = 3 + this.rand.nextInt(4);
    if (recipe.getToolUses() == 1 || this.rand.nextInt(5) == 0) {
      this.timeUntilReset = 40;
      this.needsInitilization = true;
      this.isWillingToTrade = true;
      if (this.buyingPlayer != null) {
        this.lastBuyingPlayer = this.buyingPlayer.getCommandSenderName();
      } else {
        this.lastBuyingPlayer = null;
      } 
      i += 5;
    } 
    if (recipe.getItemToBuy().getItem() == Items.emerald)
      this.wealth += (recipe.getItemToBuy()).stackSize; 
    if (recipe.getRewardsExp())
      this.worldObj.spawnEntityInWorld((Entity)new EntityXPOrb(this.worldObj, this.posX, this.posY + 0.5D, this.posZ, i)); 
  }
  
  public void verifySellingItem(ItemStack stack) {
    if (!this.worldObj.isRemote && this.livingSoundTime > -getTalkInterval() + 20) {
      this.livingSoundTime = -getTalkInterval();
      if (stack != null) {
        playSound("mob.villager.yes", getSoundVolume(), getSoundPitch());
      } else {
        playSound("mob.villager.no", getSoundVolume(), getSoundPitch());
      } 
    } 
  }
  
  public MerchantRecipeList getRecipes(EntityPlayer p_70934_1_) {
    if (this.buyingList == null)
      populateBuyingList(); 
    return this.buyingList;
  }
  
  private void populateBuyingList() {
    ITradeList[][][] aentityvillager$itradelist = DEFAULT_TRADE_LIST_MAP[getProfession()];
    if (this.careerId != 0 && this.careerLevel != 0) {
      this.careerLevel++;
    } else {
      this.careerId = this.rand.nextInt(aentityvillager$itradelist.length) + 1;
      this.careerLevel = 1;
    } 
    if (this.buyingList == null)
      this.buyingList = new MerchantRecipeList(); 
    int i = this.careerId - 1;
    int j = this.careerLevel - 1;
    ITradeList[][] aentityvillager$itradelist1 = aentityvillager$itradelist[i];
    if (j >= 0 && j < aentityvillager$itradelist1.length) {
      ITradeList[] aentityvillager$itradelist2 = aentityvillager$itradelist1[j];
      for (ITradeList entityvillager$itradelist : aentityvillager$itradelist2)
        entityvillager$itradelist.modifyMerchantRecipeList(this.buyingList, this.rand); 
    } 
  }
  
  public void setRecipes(MerchantRecipeList recipeList) {}
  
  public IChatComponent getDisplayName() {
    String s = getCustomNameTag();
    if (s != null && s.length() > 0) {
      ChatComponentText chatcomponenttext = new ChatComponentText(s);
      chatcomponenttext.getChatStyle().setChatHoverEvent(getHoverEvent());
      chatcomponenttext.getChatStyle().setInsertion(getUniqueID().toString());
      return (IChatComponent)chatcomponenttext;
    } 
    if (this.buyingList == null)
      populateBuyingList(); 
    String s1 = null;
    switch (getProfession()) {
      case 0:
        if (this.careerId == 1) {
          s1 = "farmer";
          break;
        } 
        if (this.careerId == 2) {
          s1 = "fisherman";
          break;
        } 
        if (this.careerId == 3) {
          s1 = "shepherd";
          break;
        } 
        if (this.careerId == 4)
          s1 = "fletcher"; 
        break;
      case 1:
        s1 = "librarian";
        break;
      case 2:
        s1 = "cleric";
        break;
      case 3:
        if (this.careerId == 1) {
          s1 = "armor";
          break;
        } 
        if (this.careerId == 2) {
          s1 = "weapon";
          break;
        } 
        if (this.careerId == 3)
          s1 = "tool"; 
        break;
      case 4:
        if (this.careerId == 1) {
          s1 = "butcher";
          break;
        } 
        if (this.careerId == 2)
          s1 = "leather"; 
        break;
    } 
    if (s1 != null) {
      ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("entity.Villager." + s1, new Object[0]);
      chatcomponenttranslation.getChatStyle().setChatHoverEvent(getHoverEvent());
      chatcomponenttranslation.getChatStyle().setInsertion(getUniqueID().toString());
      return (IChatComponent)chatcomponenttranslation;
    } 
    return super.getDisplayName();
  }
  
  public float getEyeHeight() {
    float f = 1.62F;
    if (isChild())
      f = (float)(f - 0.81D); 
    return f;
  }
  
  public void handleHealthUpdate(byte id) {
    if (id == 12) {
      spawnParticles(EnumParticleTypes.HEART);
    } else if (id == 13) {
      spawnParticles(EnumParticleTypes.VILLAGER_ANGRY);
    } else if (id == 14) {
      spawnParticles(EnumParticleTypes.VILLAGER_HAPPY);
    } else {
      super.handleHealthUpdate(id);
    } 
  }
  
  private void spawnParticles(EnumParticleTypes particleType) {
    for (int i = 0; i < 5; i++) {
      double d0 = this.rand.nextGaussian() * 0.02D;
      double d1 = this.rand.nextGaussian() * 0.02D;
      double d2 = this.rand.nextGaussian() * 0.02D;
      this.worldObj.spawnParticle(particleType, this.posX + (this.rand.nextFloat() * this.width * 2.0F) - this.width, this.posY + 1.0D + (this.rand.nextFloat() * this.height), this.posZ + (this.rand.nextFloat() * this.width * 2.0F) - this.width, d0, d1, d2, new int[0]);
    } 
  }
  
  public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
    livingdata = super.onInitialSpawn(difficulty, livingdata);
    setProfession(this.worldObj.rand.nextInt(5));
    setAdditionalAItasks();
    return livingdata;
  }
  
  public void setLookingForHome() {
    this.isLookingForHome = true;
  }
  
  public EntityVillager createChild(EntityAgeable ageable) {
    EntityVillager entityvillager = new EntityVillager(this.worldObj);
    entityvillager.onInitialSpawn(this.worldObj.getDifficultyForLocation(new BlockPos((Entity)entityvillager)), (IEntityLivingData)null);
    return entityvillager;
  }
  
  public boolean allowLeashing() {
    return false;
  }
  
  public void onStruckByLightning(EntityLightningBolt lightningBolt) {
    if (!this.worldObj.isRemote && !this.isDead) {
      EntityWitch entitywitch = new EntityWitch(this.worldObj);
      entitywitch.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      entitywitch.onInitialSpawn(this.worldObj.getDifficultyForLocation(new BlockPos((Entity)entitywitch)), (IEntityLivingData)null);
      entitywitch.setNoAI(isAIDisabled());
      if (hasCustomName()) {
        entitywitch.setCustomNameTag(getCustomNameTag());
        entitywitch.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
      } 
      this.worldObj.spawnEntityInWorld((Entity)entitywitch);
      setDead();
    } 
  }
  
  public InventoryBasic getVillagerInventory() {
    return this.villagerInventory;
  }
  
  protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
    ItemStack itemstack = itemEntity.getEntityItem();
    Item item = itemstack.getItem();
    if (canVillagerPickupItem(item)) {
      ItemStack itemstack1 = this.villagerInventory.func_174894_a(itemstack);
      if (itemstack1 == null) {
        itemEntity.setDead();
      } else {
        itemstack.stackSize = itemstack1.stackSize;
      } 
    } 
  }
  
  private boolean canVillagerPickupItem(Item itemIn) {
    return (itemIn == Items.bread || itemIn == Items.potato || itemIn == Items.carrot || itemIn == Items.wheat || itemIn == Items.wheat_seeds);
  }
  
  public boolean func_175553_cp() {
    return hasEnoughItems(1);
  }
  
  public boolean canAbondonItems() {
    return hasEnoughItems(2);
  }
  
  public boolean func_175557_cr() {
    boolean flag = (getProfession() == 0);
    return flag ? (!hasEnoughItems(5)) : (!hasEnoughItems(1));
  }
  
  private boolean hasEnoughItems(int multiplier) {
    boolean flag = (getProfession() == 0);
    for (int i = 0; i < this.villagerInventory.getSizeInventory(); i++) {
      ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
      if (itemstack != null) {
        if ((itemstack.getItem() == Items.bread && itemstack.stackSize >= 3 * multiplier) || (itemstack.getItem() == Items.potato && itemstack.stackSize >= 12 * multiplier) || (itemstack.getItem() == Items.carrot && itemstack.stackSize >= 12 * multiplier))
          return true; 
        if (flag && itemstack.getItem() == Items.wheat && itemstack.stackSize >= 9 * multiplier)
          return true; 
      } 
    } 
    return false;
  }
  
  public boolean isFarmItemInInventory() {
    for (int i = 0; i < this.villagerInventory.getSizeInventory(); i++) {
      ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
      if (itemstack != null && (itemstack.getItem() == Items.wheat_seeds || itemstack.getItem() == Items.potato || itemstack.getItem() == Items.carrot))
        return true; 
    } 
    return false;
  }
  
  public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
    if (super.replaceItemInInventory(inventorySlot, itemStackIn))
      return true; 
    int i = inventorySlot - 300;
    if (i >= 0 && i < this.villagerInventory.getSizeInventory()) {
      this.villagerInventory.setInventorySlotContents(i, itemStackIn);
      return true;
    } 
    return false;
  }
  
  static class EmeraldForItems implements ITradeList {
    public Item sellItem;
    
    public EntityVillager.PriceInfo price;
    
    public EmeraldForItems(Item itemIn, EntityVillager.PriceInfo priceIn) {
      this.sellItem = itemIn;
      this.price = priceIn;
    }
    
    public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random) {
      int i = 1;
      if (this.price != null)
        i = this.price.getPrice(random); 
      recipeList.add(new MerchantRecipe(new ItemStack(this.sellItem, i, 0), Items.emerald));
    }
  }
  
  static interface ITradeList {
    void modifyMerchantRecipeList(MerchantRecipeList param1MerchantRecipeList, Random param1Random);
  }
  
  static class ItemAndEmeraldToItem implements ITradeList {
    public ItemStack buyingItemStack;
    
    public EntityVillager.PriceInfo buyingPriceInfo;
    
    public ItemStack sellingItemstack;
    
    public EntityVillager.PriceInfo field_179408_d;
    
    public ItemAndEmeraldToItem(Item p_i45813_1_, EntityVillager.PriceInfo p_i45813_2_, Item p_i45813_3_, EntityVillager.PriceInfo p_i45813_4_) {
      this.buyingItemStack = new ItemStack(p_i45813_1_);
      this.buyingPriceInfo = p_i45813_2_;
      this.sellingItemstack = new ItemStack(p_i45813_3_);
      this.field_179408_d = p_i45813_4_;
    }
    
    public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random) {
      int i = 1;
      if (this.buyingPriceInfo != null)
        i = this.buyingPriceInfo.getPrice(random); 
      int j = 1;
      if (this.field_179408_d != null)
        j = this.field_179408_d.getPrice(random); 
      recipeList.add(new MerchantRecipe(new ItemStack(this.buyingItemStack.getItem(), i, this.buyingItemStack.getMetadata()), new ItemStack(Items.emerald), new ItemStack(this.sellingItemstack.getItem(), j, this.sellingItemstack.getMetadata())));
    }
  }
  
  static class ListEnchantedBookForEmeralds implements ITradeList {
    public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random) {
      Enchantment enchantment = Enchantment.enchantmentsBookList[random.nextInt(Enchantment.enchantmentsBookList.length)];
      int i = MathHelper.getRandomIntegerInRange(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
      ItemStack itemstack = Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(enchantment, i));
      int j = 2 + random.nextInt(5 + i * 10) + 3 * i;
      if (j > 64)
        j = 64; 
      recipeList.add(new MerchantRecipe(new ItemStack(Items.book), new ItemStack(Items.emerald, j), itemstack));
    }
  }
  
  static class ListEnchantedItemForEmeralds implements ITradeList {
    public ItemStack enchantedItemStack;
    
    public EntityVillager.PriceInfo priceInfo;
    
    public ListEnchantedItemForEmeralds(Item p_i45814_1_, EntityVillager.PriceInfo p_i45814_2_) {
      this.enchantedItemStack = new ItemStack(p_i45814_1_);
      this.priceInfo = p_i45814_2_;
    }
    
    public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random) {
      int i = 1;
      if (this.priceInfo != null)
        i = this.priceInfo.getPrice(random); 
      ItemStack itemstack = new ItemStack(Items.emerald, i, 0);
      ItemStack itemstack1 = new ItemStack(this.enchantedItemStack.getItem(), 1, this.enchantedItemStack.getMetadata());
      itemstack1 = EnchantmentHelper.addRandomEnchantment(random, itemstack1, 5 + random.nextInt(15));
      recipeList.add(new MerchantRecipe(itemstack, itemstack1));
    }
  }
  
  static class ListItemForEmeralds implements ITradeList {
    public ItemStack itemToBuy;
    
    public EntityVillager.PriceInfo priceInfo;
    
    public ListItemForEmeralds(Item par1Item, EntityVillager.PriceInfo priceInfo) {
      this.itemToBuy = new ItemStack(par1Item);
      this.priceInfo = priceInfo;
    }
    
    public ListItemForEmeralds(ItemStack stack, EntityVillager.PriceInfo priceInfo) {
      this.itemToBuy = stack;
      this.priceInfo = priceInfo;
    }
    
    public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random) {
      ItemStack itemstack, itemstack1;
      int i = 1;
      if (this.priceInfo != null)
        i = this.priceInfo.getPrice(random); 
      if (i < 0) {
        itemstack = new ItemStack(Items.emerald, 1, 0);
        itemstack1 = new ItemStack(this.itemToBuy.getItem(), -i, this.itemToBuy.getMetadata());
      } else {
        itemstack = new ItemStack(Items.emerald, i, 0);
        itemstack1 = new ItemStack(this.itemToBuy.getItem(), 1, this.itemToBuy.getMetadata());
      } 
      recipeList.add(new MerchantRecipe(itemstack, itemstack1));
    }
  }
  
  static class PriceInfo extends Tuple<Integer, Integer> {
    public PriceInfo(int p_i45810_1_, int p_i45810_2_) {
      super(Integer.valueOf(p_i45810_1_), Integer.valueOf(p_i45810_2_));
    }
    
    public int getPrice(Random rand) {
      return (((Integer)getFirst()).intValue() >= ((Integer)getSecond()).intValue()) ? ((Integer)getFirst()).intValue() : (((Integer)getFirst()).intValue() + rand.nextInt(((Integer)getSecond()).intValue() - ((Integer)getFirst()).intValue() + 1));
    }
  }
}
