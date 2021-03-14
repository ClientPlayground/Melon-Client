package net.optifine.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntityChest;
import net.optifine.reflect.Reflector;

public class ModelAdapterChest extends ModelAdapter {
  public ModelAdapterChest() {
    super(TileEntityChest.class, "chest", 0.0F);
  }
  
  public ModelBase makeModel() {
    return (ModelBase)new ModelChest();
  }
  
  public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
    if (!(model instanceof ModelChest))
      return null; 
    ModelChest modelchest = (ModelChest)model;
    return modelPart.equals("lid") ? modelchest.chestLid : (modelPart.equals("base") ? modelchest.chestBelow : (modelPart.equals("knob") ? modelchest.chestKnob : null));
  }
  
  public String[] getModelRendererNames() {
    return new String[] { "lid", "base", "knob" };
  }
  
  public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
    TileEntityChestRenderer tileEntityChestRenderer;
    TileEntityRendererDispatcher tileentityrendererdispatcher = TileEntityRendererDispatcher.instance;
    TileEntitySpecialRenderer tileentityspecialrenderer = tileentityrendererdispatcher.getSpecialRendererByClass(TileEntityChest.class);
    if (!(tileentityspecialrenderer instanceof TileEntityChestRenderer))
      return null; 
    if (tileentityspecialrenderer.getEntityClass() == null) {
      tileEntityChestRenderer = new TileEntityChestRenderer();
      tileEntityChestRenderer.setRendererDispatcher(tileentityrendererdispatcher);
    } 
    if (!Reflector.TileEntityChestRenderer_simpleChest.exists()) {
      Config.warn("Field not found: TileEntityChestRenderer.simpleChest");
      return null;
    } 
    Reflector.setFieldValue(tileEntityChestRenderer, Reflector.TileEntityChestRenderer_simpleChest, modelBase);
    return (IEntityRenderer)tileEntityChestRenderer;
  }
}
