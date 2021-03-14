package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityPortalFX extends EntityFX {
  private float portalParticleScale;
  
  private double portalPosX;
  
  private double portalPosY;
  
  private double portalPosZ;
  
  protected EntityPortalFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
    super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    this.motionX = xSpeedIn;
    this.motionY = ySpeedIn;
    this.motionZ = zSpeedIn;
    this.portalPosX = this.posX = xCoordIn;
    this.portalPosY = this.posY = yCoordIn;
    this.portalPosZ = this.posZ = zCoordIn;
    float f = this.rand.nextFloat() * 0.6F + 0.4F;
    this.portalParticleScale = this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
    this.particleRed = this.particleGreen = this.particleBlue = 1.0F * f;
    this.particleGreen *= 0.3F;
    this.particleRed *= 0.9F;
    this.particleMaxAge = (int)(Math.random() * 10.0D) + 40;
    this.noClip = true;
    setParticleTextureIndex((int)(Math.random() * 8.0D));
  }
  
  public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
    float f = (this.particleAge + partialTicks) / this.particleMaxAge;
    f = 1.0F - f;
    f *= f;
    f = 1.0F - f;
    this.particleScale = this.portalParticleScale * f;
    super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
  }
  
  public int getBrightnessForRender(float partialTicks) {
    int i = super.getBrightnessForRender(partialTicks);
    float f = this.particleAge / this.particleMaxAge;
    f *= f;
    f *= f;
    int j = i & 0xFF;
    int k = i >> 16 & 0xFF;
    k += (int)(f * 15.0F * 16.0F);
    if (k > 240)
      k = 240; 
    return j | k << 16;
  }
  
  public float getBrightness(float partialTicks) {
    float f = super.getBrightness(partialTicks);
    float f1 = this.particleAge / this.particleMaxAge;
    f1 = f1 * f1 * f1 * f1;
    return f * (1.0F - f1) + f1;
  }
  
  public void onUpdate() {
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    float f = this.particleAge / this.particleMaxAge;
    f = -f + f * f * 2.0F;
    f = 1.0F - f;
    this.posX = this.portalPosX + this.motionX * f;
    this.posY = this.portalPosY + this.motionY * f + (1.0F - f);
    this.posZ = this.portalPosZ + this.motionZ * f;
    if (this.particleAge++ >= this.particleMaxAge)
      setDead(); 
  }
  
  public static class Factory implements IParticleFactory {
    public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
      return new EntityPortalFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }
  }
}
