package me.kaimson.melonclient.ingames;

import com.google.common.collect.Lists;
import java.util.List;
import me.kaimson.melonclient.ingames.utils.Combo;
import me.kaimson.melonclient.ingames.utils.MoreParticles;
import me.kaimson.melonclient.ingames.utils.Perspective;
import me.kaimson.melonclient.ingames.utils.Reach;
import me.kaimson.melonclient.ingames.utils.ReplayMod.recording.ReplayModRecording;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.ReplayModSimplePathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import me.kaimson.melonclient.ingames.utils.WindowedFullscreen;

public class IngameManager {
  private final List<Ingame> renders = Lists.newArrayList();
  
  public IngameManager() {
    add((Class)ReplayModReplay.class);
    add((Class)ReplayModSimplePathing.class);
    add((Class)ReplayModRecording.class);
    add((Class)Perspective.class);
    add((Class)WindowedFullscreen.class);
    add((Class)Reach.class);
    add((Class)MoreParticles.class);
    add((Class)Combo.class);
  }
  
  private void add(Class<? extends Ingame> clazz) {
    try {
      this.renders.add(clazz.newInstance());
    } catch (Throwable $ex) {
      throw $ex;
    } 
  }
  
  public void init() {
    for (Ingame clazz : this.renders)
      clazz.init(); 
  }
  
  public void patch() {
    for (Ingame clazz : this.renders)
      clazz.patch(); 
  }
}
