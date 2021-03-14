package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.util.Location;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.Events.ReplayEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.Restrictions;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraController;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraEntity;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.SpectatorCameraController;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.overlay.GuiReplayOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import org.lwjgl.opengl.Display;

public class ReplayHandler {
  private static final Minecraft mc = Minecraft.getMinecraft();
  
  private final ReplayFile replayFile;
  
  private final FullReplaySender replaySender;
  
  private Restrictions restrictions = new Restrictions();
  
  private boolean suppressCameraMovements;
  
  private final GuiReplayOverlay overlay;
  
  private EmbeddedChannel channel;
  
  private int replayDuration;
  
  private Location targetCameraPosition;
  
  private UUID spectating;
  
  public ReplayHandler(ReplayFile replayFile, boolean asyncMode) throws IOException {
    Preconditions.checkState(mc.isCallingFromMinecraftThread(), "Must be called from Minecraft thread.");
    this.replayFile = replayFile;
    this.replayDuration = replayFile.getMetaData().getDuration();
    this.replaySender = new FullReplaySender(this, replayFile, false);
    setup();
    this.overlay = new GuiReplayOverlay(this);
    this.overlay.setVisible(true);
    EventHandler.call((Event)new ReplayEvent(ReplayEvent.State.OPENED, this));
    this.replaySender.setAsyncMode(asyncMode);
  }
  
  void restartedReplay() {
    Preconditions.checkState(mc.isCallingFromMinecraftThread(), "Must be called from Minecraft thread.");
    this.channel.close();
    mc.setIngameNotInFocus();
    mc.displayGuiScreen(null);
    mc.loadWorld(null);
    this.restrictions = new Restrictions();
    setup();
  }
  
  public void endReplay() throws IOException {
    Preconditions.checkState(mc.isCallingFromMinecraftThread(), "Must be called from Minecraft thread.");
    EventHandler.call((Event)new ReplayEvent(ReplayEvent.State.CLOSING, this));
    this.replaySender.terminateReplay();
    this.replayFile.save();
    this.replayFile.close();
    this.channel.close().awaitUninterruptibly();
    if (mc.thePlayer instanceof CameraEntity)
      mc.thePlayer.setDead(); 
    if (mc.theWorld != null) {
      mc.theWorld.sendQuittingDisconnectingPacket();
      mc.loadWorld(null);
    } 
    mc.timer.timerSpeed = 1.0F;
    this.overlay.setVisible(false);
    (ReplayModReplay.getInstance()).replayHandler = null;
    mc.displayGuiScreen(null);
    EventHandler.call((Event)new ReplayEvent(ReplayEvent.State.CLOSED, this));
  }
  
