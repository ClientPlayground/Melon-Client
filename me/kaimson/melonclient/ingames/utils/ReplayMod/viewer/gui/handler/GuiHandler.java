package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.GuiScreenEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.recording.ConnectionEventHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.recording.ReplayModRecording;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.overlay.GuiReplayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiHandler {
  private static final int BUTTON_EXIT_SERVER = 1;
  
  private static final int BUTTON_ACHIEVEMENTS = 5;
  
  private static final int BUTTON_STATS = 6;
  
  private static final int BUTTON_OPEN_TO_LAN = 7;
  
  private static final int BUTTON_REPLAY_VIEWER = 17890234;
  
  private static final int BUTTON_EXIT_REPLAY = 17890235;
  
  private static final Minecraft mc = Minecraft.getMinecraft();
  
  public void register() {
    EventHandler.register(this);
  }
  
  @TypeEvent
  public void injectIntoIngameMenu(GuiScreenEvent.Init event) {
    if (!(event.screen instanceof net.minecraft.client.gui.GuiIngameMenu))
      return; 
    if (ReplayModReplay.getInstance().getReplayHandler() != null) {
      ReplayModReplay.getInstance().getReplayHandler().getReplaySender().setReplaySpeed(0.0D);
      GuiButton achievements = null, stats = null, openToLan = null;
      List<GuiButton> buttonList = event.buttonList;
      for (GuiButton b : new ArrayList(buttonList)) {
        switch (b.id) {
          case 1:
            b.displayString = I18n.format("Exit replay", new Object[0]);
            b.id = 17890235;
          case 5:
            buttonList.remove(achievements = b);
          case 6:
            buttonList.remove(stats = b);
          case 7:
            buttonList.remove(openToLan = b);
          case 98:
            buttonList.remove(b);
          case 99:
            b.width = 98;
            b.xPosition += 102;
            b.yPosition += 24;
            b.enabled = false;
        } 
      } 
    } else {
      GuiButton button;
      event.buttonList.add(button = new GuiButton(333, event.screen.width / 2 - 100, event.screen.height / 4 + 144 - 16, "Start Replay Recording"));
      if (ConnectionEventHandler.getRecordingEventHandler() != null)
        button.enabled = false; 
    } 
  }
  
  private void moveAllButtonsDirectlyBelowUpwards(List<GuiButton> buttons, int belowY, int xStart, int xEnd) {
    for (GuiButton button : buttons) {
      if (button.yPosition >= belowY && button.xPosition <= xEnd && button.xPosition + button.width >= xStart)
        button.yPosition -= 24; 
    } 
  }
  
  @TypeEvent
  public void modify(GuiScreenEvent.Init event) {
    if (!(event.screen instanceof GuiMainMenu))
      return; 
    if (ReplayModReplay.getInstance().getReplayHandler() != null)
      try {
        ReplayModReplay.getInstance().getReplayHandler().endReplay();
      } catch (IOException e) {
        Client.error("Trying to stop broken replay: ", new Object[] { e });
      } finally {
        if (ReplayModReplay.getInstance().getReplayHandler() != null)
          ReplayModReplay.getInstance().forcefullyStopReplay(); 
      }  
    event.buttonList.removeIf(button -> (button.id == 14));
    event.buttonList.add(new GuiButton(17890234, event.screen.width / 2 - 100, event.screen.height / 4 + 48 + 48, "Replay Viewer"));
  }
  
  @TypeEvent
  public void onButton(GuiScreenEvent.ActionPerformed.Pre event) {
    if (!event.button.enabled)
      return; 
    if (event.screen instanceof GuiMainMenu)
      if (event.button.id == 17890234)
        Minecraft.getMinecraft().displayGuiScreen((GuiScreen)new GuiReplayList());  
    if (event.screen instanceof net.minecraft.client.gui.GuiIngameMenu)
      if (ReplayModReplay.getInstance().getReplayHandler() != null) {
        if (event.button.id == 17890235) {
          event.button.enabled = false;
          mc.displayGuiScreen((GuiScreen)new GuiMainMenu());
          try {
            ReplayModReplay.getInstance().getReplayHandler().endReplay();
          } catch (Exception exception) {}
        } 
      } else if (event.button.id == 333 && 
        event.button.displayString.equals("Start Replay Recording")) {
        ReplayModRecording.INSTANCE.initiateRecording(Minecraft.getMinecraft().getNetHandler().getNetworkManager());
        event.button.enabled = false;
      }  
  }
}
