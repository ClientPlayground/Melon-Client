package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera;

import java.util.Arrays;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

public class SpectatorCameraController implements CameraController {
  private final CameraEntity camera;
  
  public SpectatorCameraController(CameraEntity camera) {
    this.camera = camera;
  }
  
  public void update(float partialTicksPassed) {
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.gameSettings.keyBindSneak.isPressed())
      ReplayModReplay.getInstance().getReplayHandler().spectateCamera(); 
    for (KeyBinding binding : Arrays.<KeyBinding>asList(new KeyBinding[] { mc.gameSettings.keyBindAttack, mc.gameSettings.keyBindUseItem, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight }))
      binding.pressTime = 0; 
    Mouse.updateCursor();
    Entity view = mc.getRenderViewEntity();
    if (view != null && view != this.camera) {
      this.camera.setCameraPosRot(mc.getRenderViewEntity());
      if (view instanceof EntityPlayer) {
        EntityPlayer viewPlayer = (EntityPlayer)view;
        this.camera.inventory = viewPlayer.inventory;
        this.camera.itemInUse = viewPlayer.itemInUse;
        this.camera.itemInUseCount = viewPlayer.itemInUseCount;
      } 
    } 
  }
  
  public void increaseSpeed() {}
  
  public void decreaseSpeed() {}
}
