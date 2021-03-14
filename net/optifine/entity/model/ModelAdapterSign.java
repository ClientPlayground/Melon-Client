package net.optifine.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntitySign;
import net.optifine.reflect.Reflector;

public class ModelAdapterSign extends ModelAdapter {
  public ModelAdapterSign() {
    super(TileEntitySign.class, "sign", 0.0F);
  }
  
  public ModelBase makeModel() {
    return (ModelBase)new ModelSign();
  }
  
  public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
    if (!(model instanceof ModelSign))
      return null; 
    ModelSign modelsign = (ModelSign)model;
    return modelPart.equals("board") ? modelsign.signBoard : (modelPart.equals("stick") ? modelsign.signStick : null);
  }
  
  public String[] getModelRendererNames() {
    return new String[] { "board", "stick" };
  }
  
  public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
    TileEntitySignRenderer tileEntitySignRenderer;
    TileEntityRendererDispatcher tileentityrendererdispatcher = TileEntityRendererDispatcher.instance;
    TileEntitySpecialRenderer tileentityspecialrenderer = tileentityrendererdispatcher.getSpecialRendererByClass(TileEntitySign.class);
    if (!(tileentityspecialrenderer instanceof TileEntitySignRenderer))
      return null; 
    if (tileentityspecialrenderer.getEntityClass() == null) {
      tileEntitySignRenderer = new TileEntitySignRenderer();
      tileEntitySignRenderer.setRendererDispatcher(tileentityrendererdispatcher);
    } 
    if (!Reflector.TileEntitySignRenderer_model.exists()) {
      Config.warn("Field not found: TileEntitySignRenderer.model");
      return null;
    } 
    Reflector.setFieldValue(tileEntitySignRenderer, Reflector.TileEntitySignRenderer_model, modelBase);
    return (IEntityRenderer)tileEntitySignRenderer;
  }
}
