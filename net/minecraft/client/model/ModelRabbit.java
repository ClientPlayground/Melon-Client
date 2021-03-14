package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.MathHelper;

public class ModelRabbit extends ModelBase {
  ModelRenderer rabbitLeftFoot;
  
  ModelRenderer rabbitRightFoot;
  
  ModelRenderer rabbitLeftThigh;
  
  ModelRenderer rabbitRightThigh;
  
  ModelRenderer rabbitBody;
  
  ModelRenderer rabbitLeftArm;
  
  ModelRenderer rabbitRightArm;
  
  ModelRenderer rabbitHead;
  
  ModelRenderer rabbitRightEar;
  
  ModelRenderer rabbitLeftEar;
  
  ModelRenderer rabbitTail;
  
  ModelRenderer rabbitNose;
  
  private float field_178701_m = 0.0F;
  
  private float field_178699_n = 0.0F;
  
  public ModelRabbit() {
    setTextureOffset("head.main", 0, 0);
    setTextureOffset("head.nose", 0, 24);
    setTextureOffset("head.ear1", 0, 10);
    setTextureOffset("head.ear2", 6, 10);
    this.rabbitLeftFoot = new ModelRenderer(this, 26, 24);
    this.rabbitLeftFoot.addBox(-1.0F, 5.5F, -3.7F, 2, 1, 7);
    this.rabbitLeftFoot.setRotationPoint(3.0F, 17.5F, 3.7F);
    this.rabbitLeftFoot.mirror = true;
    setRotationOffset(this.rabbitLeftFoot, 0.0F, 0.0F, 0.0F);
    this.rabbitRightFoot = new ModelRenderer(this, 8, 24);
    this.rabbitRightFoot.addBox(-1.0F, 5.5F, -3.7F, 2, 1, 7);
    this.rabbitRightFoot.setRotationPoint(-3.0F, 17.5F, 3.7F);
    this.rabbitRightFoot.mirror = true;
    setRotationOffset(this.rabbitRightFoot, 0.0F, 0.0F, 0.0F);
    this.rabbitLeftThigh = new ModelRenderer(this, 30, 15);
    this.rabbitLeftThigh.addBox(-1.0F, 0.0F, 0.0F, 2, 4, 5);
    this.rabbitLeftThigh.setRotationPoint(3.0F, 17.5F, 3.7F);
    this.rabbitLeftThigh.mirror = true;
    setRotationOffset(this.rabbitLeftThigh, -0.34906584F, 0.0F, 0.0F);
    this.rabbitRightThigh = new ModelRenderer(this, 16, 15);
    this.rabbitRightThigh.addBox(-1.0F, 0.0F, 0.0F, 2, 4, 5);
    this.rabbitRightThigh.setRotationPoint(-3.0F, 17.5F, 3.7F);
    this.rabbitRightThigh.mirror = true;
    setRotationOffset(this.rabbitRightThigh, -0.34906584F, 0.0F, 0.0F);
    this.rabbitBody = new ModelRenderer(this, 0, 0);
    this.rabbitBody.addBox(-3.0F, -2.0F, -10.0F, 6, 5, 10);
    this.rabbitBody.setRotationPoint(0.0F, 19.0F, 8.0F);
    this.rabbitBody.mirror = true;
    setRotationOffset(this.rabbitBody, -0.34906584F, 0.0F, 0.0F);
    this.rabbitLeftArm = new ModelRenderer(this, 8, 15);
    this.rabbitLeftArm.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2);
    this.rabbitLeftArm.setRotationPoint(3.0F, 17.0F, -1.0F);
    this.rabbitLeftArm.mirror = true;
    setRotationOffset(this.rabbitLeftArm, -0.17453292F, 0.0F, 0.0F);
    this.rabbitRightArm = new ModelRenderer(this, 0, 15);
    this.rabbitRightArm.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2);
    this.rabbitRightArm.setRotationPoint(-3.0F, 17.0F, -1.0F);
    this.rabbitRightArm.mirror = true;
    setRotationOffset(this.rabbitRightArm, -0.17453292F, 0.0F, 0.0F);
    this.rabbitHead = new ModelRenderer(this, 32, 0);
    this.rabbitHead.addBox(-2.5F, -4.0F, -5.0F, 5, 4, 5);
    this.rabbitHead.setRotationPoint(0.0F, 16.0F, -1.0F);
    this.rabbitHead.mirror = true;
    setRotationOffset(this.rabbitHead, 0.0F, 0.0F, 0.0F);
    this.rabbitRightEar = new ModelRenderer(this, 52, 0);
    this.rabbitRightEar.addBox(-2.5F, -9.0F, -1.0F, 2, 5, 1);
    this.rabbitRightEar.setRotationPoint(0.0F, 16.0F, -1.0F);
    this.rabbitRightEar.mirror = true;
    setRotationOffset(this.rabbitRightEar, 0.0F, -0.2617994F, 0.0F);
    this.rabbitLeftEar = new ModelRenderer(this, 58, 0);
    this.rabbitLeftEar.addBox(0.5F, -9.0F, -1.0F, 2, 5, 1);
    this.rabbitLeftEar.setRotationPoint(0.0F, 16.0F, -1.0F);
    this.rabbitLeftEar.mirror = true;
    setRotationOffset(this.rabbitLeftEar, 0.0F, 0.2617994F, 0.0F);
    this.rabbitTail = new ModelRenderer(this, 52, 6);
    this.rabbitTail.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 2);
    this.rabbitTail.setRotationPoint(0.0F, 20.0F, 7.0F);
    this.rabbitTail.mirror = true;
    setRotationOffset(this.rabbitTail, -0.3490659F, 0.0F, 0.0F);
    this.rabbitNose = new ModelRenderer(this, 32, 9);
    this.rabbitNose.addBox(-0.5F, -2.5F, -5.5F, 1, 1, 1);
    this.rabbitNose.setRotationPoint(0.0F, 16.0F, -1.0F);
    this.rabbitNose.mirror = true;
    setRotationOffset(this.rabbitNose, 0.0F, 0.0F, 0.0F);
  }
  
  private void setRotationOffset(ModelRenderer p_178691_1_, float p_178691_2_, float p_178691_3_, float p_178691_4_) {
    p_178691_1_.rotateAngleX = p_178691_2_;
    p_178691_1_.rotateAngleY = p_178691_3_;
    p_178691_1_.rotateAngleZ = p_178691_4_;
  }
  
  public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
    setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
    if (this.isChild) {
      float f = 2.0F;
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, 5.0F * scale, 2.0F * scale);
      this.rabbitHead.render(scale);
      this.rabbitLeftEar.render(scale);
      this.rabbitRightEar.render(scale);
      this.rabbitNose.render(scale);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
      GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
      this.rabbitLeftFoot.render(scale);
      this.rabbitRightFoot.render(scale);
      this.rabbitLeftThigh.render(scale);
      this.rabbitRightThigh.render(scale);
      this.rabbitBody.render(scale);
      this.rabbitLeftArm.render(scale);
      this.rabbitRightArm.render(scale);
      this.rabbitTail.render(scale);
      GlStateManager.popMatrix();
    } else {
      this.rabbitLeftFoot.render(scale);
      this.rabbitRightFoot.render(scale);
      this.rabbitLeftThigh.render(scale);
      this.rabbitRightThigh.render(scale);
      this.rabbitBody.render(scale);
      this.rabbitLeftArm.render(scale);
      this.rabbitRightArm.render(scale);
      this.rabbitHead.render(scale);
      this.rabbitRightEar.render(scale);
      this.rabbitLeftEar.render(scale);
      this.rabbitTail.render(scale);
      this.rabbitNose.render(scale);
    } 
  }
  
  public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    float f = ageInTicks - entityIn.ticksExisted;
    EntityRabbit entityrabbit = (EntityRabbit)entityIn;
    this.rabbitLeftEar.rotateAngleX = headPitch * 0.017453292F;
    this.rabbitHead.rotateAngleY = netHeadYaw * 0.017453292F;
    this.rabbitNose.rotateAngleY -= 0.2617994F;
    this.rabbitNose.rotateAngleY += 0.2617994F;
    this.field_178701_m = MathHelper.sin(entityrabbit.func_175521_o(f) * 3.1415927F);
    this.rabbitRightThigh.rotateAngleX = (this.field_178701_m * 50.0F - 21.0F) * 0.017453292F;
    this.rabbitRightFoot.rotateAngleX = this.field_178701_m * 50.0F * 0.017453292F;
    this.rabbitRightArm.rotateAngleX = (this.field_178701_m * -40.0F - 11.0F) * 0.017453292F;
  }
  
  public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {}
}
