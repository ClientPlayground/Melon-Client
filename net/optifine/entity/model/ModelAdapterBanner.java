package net.optifine.entity.model;

import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntityBanner;
import net.optifine.reflect.Reflector;

public class ModelAdapterBanner extends ModelAdapter {
  public ModelAdapterBanner() {
    super(TileEntityBanner.class, "banner", 0.0F);
  }
  
  public ModelBase makeModel() {
    return (ModelBase)new ModelBanner();
  }
  
  public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
    if (!(model instanceof ModelBanner))
      return null; 
    ModelBanner modelbanner = (ModelBanner)model;
    return modelPart.equals("slate") ? modelbanner.bannerSlate : (modelPart.equals("stand") ? modelbanner.bannerStand : (modelPart.equals("top") ? modelbanner.bannerTop : null));
  }
  
  public String[] getModelRendererNames() {
    return new String[] { "slate", "stand", "top" };
  }
  
  public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
    TileEntityBannerRenderer tileEntityBannerRenderer;
    TileEntityRendererDispatcher tileentityrendererdispatcher = TileEntityRendererDispatcher.instance;
    TileEntitySpecialRenderer tileentityspecialrenderer = tileentityrendererdispatcher.getSpecialRendererByClass(TileEntityBanner.class);
    if (!(tileentityspecialrenderer instanceof TileEntityBannerRenderer))
      return null; 
    if (tileentityspecialrenderer.getEntityClass() == null) {
      tileEntityBannerRenderer = new TileEntityBannerRenderer();
      tileEntityBannerRenderer.setRendererDispatcher(tileentityrendererdispatcher);
    } 
    if (!Reflector.TileEntityBannerRenderer_bannerModel.exists()) {
      Config.warn("Field not found: TileEntityBannerRenderer.bannerModel");
      return null;
    } 
    Reflector.setFieldValue(tileEntityBannerRenderer, Reflector.TileEntityBannerRenderer_bannerModel, modelBase);
    return (IEntityRenderer)tileEntityBannerRenderer;
  }
}
