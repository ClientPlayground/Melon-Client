package me.kaimson.melonclient.ingames.utils.ReplayMod.recording;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.replaystudio.replay.ReplayFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.IProgressUpdate;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourcePackRecorder {
  private static final Logger logger = LogManager.getLogger();
  
  private static final Minecraft mc = Minecraft.getMinecraft();
  
  private final ReplayFile replayFile;
  
  private int nextRequestId;
  
  public ResourcePackRecorder(ReplayFile replayFile) {
    this.replayFile = replayFile;
  }
  
  public void recordResourcePack(File file, int requestId) {
    try {
      byte[] bytes = Files.toByteArray(file);
      String hash = Hashing.sha1().hashBytes(bytes).toString();
      boolean doWrite = false;
      synchronized (this.replayFile) {
        Map<Integer, String> index = this.replayFile.getResourcePackIndex();
        if (index == null)
          index = new HashMap<>(); 
        if (!index.containsValue(hash))
          doWrite = true; 
        index.put(Integer.valueOf(requestId), hash);
        this.replayFile.writeResourcePackIndex(index);
      } 
      if (doWrite)
        try (OutputStream out = this.replayFile.writeResourcePack(hash)) {
          out.write(bytes);
        }  
    } catch (IOException e) {
      logger.warn("Failed to save resource pack.", e);
    } 
  }
  
  public synchronized S48PacketResourcePackSend handleResourcePack(S48PacketResourcePackSend packet) {
    final int requestId = this.nextRequestId++;
    NetHandlerPlayClient netHandler = mc.getNetHandler();
    final NetworkManager netManager = netHandler.getNetworkManager();
    String url = packet.getURL();
    final String hash = packet.getHash();
    if (url.startsWith("level://")) {
      String levelName = url.substring("level://".length());
      File savesDir = new File(mc.mcDataDir, "saves");
      final File levelDir = new File(savesDir, levelName);
      if (levelDir.isFile()) {
        netManager.sendPacket((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.ACCEPTED));
        Futures.addCallback(mc.getResourcePackRepository().setResourcePackInstance(levelDir), new FutureCallback<Object>() {
              public void onSuccess(Object result) {
                ResourcePackRecorder.this.recordResourcePack(levelDir, requestId);
                netManager.sendPacket((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
              }
              
              public void onFailure(Throwable throwable) {
                netManager.sendPacket((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
              }
            });
      } else {
        netManager.sendPacket((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
      } 
    } else {
      ServerData serverData = mc.getCurrentServerData();
      if (serverData != null && serverData.getResourceMode() == ServerData.ServerResourceMode.ENABLED) {
        netManager.sendPacket((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.ACCEPTED));
        downloadResourcePackFuture(requestId, url, hash);
      } else if (serverData != null && serverData.getResourceMode() != ServerData.ServerResourceMode.PROMPT) {
        netManager.sendPacket((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.DECLINED));
      } else {
        mc.addScheduledTask(() -> mc.displayGuiScreen((GuiScreen)new GuiYesNo((), I18n.format("multiplayer.texturePrompt.line1", new Object[0]), I18n.format("multiplayer.texturePrompt.line2", new Object[0]), 0)));
      } 
    } 
    return new S48PacketResourcePackSend("replay://" + requestId, "");
  }
  
  private void downloadResourcePackFuture(int requestId, String url, final String hash) {
    Futures.addCallback(downloadResourcePack(requestId, url, hash), new FutureCallback() {
          public void onSuccess(Object result) {
            ResourcePackRecorder.mc.getNetHandler().addToSendQueue((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
          }
          
          public void onFailure(Throwable throwable) {
            ResourcePackRecorder.mc.getNetHandler().addToSendQueue((Packet)new C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
          }
        });
  }
  
  private ListenableFuture downloadResourcePack(final int requestId, String url, String hash) {
    String fileName;
    final ResourcePackRepository repo = ResourcePackRecorder.mc.getResourcePackRepository();
    if (hash.matches("^[a-f0-9]{40}$")) {
      fileName = hash;
    } else {
      fileName = url.substring(url.lastIndexOf("/") + 1);
      if (fileName.contains("?"))
        fileName = fileName.substring(0, fileName.indexOf("?")); 
      if (!fileName.endsWith(".zip"))
        return Futures.immediateFailedFuture(new IllegalArgumentException("Invalid filename; must end in .zip")); 
      fileName = "legacy_" + fileName.replaceAll("\\W", "");
    } 
    final File file = new File(repo.dirServerResourcepacks, fileName);
    repo.lock.lock();
    try {
      repo.clearResourcePack();
      if (file.exists() && hash.length() == 40)
        try {
          String fileHash = Hashing.sha1().hashBytes(Files.toByteArray(file)).toString();
          if (fileHash.equals(hash)) {
            recordResourcePack(file, requestId);
            return repo.setResourcePackInstance(file);
          } 
          logger.warn("File " + file + " had wrong hash (expected " + hash + ", found " + fileHash + "). Deleting it.");
          FileUtils.deleteQuietly(file);
        } catch (IOException ioexception) {
          logger.warn("File " + file + " couldn't be hashed. Deleting it.", ioexception);
          FileUtils.deleteQuietly(file);
        }  
      GuiScreenWorking guiScreen = new GuiScreenWorking();
      Minecraft mc = Minecraft.getMinecraft();
      Futures.getUnchecked((Future)mc.addScheduledTask(() -> mc.displayGuiScreen((GuiScreen)guiScreen)));
      Map<String, String> sessionInfo = Minecraft.getSessionInfo();
      repo.downloadingPacks = HttpUtil.downloadResourcePack(file, url, sessionInfo, 52428800, (IProgressUpdate)guiScreen, mc.getProxy());
      Futures.addCallback(repo.downloadingPacks, new FutureCallback<Object>() {
            public void onSuccess(Object value) {
              ResourcePackRecorder.this.recordResourcePack(file, requestId);
              repo.setResourcePackInstance(file);
            }
            
            public void onFailure(Throwable throwable) {
              throwable.printStackTrace();
            }
          });
      return repo.downloadingPacks;
    } finally {
      repo.lock.unlock();
    } 
  }
}
