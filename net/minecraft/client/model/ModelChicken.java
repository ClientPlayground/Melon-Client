package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelChicken extends ModelBase {
  public ModelRenderer head;
  
  public ModelRenderer body;
  
  public ModelRenderer rightLeg;
  
  public ModelRenderer leftLeg;
  
  public ModelRenderer rightWing;
  
  public ModelRenderer leftWing;
  
  public ModelRenderer bill;
  
  public ModelRenderer chin;
  
  public ModelChicken() {
    int i = 16;
    this.head = new ModelRenderer(this, 0, 0);
    this.head.addBox(-2.0F, -6.0F, -2.0F, 4, 6, 3, 0.0F);
    this.head.setRotationPoint(0.0F, (-1 + i), -4.0F);
    this.bill = new ModelRenderer(this, 14, 0);
    this.bill.addBox(-2.0F, -4.0F, -4.0F, 4, 2, 2, 0.0F);
    this.bill.setRotationPoint(0.0F, (-1 + i), -4.0F);
    this.chin = new ModelRenderer(this, 14, 4);
    this.chin.addBox(-1.0F, -2.0F, -3.0F, 2, 2, 2, 0.0F);
    this.chin.setRotationPoint(0.0F, (-1 + i), -4.0F);
    this.body = new ModelRenderer(this, 0, 9);
    this.body.addBox(-3.0F, -4.0F, -3.0F, 6, 8, 6, 0.0F);
    this.body.setRotationPoint(0.0F, i, 0.0F);
    this.rightLeg = new ModelRenderer(this, 26, 0);
    this.rightLeg.addBox(-1.0F, 0.0F, -3.0F, 3, 5, 3);
    this.rightLeg.setRotationPoint(-2.0F, (3 + i), 1.0F);
    this.leftLeg = new ModelRenderer(this, 26, 0);
    this.leftLeg.addBox(-1.0F, 0.0F, -3.0F, 3, 5, 3);
    this.leftLeg.setRotationPoint(1.0F, (3 + i), 1.0F);
    this.rightWing = new ModelRenderer(this, 24, 13);
    this.rightWing.addBox(0.0F, 0.0F, -3.0F, 1, 4, 6);
    this.rightWing.setRotationPoint(-4.0F, (-3 + i), 0.0F);
    this.leftWing = new ModelRenderer(this, 24, 13);
    this.leftWing.addBox(-1.0F, 0.0F, -3.0F, 1, 4, 6);
    this.leftWing.setRotationPoint(4.0F, (-3 + i), 0.0F);
  }
  
  public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
    setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
    if (this.isChild) {
      float f = 2.0F;
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, 5.0F * scale, 2.0F * scale);
      this.head.render(scale);
      this.bill.render(scale);
      this.chin.render(scale);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
      GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
      this.body.render(scale);
      this.rightLeg.render(scale);
      this.leftLeg.render(scale);
      this.rightWing.render(scale);
      this.leftWing.render(scale);
      GlStateManager.popMatrix();
    } else {
      this.head.render(scale);
      this.bill.render(scale);
      this.chin.render(scale);
      this.body.render(scale);
      this.rightLeg.render(scale);
      this.leftLeg.render(scale);
      this.rightWing.render(scale);
      this.leftWing.render(scale);
    } 
  }
  
  public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    this.head.rotateAngleX = headPitch / 57.295776F;
    this.head.rotateAngleY = netHeadYaw / 57.295776F;
    this.bill.rotateAngleX = this.head.rotateAngleX;
    this.bill.rotateAngleY = this.head.rotateAngleY;
    this.chin.rotateAngleX = this.head.rotateAngleX;
    this.chin.rotateAngleY = this.head.rotateAngleY;
    this.body.rotateAngleX = 1.5707964F;
    this.rightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    this.leftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
    this.rightWing.rotateAngleZ = ageInTicks;
    this.leftWing.rotateAngleZ = -ageInTicks;
  }
}
