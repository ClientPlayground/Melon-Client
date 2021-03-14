package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.preview;

import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.util.EntityPositionTracker;
import com.replaymod.replaystudio.util.Location;
import java.util.Comparator;
import java.util.Optional;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.RenderWorldLastEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.ReplayModSimplePathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.SPTimeline;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.gui.GuiPathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.CameraProperties;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.SpectatorProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.TimestampProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

public class PathPreviewRenderer {
  private static final ResourceLocation CAMERA_HEAD = new ResourceLocation("melonclient/replaymod/camera_head.png");
  
  private static final Minecraft mc = Minecraft.getMinecraft();
  
  private static final int SLOW_PATH_COLOR = 16764108;
  
  private static final int FAST_PATH_COLOR = 6684672;
  
  private static final double FASTEST_PATH_SPEED = 0.01D;
  
  private final ReplayModSimplePathing mod;
  
  private final ReplayHandler replayHandler;
  
  public PathPreviewRenderer(ReplayModSimplePathing mod, ReplayHandler replayHandler) {
    this.mod = mod;
    this.replayHandler = replayHandler;
  }
  
  @TypeEvent
  public void renderCameraPath(RenderWorldLastEvent event) {
    if (!this.replayHandler.getReplaySender().isAsyncMode() || mc.gameSettings.hideGUI)
      return; 
    Entity view = mc.getRenderViewEntity();
    if (view == null)
      return; 
    GuiPathing guiPathing = this.mod.getGuiPathing();
    if (guiPathing == null)
      return; 
    EntityPositionTracker entityTracker = guiPathing.getEntityTracker();
    SPTimeline timeline = this.mod.getCurrentTimeline();
    if (timeline == null)
      return; 
    Path path = timeline.getPositionPath();
    if (path.getKeyframes().isEmpty())
      return; 
    Path timePath = timeline.getTimePath();
    path.update();
    int renderDistance = mc.gameSettings.renderDistanceChunks * 16;
    int renderDistanceSquared = renderDistance * renderDistance;
    Triple<Double, Double, Double> viewPos = Triple.of(
        Double.valueOf(view.posX), 
        Double.valueOf(view.posY - view.getEyeHeight()), 
        Double.valueOf(view.posZ));
    GL11.glPushAttrib(1048575);
    GL11.glPushMatrix();
    try {
      GL11.glDisable(2896);
      GL11.glDisable(3553);
      for (PathSegment segment : path.getSegments()) {
        Interpolator interpolator = segment.getInterpolator();
        Keyframe start = segment.getStartKeyframe();
        Keyframe end = segment.getEndKeyframe();
        long diff = (int)(end.getTime() - start.getTime());
        boolean spectator = interpolator.getKeyframeProperties().contains(SpectatorProperty.PROPERTY);
        if (spectator && entityTracker == null)
          continue; 
        long steps = spectator ? Math.max(diff / 50L, 10L) : 100L;
        Triple<Double, Double, Double> prevPos = null;
        for (int i = 0; i <= steps; i++) {
          long l = start.getTime() + diff * i / steps;
          if (spectator) {
            Optional<Integer> optional1 = path.getValue((Property)SpectatorProperty.PROPERTY, l);
            Optional<Integer> replayTime = timePath.getValue((Property)TimestampProperty.PROPERTY, l);
            if (optional1.isPresent() && replayTime.isPresent()) {
              Location loc = entityTracker.getEntityPositionAtTimestamp(((Integer)optional1.get()).intValue(), ((Integer)replayTime.get()).intValue());
              if (loc != null) {
                Triple<Double, Double, Double> pos = Triple.of(Double.valueOf(loc.getX()), Double.valueOf(loc.getY()), Double.valueOf(loc.getZ()));
                if (prevPos != null)
                  drawConnection(viewPos, prevPos, pos, 255, renderDistanceSquared); 
                prevPos = pos;
                continue;
              } 
            } 
          } else {
            Optional<Triple<Double, Double, Double>> optPos = path.getValue((Property)CameraProperties.POSITION, l);
            if (optPos.isPresent()) {
              Triple<Double, Double, Double> pos = optPos.get();
              if (prevPos != null) {
                double distance = Math.sqrt(distanceSquared(prevPos, pos));
                double speed = Math.min(distance / (diff / steps), 0.01D);
                double speedFraction = speed / 0.01D;
                int color = interpolateColor(16764108, 6684672, speedFraction);
                drawConnection(viewPos, prevPos, pos, color, renderDistanceSquared);
              } 
              prevPos = pos;
              continue;
            } 
          } 
          prevPos = null;
          continue;
        } 
      } 
      GL11.glEnable(3042);
      GL11.glEnable(3553);
      GL11.glBlendFunc(774, 768);
      GL11.glDisable(2929);
      path.getKeyframes().stream()
        .map(k -> Pair.of(k, k.getValue((Property)CameraProperties.POSITION)))
        .filter(p -> ((Optional)p.getRight()).isPresent())
        .map(p -> Pair.of(p.getLeft(), ((Optional)p.getRight()).get()))
        .filter(p -> (distanceSquared((Triple<Double, Double, Double>)p.getRight(), viewPos) < renderDistanceSquared))
        .sorted(new KeyframeComparator(viewPos))
        .forEachOrdered(p -> drawPoint(viewPos, (Triple<Double, Double, Double>)p.getRight(), (Keyframe)p.getLeft()));
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2929);
      int time = guiPathing.timeline.getCursorPosition();
      Optional<Integer> entityId = path.getValue((Property)SpectatorProperty.PROPERTY, time);
      if (entityId.isPresent()) {
        if (entityTracker != null) {
          Optional<Integer> replayTime = timePath.getValue((Property)TimestampProperty.PROPERTY, time);
          if (replayTime.isPresent()) {
            Location loc = entityTracker.getEntityPositionAtTimestamp(((Integer)entityId.get()).intValue(), ((Integer)replayTime.get()).intValue());
            if (loc != null)
              drawCamera(viewPos, 
                  Triple.of(Double.valueOf(loc.getX()), Double.valueOf(loc.getY()), Double.valueOf(loc.getZ())), 
                  Triple.of(Float.valueOf(loc.getYaw()), Float.valueOf(loc.getPitch()), Float.valueOf(0.0F))); 
          } 
        } 
      } else {
        Optional<Triple<Double, Double, Double>> cameraPos = path.getValue((Property)CameraProperties.POSITION, time);
        Optional<Triple<Float, Float, Float>> cameraRot = path.getValue((Property)CameraProperties.ROTATION, time);
        if (cameraPos.isPresent() && cameraRot.isPresent())
          drawCamera(viewPos, cameraPos.get(), cameraRot.get()); 
      } 
    } finally {
      GL11.glPopMatrix();
      GlStateManager.popAttrib();
    } 
  }
  
  private static int interpolateColor(int c1, int c2, double weight) {
    return interpolateColorComponent(c1 >> 16 & 0xFF, c2 >> 16 & 0xFF, weight) << 16 | 
      interpolateColorComponent(c1 >> 8 & 0xFF, c2 >> 8 & 0xFF, weight) << 8 | 
      interpolateColorComponent(c1 & 0xFF, c2 & 0xFF, weight);
  }
  
  private static int interpolateColorComponent(int c1, int c2, double weight) {
    return (int)(c1 + (1.0D - Math.pow(Math.E, -4.0D * weight)) * (c2 - c1)) & 0xFF;
  }
  
  private static double distanceSquared(Triple<Double, Double, Double> p1, Triple<Double, Double, Double> p2) {
    double dx = ((Double)p1.getLeft()).doubleValue() - ((Double)p2.getLeft()).doubleValue();
    double dy = ((Double)p1.getMiddle()).doubleValue() - ((Double)p2.getMiddle()).doubleValue();
    double dz = ((Double)p1.getRight()).doubleValue() - ((Double)p2.getRight()).doubleValue();
    return dx * dx + dy * dy + dz * dz;
  }
  
  private void drawConnection(Triple<Double, Double, Double> view, Triple<Double, Double, Double> pos1, Triple<Double, Double, Double> pos2, int color, int renderDistanceSquared) {
    if (distanceSquared(view, pos1) > renderDistanceSquared)
      return; 
    if (distanceSquared(view, pos2) > renderDistanceSquared)
      return; 
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    worldRenderer.setTranslation(-((Double)view.getLeft()).doubleValue(), -((Double)view.getMiddle()).doubleValue(), -((Double)view.getRight()).doubleValue());
    worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
    worldRenderer.pos(((Double)pos1.getLeft()).doubleValue(), ((Double)pos1.getMiddle()).doubleValue(), ((Double)pos1.getRight()).doubleValue()).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 255).endVertex();
    worldRenderer.pos(((Double)pos2.getLeft()).doubleValue(), ((Double)pos2.getMiddle()).doubleValue(), ((Double)pos2.getRight()).doubleValue()).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 255).endVertex();
    GL11.glLineWidth(3.0F);
    tessellator.draw();
    worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
  }
  
  private void drawPoint(Triple<Double, Double, Double> view, Triple<Double, Double, Double> pos, Keyframe keyframe) {
    Minecraft.getMinecraft().getTextureManager().bindTexture(ReplayCore.TEXTURE);
    float posX = 0.3125F;
    float posY = 0.0F;
    float size = 0.0390625F;
    if (this.mod.isSelected(keyframe))
      posY += size; 
    if (keyframe.getValue((Property)SpectatorProperty.PROPERTY).isPresent())
      posX += size; 
    float minX = -0.5F;
    float minY = -0.5F;
    float maxX = 0.5F;
    float maxY = 0.5F;
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
    worldRenderer.pos(minX, minY, 0.0D).tex((posX + size), (posY + size)).endVertex();
    worldRenderer.pos(minX, maxY, 0.0D).tex((posX + size), posY).endVertex();
    worldRenderer.pos(maxX, maxY, 0.0D).tex(posX, posY).endVertex();
    worldRenderer.pos(maxX, minY, 0.0D).tex(posX, (posY + size)).endVertex();
    GL11.glPushMatrix();
    GL11.glTranslated(((Double)pos
        .getLeft()).doubleValue() - ((Double)view.getLeft()).doubleValue(), ((Double)pos
        .getMiddle()).doubleValue() - ((Double)view.getMiddle()).doubleValue(), ((Double)pos
        .getRight()).doubleValue() - ((Double)view.getRight()).doubleValue());
    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
    GL11.glRotatef(-(mc.getRenderManager()).playerViewY, 0.0F, 1.0F, 0.0F);
    GL11.glRotatef((mc.getRenderManager()).playerViewX, 1.0F, 0.0F, 0.0F);
    tessellator.draw();
    GL11.glPopMatrix();
  }
  
  private void drawCamera(Triple<Double, Double, Double> view, Triple<Double, Double, Double> pos, Triple<Float, Float, Float> rot) {
    Minecraft.getMinecraft().getTextureManager().bindTexture(CAMERA_HEAD);
    GL11.glPushMatrix();
    GL11.glTranslated(((Double)pos
        .getLeft()).doubleValue() - ((Double)view.getLeft()).doubleValue(), ((Double)pos
        .getMiddle()).doubleValue() - ((Double)view.getMiddle()).doubleValue(), ((Double)pos
        .getRight()).doubleValue() - ((Double)view.getRight()).doubleValue());
    GL11.glRotated(-((Float)rot.getLeft()).floatValue(), 0.0D, 1.0D, 0.0D);
    GL11.glRotated(((Float)rot.getMiddle()).floatValue(), 1.0D, 0.0D, 0.0D);
    GL11.glRotated(((Float)rot.getRight()).floatValue(), 0.0D, 0.0D, 1.0D);
    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
    GL11.glDisable(3553);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
    worldRenderer.pos(0.0D, 0.0D, 0.0D).color(0, 255, 0, 170).endVertex();
    worldRenderer.pos(0.0D, 0.0D, 2.0D).color(0, 255, 0, 170).endVertex();
    tessellator.draw();
    GL11.glEnable(3553);
    float cubeSize = 0.5F;
    double r = (-cubeSize / 2.0F);
    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
    worldRenderer.pos(r, r + cubeSize, r).tex(0.375D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r + cubeSize, r).tex(0.5D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r, r).tex(0.5D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r, r).tex(0.375D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r, r + cubeSize).tex(0.25D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r + cubeSize, r + cubeSize).tex(0.25D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r + cubeSize, r + cubeSize).tex(0.125D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r, r + cubeSize).tex(0.125D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r + cubeSize, r).tex(0.0D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r + cubeSize, r + cubeSize).tex(0.125D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r, r + cubeSize).tex(0.125D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r, r).tex(0.0D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r + cubeSize, r + cubeSize).tex(0.25D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r + cubeSize, r).tex(0.375D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r, r).tex(0.375D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r, r + cubeSize).tex(0.25D, 0.25D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r, r).tex(0.375D, 0.0D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r, r + cubeSize).tex(0.375D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r, r + cubeSize).tex(0.25D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r, r).tex(0.25D, 0.0D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r + cubeSize, r).tex(0.125D, 0.0D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r, r + cubeSize, r + cubeSize).tex(0.125D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r + cubeSize, r + cubeSize).tex(0.25D, 0.125D).color(255, 255, 255, 200).endVertex();
    worldRenderer.pos(r + cubeSize, r + cubeSize, r).tex(0.25D, 0.0D).color(255, 255, 255, 200).endVertex();
    tessellator.draw();
    GL11.glPopMatrix();
  }
  
  private class KeyframeComparator implements Comparator<Pair<Keyframe, Triple<Double, Double, Double>>> {
    private final Triple<Double, Double, Double> viewPos;
    
    public KeyframeComparator(Triple<Double, Double, Double> viewPos) {
      this.viewPos = viewPos;
    }
    
    public int compare(Pair<Keyframe, Triple<Double, Double, Double>> o1, Pair<Keyframe, Triple<Double, Double, Double>> o2) {
      return -Double.compare(PathPreviewRenderer.distanceSquared((Triple)o1.getRight(), this.viewPos), PathPreviewRenderer.distanceSquared((Triple)o2.getRight(), this.viewPos));
    }
  }
}
