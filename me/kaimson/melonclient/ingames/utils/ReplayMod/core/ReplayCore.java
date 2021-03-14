package me.kaimson.melonclient.ingames.utils.ReplayMod.core;

import com.google.common.util.concurrent.ListenableFutureTask;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.recording.ConnectionEventHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.recording.RecordingEventHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

public class ReplayCore {
  private final SettingsRegistry settingsRegistry;
  
  private final KeyBindingRegistry keyBindingRegistry;
  
  public SettingsRegistry getSettingsRegistry() {
    return this.settingsRegistry;
  }
  
  public KeyBindingRegistry getKeyBindingRegistry() {
    return this.keyBindingRegistry;
  }
  
  public static String getMinecraftVersion() {
    return minecraftVersion;
  }
  
  private static final String minecraftVersion = parseMinecraftVersion();
  
  private static String parseMinecraftVersion() {
    CrashReport crashReport = new CrashReport("", new Throwable());
    List<CrashReportCategory.Entry> list = (crashReport.getCategory()).children;
    for (CrashReportCategory.Entry entry : list) {
      if ("Minecraft Version".equals(entry.getKey()))
        return entry.getValue(); 
    } 
    return "Unknown";
  }
  
  public static final ResourceLocation TEXTURE = new ResourceLocation("melonclient/replaymod/replay_gui.png");
  
  public static final int TEXTURE_SIZE = 256;
  
  private final double version = 0.1D;
  
  private static ReplayCore instance;
  
  private boolean inRunLater;
  
  public double getVersion() {
    getClass();
    return 0.1D;
  }
  
  public static ReplayCore getInstance() {
    return instance;
  }
  
  public File getReplayFolder() throws IOException {
    String path = getSettingsRegistry().<String>get(Setting.RECORDING_PATH);
    File folder = new File(path.startsWith("./") ? (Minecraft.getMinecraft()).mcDataDir : null, path);
    FileUtils.forceMkdir(folder);
    return folder;
  }
  
  public PacketTypeRegistry getPacketTypeRegistry(boolean loginPhase) {
    try {
      return PacketTypeRegistry.get(ProtocolVersion.getProtocol(RealmsSharedConstants.NETWORK_PROTOCOL_VERSION), loginPhase ? State.LOGIN : State.PLAY);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public ReplayCore() {
    this.inRunLater = false;
    instance = this;
    this.settingsRegistry = new SettingsRegistry();
    this.settingsRegistry.register(Setting.class);
    this.settingsRegistry.register(Setting.class);
    this.keyBindingRegistry = new KeyBindingRegistry();
    EventHandler.register(this.keyBindingRegistry);
  }
  
  public void runLater(final Runnable runnable) {
    if (Minecraft.getMinecraft().isCallingFromMinecraftThread() && this.inRunLater) {
      EventHandler.register(new Object() {
            @TypeEvent
            public void onRenderTick(TickEvent.RenderTick e) {
              if (e.phase == TickEvent.Phase.START) {
                ReplayCore.this.runLater(runnable);
                EventHandler.unregister(this);
              } 
            }
          });
      return;
    } 
    synchronized ((Minecraft.getMinecraft()).scheduledTasks) {
      (Minecraft.getMinecraft()).scheduledTasks.add(ListenableFutureTask.create(() -> {
              this.inRunLater = true;
              try {
                runnable.run();
              } finally {
                this.inRunLater = false;
              } 
            }null));
    } 
  }
  
  public void recordOwnJoin(S38PacketPlayerListItem packet, Minecraft gameController, Map<UUID, NetworkPlayerInfo> playerInfoMap) {
    if (gameController.thePlayer == null)
      return; 
    RecordingEventHandler handler = ConnectionEventHandler.getRecordingEventHandler();
    if (handler != null && packet.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER)
      for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
        if (data.getProfile() == null || data.getProfile().getId() == null)
          continue; 
        if (data.getProfile().getId().equals((Minecraft.getMinecraft()).thePlayer.getGameProfile().getId()) && 
          !playerInfoMap.containsKey(data.getProfile().getId()))
          handler.onPlayerJoin(); 
      }  
  }
}
