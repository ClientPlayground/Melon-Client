package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelSlime extends ModelBase {
  ModelRenderer slimeBodies;
  
  ModelRenderer slimeRightEye;
  
  ModelRenderer slimeLeftEye;
  
  ModelRenderer slimeMouth;
  
  public ModelSlime(int p_i1157_1_) {
    this.slimeBodies = new ModelRenderer(this, 0, p_i1157_1_);
    this.slimeBodies.addBox(-4.0F, 16.0F, -4.0F, 8, 8, 8);
    if (p_i1157_1_ > 0) {
      this.slimeBodies = new ModelRenderer(this, 0, p_i1157_1_);
      this.slimeBodies.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
      this.slimeRightEye = new ModelRenderer(this, 32, 0);
      this.slimeRightEye.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
      this.slimeLeftEye = new ModelRenderer(this, 32, 4);
      this.slimeLeftEye.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
      this.slimeMouth = new ModelRenderer(this, 32, 8);
      this.slimeMouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
    } 
  }
  
  public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
    setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
    this.slimeBodies.render(scale);
    if (this.slimeRightEye != null) {
      this.slimeRightEye.render(scale);
      this.slimeLeftEye.render(scale);
      this.slimeMouth.render(scale);
    } 
  }
}
