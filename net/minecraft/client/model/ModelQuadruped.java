package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelQuadruped extends ModelBase {
  public ModelRenderer head = new ModelRenderer(this, 0, 0);
  
  public ModelRenderer body;
  
  public ModelRenderer leg1;
  
  public ModelRenderer leg2;
  
  public ModelRenderer leg3;
  
  public ModelRenderer leg4;
  
  protected float childYOffset = 8.0F;
  
  protected float childZOffset = 4.0F;
  
  public ModelQuadruped(int p_i1154_1_, float p_i1154_2_) {
    this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, p_i1154_2_);
    this.head.setRotationPoint(0.0F, (18 - p_i1154_1_), -6.0F);
    this.body = new ModelRenderer(this, 28, 8);
    this.body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, p_i1154_2_);
    this.body.setRotationPoint(0.0F, (17 - p_i1154_1_), 2.0F);
    this.leg1 = new ModelRenderer(this, 0, 16);
    this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
    this.leg1.setRotationPoint(-3.0F, (24 - p_i1154_1_), 7.0F);
    this.leg2 = new ModelRenderer(this, 0, 16);
    this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
    this.leg2.setRotationPoint(3.0F, (24 - p_i1154_1_), 7.0F);
    this.leg3 = new ModelRenderer(this, 0, 16);
    this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
    this.leg3.setRotationPoint(-3.0F, (24 - p_i1154_1_), -5.0F);
    this.leg4 = new ModelRenderer(this, 0, 16);
    this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
    this.leg4.setRotationPoint(3.0F, (24 - p_i1154_1_), -5.0F);
  }
  
  public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
    setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
    if (this.isChild) {
      float f = 2.0F;
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, this.childYOffset * scale, this.childZOffset * scale);
      this.head.render(scale);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
      GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
      this.body.render(scale);
      this.leg1.render(scale);
      this.leg2.render(scale);
      this.leg3.render(scale);
      this.leg4.render(scale);
      GlStateManager.popMatrix();
    } else {
      this.head.render(scale);
      this.body.render(scale);
      this.leg1.render(scale);
      this.leg2.render(scale);
      this.leg3.render(scale);
      this.leg4.render(scale);
    } 
  }
  
  public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    float f = 57.295776F;
    this.head.rotateAngleX = headPitch / 57.295776F;
    this.head.rotateAngleY = netHeadYaw / 57.295776F;
    this.body.rotateAngleX = 1.5707964F;
    this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
    this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
    this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
  }
}
