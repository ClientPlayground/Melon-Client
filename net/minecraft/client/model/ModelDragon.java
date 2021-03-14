package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;

public class ModelDragon extends ModelBase {
  private ModelRenderer head;
  
  private ModelRenderer spine;
  
  private ModelRenderer jaw;
  
  private ModelRenderer body;
  
  private ModelRenderer rearLeg;
  
  private ModelRenderer frontLeg;
  
  private ModelRenderer rearLegTip;
  
  private ModelRenderer frontLegTip;
  
  private ModelRenderer rearFoot;
  
  private ModelRenderer frontFoot;
  
  private ModelRenderer wing;
  
  private ModelRenderer wingTip;
  
  private float partialTicks;
  
  public ModelDragon(float p_i46360_1_) {
    this.textureWidth = 256;
    this.textureHeight = 256;
    setTextureOffset("body.body", 0, 0);
    setTextureOffset("wing.skin", -56, 88);
    setTextureOffset("wingtip.skin", -56, 144);
    setTextureOffset("rearleg.main", 0, 0);
    setTextureOffset("rearfoot.main", 112, 0);
    setTextureOffset("rearlegtip.main", 196, 0);
    setTextureOffset("head.upperhead", 112, 30);
    setTextureOffset("wing.bone", 112, 88);
    setTextureOffset("head.upperlip", 176, 44);
    setTextureOffset("jaw.jaw", 176, 65);
    setTextureOffset("frontleg.main", 112, 104);
    setTextureOffset("wingtip.bone", 112, 136);
    setTextureOffset("frontfoot.main", 144, 104);
    setTextureOffset("neck.box", 192, 104);
    setTextureOffset("frontlegtip.main", 226, 138);
    setTextureOffset("body.scale", 220, 53);
    setTextureOffset("head.scale", 0, 0);
    setTextureOffset("neck.scale", 48, 0);
    setTextureOffset("head.nostril", 112, 0);
    float f = -16.0F;
    this.head = new ModelRenderer(this, "head");
    this.head.addBox("upperlip", -6.0F, -1.0F, -8.0F + f, 12, 5, 16);
    this.head.addBox("upperhead", -8.0F, -8.0F, 6.0F + f, 16, 16, 16);
    this.head.mirror = true;
    this.head.addBox("scale", -5.0F, -12.0F, 12.0F + f, 2, 4, 6);
    this.head.addBox("nostril", -5.0F, -3.0F, -6.0F + f, 2, 2, 4);
    this.head.mirror = false;
    this.head.addBox("scale", 3.0F, -12.0F, 12.0F + f, 2, 4, 6);
    this.head.addBox("nostril", 3.0F, -3.0F, -6.0F + f, 2, 2, 4);
    this.jaw = new ModelRenderer(this, "jaw");
    this.jaw.setRotationPoint(0.0F, 4.0F, 8.0F + f);
    this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16);
    this.head.addChild(this.jaw);
    this.spine = new ModelRenderer(this, "neck");
    this.spine.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10);
    this.spine.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6);
    this.body = new ModelRenderer(this, "body");
    this.body.setRotationPoint(0.0F, 4.0F, 8.0F);
    this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64);
    this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12);
    this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12);
    this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12);
    this.wing = new ModelRenderer(this, "wing");
    this.wing.setRotationPoint(-12.0F, 5.0F, 2.0F);
    this.wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8);
    this.wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
    this.wingTip = new ModelRenderer(this, "wingtip");
    this.wingTip.setRotationPoint(-56.0F, 0.0F, 0.0F);
    this.wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4);
    this.wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
    this.wing.addChild(this.wingTip);
    this.frontLeg = new ModelRenderer(this, "frontleg");
    this.frontLeg.setRotationPoint(-12.0F, 20.0F, 2.0F);
    this.frontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8);
    this.frontLegTip = new ModelRenderer(this, "frontlegtip");
    this.frontLegTip.setRotationPoint(0.0F, 20.0F, -1.0F);
    this.frontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6);
    this.frontLeg.addChild(this.frontLegTip);
    this.frontFoot = new ModelRenderer(this, "frontfoot");
    this.frontFoot.setRotationPoint(0.0F, 23.0F, 0.0F);
    this.frontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16);
    this.frontLegTip.addChild(this.frontFoot);
    this.rearLeg = new ModelRenderer(this, "rearleg");
    this.rearLeg.setRotationPoint(-16.0F, 16.0F, 42.0F);
    this.rearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16);
    this.rearLegTip = new ModelRenderer(this, "rearlegtip");
    this.rearLegTip.setRotationPoint(0.0F, 32.0F, -4.0F);
    this.rearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12);
    this.rearLeg.addChild(this.rearLegTip);
    this.rearFoot = new ModelRenderer(this, "rearfoot");
    this.rearFoot.setRotationPoint(0.0F, 31.0F, 4.0F);
    this.rearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24);
    this.rearLegTip.addChild(this.rearFoot);
  }
  
  public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {
    this.partialTicks = partialTickTime;
  }
  
  public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
    GlStateManager.pushMatrix();
    EntityDragon entitydragon = (EntityDragon)entityIn;
    float f = entitydragon.prevAnimTime + (entitydragon.animTime - entitydragon.prevAnimTime) * this.partialTicks;
    this.jaw.rotateAngleX = (float)(Math.sin((f * 3.1415927F * 2.0F)) + 1.0D) * 0.2F;
    float f1 = (float)(Math.sin((f * 3.1415927F * 2.0F - 1.0F)) + 1.0D);
    f1 = (f1 * f1 * 1.0F + f1 * 2.0F) * 0.05F;
    GlStateManager.translate(0.0F, f1 - 2.0F, -3.0F);
    GlStateManager.rotate(f1 * 2.0F, 1.0F, 0.0F, 0.0F);
    float f2 = -30.0F;
    float f4 = 0.0F;
    float f5 = 1.5F;
    double[] adouble = entitydragon.getMovementOffsets(6, this.partialTicks);
    float f6 = updateRotations(entitydragon.getMovementOffsets(5, this.partialTicks)[0] - entitydragon.getMovementOffsets(10, this.partialTicks)[0]);
    float f7 = updateRotations(entitydragon.getMovementOffsets(5, this.partialTicks)[0] + (f6 / 2.0F));
    f2 += 2.0F;
    float f8 = f * 3.1415927F * 2.0F;
    f2 = 20.0F;
    float f3 = -12.0F;
    for (int i = 0; i < 5; i++) {
      double[] adouble1 = entitydragon.getMovementOffsets(5 - i, this.partialTicks);
      float f9 = (float)Math.cos((i * 0.45F + f8)) * 0.15F;
      this.spine.rotateAngleY = updateRotations(adouble1[0] - adouble[0]) * 3.1415927F / 180.0F * f5;
      this.spine.rotateAngleX = f9 + (float)(adouble1[1] - adouble[1]) * 3.1415927F / 180.0F * f5 * 5.0F;
      this.spine.rotateAngleZ = -updateRotations(adouble1[0] - f7) * 3.1415927F / 180.0F * f5;
      this.spine.rotationPointY = f2;
      this.spine.rotationPointZ = f3;
      this.spine.rotationPointX = f4;
      f2 = (float)(f2 + Math.sin(this.spine.rotateAngleX) * 10.0D);
      f3 = (float)(f3 - Math.cos(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0D);
      f4 = (float)(f4 - Math.sin(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0D);
      this.spine.render(scale);
    } 
    this.head.rotationPointY = f2;
    this.head.rotationPointZ = f3;
    this.head.rotationPointX = f4;
    double[] adouble2 = entitydragon.getMovementOffsets(0, this.partialTicks);
    this.head.rotateAngleY = updateRotations(adouble2[0] - adouble[0]) * 3.1415927F / 180.0F * 1.0F;
    this.head.rotateAngleZ = -updateRotations(adouble2[0] - f7) * 3.1415927F / 180.0F * 1.0F;
    this.head.render(scale);
    GlStateManager.pushMatrix();
    GlStateManager.translate(0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(-f6 * f5 * 1.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.translate(0.0F, -1.0F, 0.0F);
    this.body.rotateAngleZ = 0.0F;
    this.body.render(scale);
    for (int j = 0; j < 2; j++) {
      GlStateManager.enableCull();
      float f11 = f * 3.1415927F * 2.0F;
      this.wing.rotateAngleX = 0.125F - (float)Math.cos(f11) * 0.2F;
      this.wing.rotateAngleY = 0.25F;
      this.wing.rotateAngleZ = (float)(Math.sin(f11) + 0.125D) * 0.8F;
      this.wingTip.rotateAngleZ = -((float)(Math.sin((f11 + 2.0F)) + 0.5D)) * 0.75F;
      this.rearLeg.rotateAngleX = 1.0F + f1 * 0.1F;
      this.rearLegTip.rotateAngleX = 0.5F + f1 * 0.1F;
      this.rearFoot.rotateAngleX = 0.75F + f1 * 0.1F;
      this.frontLeg.rotateAngleX = 1.3F + f1 * 0.1F;
      this.frontLegTip.rotateAngleX = -0.5F - f1 * 0.1F;
      this.frontFoot.rotateAngleX = 0.75F + f1 * 0.1F;
      this.wing.render(scale);
      this.frontLeg.render(scale);
      this.rearLeg.render(scale);
      GlStateManager.scale(-1.0F, 1.0F, 1.0F);
      if (j == 0)
        GlStateManager.cullFace(1028); 
    } 
    GlStateManager.popMatrix();
    GlStateManager.cullFace(1029);
    GlStateManager.disableCull();
    float f10 = -((float)Math.sin((f * 3.1415927F * 2.0F))) * 0.0F;
    f8 = f * 3.1415927F * 2.0F;
    f2 = 10.0F;
    f3 = 60.0F;
    f4 = 0.0F;
    adouble = entitydragon.getMovementOffsets(11, this.partialTicks);
    for (int k = 0; k < 12; k++) {
      adouble2 = entitydragon.getMovementOffsets(12 + k, this.partialTicks);
      f10 = (float)(f10 + Math.sin((k * 0.45F + f8)) * 0.05000000074505806D);
      this.spine.rotateAngleY = (updateRotations(adouble2[0] - adouble[0]) * f5 + 180.0F) * 3.1415927F / 180.0F;
      this.spine.rotateAngleX = f10 + (float)(adouble2[1] - adouble[1]) * 3.1415927F / 180.0F * f5 * 5.0F;
      this.spine.rotateAngleZ = updateRotations(adouble2[0] - f7) * 3.1415927F / 180.0F * f5;
      this.spine.rotationPointY = f2;
      this.spine.rotationPointZ = f3;
      this.spine.rotationPointX = f4;
      f2 = (float)(f2 + Math.sin(this.spine.rotateAngleX) * 10.0D);
      f3 = (float)(f3 - Math.cos(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0D);
      f4 = (float)(f4 - Math.sin(this.spine.rotateAngleY) * Math.cos(this.spine.rotateAngleX) * 10.0D);
      this.spine.render(scale);
    } 
    GlStateManager.popMatrix();
  }
  
  private float updateRotations(double p_78214_1_) {
    while (p_78214_1_ >= 180.0D)
      p_78214_1_ -= 360.0D; 
    while (p_78214_1_ < -180.0D)
      p_78214_1_ += 360.0D; 
    return (float)p_78214_1_;
  }
}
