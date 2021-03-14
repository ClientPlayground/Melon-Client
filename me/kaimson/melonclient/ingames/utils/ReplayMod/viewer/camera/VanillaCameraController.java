package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class VanillaCameraController implements CameraController {
  private static final int MAX_SPEED = 1000;
  
  private static final int MIN_SPEED = -1000;
  
  private static final Vector3f[] DIRECTIONS = new Vector3f[] { new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(0.0F, -1.0F, 0.0F) };
  
  private final KeyBinding[] bindings = new KeyBinding[6];
  
  private final CameraEntity camera;
  
  private int speed;
  
  public VanillaCameraController(Minecraft mc, CameraEntity camera) {
    this.camera = camera;
    GameSettings gameSettings = mc.gameSettings;
    this.bindings[0] = gameSettings.keyBindForward;
    this.bindings[1] = gameSettings.keyBindBack;
    this.bindings[2] = gameSettings.keyBindLeft;
    this.bindings[3] = gameSettings.keyBindRight;
    this.bindings[4] = gameSettings.keyBindJump;
    this.bindings[5] = gameSettings.keyBindSneak;
  }
  
  public void update(float partialTicksPassed) {
    if (partialTicksPassed == 0.0F)
      return; 
    Vector3f direction = new Vector3f(0.0F, 0.0F, 0.0F);
    for (int i = 0; i < 6; i++) {
      if (this.bindings[i].isPressed())
        Vector3f.add(direction, DIRECTIONS[i], direction); 
    } 
    if (direction.length() == 0.0F)
      return; 
    direction.normalise(direction);
    double yawRadians = Math.toRadians(this.camera.rotationYaw);
    float yawSin = (float)Math.sin(yawRadians), yawCos = (float)Math.cos(yawRadians);
    direction.set(direction.x * yawCos - direction.z * yawSin, direction.y, direction.x * yawSin + direction.z * yawCos);
    direction.scale((float)Math.pow(2.0D, this.speed / 300.0D + 1.0D));
    direction.scale(partialTicksPassed / 20.0F);
    this.camera.moveCamera(direction.x, direction.y, direction.z);
  }
  
  public void increaseSpeed() {
    this.speed = Math.min(1000, this.speed + 1);
  }
  
  public void decreaseSpeed() {
    this.speed = Math.max(-1000, this.speed - 1);
  }
}