  private void setup() {
    mc.ingameGUI.getChatGUI().clearChatMessages();
    NetworkManager networkManager = new NetworkManager(EnumPacketDirection.CLIENTBOUND) {
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
          t.printStackTrace();
        }
      };
    networkManager.setNetHandler((INetHandler)new NetHandlerPlayClient(mc, null, networkManager, new GameProfile(UUID.randomUUID(), "Player")));
    ChannelOutboundHandlerAdapter dummyHandler = new ChannelOutboundHandlerAdapter();
    this.channel = new EmbeddedChannel(new ChannelHandler[] { (ChannelHandler)dummyHandler });
    this.channel.pipeline().remove((ChannelHandler)dummyHandler);
    this.channel.pipeline().addLast("ReplayModReplay_replaySender", (ChannelHandler)this.replaySender);
    this.channel.pipeline().addLast("packet_handler", (ChannelHandler)networkManager);
    this.channel.pipeline().fireChannelActive();
  }
  
  public ReplayFile getReplayFile() {
    return this.replayFile;
  }
  
  public Restrictions getRestrictions() {
    return this.restrictions;
  }
  
  public ReplaySender getReplaySender() {
    return this.replaySender;
  }
  
  public GuiReplayOverlay getOverlay() {
    return this.overlay;
  }
  
  public int getReplayDuration() {
    return this.replayDuration;
  }
  
  public boolean shouldSuppressCameraMovements() {
    return this.suppressCameraMovements;
  }
  
  public void setSuppressCameraMovements(boolean suppressCameraMovements) {
    this.suppressCameraMovements = suppressCameraMovements;
  }
  
  public void spectateEntity(Entity e) {
    CameraEntity cameraEntity1, cameraEntity = getCameraEntity();
    if (cameraEntity == null)
      return; 
    if (e == null || e == cameraEntity) {
      this.spectating = null;
      cameraEntity1 = cameraEntity;
    } else if (cameraEntity1 instanceof net.minecraft.entity.player.EntityPlayer) {
      this.spectating = cameraEntity1.getUniqueID();
    } 
    if (cameraEntity1 == cameraEntity) {
      cameraEntity.setCameraController(ReplayModReplay.getInstance().createCameraController(cameraEntity));
    } else {
      cameraEntity.setCameraController((CameraController)new SpectatorCameraController(cameraEntity));
    } 
    if (mc.getRenderViewEntity() != cameraEntity1) {
      mc.setRenderViewEntity((Entity)cameraEntity1);
      cameraEntity.setCameraPosRot((Entity)cameraEntity1);
    } 
  }
  
  public void spectateCamera() {
    spectateEntity(null);
  }
  
  public boolean isCameraView() {
    return (mc.thePlayer instanceof CameraEntity && mc.thePlayer == mc.getRenderViewEntity());
  }
  
  public CameraEntity getCameraEntity() {
    return (mc.thePlayer instanceof CameraEntity) ? (CameraEntity)mc.thePlayer : null;
  }
  
  public UUID getSpectatedUUID() {
    return this.spectating;
  }
  
  public void setTargetPosition(Location pos) {
    this.targetCameraPosition = pos;
  }
  
  public void moveCameraToTargetPosition() {
    CameraEntity cam = getCameraEntity();
    if (cam != null && this.targetCameraPosition != null)
      cam.setCameraPosRot(this.targetCameraPosition); 
  }
  
  public void doJump(int targetTime, boolean retainCameraPosition) {
    if (this.replaySender.isHurrying())
      return; 
    if (targetTime < this.replaySender.currentTimeStamp())
      mc.displayGuiScreen(null); 
    if (retainCameraPosition) {
      CameraEntity cam = getCameraEntity();
      if (cam != null) {
        this.targetCameraPosition = new Location(cam.posX, cam.posY, cam.posZ, cam.rotationYaw, cam.rotationPitch);
      } else {
        this.targetCameraPosition = null;
      } 
    } 
    long diff = targetTime - this.replaySender.getDesiredTimestamp();
    if (diff != 0L)
      if (diff > 0L && diff < 5000L) {
        this.replaySender.jumpToTime(targetTime);
      } else {
        GuiScreen guiScreen = new GuiScreen() {
            public void drawScreen(int mouseX, int mouseY, float partialTicks) {
              drawBackground(0);
              drawCenteredString(this.fontRendererObj, I18n.format("replaymod.gui.pleasewait", new Object[0]), this.width / 2, this.height / 2, -1);
            }
          };
        this.replaySender.setSyncModeAndWait();
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        GlStateManager.enableTexture2D();
        mc.getFramebuffer().bindFramebuffer(true);
        mc.entityRenderer.setupOverlayRendering();
        ScaledResolution resolution = new ScaledResolution(mc);
        guiScreen.setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
        guiScreen.drawScreen(0, 0, 0.0F);
        mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        mc.getFramebuffer().framebufferRender(mc.displayWidth, mc.displayHeight);
        GlStateManager.popMatrix();
        Display.update();
        this.replaySender.sendPacketsTill(targetTime);
        this.replaySender.setAsyncMode(true);
        this.replaySender.setReplaySpeed(0.0D);
        mc.getNetHandler().getNetworkManager().processReceivedPackets();
        Iterator<Entity> iterator = mc.theWorld.loadedEntityList.iterator();
        if (iterator.hasNext()) {
          Entity entity = iterator.next();
          if (entity instanceof EntityOtherPlayerMP) {
            EntityOtherPlayerMP e = (EntityOtherPlayerMP)entity;
            e.setPosition(e.otherPlayerMPX, e.otherPlayerMPY, e.otherPlayerMPZ);
            e.rotationYaw = (float)e.otherPlayerMPYaw;
            e.rotationPitch = (float)e.otherPlayerMPPitch;
          } 
          entity.lastTickPosX = entity.prevPosX = entity.posX;
          entity.lastTickPosY = entity.prevPosY = entity.posY;
          entity.lastTickPosZ = entity.prevPosZ = entity.posZ;
          entity.prevRotationYaw = entity.rotationYaw;
          entity.prevRotationPitch = entity.rotationPitch;
        } else {
          try {
            mc.runTick();
          } catch (IOException e) {
            e.printStackTrace();
          } 
          moveCameraToTargetPosition();
          return;
        } 
      }  
  }
}
