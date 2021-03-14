package net.minecraft.client.renderer.block.statemap;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class DefaultStateMapper extends StateMapperBase {
  protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
    return new ModelResourceLocation((ResourceLocation)Block.blockRegistry.getNameForObject(state.getBlock()), getPropertyString((Map<IProperty, Comparable>)state.getProperties()));
  }
}
