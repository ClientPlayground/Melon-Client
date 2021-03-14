package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

public interface ReplaySender {
  int currentTimeStamp();
  
  default boolean paused() {
    Minecraft mc = Minecraft.getMinecraft();
    Timer timer = mc.timer;
    return (timer.timerSpeed == 0.0F);
  }
  
  void setReplaySpeed(double paramDouble);
  
  double getReplaySpeed();
  
  boolean isAsyncMode();
  
  void setAsyncMode(boolean paramBoolean);
  
  void setSyncModeAndWait();
  
  void jumpToTime(int paramInt);
  
  void sendPacketsTill(int paramInt);
}
