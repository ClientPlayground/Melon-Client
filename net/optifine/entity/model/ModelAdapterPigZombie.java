package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPigZombie;
import net.minecraft.entity.monster.EntityPigZombie;

public class ModelAdapterPigZombie extends ModelAdapterBiped {
  public ModelAdapterPigZombie() {
    super(EntityPigZombie.class, "zombie_pigman", 0.5F);
  }
  
  public ModelBase makeModel() {
    return (ModelBase)new ModelZombie();
  }
  
  public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
    RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
    RenderPigZombie renderpigzombie = new RenderPigZombie(rendermanager);
    Render.setModelBipedMain((RenderBiped)renderpigzombie, (ModelBiped)modelBase);
    renderpigzombie.mainModel = modelBase;
    renderpigzombie.shadowSize = shadowSize;
    return (IEntityRenderer)renderpigzombie;
  }
}
