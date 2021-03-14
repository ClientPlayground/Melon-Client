package net.minecraft.client.entity;

import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.imp.Player.PlayerDamagedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

public class EntityPlayerSP extends AbstractClientPlayer {
  public final NetHandlerPlayClient sendQueue;
  
  private final StatFileWriter statWriter;
  
  private double lastReportedPosX;
  
  private double lastReportedPosY;
  
  private double lastReportedPosZ;
  
  private float lastReportedYaw;
  
  private float lastReportedPitch;
  
  private boolean serverSneakState;
  
  private boolean serverSprintState;
  
  private int positionUpdateTicks;
  
  private boolean hasValidHealth;
  
  private String clientBrand;
  
  public MovementInput movementInput;
  
  protected Minecraft mc;
  
  protected int sprintToggleTimer;
  
  public int sprintingTicksLeft;
  
  public float renderArmYaw;
  
  public float renderArmPitch;
  
  public float prevRenderArmYaw;
  
  public float prevRenderArmPitch;
  
  private int horseJumpPowerCounter;
  
  private float horseJumpPower;
  
  public float timeInPortal;
  
  public float prevTimeInPortal;
  
  public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatFileWriter statFile) {
    super(worldIn, netHandler.getGameProfile());
    this.sendQueue = netHandler;
    this.statWriter = statFile;
    this.mc = mcIn;
    this.dimension = 0;
  }
  
  public boolean attackEntityFrom(DamageSource source, float amount) {
    EventHandler.call((Event)new PlayerDamagedEvent(this, source, amount));
    return false;
  }
  
  public void heal(float healAmount) {}
  
  public void mountEntity(Entity entityIn) {
    super.mountEntity(entityIn);
    if (entityIn instanceof EntityMinecart)
      this.mc.getSoundHandler().playSound((ISound)new MovingSoundMinecartRiding(this, (EntityMinecart)entityIn)); 
  }
  
  public void onUpdate() {
    if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
      super.onUpdate();
      if (isRiding()) {
        this.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
        this.sendQueue.addToSendQueue((Packet)new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
      } else {
        onUpdateWalkingPlayer();
      } 
    } 
  }
  
  public void onUpdateWalkingPlayer() {
    boolean flag = isSprinting();
    if (flag != this.serverSprintState) {
      if (flag) {
        this.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)this, C0BPacketEntityAction.Action.START_SPRINTING));
      } else {
        this.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)this, C0BPacketEntityAction.Action.STOP_SPRINTING));
      } 
      this.serverSprintState = flag;
    } 
    boolean flag1 = isSneaking();
    if (flag1 != this.serverSneakState) {
      if (flag1) {
        this.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)this, C0BPacketEntityAction.Action.START_SNEAKING));
      } else {
        this.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)this, C0BPacketEntityAction.Action.STOP_SNEAKING));
      } 
      this.serverSneakState = flag1;
    } 
    if (isCurrentViewEntity()) {
      double d0 = this.posX - this.lastReportedPosX;
      double d1 = (getEntityBoundingBox()).minY - this.lastReportedPosY;
      double d2 = this.posZ - this.lastReportedPosZ;
      double d3 = (this.rotationYaw - this.lastReportedYaw);
      double d4 = (this.rotationPitch - this.lastReportedPitch);
      boolean flag2 = (d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.positionUpdateTicks >= 20);
      boolean flag3 = (d3 != 0.0D || d4 != 0.0D);
      if (this.ridingEntity == null) {
        if (flag2 && flag3) {
          this.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C06PacketPlayerPosLook(this.posX, (getEntityBoundingBox()).minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround));
        } else if (flag2) {
          this.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(this.posX, (getEntityBoundingBox()).minY, this.posZ, this.onGround));
        } else if (flag3) {
          this.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
        } else {
          this.sendQueue.addToSendQueue((Packet)new C03PacketPlayer(this.onGround));
        } 
      } else {
        this.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, this.rotationYaw, this.rotationPitch, this.onGround));
        flag2 = false;
      } 
      this.positionUpdateTicks++;
      if (flag2) {
        this.lastReportedPosX = this.posX;
        this.lastReportedPosY = (getEntityBoundingBox()).minY;
        this.lastReportedPosZ = this.posZ;
        this.positionUpdateTicks = 0;
      } 
      if (flag3) {
        this.lastReportedYaw = this.rotationYaw;
        this.lastReportedPitch = this.rotationPitch;
      } 
    } 
  }
  
  public EntityItem dropOneItem(boolean dropAll) {
    C07PacketPlayerDigging.Action c07packetplayerdigging$action = dropAll ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
    this.sendQueue.addToSendQueue((Packet)new C07PacketPlayerDigging(c07packetplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
    return null;
  }
  
  protected void joinEntityItemWithWorld(EntityItem itemIn) {}
  
  public void sendChatMessage(String message) {
    this.sendQueue.addToSendQueue((Packet)new C01PacketChatMessage(message));
  }
  
  public void swingItem() {
    super.swingItem();
    this.sendQueue.addToSendQueue((Packet)new C0APacketAnimation());
  }
  
  public void respawnPlayer() {
    this.sendQueue.addToSendQueue((Packet)new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
  }
  
  protected void damageEntity(DamageSource damageSrc, float damageAmount) {
    if (!isEntityInvulnerable(damageSrc))
      setHealth(getHealth() - damageAmount); 
  }
  
  public void closeScreen() {
    this.sendQueue.addToSendQueue((Packet)new C0DPacketCloseWindow(this.openContainer.windowId));
    closeScreenAndDropStack();
  }
  
  public void closeScreenAndDropStack() {
    this.inventory.setItemStack((ItemStack)null);
    super.closeScreen();
    this.mc.displayGuiScreen((GuiScreen)null);
  }
  
  public void setPlayerSPHealth(float health) {
    if (this.hasValidHealth) {
      float f = getHealth() - health;
      if (f <= 0.0F) {
        setHealth(health);
        if (f < 0.0F)
          this.hurtResistantTime = this.maxHurtResistantTime / 2; 
      } else {
        this.lastDamage = f;
        setHealth(getHealth());
        this.hurtResistantTime = this.maxHurtResistantTime;
        damageEntity(DamageSource.generic, f);
        this.hurtTime = this.maxHurtTime = 10;
      } 
    } else {
      setHealth(health);
      this.hasValidHealth = true;
    } 
  }
  
  public void addStat(StatBase stat, int amount) {
    if (stat != null)
      if (stat.isIndependent)
        super.addStat(stat, amount);  
  }
  
  public void sendPlayerAbilities() {
    this.sendQueue.addToSendQueue((Packet)new C13PacketPlayerAbilities(this.capabilities));
  }
  
  public boolean isUser() {
    return true;
  }
  
  protected void sendHorseJump() {
    this.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)this, C0BPacketEntityAction.Action.RIDING_JUMP, (int)(getHorseJumpPower() * 100.0F)));
  }
  
  public void sendHorseInventory() {
    this.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)this, C0BPacketEntityAction.Action.OPEN_INVENTORY));
  }
  
  public void setClientBrand(String brand) {
    this.clientBrand = brand;
  }
  
  public String getClientBrand() {
    return this.clientBrand;
  }
  
  public StatFileWriter getStatFileWriter() {
    return this.statWriter;
  }
  
  public void addChatComponentMessage(IChatComponent chatComponent) {
    this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
  }
  
  protected boolean pushOutOfBlocks(double x, double y, double z) {
    if (this.noClip)
      return false; 
    BlockPos blockpos = new BlockPos(x, y, z);
    double d0 = x - blockpos.getX();
    double d1 = z - blockpos.getZ();
    if (!isOpenBlockSpace(blockpos)) {
      int i = -1;
      double d2 = 9999.0D;
      if (isOpenBlockSpace(blockpos.west()) && d0 < d2) {
        d2 = d0;
        i = 0;
      } 
      if (isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2) {
        d2 = 1.0D - d0;
        i = 1;
      } 
      if (isOpenBlockSpace(blockpos.north()) && d1 < d2) {
        d2 = d1;
        i = 4;
      } 
      if (isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2) {
        d2 = 1.0D - d1;
        i = 5;
      } 
      float f = 0.1F;
      if (i == 0)
        this.motionX = -f; 
      if (i == 1)
        this.motionX = f; 
      if (i == 4)
        this.motionZ = -f; 
      if (i == 5)
        this.motionZ = f; 
    } 
    return false;
  }
  
  private boolean isOpenBlockSpace(BlockPos pos) {
    return (!this.worldObj.getBlockState(pos).getBlock().isNormalCube() && !this.worldObj.getBlockState(pos.up()).getBlock().isNormalCube());
  }
  
  public void setSprinting(boolean sprinting) {
    super.setSprinting(sprinting);
    this.sprintingTicksLeft = sprinting ? 600 : 0;
  }
  
  public void setXPStats(float currentXP, int maxXP, int level) {
    this.experience = currentXP;
    this.experienceTotal = maxXP;
    this.experienceLevel = level;
  }
  
  public void addChatMessage(IChatComponent component) {
    this.mc.ingameGUI.getChatGUI().printChatMessage(component);
  }
  
  public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
    return (permLevel <= 0);
  }
  
  public BlockPos getPosition() {
    return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
  }
  
  public void playSound(String name, float volume, float pitch) {
    this.worldObj.playSound(this.posX, this.posY, this.posZ, name, volume, pitch, false);
  }
  
  public boolean isServerWorld() {
    return true;
  }
  
  public boolean isRidingHorse() {
    return (this.ridingEntity != null && this.ridingEntity instanceof EntityHorse && ((EntityHorse)this.ridingEntity).isHorseSaddled());
  }
  
  public float getHorseJumpPower() {
    return this.horseJumpPower;
  }
  
  public void openEditSign(TileEntitySign signTile) {
    this.mc.displayGuiScreen((GuiScreen)new GuiEditSign(signTile));
  }
  
  public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic) {
    this.mc.displayGuiScreen((GuiScreen)new GuiCommandBlock(cmdBlockLogic));
  }
  
  public void displayGUIBook(ItemStack bookStack) {
    Item item = bookStack.getItem();
    if (item == Items.writable_book)
      this.mc.displayGuiScreen((GuiScreen)new GuiScreenBook(this, bookStack, true)); 
  }
  
  public void displayGUIChest(IInventory chestInventory) {
    String s = (chestInventory instanceof IInteractionObject) ? ((IInteractionObject)chestInventory).getGuiID() : "minecraft:container";
    if ("minecraft:chest".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiChest((IInventory)this.inventory, chestInventory));
    } else if ("minecraft:hopper".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiHopper(this.inventory, chestInventory));
    } else if ("minecraft:furnace".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiFurnace(this.inventory, chestInventory));
    } else if ("minecraft:brewing_stand".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiBrewingStand(this.inventory, chestInventory));
    } else if ("minecraft:beacon".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiBeacon(this.inventory, chestInventory));
    } else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiChest((IInventory)this.inventory, chestInventory));
    } else {
      this.mc.displayGuiScreen((GuiScreen)new GuiDispenser(this.inventory, chestInventory));
    } 
  }
  
  public void displayGUIHorse(EntityHorse horse, IInventory horseInventory) {
    this.mc.displayGuiScreen((GuiScreen)new GuiScreenHorseInventory((IInventory)this.inventory, horseInventory, horse));
  }
  
  public void displayGui(IInteractionObject guiOwner) {
    String s = guiOwner.getGuiID();
    if ("minecraft:crafting_table".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiCrafting(this.inventory, this.worldObj));
    } else if ("minecraft:enchanting_table".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiEnchantment(this.inventory, this.worldObj, (IWorldNameable)guiOwner));
    } else if ("minecraft:anvil".equals(s)) {
      this.mc.displayGuiScreen((GuiScreen)new GuiRepair(this.inventory, this.worldObj));
    } 
  }
  
  public void displayVillagerTradeGui(IMerchant villager) {
    this.mc.displayGuiScreen((GuiScreen)new GuiMerchant(this.inventory, villager, this.worldObj));
  }
  
  public void onCriticalHit(Entity entityHit) {
    this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
  }
  
  public void onEnchantmentCritical(Entity entityHit) {
    this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
  }
  
  public boolean isSneaking() {
    boolean flag = (this.movementInput != null) ? this.movementInput.sneak : false;
    return (flag && !this.sleeping);
  }
  
  public void updateEntityActionState() {
    super.updateEntityActionState();
    if (isCurrentViewEntity()) {
      this.moveStrafing = this.movementInput.moveStrafe;
      this.moveForward = this.movementInput.moveForward;
      this.isJumping = this.movementInput.jump;
      this.prevRenderArmYaw = this.renderArmYaw;
      this.prevRenderArmPitch = this.renderArmPitch;
      this.renderArmPitch = (float)(this.renderArmPitch + (this.rotationPitch - this.renderArmPitch) * 0.5D);
      this.renderArmYaw = (float)(this.renderArmYaw + (this.rotationYaw - this.renderArmYaw) * 0.5D);
    } 
  }
  
  protected boolean isCurrentViewEntity() {
    return (this.mc.getRenderViewEntity() == this);
  }
  
  public void onLivingUpdate() {
    if (this.sprintingTicksLeft > 0) {
      this.sprintingTicksLeft--;
      if (this.sprintingTicksLeft == 0)
        setSprinting(false); 
    } 
    if (this.sprintToggleTimer > 0)
      this.sprintToggleTimer--; 
    this.prevTimeInPortal = this.timeInPortal;
    if (this.inPortal) {
      if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame())
        this.mc.displayGuiScreen((GuiScreen)null); 
      if (this.timeInPortal == 0.0F)
        this.mc.getSoundHandler().playSound((ISound)PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F)); 
      this.timeInPortal += 0.0125F;
      if (this.timeInPortal >= 1.0F)
        this.timeInPortal = 1.0F; 
      this.inPortal = false;
    } else if (isPotionActive(Potion.confusion) && getActivePotionEffect(Potion.confusion).getDuration() > 60) {
      this.timeInPortal += 0.006666667F;
      if (this.timeInPortal > 1.0F)
        this.timeInPortal = 1.0F; 
    } else {
      if (this.timeInPortal > 0.0F)
        this.timeInPortal -= 0.05F; 
      if (this.timeInPortal < 0.0F)
        this.timeInPortal = 0.0F; 
    } 
    if (this.timeUntilPortal > 0)
      this.timeUntilPortal--; 
    boolean flag = this.movementInput.jump;
    boolean flag1 = this.movementInput.sneak;
    float f = 0.8F;
    boolean flag2 = (this.movementInput.moveForward >= f);
    this.movementInput.updatePlayerMoveState();
    if (isUsingItem() && !isRiding()) {
      this.movementInput.moveStrafe *= 0.2F;
      this.movementInput.moveForward *= 0.2F;
      this.sprintToggleTimer = 0;
    } 
    pushOutOfBlocks(this.posX - this.width * 0.35D, (getEntityBoundingBox()).minY + 0.5D, this.posZ + this.width * 0.35D);
    pushOutOfBlocks(this.posX - this.width * 0.35D, (getEntityBoundingBox()).minY + 0.5D, this.posZ - this.width * 0.35D);
    pushOutOfBlocks(this.posX + this.width * 0.35D, (getEntityBoundingBox()).minY + 0.5D, this.posZ - this.width * 0.35D);
    pushOutOfBlocks(this.posX + this.width * 0.35D, (getEntityBoundingBox()).minY + 0.5D, this.posZ + this.width * 0.35D);
    boolean flag3 = (getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying);
    if (this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= f && !isSprinting() && flag3 && !isUsingItem() && !isPotionActive(Potion.blindness))
      if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
        this.sprintToggleTimer = 7;
      } else {
        setSprinting(true);
      }  
    if (!isSprinting() && this.movementInput.moveForward >= f && flag3 && !isUsingItem() && !isPotionActive(Potion.blindness) && this.mc.gameSettings.keyBindSprint.isKeyDown())
      setSprinting(true); 
    if (isSprinting() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !flag3))
      setSprinting(false); 
    if (this.capabilities.allowFlying)
      if (this.mc.playerController.isSpectatorMode()) {
        if (!this.capabilities.isFlying) {
          this.capabilities.isFlying = true;
          sendPlayerAbilities();
        } 
      } else if (!flag && this.movementInput.jump) {
        if (this.flyToggleTimer == 0) {
          this.flyToggleTimer = 7;
        } else {
          this.capabilities.isFlying = !this.capabilities.isFlying;
          sendPlayerAbilities();
          this.flyToggleTimer = 0;
        } 
      }  
    if (this.capabilities.isFlying && isCurrentViewEntity()) {
      if (this.movementInput.sneak)
        this.motionY -= (this.capabilities.getFlySpeed() * 3.0F); 
      if (this.movementInput.jump)
        this.motionY += (this.capabilities.getFlySpeed() * 3.0F); 
    } 
    if (isRidingHorse()) {
      if (this.horseJumpPowerCounter < 0) {
        this.horseJumpPowerCounter++;
        if (this.horseJumpPowerCounter == 0)
          this.horseJumpPower = 0.0F; 
      } 
      if (flag && !this.movementInput.jump) {
        this.horseJumpPowerCounter = -10;
        sendHorseJump();
      } else if (!flag && this.movementInput.jump) {
        this.horseJumpPowerCounter = 0;
        this.horseJumpPower = 0.0F;
      } else if (flag) {
        this.horseJumpPowerCounter++;
        if (this.horseJumpPowerCounter < 10) {
          this.horseJumpPower = this.horseJumpPowerCounter * 0.1F;
        } else {
          this.horseJumpPower = 0.8F + 2.0F / (this.horseJumpPowerCounter - 9) * 0.1F;
        } 
      } 
    } else {
      this.horseJumpPower = 0.0F;
    } 
    super.onLivingUpdate();
    if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
      this.capabilities.isFlying = false;
      sendPlayerAbilities();
    } 
  }
}
