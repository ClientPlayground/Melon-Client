package net.minecraft.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemBlock extends Item {
  protected final Block block;
  
  public ItemBlock(Block block) {
    this.block = block;
  }
  
  public ItemBlock setUnlocalizedName(String unlocalizedName) {
    super.setUnlocalizedName(unlocalizedName);
    return this;
  }
  
  public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
    IBlockState iblockstate = worldIn.getBlockState(pos);
    Block block = iblockstate.getBlock();
    if (!block.isReplaceable(worldIn, pos))
      pos = pos.offset(side); 
    if (stack.stackSize == 0)
      return false; 
    if (!playerIn.canPlayerEdit(pos, side, stack))
      return false; 
    if (worldIn.canBlockBePlaced(this.block, pos, false, side, (Entity)null, stack)) {
      int i = getMetadata(stack.getMetadata());
      IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, pos, side, hitX, hitY, hitZ, i, (EntityLivingBase)playerIn);
      if (worldIn.setBlockState(pos, iblockstate1, 3)) {
        iblockstate1 = worldIn.getBlockState(pos);
        if (iblockstate1.getBlock() == this.block) {
          setTileEntityNBT(worldIn, playerIn, pos, stack);
          this.block.onBlockPlacedBy(worldIn, pos, iblockstate1, (EntityLivingBase)playerIn, stack);
        } 
        worldIn.playSoundEffect((pos.getX() + 0.5F), (pos.getY() + 0.5F), (pos.getZ() + 0.5F), this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
        stack.stackSize--;
      } 
      return true;
    } 
    return false;
  }
  
  public static boolean setTileEntityNBT(World worldIn, EntityPlayer pos, BlockPos stack, ItemStack p_179224_3_) {
    MinecraftServer minecraftserver = MinecraftServer.getServer();
    if (minecraftserver == null)
      return false; 
    if (p_179224_3_.hasTagCompound() && p_179224_3_.getTagCompound().hasKey("BlockEntityTag", 10)) {
      TileEntity tileentity = worldIn.getTileEntity(stack);
      if (tileentity != null) {
        if (!worldIn.isRemote && tileentity.func_183000_F() && !minecraftserver.getConfigurationManager().canSendCommands(pos.getGameProfile()))
          return false; 
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttagcompound.copy();
        tileentity.writeToNBT(nbttagcompound);
        NBTTagCompound nbttagcompound2 = (NBTTagCompound)p_179224_3_.getTagCompound().getTag("BlockEntityTag");
        nbttagcompound.merge(nbttagcompound2);
        nbttagcompound.setInteger("x", stack.getX());
        nbttagcompound.setInteger("y", stack.getY());
        nbttagcompound.setInteger("z", stack.getZ());
        if (!nbttagcompound.equals(nbttagcompound1)) {
          tileentity.readFromNBT(nbttagcompound);
          tileentity.markDirty();
          return true;
        } 
      } 
    } 
    return false;
  }
  
  public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
    Block block = worldIn.getBlockState(pos).getBlock();
    if (block == Blocks.snow_layer) {
      side = EnumFacing.UP;
    } else if (!block.isReplaceable(worldIn, pos)) {
      pos = pos.offset(side);
    } 
    return worldIn.canBlockBePlaced(this.block, pos, false, side, (Entity)null, stack);
  }
  
  public String getUnlocalizedName(ItemStack stack) {
    return this.block.getUnlocalizedName();
  }
  
  public String getUnlocalizedName() {
    return this.block.getUnlocalizedName();
  }
  
  public CreativeTabs getCreativeTab() {
    return this.block.getCreativeTabToDisplayOn();
  }
  
  public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    this.block.getSubBlocks(itemIn, tab, subItems);
  }
  
  public Block getBlock() {
    return this.block;
  }
}
