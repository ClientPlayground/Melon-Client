package net.minecraft.server.management;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

public class ItemInWorldManager {
  public World theWorld;
  
  public EntityPlayerMP thisPlayerMP;
  
  private WorldSettings.GameType gameType = WorldSettings.GameType.NOT_SET;
  
  private boolean isDestroyingBlock;
  
  private int initialDamage;
  
  private BlockPos field_180240_f = BlockPos.ORIGIN;
  
  private int curblockDamage;
  
  private boolean receivedFinishDiggingPacket;
  
  private BlockPos field_180241_i = BlockPos.ORIGIN;
  
  private int initialBlockDamage;
  
  private int durabilityRemainingOnBlock = -1;
  
  public ItemInWorldManager(World worldIn) {
    this.theWorld = worldIn;
  }
  
  public void setGameType(WorldSettings.GameType type) {
    this.gameType = type;
    type.configurePlayerCapabilities(this.thisPlayerMP.capabilities);
    this.thisPlayerMP.sendPlayerAbilities();
    this.thisPlayerMP.mcServer.getConfigurationManager().sendPacketToAllPlayers((Packet)new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.UPDATE_GAME_MODE, new EntityPlayerMP[] { this.thisPlayerMP }));
  }
  
  public WorldSettings.GameType getGameType() {
    return this.gameType;
  }
  
  public boolean survivalOrAdventure() {
    return this.gameType.isSurvivalOrAdventure();
  }
  
  public boolean isCreative() {
    return this.gameType.isCreative();
  }
  
  public void initializeGameType(WorldSettings.GameType type) {
    if (this.gameType == WorldSettings.GameType.NOT_SET)
      this.gameType = type; 
    setGameType(this.gameType);
  }
  
  public void updateBlockRemoving() {
    this.curblockDamage++;
    if (this.receivedFinishDiggingPacket) {
      int i = this.curblockDamage - this.initialBlockDamage;
      Block block = this.theWorld.getBlockState(this.field_180241_i).getBlock();
      if (block.getMaterial() == Material.air) {
        this.receivedFinishDiggingPacket = false;
      } else {
        float f = block.getPlayerRelativeBlockHardness((EntityPlayer)this.thisPlayerMP, this.thisPlayerMP.worldObj, this.field_180241_i) * (i + 1);
        int j = (int)(f * 10.0F);
        if (j != this.durabilityRemainingOnBlock) {
          this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.field_180241_i, j);
          this.durabilityRemainingOnBlock = j;
        } 
        if (f >= 1.0F) {
          this.receivedFinishDiggingPacket = false;
          tryHarvestBlock(this.field_180241_i);
        } 
      } 
    } else if (this.isDestroyingBlock) {
      Block block1 = this.theWorld.getBlockState(this.field_180240_f).getBlock();
      if (block1.getMaterial() == Material.air) {
        this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.field_180240_f, -1);
        this.durabilityRemainingOnBlock = -1;
        this.isDestroyingBlock = false;
      } else {
        int k = this.curblockDamage - this.initialDamage;
        float f1 = block1.getPlayerRelativeBlockHardness((EntityPlayer)this.thisPlayerMP, this.thisPlayerMP.worldObj, this.field_180241_i) * (k + 1);
        int l = (int)(f1 * 10.0F);
        if (l != this.durabilityRemainingOnBlock) {
          this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.field_180240_f, l);
          this.durabilityRemainingOnBlock = l;
        } 
      } 
    } 
  }
  
  public void onBlockClicked(BlockPos pos, EnumFacing side) {
    if (isCreative()) {
      if (!this.theWorld.extinguishFire((EntityPlayer)null, pos, side))
        tryHarvestBlock(pos); 
    } else {
      Block block = this.theWorld.getBlockState(pos).getBlock();
      if (this.gameType.isAdventure()) {
        if (this.gameType == WorldSettings.GameType.SPECTATOR)
          return; 
        if (!this.thisPlayerMP.isAllowEdit()) {
          ItemStack itemstack = this.thisPlayerMP.getCurrentEquippedItem();
          if (itemstack == null)
            return; 
          if (!itemstack.canDestroy(block))
            return; 
        } 
      } 
      this.theWorld.extinguishFire((EntityPlayer)null, pos, side);
      this.initialDamage = this.curblockDamage;
      float f = 1.0F;
      if (block.getMaterial() != Material.air) {
        block.onBlockClicked(this.theWorld, pos, (EntityPlayer)this.thisPlayerMP);
        f = block.getPlayerRelativeBlockHardness((EntityPlayer)this.thisPlayerMP, this.thisPlayerMP.worldObj, pos);
      } 
      if (block.getMaterial() != Material.air && f >= 1.0F) {
        tryHarvestBlock(pos);
      } else {
        this.isDestroyingBlock = true;
        this.field_180240_f = pos;
        int i = (int)(f * 10.0F);
        this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), pos, i);
        this.durabilityRemainingOnBlock = i;
      } 
    } 
  }
  
  public void blockRemoving(BlockPos pos) {
    if (pos.equals(this.field_180240_f)) {
      int i = this.curblockDamage - this.initialDamage;
      Block block = this.theWorld.getBlockState(pos).getBlock();
      if (block.getMaterial() != Material.air) {
        float f = block.getPlayerRelativeBlockHardness((EntityPlayer)this.thisPlayerMP, this.thisPlayerMP.worldObj, pos) * (i + 1);
        if (f >= 0.7F) {
          this.isDestroyingBlock = false;
          this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), pos, -1);
          tryHarvestBlock(pos);
        } else if (!this.receivedFinishDiggingPacket) {
          this.isDestroyingBlock = false;
          this.receivedFinishDiggingPacket = true;
          this.field_180241_i = pos;
          this.initialBlockDamage = this.initialDamage;
        } 
      } 
    } 
  }
  
  public void cancelDestroyingBlock() {
    this.isDestroyingBlock = false;
    this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.field_180240_f, -1);
  }
  
  private boolean removeBlock(BlockPos pos) {
    IBlockState iblockstate = this.theWorld.getBlockState(pos);
    iblockstate.getBlock().onBlockHarvested(this.theWorld, pos, iblockstate, (EntityPlayer)this.thisPlayerMP);
    boolean flag = this.theWorld.setBlockToAir(pos);
    if (flag)
      iblockstate.getBlock().onBlockDestroyedByPlayer(this.theWorld, pos, iblockstate); 
    return flag;
  }
  
  public boolean tryHarvestBlock(BlockPos pos) {
    if (this.gameType.isCreative() && this.thisPlayerMP.getHeldItem() != null && this.thisPlayerMP.getHeldItem().getItem() instanceof net.minecraft.item.ItemSword)
      return false; 
    IBlockState iblockstate = this.theWorld.getBlockState(pos);
    TileEntity tileentity = this.theWorld.getTileEntity(pos);
    if (this.gameType.isAdventure()) {
      if (this.gameType == WorldSettings.GameType.SPECTATOR)
        return false; 
      if (!this.thisPlayerMP.isAllowEdit()) {
        ItemStack itemstack = this.thisPlayerMP.getCurrentEquippedItem();
        if (itemstack == null)
          return false; 
        if (!itemstack.canDestroy(iblockstate.getBlock()))
          return false; 
      } 
    } 
    this.theWorld.playAuxSFXAtEntity((EntityPlayer)this.thisPlayerMP, 2001, pos, Block.getStateId(iblockstate));
    boolean flag1 = removeBlock(pos);
    if (isCreative()) {
      this.thisPlayerMP.playerNetServerHandler.sendPacket((Packet)new S23PacketBlockChange(this.theWorld, pos));
    } else {
      ItemStack itemstack1 = this.thisPlayerMP.getCurrentEquippedItem();
      boolean flag = this.thisPlayerMP.canHarvestBlock(iblockstate.getBlock());
      if (itemstack1 != null) {
        itemstack1.onBlockDestroyed(this.theWorld, iblockstate.getBlock(), pos, (EntityPlayer)this.thisPlayerMP);
        if (itemstack1.stackSize == 0)
          this.thisPlayerMP.destroyCurrentEquippedItem(); 
      } 
      if (flag1 && flag)
        iblockstate.getBlock().harvestBlock(this.theWorld, (EntityPlayer)this.thisPlayerMP, pos, iblockstate, tileentity); 
    } 
    return flag1;
  }
  
  public boolean tryUseItem(EntityPlayer player, World worldIn, ItemStack stack) {
    if (this.gameType == WorldSettings.GameType.SPECTATOR)
      return false; 
    int i = stack.stackSize;
    int j = stack.getMetadata();
    ItemStack itemstack = stack.useItemRightClick(worldIn, player);
    if (itemstack != stack || (itemstack != null && (itemstack.stackSize != i || itemstack.getMaxItemUseDuration() > 0 || itemstack.getMetadata() != j))) {
      player.inventory.mainInventory[player.inventory.currentItem] = itemstack;
      if (isCreative()) {
        itemstack.stackSize = i;
        if (itemstack.isItemStackDamageable())
          itemstack.setItemDamage(j); 
      } 
      if (itemstack.stackSize == 0)
        player.inventory.mainInventory[player.inventory.currentItem] = null; 
      if (!player.isUsingItem())
        ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer); 
      return true;
    } 
    return false;
  }
  
  public boolean activateBlockOrUseItem(EntityPlayer player, World worldIn, ItemStack stack, BlockPos pos, EnumFacing side, float offsetX, float offsetY, float offsetZ) {
    if (this.gameType == WorldSettings.GameType.SPECTATOR) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof ILockableContainer) {
        Block block = worldIn.getBlockState(pos).getBlock();
        ILockableContainer ilockablecontainer = (ILockableContainer)tileentity;
        if (ilockablecontainer instanceof net.minecraft.tileentity.TileEntityChest && block instanceof BlockChest)
          ilockablecontainer = ((BlockChest)block).getLockableContainer(worldIn, pos); 
        if (ilockablecontainer != null) {
          player.displayGUIChest((IInventory)ilockablecontainer);
          return true;
        } 
      } else if (tileentity instanceof IInventory) {
        player.displayGUIChest((IInventory)tileentity);
        return true;
      } 
      return false;
    } 
    if (!player.isSneaking() || player.getHeldItem() == null) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, side, offsetX, offsetY, offsetZ))
        return true; 
    } 
    if (stack == null)
      return false; 
    if (isCreative()) {
      int j = stack.getMetadata();
      int i = stack.stackSize;
      boolean flag = stack.onItemUse(player, worldIn, pos, side, offsetX, offsetY, offsetZ);
      stack.setItemDamage(j);
      stack.stackSize = i;
      return flag;
    } 
    return stack.onItemUse(player, worldIn, pos, side, offsetX, offsetY, offsetZ);
  }
  
  public void setWorld(WorldServer serverWorld) {
    this.theWorld = (World)serverWorld;
  }
}
