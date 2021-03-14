package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.gui;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.util.EntityPositionTracker;
import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.Utils;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiClickable;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiElement;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiPopup;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiHorizontalScrollbar;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiInfoPopup;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiLabel;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiPanel;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiProgressBar;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiTexturedButton;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiTimelineTime;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiTooltip;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.ReplayModSimplePathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.SPTimeline;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.player.RealtimeTimelinePlayer;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.CameraProperties;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.SpectatorProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.TimestampProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraEntity;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.overlay.GuiReplayOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class GuiPathing {
  private static final Logger logger = LogManager.getLogger();
  
  public final GuiTexturedButton playPauseButton = (GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)(new GuiTexturedButton() {
      public GuiElement getTooltip(RenderInfo renderInfo) {
        GuiTooltip tooltip = (GuiTooltip)super.getTooltip(renderInfo);
        if (tooltip != null)
          if (GuiPathing.this.player.isActive()) {
            tooltip.setI18nText("replaymod.gui.ingame.menu.pausepath", new Object[0]);
          } else if (Keyboard.isKeyDown(29)) {
            tooltip.setI18nText("replaymod.gui.ingame.menu.playpathfromstart", new Object[0]);
          } else {
            tooltip.setI18nText("replaymod.gui.ingame.menu.playpath", new Object[0]);
          }  
        return (GuiElement)tooltip;
      }
    }).setSize(20, 20)).setTexture(ReplayCore.TEXTURE, 256)).setTooltip((GuiElement)new GuiTooltip());
  
  public final GuiTexturedButton renderButton = (GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)(new GuiTexturedButton()).onClick(new Runnable() {
        public void run() {
          if (!GuiPathing.this.preparePathsForPlayback())
            return; 
          SPTimeline spTimeline = ReplayModSimplePathing.getInstance().getCurrentTimeline();
          try {
            TimelineSerialization serialization = new TimelineSerialization((PathingRegistry)spTimeline, null);
            String serialized = serialization.serialize(Collections.singletonMap("", spTimeline.getTimeline()));
            Timeline timeline = (Timeline)serialization.deserialize(serialized).get("");
          } catch (Throwable t) {
            Utils.error((GuiContainer)GuiPathing.this.replayHandler.getOverlay(), CrashReport.makeCrashReport(t, "Cloning timeline"), () -> {
                
                });
            return;
          } 
        }
      })).setSize(20, 20)).setTexture(ReplayCore.TEXTURE, 256)).setTexturePosH(40, 0))
    .setTooltip((GuiElement)(new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.renderpath", new Object[0]));
  
  public final GuiTexturedButton positionKeyframeButton = (GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)(new GuiTexturedButton() {
      public GuiElement getTooltip(RenderInfo renderInfo) {
        GuiTooltip tooltip = (GuiTooltip)super.getTooltip(renderInfo);
        if (tooltip != null)
          if (getTextureNormal().getY() == 40) {
            if (getTextureNormal().getX() == 0) {
              tooltip.setI18nText("replaymod.gui.ingame.menu.addposkeyframe", new Object[0]);
            } else {
              tooltip.setI18nText("replaymod.gui.ingame.menu.addspeckeyframe", new Object[0]);
            } 
          } else if (getTextureNormal().getX() == 0) {
            tooltip.setI18nText("replaymod.gui.ingame.menu.removeposkeyframe", new Object[0]);
          } else {
            tooltip.setI18nText("replaymod.gui.ingame.menu.removespeckeyframe", new Object[0]);
          }  
        return (GuiElement)tooltip;
      }
    }).setSize(20, 20)).setTexture(ReplayCore.TEXTURE, 256)).setTooltip((GuiElement)new GuiTooltip());
  
  public final GuiTexturedButton timeKeyframeButton = (GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)(new GuiTexturedButton() {
      public GuiElement getTooltip(RenderInfo renderInfo) {
        GuiTooltip tooltip = (GuiTooltip)super.getTooltip(renderInfo);
        if (tooltip != null)
          if (getTextureNormal().getY() == 80) {
            tooltip.setI18nText("replaymod.gui.ingame.menu.addtimekeyframe", new Object[0]);
          } else {
            tooltip.setI18nText("replaymod.gui.ingame.menu.removetimekeyframe", new Object[0]);
          }  
        return (GuiElement)tooltip;
      }
    }).setSize(20, 20)).setTexture(ReplayCore.TEXTURE, 256)).setTooltip((GuiElement)new GuiTooltip());
  
  public final GuiKeyframeTimeline timeline = (GuiKeyframeTimeline)((GuiKeyframeTimeline)((GuiKeyframeTimeline)(new GuiKeyframeTimeline(this) {
      public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (GuiPathing.this.player.isActive())
          setCursorPosition((int)GuiPathing.this.player.getTimePassed()); 
        super.draw(renderer, size, renderInfo);
      }
    }).setSize(2147483647, 20)).setLength(1800000)).setMarkers();
  
  public final GuiHorizontalScrollbar scrollbar = (GuiHorizontalScrollbar)(new GuiHorizontalScrollbar()).setSize(2147483647, 9);
  
  public final GuiTimelineTime<GuiKeyframeTimeline> timelineTime;
  
  public final GuiTexturedButton zoomInButton;
  
  public final GuiTexturedButton zoomOutButton;
  
  public final GuiPanel zoomButtonPanel;
  
  public final GuiPanel timelinePanel;
  
  public final GuiPanel panel;
  
  private final IGuiClickable clickCatcher;
  
  private final ReplayHandler replayHandler;
  
  private final RealtimeTimelinePlayer player;
  
  private boolean errorShown;
  
  private EntityPositionTracker entityTracker;
  
  private Consumer<Double> entityTrackerLoadingProgress;
  
  private SettableFuture<Void> entityTrackerFuture;
  
  public GuiPathing(final ReplayHandler replayHandler) {
    ((GuiHorizontalScrollbar)this.scrollbar.onValueChanged(new Runnable() {
          public void run() {
            GuiPathing.this.timeline.setOffset((int)(GuiPathing.this.scrollbar.getPosition() * GuiPathing.this.timeline.getLength()));
            GuiPathing.this.timeline.setZoom(GuiPathing.this.scrollbar.getZoom());
          }
        })).setZoom(0.1D);
    this
      .timelineTime = (GuiTimelineTime<GuiKeyframeTimeline>)(new GuiTimelineTime()).setTimeline(this.timeline);
    this
      
      .zoomInButton = (GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)(new GuiTexturedButton()).setSize(9, 9)).onClick(new Runnable() {
          public void run() {
            GuiPathing.this.zoomTimeline(0.6666666666666666D);
          }
        })).setTexture(ReplayCore.TEXTURE, 256)).setTexturePosH(40, 20)).setTooltip((GuiElement)(new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.zoomin", new Object[0]));
    this
      
      .zoomOutButton = (GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)(new GuiTexturedButton()).setSize(9, 9)).onClick(new Runnable() {
          public void run() {
            GuiPathing.this.zoomTimeline(1.5D);
          }
        })).setTexture(ReplayCore.TEXTURE, 256)).setTexturePosH(40, 30)).setTooltip((GuiElement)(new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.zoomout", new Object[0]));
    this
      
      .zoomButtonPanel = (GuiPanel)((GuiPanel)(new GuiPanel()).setLayout((Layout)(new VerticalLayout(VerticalLayout.Alignment.CENTER)).setSpacing(2))).addElements(null, new GuiElement[] { (GuiElement)this.zoomInButton, (GuiElement)this.zoomOutButton });
    this
      
      .timelinePanel = (GuiPanel)((GuiPanel)((GuiPanel)(new GuiPanel()).setSize(2147483647, 40)).setLayout((Layout)new CustomLayout<GuiPanel>() {
          protected void layout(GuiPanel container, int width, int height) {
            pos((GuiElement)GuiPathing.this.zoomButtonPanel, width - width((GuiElement)GuiPathing.this.zoomButtonPanel), 10);
            pos((GuiElement)GuiPathing.this.timelineTime, 0, 2);
            size((GuiElement)GuiPathing.this.timelineTime, x((GuiElement)GuiPathing.this.zoomButtonPanel), 8);
            pos((GuiElement)GuiPathing.this.timeline, 0, y((GuiElement)GuiPathing.this.timelineTime) + height((GuiElement)GuiPathing.this.timelineTime));
            size((GuiElement)GuiPathing.this.timeline, x((GuiElement)GuiPathing.this.zoomButtonPanel) - 2, 20);
            pos((GuiElement)GuiPathing.this.scrollbar, 0, y((GuiElement)GuiPathing.this.timeline) + height((GuiElement)GuiPathing.this.timeline) + 1);
            size((GuiElement)GuiPathing.this.scrollbar, x((GuiElement)GuiPathing.this.zoomButtonPanel) - 2, 9);
          }
        })).addElements(null, new GuiElement[] { (GuiElement)this.timelineTime, (GuiElement)this.timeline, (GuiElement)this.scrollbar, (GuiElement)this.zoomButtonPanel });
    this
      
      .panel = (GuiPanel)((GuiPanel)(new GuiPanel()).setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(5))).addElements((LayoutData)new HorizontalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.playPauseButton, (GuiElement)this.renderButton, (GuiElement)this.positionKeyframeButton, (GuiElement)this.timeKeyframeButton, (GuiElement)this.timelinePanel });
    this.clickCatcher = (IGuiClickable)new AbstractGuiClickable() {
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
          if (GuiPathing.this.player.isActive())
            GuiPathing.this.replayHandler.getOverlay().setMouseVisible(true); 
        }
        
        protected AbstractGuiElement getThis() {
          return (AbstractGuiElement)this;
        }
        
        protected ReadableDimension calcMinSize() {
          return (ReadableDimension)new Dimension(0, 0);
        }
        
        public boolean mouseClick(ReadablePoint position, int button) {
          if (GuiPathing.this.player.isActive()) {
            GuiPathing.this.playPauseButton.mouseClick(position, button);
            return true;
          } 
          return false;
        }
        
        public int getLayer() {
          return GuiPathing.this.player.isActive() ? 10 : 0;
        }
      };
    this.replayHandler = replayHandler;
    this.player = new RealtimeTimelinePlayer(replayHandler);
    final GuiReplayOverlay overlay = replayHandler.getOverlay();
    ((GuiTexturedButton)this.playPauseButton.setTexturePosH(new ReadablePoint() {
          public int getX() {
            return 0;
          }
          
          public int getY() {
            return GuiPathing.this.player.isActive() ? 20 : 0;
          }
          
          public void getLocation(WritablePoint dest) {
            dest.setLocation(getX(), getY());
          }
        })).onClick(() -> {
          if (this.player.isActive()) {
            this.player.getFuture().cancel(false);
          } else {
            final Path timePath = ReplayModSimplePathing.getInstance().getCurrentTimeline().getTimePath();
            if (!preparePathsForPlayback())
              return; 
            timePath.setActive(!Keyboard.isKeyDown(42));
            int startTime = Keyboard.isKeyDown(29) ? 0 : this.timeline.getCursorPosition();
            final ListenableFuture<Void> future = this.player.start(ReplayModSimplePathing.getInstance().getCurrentTimeline().getTimeline(), startTime);
            overlay.setCloseable(false);
            overlay.setMouseVisible(true);
            Client.sendChatMessage("replaymod.chat.pathstarted");
            Futures.addCallback(future, new FutureCallback<Void>() {
                  public void onSuccess(Void result) {
                    if (future.isCancelled()) {
                      Client.sendChatMessage("replaymod.chat.pathinterrupted");
                    } else {
                      Client.sendChatMessage("replaymod.chat.pathfinished");
                    } 
                    overlay.setCloseable(true);
                    timePath.setActive(true);
                  }
                  
                  public void onFailure(Throwable t) {
                    t.printStackTrace();
                    overlay.setCloseable(true);
                    timePath.setActive(true);
                  }
                });
          } 
        });
    ((GuiTexturedButton)this.positionKeyframeButton.setTexturePosH(new ReadablePoint() {
          public int getX() {
            SPTimeline.SPPath keyframePath = ReplayModSimplePathing.getInstance().getSelectedPath();
            long keyframeTime = ReplayModSimplePathing.getInstance().getSelectedTime();
            if (keyframePath != SPTimeline.SPPath.POSITION) {
              keyframeTime = GuiPathing.this.timeline.getCursorPosition();
              keyframePath = ReplayModSimplePathing.getInstance().getCurrentTimeline().isPositionKeyframe(keyframeTime) ? SPTimeline.SPPath.POSITION : null;
            } 
            if (keyframePath != SPTimeline.SPPath.POSITION)
              return replayHandler.isCameraView() ? 0 : 40; 
            return ReplayModSimplePathing.getInstance().getCurrentTimeline().isSpectatorKeyframe(keyframeTime) ? 40 : 0;
          }
          
          public int getY() {
            SPTimeline.SPPath keyframePath = ReplayModSimplePathing.getInstance().getSelectedPath();
            if (keyframePath != SPTimeline.SPPath.POSITION)
              keyframePath = ReplayModSimplePathing.getInstance().getCurrentTimeline().isPositionKeyframe(GuiPathing.this.timeline.getCursorPosition()) ? SPTimeline.SPPath.POSITION : null; 
            return (keyframePath == SPTimeline.SPPath.POSITION) ? 60 : 40;
          }
          
          public void getLocation(WritablePoint dest) {
            dest.setLocation(getX(), getY());
          }
        })).onClick(new Runnable() {
          public void run() {
            GuiPathing.this.updateKeyframe(SPTimeline.SPPath.POSITION);
          }
        });
    ((GuiTexturedButton)this.timeKeyframeButton.setTexturePosH(new ReadablePoint() {
          public int getX() {
            return 0;
          }
          
          public int getY() {
            SPTimeline.SPPath keyframePath = ReplayModSimplePathing.getInstance().getSelectedPath();
            if (keyframePath != SPTimeline.SPPath.TIME)
              keyframePath = ReplayModSimplePathing.getInstance().getCurrentTimeline().isTimeKeyframe(GuiPathing.this.timeline.getCursorPosition()) ? SPTimeline.SPPath.TIME : null; 
            return (keyframePath == SPTimeline.SPPath.TIME) ? 100 : 80;
          }
          
          public void getLocation(WritablePoint dest) {
            dest.setLocation(getX(), getY());
          }
        })).onClick(new Runnable() {
          public void run() {
            GuiPathing.this.updateKeyframe(SPTimeline.SPPath.TIME);
          }
        });
    overlay.addElements(null, new GuiElement[] { (GuiElement)this.panel, (GuiElement)this.clickCatcher });
    overlay.setLayout((Layout)new CustomLayout<GuiReplayOverlay>(overlay.getLayout()) {
          protected void layout(GuiReplayOverlay container, int width, int height) {
            pos((GuiElement)GuiPathing.this.panel, 10, y((GuiElement)overlay.topPanel) + height((GuiElement)overlay.topPanel) + 3);
            size((GuiElement)GuiPathing.this.panel, width - 20, 40);
            size((GuiElement)GuiPathing.this.clickCatcher, 0, 0);
          }
        });
    startLoadingEntityTracker();
  }
  
  public void keyframeRepoButtonPressed() {
    try {
      GuiKeyframeRepository gui = new GuiKeyframeRepository((PathingRegistry)ReplayModSimplePathing.getInstance().getCurrentTimeline(), this.replayHandler.getReplayFile(), ReplayModSimplePathing.getInstance().getCurrentTimeline().getTimeline());
      Futures.addCallback((ListenableFuture)gui.getFuture(), new FutureCallback<Timeline>() {
            public void onSuccess(Timeline result) {
              if (result != null)
                ReplayModSimplePathing.getInstance().setCurrentTimeline(new SPTimeline(result)); 
            }
            
            public void onFailure(Throwable t) {
              t.printStackTrace();
              Client.sendChatMessage("Error loading timeline: " + t.getMessage());
            }
          });
      gui.display();
    } catch (IOException e) {
      e.printStackTrace();
      Client.sendChatMessage("Error loading timeline: " + e.getMessage());
    } 
  }
  
  public void clearKeyframesButtonPressed() {
    GuiYesNoPopup popup = GuiYesNoPopup.open((GuiContainer)this.replayHandler.getOverlay(), new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.clearcallback.title", new Object[0])).setColor(Colors.BLACK) }).setYesI18nLabel("gui.yes", new Object[0]).setNoI18nLabel("gui.no", new Object[0]);
    Futures.addCallback(popup.getFuture(), new FutureCallback<Boolean>() {
          public void onSuccess(Boolean delete) {
            if (delete.booleanValue()) {
              ReplayModSimplePathing.getInstance().clearCurrentTimeline();
              if (GuiPathing.this.entityTracker != null)
                ReplayModSimplePathing.getInstance().getCurrentTimeline().setEntityTracker(GuiPathing.this.entityTracker); 
            } 
          }
          
          public void onFailure(Throwable t) {
            t.printStackTrace();
          }
        });
  }
  
  public void syncTimeButtonPressed() {
    int time = this.replayHandler.getReplaySender().currentTimeStamp();
    int cursor = this.timeline.getCursorPosition();
    ReplayModSimplePathing.getInstance().getCurrentTimeline().getTimePath().getKeyframes().stream()
      .filter(it -> (it.getTime() <= cursor)).reduce((__, last) -> last).ifPresent(keyframe -> {
          int keyframeCursor = (int)keyframe.getTime();
          int keyframeTime = ((Integer)keyframe.getValue((Property)TimestampProperty.PROPERTY).get()).intValue();
          int timePassed = time - keyframeTime;
          double speed = Keyboard.isKeyDown(42) ? 1.0D : this.replayHandler.getOverlay().getSpeedSliderValue();
          int cursorPassed = (int)(timePassed / speed);
          this.timeline.setCursorPosition(keyframeCursor + cursorPassed);
          ReplayModSimplePathing.getInstance().setSelected(null, 0L);
        });
  }
  
  public void deleteButtonPressed() {
    if (ReplayModSimplePathing.getInstance().getSelectedPath() != null)
      updateKeyframe(ReplayModSimplePathing.getInstance().getSelectedPath()); 
  }
  
  private void startLoadingEntityTracker() {
    Preconditions.checkState((this.entityTrackerFuture == null));
    this.entityTrackerFuture = SettableFuture.create();
    (new Thread(() -> {
          EntityPositionTracker tracker = new EntityPositionTracker(this.replayHandler.getReplayFile());
          try {
            long start = System.currentTimeMillis();
            tracker.load(());
            logger.info("Loaded entity tracker in " + (System.currentTimeMillis() - start) + "ms");
          } catch (Throwable e) {
            logger.error("Loading entity tracker:", e);
            ReplayCore.getInstance().runLater(());
            return;
          } 
          this.entityTracker = tracker;
          ReplayCore.getInstance().runLater(());
        })).start();
  }
  
  private boolean preparePathsForPlayback() {
    SPTimeline timeline = ReplayModSimplePathing.getInstance().getCurrentTimeline();
    timeline.getTimeline().getPaths().forEach(Path::updateAll);
    int lastTime = 0;
    for (Keyframe keyframe : timeline.getTimePath().getKeyframes()) {
      int time = ((Integer)keyframe.getValue((Property)TimestampProperty.PROPERTY).orElseThrow(IllegalStateException::new)).intValue();
      if (time < lastTime) {
        GuiInfoPopup.open((GuiContainer)this.replayHandler.getOverlay(), new String[] { "replaymod.error.negativetime1", "replaymod.error.negativetime2", "replaymod.error.negativetime3" });
        return false;
      } 
      lastTime = time;
    } 
    if (timeline.getPositionPath().getSegments().isEmpty() || timeline
      .getTimePath().getSegments().isEmpty()) {
      GuiInfoPopup.open((GuiContainer)this.replayHandler.getOverlay(), new String[] { "replaymod.chat.morekeyframes" });
      return false;
    } 
    return true;
  }
  
  public void zoomTimeline(double factor) {
    this.scrollbar.setZoom(this.scrollbar.getZoom() * factor);
  }
  
  public boolean loadEntityTracker(final Runnable thenRun) {
    if (this.entityTracker == null && !this.errorShown) {
      Client.debug("Entity tracker not yet loaded, delaying...", new Object[0]);
      final LoadEntityTrackerPopup popup = new LoadEntityTrackerPopup((GuiContainer)this.replayHandler.getOverlay());
      this.entityTrackerLoadingProgress = (p -> (GuiProgressBar)popup.progressBar.setProgress(p.floatValue()));
      Futures.addCallback((ListenableFuture)this.entityTrackerFuture, new FutureCallback<Void>() {
            public void onSuccess(Void result) {
              popup.close();
              if (ReplayModSimplePathing.getInstance().getCurrentTimeline().getEntityTracker() == null)
                ReplayModSimplePathing.getInstance().getCurrentTimeline().setEntityTracker(GuiPathing.this.entityTracker); 
              thenRun.run();
            }
            
            public void onFailure(Throwable t) {
              if (!GuiPathing.this.errorShown) {
                String message = "Failed to load entity tracker, spectator keyframes will be broken.";
                GuiReplayOverlay overlay = GuiPathing.this.replayHandler.getOverlay();
                Utils.error((GuiContainer)overlay, CrashReport.makeCrashReport(t, message), () -> {
                      popup.close();
                      thenRun.run();
                    });
                GuiPathing.this.errorShown = true;
              } else {
                thenRun.run();
              } 
            }
          });
      return false;
    } 
    if (ReplayModSimplePathing.getInstance().getCurrentTimeline().getEntityTracker() == null)
      ReplayModSimplePathing.getInstance().getCurrentTimeline().setEntityTracker(this.entityTracker); 
    return true;
  }
  
  private void updateKeyframe(SPTimeline.SPPath path) {
    CameraEntity camera;
    int spectatedId;
    Client.debug("Updating keyframe on path {}" + path, new Object[0]);
    if (!loadEntityTracker(() -> updateKeyframe(path)))
      return; 
    int time = this.timeline.getCursorPosition();
    SPTimeline timeline = ReplayModSimplePathing.getInstance().getCurrentTimeline();
    switch (path) {
      case TIME:
        if (ReplayModSimplePathing.getInstance().getSelectedPath() == path) {
          Client.debug("Selected keyframe is time keyframe -> removing keyframe", new Object[0]);
          timeline.removeTimeKeyframe(ReplayModSimplePathing.getInstance().getSelectedTime());
          ReplayModSimplePathing.getInstance().setSelected(null, 0L);
          break;
        } 
        if (timeline.isTimeKeyframe(time)) {
          Client.debug("Keyframe at cursor position is time keyframe -> removing keyframe", new Object[0]);
          timeline.removeTimeKeyframe(time);
          ReplayModSimplePathing.getInstance().setSelected(null, 0L);
          break;
        } 
        Client.debug("No time keyframe found -> adding new keyframe", new Object[0]);
        timeline.addTimeKeyframe(time, this.replayHandler.getReplaySender().currentTimeStamp());
        ReplayModSimplePathing.getInstance().setSelected(path, time);
        break;
      case POSITION:
        if (ReplayModSimplePathing.getInstance().getSelectedPath() == path) {
          Client.debug("Selected keyframe is position keyframe -> removing keyframe", new Object[0]);
          timeline.removePositionKeyframe(ReplayModSimplePathing.getInstance().getSelectedTime());
          ReplayModSimplePathing.getInstance().setSelected(null, 0L);
          break;
        } 
        if (timeline.isPositionKeyframe(time)) {
          Client.debug("Keyframe at cursor position is position keyframe -> removing keyframe", new Object[0]);
          timeline.removePositionKeyframe(time);
          ReplayModSimplePathing.getInstance().setSelected(null, 0L);
          break;
        } 
        Client.debug("No position keyframe found -> adding new keyframe", new Object[0]);
        camera = this.replayHandler.getCameraEntity();
        spectatedId = -1;
        if (!this.replayHandler.isCameraView())
          spectatedId = Minecraft.getMinecraft().getRenderViewEntity().getEntityId(); 
        timeline.addPositionKeyframe(time, camera.posX, camera.posY, camera.posZ, camera.rotationYaw, camera.rotationPitch, camera.roll, spectatedId);
        ReplayModSimplePathing.getInstance().setSelected(path, time);
        break;
    } 
  }
  
  public EntityPositionTracker getEntityTracker() {
    return this.entityTracker;
  }
  
  public void openEditKeyframePopup(SPTimeline.SPPath path, long time) {
    if (!loadEntityTracker(() -> openEditKeyframePopup(path, time)))
      return; 
    Keyframe keyframe = ReplayModSimplePathing.getInstance().getCurrentTimeline().getKeyframe(path, time);
    if (keyframe.getProperties().contains(SpectatorProperty.PROPERTY)) {
      (new GuiEditKeyframe.Spectator(this, path, keyframe.getTime())).open();
    } else if (keyframe.getProperties().contains(CameraProperties.POSITION)) {
      (new GuiEditKeyframe.Position(this, path, keyframe.getTime())).open();
    } else {
      (new GuiEditKeyframe.Time(this, path, keyframe.getTime())).open();
    } 
  }
  
  private class LoadEntityTrackerPopup extends AbstractGuiPopup<LoadEntityTrackerPopup> {
    private final GuiProgressBar progressBar = (GuiProgressBar)((GuiProgressBar)(new GuiProgressBar((GuiContainer)this.popup)).setSize(300, 20))
      .setI18nLabel("replaymod.gui.loadentitytracker", new Object[0]);
    
    public LoadEntityTrackerPopup(GuiContainer container) {
      super(container);
      open();
    }
    
    public void close() {
      super.close();
    }
    
    protected LoadEntityTrackerPopup getThis() {
      return this;
    }
  }
}
