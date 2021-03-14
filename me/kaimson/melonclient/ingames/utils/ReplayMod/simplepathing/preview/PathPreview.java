package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.preview;

import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.Events.ReplayEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.KeyBindingRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.ReplayModSimplePathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.Setting;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;

public class PathPreview {
  private final ReplayModSimplePathing mod;
  
  private ReplayHandler replayHandler;
  
  private PathPreviewRenderer renderer;
  
  public PathPreview(ReplayModSimplePathing mod) {
    this.mod = mod;
  }
  
  @TypeEvent
  public void onReplayEvent(ReplayEvent event) {
    if (event.getState() == ReplayEvent.State.OPENED) {
      this.replayHandler = event.getReplayHandler();
      update();
    } else if (event.getState() == ReplayEvent.State.CLOSED) {
      this.replayHandler = null;
      update();
    } 
  }
  
  public void registerKeyBindings(KeyBindingRegistry registry) {
    registry.registerKeyBinding("replaymod.input.pathpreview", 35, () -> {
          SettingsRegistry settings = ReplayCore.getInstance().getSettingsRegistry();
          settings.set((SettingsRegistry.SettingKey)Setting.PATH_PREVIEW, Boolean.valueOf(!((Boolean)settings.get((SettingsRegistry.SettingKey)Setting.PATH_PREVIEW)).booleanValue()));
          settings.save();
        });
  }
  
  private void update() {
    if (((Boolean)ReplayCore.getInstance().getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.PATH_PREVIEW)).booleanValue() && this.replayHandler != null) {
      if (this.renderer == null) {
        this.renderer = new PathPreviewRenderer(this.mod, this.replayHandler);
        EventHandler.register(this.renderer);
      } 
    } else if (this.renderer != null) {
      EventHandler.unregister(this.renderer);
      this.renderer = null;
    } 
  }
}
