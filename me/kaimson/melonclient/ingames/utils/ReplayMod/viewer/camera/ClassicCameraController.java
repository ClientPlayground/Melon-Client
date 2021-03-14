package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;

public class ClassicCameraController implements CameraController {
  private static final double SPEED_CHANGE = 0.5D;
  
  private static final double LOWER_SPEED = 2.0D;
  
  private static final double UPPER_SPEED = 20.0D;
  
  private final CameraEntity camera;
  
  private double MAX_SPEED = 10.0D;
  
  private double THRESHOLD = this.MAX_SPEED / 20.0D;
  
  private double DECAY = this.MAX_SPEED / 3.0D;
  
  private Vector3f direction;
  
  private Vector3f dirBefore;
  
  private double motion;
  
  private long lastCall = System.currentTimeMillis();
  
  private boolean speedup = false;
  
  public ClassicCameraController(CameraEntity camera) {
    this.camera = camera;
  }
  
  public void update(float partialTicksPassed) {
    boolean forward = false, backward = false, left = false, right = false, up = false, down = false;
    this.speedup = false;
    for (KeyBinding kb : (Minecraft.getMinecraft()).gameSettings.keyBindings) {
      if (kb.isKeyDown()) {
        if (kb.getKeyDescription().equals("key.forward")) {
          forward = true;
          this.speedup = true;
        } 
        if (kb.getKeyDescription().equals("key.back")) {
          backward = true;
          this.speedup = true;
        } 
        if (kb.getKeyDescription().equals("key.jump")) {
          up = true;
          this.speedup = true;
        } 
        if (kb.getKeyDescription().equals("key.left")) {
          left = true;
          this.speedup = true;
        } 
        if (kb.getKeyDescription().equals("key.right")) {
          right = true;
          this.speedup = true;
        } 
        if (kb.getKeyDescription().equals("key.sneak")) {
          down = true;
          this.speedup = true;
        } 
      } 
    } 
    forwardCameraMovement(forward, backward, left, right, up, down);
    updateMovement();
  }
  
  public void increaseSpeed() {
    setCameraMaximumSpeed(this.MAX_SPEED + 0.5D);
  }
  
  public void decreaseSpeed() {
    setCameraMaximumSpeed(this.MAX_SPEED - 0.5D);
  }
  
  private void setCameraMaximumSpeed(double maxSpeed) {
    if (maxSpeed < 2.0D || maxSpeed > 20.0D)
      return; 
    this.MAX_SPEED = maxSpeed;
    this.THRESHOLD = this.MAX_SPEED / 20.0D;
    this.DECAY = 5.0D;
  }
  
  private void forwardCameraMovement(boolean forward, boolean backward, boolean left, boolean right, boolean up, boolean down) {
    if (forward && !backward) {
      setMovement(MoveDirection.FORWARD);
    } else if (backward && !forward) {
      setMovement(MoveDirection.BACKWARD);
    } 
    if (left && !right) {
      setMovement(MoveDirection.LEFT);
    } else if (right && !left) {
      setMovement(MoveDirection.RIGHT);
    } 
    if (up && !down) {
      setMovement(MoveDirection.UP);
    } else if (down && !up) {
      setMovement(MoveDirection.DOWN);
    } 
  }
  
  private void updateMovement() {
    long frac = System.currentTimeMillis() - this.lastCall;
    if (frac == 0L)
      return; 
    double decFac = Math.max(0.0D, 1.0D - this.DECAY * frac / 1000.0D);
    if (this.speedup) {
      if (this.motion < this.THRESHOLD)
        this.motion = this.THRESHOLD; 
      this.motion /= decFac;
    } else {
      this.motion *= decFac;
    } 
    this.motion = Math.min(this.motion, this.MAX_SPEED);
    this.lastCall = System.currentTimeMillis();
    if (this.direction == null || this.direction.lengthSquared() == 0.0F || this.motion < this.THRESHOLD)
      return; 
    Vector3f movement = this.direction.normalise(null);
    double factor = this.motion * frac / 1000.0D;
    this.camera.moveCamera(movement.x * factor, movement.y * factor, movement.z * factor);
  }
  
  private void setMovement(MoveDirection dir) {
    float rotationPitch = this.camera.rotationPitch, rotationYaw = this.camera.rotationYaw;
    switch (dir) {
      case BACKWARD:
        this.direction = getVectorForRotation(-rotationPitch, rotationYaw - 180.0F);
        break;
      case DOWN:
        this.direction = getVectorForRotation(90.0F, 0.0F);
        break;
      case FORWARD:
        this.direction = getVectorForRotation(rotationPitch, rotationYaw);
        break;
      case LEFT:
        this.direction = getVectorForRotation(0.0F, rotationYaw - 90.0F);
        break;
      case RIGHT:
        this.direction = getVectorForRotation(0.0F, rotationYaw + 90.0F);
        break;
      case UP:
        this.direction = getVectorForRotation(-90.0F, 0.0F);
        break;
    } 
    Vector3f dbf = this.direction;
    if (this.dirBefore != null) {
      this.dirBefore.normalise(this.dirBefore);
      Vector3f.add(this.direction, this.dirBefore, this.dirBefore);
      this.direction = this.dirBefore;
    } 
    this.dirBefore = dbf;
    updateMovement();
  }
  
  private Vector3f getVectorForRotation(float pitch, float yaw) {
    float f2 = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
    float f3 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
    float f4 = -MathHelper.cos(-pitch * 0.017453292F);
    float f5 = MathHelper.sin(-pitch * 0.017453292F);
    return new Vector3f(f3 * f4, f5, f2 * f4);
  }
  
  public enum MoveDirection {
    UP, DOWN, LEFT, RIGHT, FORWARD, BACKWARD;
  }
}
