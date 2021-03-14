package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing;

import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.replay.ReplayFile;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.ingames.Ingame;
import me.kaimson.melonclient.ingames.utils.ReplayMod.Events.ReplayEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.gui.GuiPathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.preview.PathPreview;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;

public class ReplayModSimplePathing extends Ingame {
  private static ReplayModSimplePathing instance;
  
  private GuiPathing guiPathing;
  
  private ExecutorService saveService;
  
  private Change lastChange;
  
  public static ReplayModSimplePathing getInstance() {
    return instance;
  }
  
  public GuiPathing getGuiPathing() {
    return this.guiPathing;
  }
  
  private final AtomicInteger lastSaveId = new AtomicInteger();
  
  private SPTimeline currentTimeline;
  
  private SPTimeline.SPPath selectedPath;
  
  private long selectedTime;
  
  public SPTimeline getCurrentTimeline() {
    return this.currentTimeline;
  }
  
  public void setCurrentTimeline(SPTimeline currentTimeline) {
    this.currentTimeline = currentTimeline;
  }
  
  public long getSelectedTime() {
    return this.selectedTime;
  }
  
  public ReplayModSimplePathing() {
    instance = this;
    ReplayCore.getInstance().getSettingsRegistry().register(Setting.class);
    EventHandler.register(this);
    PathPreview pathPreview = new PathPreview(this);
    EventHandler.register(pathPreview);
  }
  
  public SPTimeline.SPPath getSelectedPath() {
    if ((ReplayModReplay.getInstance().getReplayHandler().getOverlay()).timeline.getSelectedMarker() != null) {
      this.selectedPath = null;
      this.selectedTime = 0L;
    } 
    return this.selectedPath;
  }
  
  @TypeEvent
  public void onReplayEventHandle(ReplayEvent event) {
    if (event.getState() == ReplayEvent.State.OPENED) {
      final ReplayFile replayFile = event.getReplayHandler().getReplayFile();
      try {
        synchronized (replayFile) {
          Timeline timeline = (Timeline)replayFile.getTimelines(new SPTimeline()).get("");
          if (timeline != null) {
            setCurrentTimeline(new SPTimeline(timeline));
          } else {
            setCurrentTimeline(new SPTimeline());
          } 
        } 
      } catch (IOException e) {
        e.printStackTrace();
      } 
      this.guiPathing = new GuiPathing(event.getReplayHandler());
      updateDefaultInterpolatorType();
      this.saveService = Executors.newSingleThreadExecutor();
      (new Runnable() {
          public void run() {
            ReplayModSimplePathing.this.maybeSaveTimeline(replayFile);
            if (ReplayModSimplePathing.this.guiPathing != null)
              ReplayCore.getInstance().runLater(this); 
          }
        }).run();
    } else if (event.getState() == ReplayEvent.State.CLOSING) {
      this.saveService.shutdown();
      try {
        this.saveService.awaitTermination(1L, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      } 
      this.saveService = null;
    } else if (event.getState() == ReplayEvent.State.CLOSED) {
      this.currentTimeline = null;
      this.guiPathing = null;
      this.selectedPath = null;
    } 
  }
  
  private void maybeSaveTimeline(ReplayFile replayFile) {
    Timeline timeline;
    SPTimeline spTimeline = this.currentTimeline;
    if (spTimeline == null || this.saveService == null) {
      this.lastChange = null;
      return;
    } 
    Change latestChange = spTimeline.getTimeline().peekUndoStack();
    if (latestChange == null || latestChange == this.lastChange)
      return; 
    this.lastChange = latestChange;
    try {
      TimelineSerialization serialization = new TimelineSerialization(spTimeline, null);
      String serialized = serialization.serialize(Collections.singletonMap("", spTimeline.getTimeline()));
      timeline = (Timeline)serialization.deserialize(serialized).get("");
    } catch (Throwable t) {
      t.printStackTrace();
      Client.log("FATAL Error: Cloning timeline");
      return;
    } 
    int id = this.lastSaveId.incrementAndGet();
    this.saveService.submit(() -> {
          if (this.lastSaveId.get() != id)
            return; 
          try {
            saveTimeline(replayFile, spTimeline, timeline);
          } catch (IOException e) {
            e.printStackTrace();
            Client.log("Error: Auto-saving timeline");
          } 
        });
  }
  
  private void saveTimeline(ReplayFile replayFile, PathingRegistry pathingRegistry, Timeline timeline) throws IOException {
    synchronized (replayFile) {
      Map<String, Timeline> timelineMap = replayFile.getTimelines(pathingRegistry);
      timelineMap.put("", timeline);
      replayFile.writeTimelines(pathingRegistry, timelineMap);
    } 
  }
  
  private void updateDefaultInterpolatorType() {
    InterpolatorType newDefaultType = InterpolatorType.fromString((String)ReplayCore.getInstance().getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.DEFAULT_INTERPOLATION));
    this.currentTimeline.setDefaultInterpolatorType(newDefaultType);
  }
  
  public boolean isSelected(Keyframe keyframe) {
    return (getSelectedPath() != null && this.currentTimeline.getKeyframe(this.selectedPath, this.selectedTime) == keyframe);
  }
  
  public void setSelected(SPTimeline.SPPath path, long time) {
    this.selectedPath = path;
    this.selectedTime = time;
    if (this.selectedPath != null)
      (ReplayModReplay.getInstance().getReplayHandler().getOverlay()).timeline.setSelectedMarker(null); 
  }
  
  public void clearCurrentTimeline() {
    setCurrentTimeline(new SPTimeline());
  }
}
