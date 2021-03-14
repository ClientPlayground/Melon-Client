package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.overlay;

import com.google.common.base.Supplier;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.util.Location;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiElement;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiTimeline;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;

public class GuiMarkerTimeline extends AbstractGuiTimeline<GuiMarkerTimeline> implements Draggable, Typeable {
  protected static final int TEXTURE_MARKER_X = 109;
  
  protected static final int TEXTURE_MARKER_Y = 20;
  
  protected static final int TEXTURE_MARKER_SELECTED_X = 114;
  
  protected static final int TEXTURE_MARKER_SELECTED_Y = 20;
  
  protected static final int MARKER_SIZE = 5;
  
  private final ReplayHandler replayHandler;
  
  private final Consumer<Set<Marker>> saveMarkers;
  
  protected Set<Marker> markers;
  
  private ReadableDimension lastSize;
  
  private Marker selectedMarker;
  
  private int draggingStartX;
  
  private int draggingTimeDelta;
  
  private boolean dragging;
  
  private long lastClickTime;
  
  public GuiMarkerTimeline(ReplayHandler replayHandler) {
    this.replayHandler = replayHandler;
    try {
      this.markers = (Set<Marker>)replayHandler.getReplayFile().getMarkers().or(HashSet::new);
    } catch (IOException e) {
      Client.error("Failed to get markers from replay", new Object[] { e });
      this.markers = new HashSet<>();
    } 
    this.saveMarkers = (markers -> {
        try {
          replayHandler.getReplayFile().writeMarkers(markers);
        } catch (IOException e) {
          Client.error("Failed to save markers to replay", new Object[] { e });
        } 
      });
  }
  
  protected GuiMarkerTimeline getThis() {
    return this;
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    this.lastSize = size;
    super.draw(renderer, size, renderInfo);
    drawMarkers(renderer, size);
  }
  
  protected void drawMarkers(GuiRenderer renderer, ReadableDimension size) {
    renderer.bindTexture(ReplayCore.TEXTURE);
    for (Marker marker : this.markers)
      drawMarker(renderer, size, marker); 
  }
  
  protected void drawMarker(GuiRenderer renderer, ReadableDimension size, Marker marker) {
    int textureX, textureY, visibleLength = (int)(getLength() * getZoom());
    int markerPos = MathHelper.clamp_int(marker.getTime(), getOffset(), getOffset() + visibleLength);
    double positionInVisible = (markerPos - getOffset());
    double fractionOfVisible = positionInVisible / visibleLength;
    int markerX = (int)(4.0D + fractionOfVisible * (size.getWidth() - 4 - 4));
    if (marker.equals(this.selectedMarker)) {
      textureX = 114;
      textureY = 20;
    } else {
      textureX = 109;
      textureY = 20;
    } 
    renderer.drawTexturedRect(markerX - 2, size.getHeight() - 3 - 5, textureX, textureY, 5, 5);
  }
  
  protected Marker getMarkerAt(int mouseX, int mouseY) {
    if (this.lastSize == null)
      return null; 
    Point mouse = new Point(mouseX, mouseY);
    getContainer().convertFor((GuiElement)this, mouse);
    mouseX = mouse.getX();
    mouseY = mouse.getY();
    if (mouseX < 0 || mouseY < this.lastSize.getHeight() - 3 - 5 || mouseX > this.lastSize
      .getWidth() || mouseY > this.lastSize.getHeight() - 3)
      return null; 
    int visibleLength = (int)(getLength() * getZoom());
    int contentWidth = this.lastSize.getWidth() - 4 - 4;
    for (Marker marker : this.markers) {
      int markerPos = MathHelper.clamp_int(marker.getTime(), getOffset(), getOffset() + visibleLength);
      double positionInVisible = (markerPos - getOffset());
      double fractionOfVisible = positionInVisible / visibleLength;
      int markerX = (int)(4.0D + fractionOfVisible * contentWidth);
      if (Math.abs(markerX - mouseX) < 3)
        return marker; 
    } 
    return null;
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    Marker marker = getMarkerAt(position.getX(), position.getY());
    if (marker != null) {
      if (button == 0) {
        long now = System.currentTimeMillis();
        Client.log("selectedmarker");
        this.selectedMarker = marker;
        if (Math.abs(this.lastClickTime - now) > 500L) {
          this.draggingStartX = position.getX();
          this.draggingTimeDelta = marker.getTime() - getTimeAt(position.getX(), position.getY());
        } else {
          (new GuiEditMarkerPopup(this.replayHandler, getContainer(), marker)).open();
        } 
        this.lastClickTime = now;
      } else if (button == 1) {
        this.selectedMarker = null;
        this.replayHandler.setTargetPosition(new Location(marker
              .getX(), marker.getY(), marker.getZ(), marker
              .getPitch(), marker.getYaw()));
        this.replayHandler.doJump(marker.getTime(), false);
      } 
      return true;
    } 
    this.selectedMarker = null;
    return super.mouseClick(position, button);
  }
  
  public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
    if (this.selectedMarker != null) {
      int diff = position.getX() - this.draggingStartX;
      if (Math.abs(diff) > 5)
        this.dragging = true; 
      if (this.dragging) {
        int timeAt = getTimeAt(position.getX(), position.getY());
        if (timeAt != -1)
          this.selectedMarker.setTime(this.draggingTimeDelta + timeAt); 
        return true;
      } 
    } 
    return false;
  }
  
  public boolean mouseRelease(ReadablePoint position, int button) {
    if (this.selectedMarker != null) {
      mouseDrag(position, button, 0L);
      if (this.dragging) {
        this.dragging = false;
        this.saveMarkers.accept(this.markers);
        return true;
      } 
    } 
    return false;
  }
  
  protected String getTooltipText(RenderInfo renderInfo) {
    Marker marker = getMarkerAt(renderInfo.mouseX, renderInfo.mouseY);
    if (marker != null)
      return (marker.getName() != null) ? marker.getName() : I18n.format("replaymod.gui.ingame.unnamedmarker", new Object[0]); 
    return super.getTooltipText(renderInfo);
  }
  
  public boolean typeKey(ReadablePoint readablePoint, int keyCode, char c, boolean b, boolean b1) {
    if (keyCode == 211 && this.selectedMarker != null) {
      this.markers.remove(this.selectedMarker);
      this.saveMarkers.accept(this.markers);
      return true;
    } 
    return false;
  }
  
  public void setSelectedMarker(Marker selectedMarker) {
    this.selectedMarker = selectedMarker;
  }
  
  public Marker getSelectedMarker() {
    return this.selectedMarker;
  }
}
