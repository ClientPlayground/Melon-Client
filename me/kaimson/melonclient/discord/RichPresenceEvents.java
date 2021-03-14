package me.kaimson.melonclient.discord;

import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.GuiScreenEvent;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import net.minecraft.client.Minecraft;

public class RichPresenceEvents {
  @TypeEvent
  private void onGuiOpen(GuiScreenEvent.Open e) {
    if (ReplayModReplay.getInstance().getReplayHandler() != null)
      return; 
    if (e.screen instanceof net.minecraft.client.gui.GuiMainMenu || e.screen instanceof net.minecraft.client.gui.GuiMultiplayer) {
      RichPresence.INSTANCE.setState("Idle");
    } else if (e.screen == null) {
      if (IngameDisplay.DISCORD_INTEGRATION_SHOW_SERVER.isEnabled()) {
        RichPresence.INSTANCE.setState(Minecraft.getMinecraft().isIntegratedServerRunning() ? "Playing Singleplayer" : ("Playing on " + (Minecraft.getMinecraft().getCurrentServerData()).serverIP));
      } else {
        RichPresence.INSTANCE.setState("Playing Minecraft 1.8.9");
      } 
    } 
  }
  
  @TypeEvent
  private void onJoinServer() {}
  
  @TypeEvent
  private void onLeaveServer() {}
}
