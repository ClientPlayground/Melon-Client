package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelArmorStand;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.src.Config;

public class ModelAdapterArmorStand extends ModelAdapterBiped {
  public ModelAdapterArmorStand() {
    super(EntityArmorStand.class, "armor_stand", 0.0F);
  }
  
  public ModelBase makeModel() {
    return (ModelBase)new ModelArmorStand();
  }
  
  public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
    if (!(model instanceof ModelArmorStand))
      return null; 
    ModelArmorStand modelarmorstand = (ModelArmorStand)model;
    return modelPart.equals("right") ? modelarmorstand.standRightSide : (modelPart.equals("left") ? modelarmorstand.standLeftSide : (modelPart.equals("waist") ? modelarmorstand.standWaist : (modelPart.equals("base") ? modelarmorstand.standBase : super.getModelRenderer((ModelBase)modelarmorstand, modelPart))));
  }
  
  public String[] getModelRendererNames() {
    String[] astring = super.getModelRendererNames();
    astring = (String[])Config.addObjectsToArray((Object[])astring, (Object[])new String[] { "right", "left", "waist", "base" });
    return astring;
  }
  
  public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
    RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
    ArmorStandRenderer armorstandrenderer = new ArmorStandRenderer(rendermanager);
    armorstandrenderer.mainModel = modelBase;
    armorstandrenderer.shadowSize = shadowSize;
    return (IEntityRenderer)armorstandrenderer;
  }
}
