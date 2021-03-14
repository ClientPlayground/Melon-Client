package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

public class BlockAnvil extends BlockFalling {
  public static final PropertyDirection FACING = PropertyDirection.create("facing", (Predicate)EnumFacing.Plane.HORIZONTAL);
  
  public static final PropertyInteger DAMAGE = PropertyInteger.create("damage", 0, 2);
  
  protected BlockAnvil() {
    super(Material.anvil);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.NORTH).withProperty((IProperty)DAMAGE, Integer.valueOf(0)));
    setLightOpacity(0);
    setCreativeTab(CreativeTabs.tabDecorations);
  }
  
  public boolean isFullCube() {
    return false;
  }
  
  public boolean isOpaqueCube() {
    return false;
  }
  
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    EnumFacing enumfacing = placer.getHorizontalFacing().rotateY();
    return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty((IProperty)FACING, (Comparable)enumfacing).withProperty((IProperty)DAMAGE, Integer.valueOf(meta >> 2));
  }
  
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!worldIn.isRemote)
      playerIn.displayGui(new Anvil(worldIn, pos)); 
    return true;
  }
  
  public int damageDropped(IBlockState state) {
    return ((Integer)state.getValue((IProperty)DAMAGE)).intValue();
  }
  
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue((IProperty)FACING);
    if (enumfacing.getAxis() == EnumFacing.Axis.X) {
      setBlockBounds(0.0F, 0.0F, 0.125F, 1.0F, 1.0F, 0.875F);
    } else {
      setBlockBounds(0.125F, 0.0F, 0.0F, 0.875F, 1.0F, 1.0F);
    } 
  }
  
  public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
    list.add(new ItemStack(itemIn, 1, 0));
    list.add(new ItemStack(itemIn, 1, 1));
    list.add(new ItemStack(itemIn, 1, 2));
  }
  
  protected void onStartFalling(EntityFallingBlock fallingEntity) {
    fallingEntity.setHurtEntities(true);
  }
  
  public void onEndFalling(World worldIn, BlockPos pos) {
    worldIn.playAuxSFX(1022, pos, 0);
  }
  
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return true;
  }
  
  public IBlockState getStateForEntityRender(IBlockState state) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)EnumFacing.SOUTH);
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)EnumFacing.getHorizontal(meta & 0x3)).withProperty((IProperty)DAMAGE, Integer.valueOf((meta & 0xF) >> 2));
  }
  
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    i |= ((EnumFacing)state.getValue((IProperty)FACING)).getHorizontalIndex();
    i |= ((Integer)state.getValue((IProperty)DAMAGE)).intValue() << 2;
    return i;
  }
  
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { (IProperty)FACING, (IProperty)DAMAGE });
  }
  
  public static class Anvil implements IInteractionObject {
    private final World world;
    
    private final BlockPos position;
    
    public Anvil(World worldIn, BlockPos pos) {
      this.world = worldIn;
      this.position = pos;
    }
    
    public String getCommandSenderName() {
      return "anvil";
    }
    
    public boolean hasCustomName() {
      return false;
    }
    
    public IChatComponent getDisplayName() {
      return (IChatComponent)new ChatComponentTranslation(Blocks.anvil.getUnlocalizedName() + ".name", new Object[0]);
    }
    
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
      return (Container)new ContainerRepair(playerInventory, this.world, this.position, playerIn);
    }
    
    public String getGuiID() {
      return "minecraft:anvil";
    }
  }
}
