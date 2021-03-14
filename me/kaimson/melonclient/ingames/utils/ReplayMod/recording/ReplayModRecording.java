package me.kaimson.melonclient.ingames.utils.ReplayMod.recording;

import io.netty.channel.Channel;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.Ingame;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import net.minecraft.network.NetworkManager;

public class ReplayModRecording extends Ingame {
  public ConnectionEventHandler connectionEventHandler;
  
  public static ReplayModRecording INSTANCE;
  
  public ReplayModRecording() {
    INSTANCE = this;
    ReplayCore.getInstance().getSettingsRegistry().register(Setting.class);
  }
  
  public void init() {
    this.connectionEventHandler = new ConnectionEventHandler();
    Client.log("ReplayModRecording initialized!");
  }
  
  public void initiateRecording(NetworkManager networkManager) {
    Channel channel = networkManager.channel;
    if (channel.pipeline().get("ReplayModReplay_replaySender") != null)
      return; 
    this.connectionEventHandler.onConnectedToServer(networkManager);
  }
}
