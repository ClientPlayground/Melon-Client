package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.gui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiElement;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiTimeline;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.ReplayModSimplePathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.SPTimeline;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.CameraProperties;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.SpectatorProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.TimestampProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.Pair;

public class GuiKeyframeTimeline extends AbstractGuiTimeline<GuiKeyframeTimeline> implements Draggable {
  protected static final int KEYFRAME_SIZE = 5;
  
  protected static final int KEYFRAME_TEXTURE_X = 74;
  
  protected static final int KEYFRAME_TEXTURE_Y = 20;
  
  private static final int DOUBLE_CLICK_INTERVAL = 250;
  
  private static final int DRAGGING_THRESHOLD = 5;
  
  private final GuiPathing gui;
  
  private long lastClickedKeyframe;
  
  private SPTimeline.SPPath lastClickedPath;
  
  private long lastClickedTime;
  
  private boolean dragging;
  
  private boolean actuallyDragging;
  
  private int draggingStartX;
  
  private Change draggingChange;
  
  public GuiKeyframeTimeline(GuiPathing gui) {
    this.gui = gui;
  }
  
  protected void drawTimelineCursor(GuiRenderer renderer, ReadableDimension size) {
    int width = size.getWidth();
    int visibleWidth = width - 4 - 4;
    int startTime = getOffset();
    int visibleTime = (int)(getZoom() * getLength());
    int endTime = getOffset() + visibleTime;
    renderer.bindTexture(ReplayCore.TEXTURE);
    SPTimeline timeline = ReplayModSimplePathing.getInstance().getCurrentTimeline();
    timeline.getTimeline().getPaths().stream().flatMap(path -> path.getKeyframes().stream()).forEach(keyframe -> {
          if (keyframe.getTime() >= startTime && keyframe.getTime() <= endTime) {
            double relativeTime = (keyframe.getTime() - startTime);
            int positonX = 4 + (int)(relativeTime / visibleTime * visibleWidth) - 2;
            int u = 74 + (ReplayModSimplePathing.getInstance().isSelected(keyframe) ? 5 : 0);
            int v = 20;
            if (keyframe.getValue((Property)CameraProperties.POSITION).isPresent()) {
              if (keyframe.getValue((Property)SpectatorProperty.PROPERTY).isPresent())
                v += 10; 
              renderer.drawTexturedRect(positonX, 4, u, v, 5, 5);
            } 
            if (keyframe.getValue((Property)TimestampProperty.PROPERTY).isPresent()) {
              v += 5;
              renderer.drawTexturedRect(positonX, 9, u, v, 5, 5);
            } 
          } 
        });
    for (PathSegment segment : timeline.getPositionPath().getSegments()) {
      if (segment.getInterpolator() == null || 
        !segment.getInterpolator().getKeyframeProperties().contains(SpectatorProperty.PROPERTY))
        continue; 
      drawQuadOnSegment(renderer, visibleWidth, segment, 5, -16742145);
    } 
    for (PathSegment segment : timeline.getTimePath().getSegments()) {
      long startTimestamp = ((Integer)segment.getStartKeyframe().getValue((Property)TimestampProperty.PROPERTY).orElseThrow(IllegalStateException::new)).intValue();
      long endTimestamp = ((Integer)segment.getEndKeyframe().getValue((Property)TimestampProperty.PROPERTY).orElseThrow(IllegalStateException::new)).intValue();
      if (endTimestamp >= startTimestamp)
        continue; 
      drawQuadOnSegment(renderer, visibleWidth, segment, 10, -65536);
    } 
    super.drawTimelineCursor(renderer, size);
  }
  
  private void drawQuadOnSegment(GuiRenderer renderer, int visibleWidth, PathSegment segment, int y, int color) {
    int startTime = getOffset();
    int visibleTime = (int)(getZoom() * getLength());
    int endTime = getOffset() + visibleTime;
    long startFrameTime = segment.getStartKeyframe().getTime();
    long endFrameTime = segment.getEndKeyframe().getTime();
    if (startFrameTime >= endTime || endFrameTime <= startTime)
      return; 
    double relativeStart = (startFrameTime - startTime);
    double relativeEnd = (endFrameTime - startTime);
    int startX = 4 + Math.max(0, (int)(relativeStart / visibleTime * visibleWidth) + 2 + 1);
    int endX = 4 + Math.min(visibleWidth, (int)(relativeEnd / visibleTime * visibleWidth) - 2);
    if (startX < endX)
      renderer.drawRect(startX + 1, y, endX - startX - 2, 3, color); 
  }
  
