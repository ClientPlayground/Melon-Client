package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TileEntityPistonRenderer extends TileEntitySpecialRenderer<TileEntityPiston> {
  private final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
  
  public void renderTileEntityAt(TileEntityPiston te, double x, double y, double z, float partialTicks, int destroyStage) {
    BlockPos blockpos = te.getPos();
    IBlockState iblockstate = te.getPistonState();
    Block block = iblockstate.getBlock();
    if (block.getMaterial() != Material.air && te.getProgress(partialTicks) < 1.0F) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      bindTexture(TextureMap.locationBlocksTexture);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.enableBlend();
      GlStateManager.disableCull();
      if (Minecraft.isAmbientOcclusionEnabled()) {
        GlStateManager.shadeModel(7425);
      } else {
        GlStateManager.shadeModel(7424);
      } 
      worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
      worldrenderer.setTranslation(((float)x - blockpos.getX() + te.getOffsetX(partialTicks)), ((float)y - blockpos.getY() + te.getOffsetY(partialTicks)), ((float)z - blockpos.getZ() + te.getOffsetZ(partialTicks)));
      World world = getWorld();
      if (block == Blocks.piston_head && te.getProgress(partialTicks) < 0.5F) {
        iblockstate = iblockstate.withProperty((IProperty)BlockPistonExtension.SHORT, Boolean.valueOf(true));
        this.blockRenderer.getBlockModelRenderer().renderModel((IBlockAccess)world, this.blockRenderer.getModelFromBlockState(iblockstate, (IBlockAccess)world, blockpos), iblockstate, blockpos, worldrenderer, true);
      } else if (te.shouldPistonHeadBeRendered() && !te.isExtending()) {
        BlockPistonExtension.EnumPistonType blockpistonextension$enumpistontype = (block == Blocks.sticky_piston) ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;
        IBlockState iblockstate1 = Blocks.piston_head.getDefaultState().withProperty((IProperty)BlockPistonExtension.TYPE, (Comparable)blockpistonextension$enumpistontype).withProperty((IProperty)BlockPistonExtension.FACING, iblockstate.getValue((IProperty)BlockPistonBase.FACING));
        iblockstate1 = iblockstate1.withProperty((IProperty)BlockPistonExtension.SHORT, Boolean.valueOf((te.getProgress(partialTicks) >= 0.5F)));
        this.blockRenderer.getBlockModelRenderer().renderModel((IBlockAccess)world, this.blockRenderer.getModelFromBlockState(iblockstate1, (IBlockAccess)world, blockpos), iblockstate1, blockpos, worldrenderer, true);
        worldrenderer.setTranslation(((float)x - blockpos.getX()), ((float)y - blockpos.getY()), ((float)z - blockpos.getZ()));
        iblockstate.withProperty((IProperty)BlockPistonBase.EXTENDED, Boolean.valueOf(true));
        this.blockRenderer.getBlockModelRenderer().renderModel((IBlockAccess)world, this.blockRenderer.getModelFromBlockState(iblockstate, (IBlockAccess)world, blockpos), iblockstate, blockpos, worldrenderer, true);
      } else {
        this.blockRenderer.getBlockModelRenderer().renderModel((IBlockAccess)world, this.blockRenderer.getModelFromBlockState(iblockstate, (IBlockAccess)world, blockpos), iblockstate, blockpos, worldrenderer, false);
      } 
      worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
      tessellator.draw();
      RenderHelper.enableStandardItemLighting();
    } 
  }
}
