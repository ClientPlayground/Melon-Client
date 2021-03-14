package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.MathHelper;

public class ModelBat extends ModelBase {
  private ModelRenderer batHead;
  
  private ModelRenderer batBody;
  
  private ModelRenderer batRightWing;
  
  private ModelRenderer batLeftWing;
  
  private ModelRenderer batOuterRightWing;
  
  private ModelRenderer batOuterLeftWing;
  
  public ModelBat() {
    this.textureWidth = 64;
    this.textureHeight = 64;
    this.batHead = new ModelRenderer(this, 0, 0);
    this.batHead.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
    ModelRenderer modelrenderer = new ModelRenderer(this, 24, 0);
    modelrenderer.addBox(-4.0F, -6.0F, -2.0F, 3, 4, 1);
    this.batHead.addChild(modelrenderer);
    ModelRenderer modelrenderer1 = new ModelRenderer(this, 24, 0);
    modelrenderer1.mirror = true;
    modelrenderer1.addBox(1.0F, -6.0F, -2.0F, 3, 4, 1);
    this.batHead.addChild(modelrenderer1);
    this.batBody = new ModelRenderer(this, 0, 16);
    this.batBody.addBox(-3.0F, 4.0F, -3.0F, 6, 12, 6);
    this.batBody.setTextureOffset(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10, 6, 1);
    this.batRightWing = new ModelRenderer(this, 42, 0);
    this.batRightWing.addBox(-12.0F, 1.0F, 1.5F, 10, 16, 1);
    this.batOuterRightWing = new ModelRenderer(this, 24, 16);
    this.batOuterRightWing.setRotationPoint(-12.0F, 1.0F, 1.5F);
    this.batOuterRightWing.addBox(-8.0F, 1.0F, 0.0F, 8, 12, 1);
    this.batLeftWing = new ModelRenderer(this, 42, 0);
    this.batLeftWing.mirror = true;
    this.batLeftWing.addBox(2.0F, 1.0F, 1.5F, 10, 16, 1);
    this.batOuterLeftWing = new ModelRenderer(this, 24, 16);
    this.batOuterLeftWing.mirror = true;
    this.batOuterLeftWing.setRotationPoint(12.0F, 1.0F, 1.5F);
    this.batOuterLeftWing.addBox(0.0F, 1.0F, 0.0F, 8, 12, 1);
    this.batBody.addChild(this.batRightWing);
    this.batBody.addChild(this.batLeftWing);
    this.batRightWing.addChild(this.batOuterRightWing);
    this.batLeftWing.addChild(this.batOuterLeftWing);
  }
  
  public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
    setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
    this.batHead.render(scale);
    this.batBody.render(scale);
  }
  
  public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    if (((EntityBat)entityIn).getIsBatHanging()) {
      float f = 57.295776F;
      this.batHead.rotateAngleX = headPitch / 57.295776F;
      this.batHead.rotateAngleY = 3.1415927F - netHeadYaw / 57.295776F;
      this.batHead.rotateAngleZ = 3.1415927F;
      this.batHead.setRotationPoint(0.0F, -2.0F, 0.0F);
      this.batRightWing.setRotationPoint(-3.0F, 0.0F, 3.0F);
      this.batLeftWing.setRotationPoint(3.0F, 0.0F, 3.0F);
      this.batBody.rotateAngleX = 3.1415927F;
      this.batRightWing.rotateAngleX = -0.15707964F;
      this.batRightWing.rotateAngleY = -1.2566371F;
      this.batOuterRightWing.rotateAngleY = -1.7278761F;
      this.batLeftWing.rotateAngleX = this.batRightWing.rotateAngleX;
      this.batLeftWing.rotateAngleY = -this.batRightWing.rotateAngleY;
      this.batOuterLeftWing.rotateAngleY = -this.batOuterRightWing.rotateAngleY;
    } else {
      float f1 = 57.295776F;
      this.batHead.rotateAngleX = headPitch / 57.295776F;
      this.batHead.rotateAngleY = netHeadYaw / 57.295776F;
      this.batHead.rotateAngleZ = 0.0F;
      this.batHead.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.batRightWing.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.batLeftWing.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.batBody.rotateAngleX = 0.7853982F + MathHelper.cos(ageInTicks * 0.1F) * 0.15F;
      this.batBody.rotateAngleY = 0.0F;
      this.batRightWing.rotateAngleY = MathHelper.cos(ageInTicks * 1.3F) * 3.1415927F * 0.25F;
      this.batLeftWing.rotateAngleY = -this.batRightWing.rotateAngleY;
      this.batRightWing.rotateAngleY *= 0.5F;
      this.batOuterLeftWing.rotateAngleY = -this.batRightWing.rotateAngleY * 0.5F;
    } 
  }
}