  private Pair<SPTimeline.SPPath, Long> getKeyframe(ReadablePoint position) {
    int time = getTimeAt(position.getX(), position.getY());
    if (time != -1) {
      Point mouse = new Point(position);
      getContainer().convertFor((GuiElement)this, mouse);
      int mouseY = mouse.getY();
      if (mouseY > 4 && mouseY < 14) {
        SPTimeline.SPPath path;
        if (mouseY <= 9) {
          path = SPTimeline.SPPath.POSITION;
        } else {
          path = SPTimeline.SPPath.TIME;
        } 
        int visibleTime = (int)(getZoom() * getLength());
        int tolerance = visibleTime * 5 / (getLastSize().getWidth() - 4 - 4) / 2;
        Optional<Keyframe> keyframe = ReplayModSimplePathing.getInstance().getCurrentTimeline().getPath(path).getKeyframes().stream().filter(k -> (Math.abs(k.getTime() - time) <= tolerance)).sorted(Comparator.comparing(k -> Long.valueOf(Math.abs(k.getTime() - time)))).findFirst();
        return Pair.of(path, keyframe.map(Keyframe::getTime).orElse(null));
      } 
    } 
    return Pair.of(null, null);
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    int time = getTimeAt(position.getX(), position.getY());
    Pair<SPTimeline.SPPath, Long> pathKeyframePair = getKeyframe(position);
    if (pathKeyframePair.getRight() != null) {
      SPTimeline.SPPath path = (SPTimeline.SPPath)pathKeyframePair.getLeft();
      long keyframeTime = ((Long)pathKeyframePair.getRight()).longValue();
      if (button == 0) {
        long now = Minecraft.getSystemTime();
        if (this.lastClickedKeyframe == keyframeTime)
          if (now - this.lastClickedTime < 250L) {
            this.gui.openEditKeyframePopup(path, keyframeTime);
            return true;
          }  
        this.lastClickedTime = now;
        this.lastClickedKeyframe = keyframeTime;
        this.lastClickedPath = path;
        ReplayModSimplePathing.getInstance().setSelected(this.lastClickedPath, this.lastClickedKeyframe);
        this.draggingStartX = position.getX();
        this.dragging = true;
      } else if (button == 1) {
        Keyframe keyframe = ReplayModSimplePathing.getInstance().getCurrentTimeline().getKeyframe(path, keyframeTime);
        for (Property<?> property : (Iterable<Property<?>>)keyframe.getProperties())
          applyPropertyToGame(property, keyframe); 
      } 
      return true;
    } 
    if (time != -1) {
      if (button == 0) {
        setCursorPosition(time);
        ReplayModSimplePathing.getInstance().setSelected(null, 0L);
      } else if (button == 1 && 
        pathKeyframePair.getLeft() != null) {
        Path path = ReplayModSimplePathing.getInstance().getCurrentTimeline().getPath((SPTimeline.SPPath)pathKeyframePair.getLeft());
        path.getKeyframes().stream().flatMap(k -> k.getProperties().stream()).distinct().forEach(p -> applyPropertyToGame(p, path, time));
      } 
      return true;
    } 
    return false;
  }
  
  private <T> void applyPropertyToGame(Property<T> property, Path path, long time) {
    Optional<T> value = path.getValue(property, time);
    if (value.isPresent())
      property.applyToGame(value.get(), ReplayModReplay.getInstance().getReplayHandler()); 
  }
  
  private <T> void applyPropertyToGame(Property<T> property, Keyframe keyframe) {
    Optional<T> value = keyframe.getValue(property);
    if (value.isPresent())
      property.applyToGame(value.get(), ReplayModReplay.getInstance().getReplayHandler()); 
  }
  
  public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
    if (!this.dragging) {
      if (button == 0) {
        int time = getTimeAt(position.getX(), position.getY());
        if (time != -1) {
          setCursorPosition(time);
          return true;
        } 
      } 
      return false;
    } 
    if (!this.actuallyDragging)
      if (Math.abs(position.getX() - this.draggingStartX) >= 5)
        this.actuallyDragging = true;  
    if (this.actuallyDragging) {
      if (!this.gui.loadEntityTracker(() -> mouseDrag(position, button, timeSinceLastCall)))
        return true; 
      SPTimeline timeline = ReplayModSimplePathing.getInstance().getCurrentTimeline();
      Point mouse = new Point(position);
      getContainer().convertFor((GuiElement)this, mouse);
      int mouseX = mouse.getX();
      int width = getLastSize().getWidth();
      int bodyWidth = width - 4 - 4;
      double segmentLength = getLength() * getZoom();
      double segmentTime = segmentLength * (mouseX - 4) / bodyWidth;
      int newTime = Math.min(Math.max((int)Math.round(getOffset() + segmentTime), 0), getLength());
      if (newTime < 0)
        return true; 
      while (timeline.getKeyframe(this.lastClickedPath, newTime) != null)
        newTime++; 
      if (this.draggingChange != null)
        this.draggingChange.undo(timeline.getTimeline()); 
      this.draggingChange = timeline.moveKeyframe(this.lastClickedPath, this.lastClickedKeyframe, newTime);
      ReplayModSimplePathing.getInstance().setSelected(this.lastClickedPath, newTime);
    } 
    return true;
  }
  
  public boolean mouseRelease(ReadablePoint position, int button) {
    if (this.dragging) {
      if (this.actuallyDragging) {
        ReplayModSimplePathing.getInstance().getCurrentTimeline().getTimeline().pushChange(this.draggingChange);
        this.draggingChange = null;
        this.actuallyDragging = false;
      } 
      this.dragging = false;
      return true;
    } 
    return false;
  }
  
  protected GuiKeyframeTimeline getThis() {
    return this;
  }
}
