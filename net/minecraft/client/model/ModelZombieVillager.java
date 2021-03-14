package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelZombieVillager extends ModelBiped {
  public ModelZombieVillager() {
    this(0.0F, 0.0F, false);
  }
  
  public ModelZombieVillager(float p_i1165_1_, float p_i1165_2_, boolean p_i1165_3_) {
    super(p_i1165_1_, 0.0F, 64, p_i1165_3_ ? 32 : 64);
    if (p_i1165_3_) {
      this.bipedHead = new ModelRenderer(this, 0, 0);
      this.bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 8, 8, p_i1165_1_);
      this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1165_2_, 0.0F);
    } else {
      this.bipedHead = new ModelRenderer(this);
      this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1165_2_, 0.0F);
      this.bipedHead.setTextureOffset(0, 32).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, p_i1165_1_);
      this.bipedHead.setTextureOffset(24, 32).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, p_i1165_1_);
    } 
  }
  
  public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
    float f = MathHelper.sin(this.swingProgress * 3.1415927F);
    float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * 3.1415927F);
    this.bipedRightArm.rotateAngleZ = 0.0F;
    this.bipedLeftArm.rotateAngleZ = 0.0F;
    this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
    this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
    this.bipedRightArm.rotateAngleX = -1.5707964F;
    this.bipedLeftArm.rotateAngleX = -1.5707964F;
    this.bipedRightArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
    this.bipedLeftArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
    this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
    this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
    this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
  }
}
