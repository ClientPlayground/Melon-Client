package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera;

import com.replaymod.replaystudio.util.Location;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.Utils;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CameraEntity extends EntityPlayerSP {
  public float roll;
  
  private CameraController cameraController;
  
  public CameraController getCameraController() {
    return this.cameraController;
  }
  
  public void setCameraController(CameraController cameraController) {
    this.cameraController = cameraController;
  }
  
  private long lastControllerUpdate = System.currentTimeMillis();
  
  private Entity lastHandRendered = null;
  
  private final CameraEntityEventHandler eventHandler;
  
  public CameraEntity(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandlerPlayClient, StatFileWriter statFileWriter) {
    super(mcIn, worldIn, netHandlerPlayClient, statFileWriter);
    EventHandler.register(this.eventHandler = new CameraEntityEventHandler(this));
    if (ReplayModReplay.getInstance().getReplayHandler().getSpectatedUUID() == null) {
      this.cameraController = ReplayModReplay.getInstance().createCameraController(this);
    } else {
      this.cameraController = new SpectatorCameraController(this);
    } 
  }
  
  public void moveCamera(double x, double y, double z) {
    setCameraPosition(this.posX + x, this.posY + y, this.posZ + z);
  }
  
  public void setCameraPosition(double x, double y, double z) {
    this.lastTickPosX = this.prevPosX = this.posX = x;
    this.lastTickPosY = this.prevPosY = this.posY = y;
    this.lastTickPosZ = this.prevPosZ = this.posZ = z;
    updateBoundingBox();
  }
  
  public void setCameraRotation(float yaw, float pitch, float roll) {
    this.prevRotationYaw = this.rotationYaw = yaw;
    this.prevRotationPitch = this.rotationPitch = pitch;
    this.roll = roll;
  }
  
  public void setCameraPosRot(Location pos) {
    setCameraRotation(pos.getYaw(), pos.getPitch(), this.roll);
    setCameraPosition(pos.getX(), pos.getY(), pos.getZ());
  }
  
  public void setCameraPosRot(Entity to) {
    this.prevPosX = to.prevPosX;
    this.prevPosY = to.prevPosY;
    this.prevPosZ = to.prevPosZ;
    this.prevRotationYaw = to.prevRotationYaw;
    this.prevRotationPitch = to.prevRotationPitch;
    this.posX = to.posX;
    this.posY = to.posY;
    this.posZ = to.posZ;
    this.rotationYaw = to.rotationYaw;
    this.rotationPitch = to.rotationPitch;
    this.lastTickPosX = to.lastTickPosX;
    this.lastTickPosY = to.lastTickPosY;
    this.lastTickPosZ = to.lastTickPosZ;
    updateBoundingBox();
  }
  
  private void updateBoundingBox() {
    setEntityBoundingBox(new AxisAlignedBB(this.posX - (this.width / 2.0F), this.posY, this.posZ - (this.width / 2.0F), this.posX + (this.width / 2.0F), this.posY + this.height, this.posZ + (this.width / 2.0F)));
  }
  
  public void onUpdate() {
    Entity view = this.mc.getRenderViewEntity();
    if (!(view instanceof CameraEntity) && !(this.cameraController instanceof SpectatorCameraController))
      ReplayModReplay.getInstance().getReplayHandler().spectateCamera(); 
    if (view != null) {
      EntityPlayer entityPlayer;
      UUID spectating = ReplayModReplay.getInstance().getReplayHandler().getSpectatedUUID();
      if ((spectating != null && (view.getUniqueID() != spectating || view.worldObj != this.worldObj)) || this.worldObj
        
        .getEntityByID(view.getEntityId()) != view) {
        if (spectating == null) {
          ReplayModReplay.getInstance().getReplayHandler().spectateEntity((Entity)this);
          return;
        } 
        entityPlayer = this.worldObj.getPlayerEntityByUUID(spectating);
        if (entityPlayer != null) {
          this.mc.setRenderViewEntity((Entity)entityPlayer);
        } else {
          this.mc.setRenderViewEntity((Entity)this);
          return;
        } 
      } 
      if (entityPlayer != this)
        setCameraPosRot((Entity)entityPlayer); 
    } 
  }
  
  public void preparePlayerToSpawn() {
    if (this.mc.theWorld != null)
      this.worldObj = (World)this.mc.theWorld; 
    super.preparePlayerToSpawn();
  }
  
  public void setAngles(float yaw, float pitch) {
    if (this.mc.getRenderViewEntity() == this)
      super.setAngles(yaw, pitch); 
  }
  
  public boolean isEntityInsideOpaqueBlock() {
    return falseUnlessSpectating(Entity::isEntityInsideOpaqueBlock);
  }
  
  public boolean isInsideOfMaterial(Material materialIn) {
    return falseUnlessSpectating(e -> Boolean.valueOf(e.isInsideOfMaterial(materialIn)));
  }
  
  public boolean isInLava() {
    return falseUnlessSpectating(Entity::isInLava);
  }
  
  public boolean isInWater() {
    return falseUnlessSpectating(Entity::isInWater);
  }
  
  public boolean isBurning() {
    return falseUnlessSpectating(Entity::isBurning);
  }
  
  private boolean falseUnlessSpectating(Function<Entity, Boolean> property) {
    Entity view = this.mc.getRenderViewEntity();
    if (view != null && view != this)
      return ((Boolean)property.apply(view)).booleanValue(); 
    return false;
  }
  
  public boolean canBePushed() {
    return false;
  }
  
  protected void createRunningParticles() {}
  
  public boolean canBeCollidedWith() {
    return false;
  }
  
  public boolean isSpectator() {
    return ReplayModReplay.getInstance().getReplayHandler().isCameraView();
  }
  
  public boolean shouldRenderInPass(int pass) {
    return false;
  }
  
  public boolean isInvisible() {
    Entity view = this.mc.getRenderViewEntity();
    if (view != this)
      return view.isInvisible(); 
    return super.isInvisible();
  }
  
  public ResourceLocation getLocationSkin() {
    Entity view = this.mc.getRenderViewEntity();
    if (view != this && view instanceof EntityPlayer)
      return Utils.getResourceLocationForPlayerUUID(view.getUniqueID()); 
    return super.getLocationSkin();
  }
  
  public String getSkinType() {
    Entity view = this.mc.getRenderViewEntity();
    if (view != this && view instanceof AbstractClientPlayer)
      return ((AbstractClientPlayer)view).getSkinType(); 
    return super.getSkinType();
  }
  
  public float getSwingProgress(float renderPartialTicks) {
    Entity view = this.mc.getRenderViewEntity();
    if (view != this && view instanceof EntityPlayer)
      return ((EntityPlayer)view).getSwingProgress(renderPartialTicks); 
    return 0.0F;
  }
  
  public MovingObjectPosition rayTrace(double p_174822_1_, float p_174822_3_) {
    MovingObjectPosition pos = super.rayTrace(p_174822_1_, 1.0F);
    if (pos != null && pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
      pos.typeOfHit = MovingObjectPosition.MovingObjectType.MISS; 
    return pos;
  }
  
  public void setDead() {
    super.setDead();
    EventHandler.unregister(this.eventHandler);
  }
  
  protected void update() {
    long now = System.currentTimeMillis();
    long timePassed = now - this.lastControllerUpdate;
    this.cameraController.update((float)timePassed / 50.0F);
    this.lastControllerUpdate = now;
    if (!((Minecraft.getMinecraft()).currentScreen instanceof me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiOverlay.UserInputGuiScreen) && (this.mc.gameSettings.keyBindAttack.isPressed() || this.mc.gameSettings.keyBindUseItem.isPressed()))
      if (canSpectate(this.mc.pointedEntity)) {
        ReplayModReplay.getInstance().getReplayHandler().spectateEntity(this.mc.pointedEntity);
        this.mc.gameSettings.keyBindSneak.pressTime = 0;
      }  
    Map<String, KeyBinding> keyBindings = ReplayCore.getInstance().getKeyBindingRegistry().getKeyBindings();
    if (((KeyBinding)keyBindings.get("replaymod.input.rollclockwise")).isKeyDown())
      this.roll = (float)(this.roll + (Utils.isCtrlDown() ? 0.2D : 1.0D)); 
    if (((KeyBinding)keyBindings.get("replaymod.input.rollcounterclockwise")).isKeyDown())
      this.roll = (float)(this.roll - (Utils.isCtrlDown() ? 0.2D : 1.0D)); 
  }
  
  protected void updateArmYawAndPitch() {
    this.prevRenderArmYaw = this.renderArmYaw;
    this.prevRenderArmPitch = this.renderArmPitch;
    this.renderArmPitch += (this.rotationPitch - this.renderArmPitch) * 0.5F;
    this.renderArmYaw += (this.rotationYaw - this.renderArmYaw) * 0.5F;
  }
  
  public boolean canSpectate(Entity e) {
    return (e != null && !e.isInvisible() && (e instanceof EntityPlayer || e instanceof net.minecraft.entity.EntityLiving || e instanceof net.minecraft.entity.item.EntityItemFrame));
  }
  
  public void addChatMessage(IChatComponent message) {
    super.addChatMessage(message);
  }
}
