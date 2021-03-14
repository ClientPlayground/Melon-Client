package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer;

import com.google.common.base.Function;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.ingames.Ingame;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.KeyBindingRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraController;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraControllerRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraEntity;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.VanillaCameraController;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.handler.GuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

public class ReplayModReplay extends Ingame {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final CameraControllerRegistry cameraControllerRegistry = new CameraControllerRegistry();
  
  protected ReplayHandler replayHandler;
  
  private static ReplayModReplay instance;
  
  public ReplayHandler getReplayHandler() {
    return this.replayHandler;
  }
  
  public static ReplayModReplay getInstance() {
    return instance;
  }
  
  public ReplayModReplay() {
    instance = this;
  }
  
  public void init() {
    EventHandler.register(this);
    this.mc.timer = (Timer)new InputReplayTimer(this.mc.timer);
    (new GuiHandler()).register();
    this.cameraControllerRegistry.register("replaymod.camera.classic", me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.ClassicCameraController::new);
    this.cameraControllerRegistry.register("replaymod.camera.vanilla", cameraEntity -> new VanillaCameraController(Minecraft.getMinecraft(), cameraEntity));
  }
  
  public void patch() {
    KeyBindingRegistry registry = ReplayCore.getInstance().getKeyBindingRegistry();
    registry.registerKeyBinding("replaymod.input.playpause", 25, () -> {
          if (this.replayHandler != null)
            (this.replayHandler.getOverlay()).playPauseButton.onClick(); 
        });
    registry.registerKeyBinding("replaymod.input.rollclockwise", 36, () -> {
        
        });
    registry.registerKeyBinding("replaymod.input.rollcounterclockwise", 38, () -> {
        
        });
    registry.registerKeyBinding("replaymod.input.resettilt", 37, () -> Optional.<ReplayHandler>ofNullable(this.replayHandler).map(ReplayHandler::getCameraEntity).ifPresent(()));
  }
  
  public void startReplay(File file) throws IOException {
    startReplay((ReplayFile)new ZipReplayFile((Studio)new ReplayStudio(), file));
  }
  
  public void startReplay(ReplayFile replayFile) throws IOException {
    if (this.replayHandler != null)
      this.replayHandler.endReplay(); 
    this.replayHandler = new ReplayHandler(replayFile, true);
  }
  
  public void forcefullyStopReplay() {
    this.replayHandler = null;
  }
  
  public CameraController createCameraController(CameraEntity cameraEntity) {
    String controllerName = (String)ReplayCore.getInstance().getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.CAMERA);
    return this.cameraControllerRegistry.create(controllerName, cameraEntity);
  }
}
