package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer;

import java.io.IOException;
import me.kaimson.melonclient.Events.Event;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.imp.KeyInputEvent;
import me.kaimson.melonclient.Events.imp.MouseInputEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.WrappedTimer;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraController;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Timer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InputReplayTimer extends WrappedTimer {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  public InputReplayTimer(Timer wrapped) {
    super(wrapped);
  }
  
  public void updateTimer() {
    super.updateTimer();
    if (ReplayModReplay.getInstance().getReplayHandler() != null)
      if (this.mc.currentScreen == null || this.mc.currentScreen.allowUserInput) {
        while (Mouse.next())
          handleMouseEvent(); 
        while (Keyboard.next())
          handleKeyEvent(); 
      } else {
        try {
          this.mc.currentScreen.handleInput();
        } catch (IOException e) {
          e.printStackTrace();
        } 
      }  
  }
  
  protected void handleMouseEvent() {
    int button = Mouse.getEventButton() - 100;
    boolean pressed = Mouse.getEventButtonState();
    KeyBinding.setKeyBindState(button, pressed);
    if (pressed)
      KeyBinding.onTick(button); 
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) {
      ReplayHandler replayHandler = ReplayModReplay.getInstance().getReplayHandler();
      if (replayHandler != null) {
        CameraEntity cameraEntity = replayHandler.getCameraEntity();
        if (cameraEntity != null) {
          CameraController controller = cameraEntity.getCameraController();
          while (wheel > 0) {
            controller.increaseSpeed();
            wheel--;
          } 
          while (wheel < 0) {
            controller.decreaseSpeed();
            wheel++;
          } 
        } 
      } 
    } 
    if (this.mc.currentScreen == null) {
      if (!this.mc.inGameHasFocus && Mouse.getEventButtonState())
        this.mc.setIngameFocus(); 
    } else {
      try {
        this.mc.currentScreen.handleMouseInput();
      } catch (IOException e) {
        e.printStackTrace();
      } 
    } 
    EventHandler.call((Event)new MouseInputEvent());
  }
  
  protected void handleKeyEvent() {
    int key = (Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey();
    boolean pressed = Keyboard.getEventKeyState();
    KeyBinding.setKeyBindState(key, pressed);
    if (pressed)
      KeyBinding.onTick(key); 
    if (this.mc.debugCrashKeyPressTime > 0L) {
      if (Minecraft.getSystemTime() - this.mc.debugCrashKeyPressTime >= 6000L)
        throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable())); 
      if (!Keyboard.isKeyDown(61) || !Keyboard.isKeyDown(46))
        this.mc.debugCrashKeyPressTime = -1L; 
    } else if (Keyboard.isKeyDown(61) && Keyboard.isKeyDown(46)) {
      this.mc.debugCrashKeyPressTime = Minecraft.getSystemTime();
    } 
    if (pressed) {
      if (key == 62 && this.mc.entityRenderer != null)
        this.mc.entityRenderer.switchUseShader(); 
      if (this.mc.currentScreen != null) {
        try {
          this.mc.currentScreen.handleKeyboardInput();
        } catch (IOException e) {
          e.printStackTrace();
        } 
      } else {
        if (key == 1)
          this.mc.displayInGameMenu(); 
        if (key == 32 && Keyboard.isKeyDown(61) && this.mc.ingameGUI != null)
          this.mc.ingameGUI.getChatGUI().clearChatMessages(); 
        if (key == 31 && Keyboard.isKeyDown(61))
          this.mc.refreshResources(); 
        if (key == 20 && Keyboard.isKeyDown(61))
          this.mc.refreshResources(); 
        if (key == 33 && Keyboard.isKeyDown(61)) {
          int i = Keyboard.isKeyDown(42) | Keyboard.isKeyDown(54);
          this.mc.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, (i != 0) ? -1 : 1);
        } 
        if (key == 30 && Keyboard.isKeyDown(61))
          this.mc.renderGlobal.loadRenderers(); 
        if (key == 48 && Keyboard.isKeyDown(61))
          this.mc.getRenderManager().setDebugBoundingBox(!this.mc.getRenderManager().isDebugBoundingBox()); 
        if (key == 25 && Keyboard.isKeyDown(61)) {
          this.mc.gameSettings.pauseOnLostFocus = !this.mc.gameSettings.pauseOnLostFocus;
          this.mc.gameSettings.saveOptions();
        } 
        if (key == 59)
          this.mc.gameSettings.hideGUI = !this.mc.gameSettings.hideGUI; 
        if (key == 61) {
          this.mc.gameSettings.showDebugInfo = !this.mc.gameSettings.showDebugInfo;
          this.mc.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
        } 
        if (this.mc.gameSettings.keyBindTogglePerspective.isPressed()) {
          this.mc.gameSettings.thirdPersonView = (this.mc.gameSettings.thirdPersonView + 1) % 3;
          if (this.mc.entityRenderer != null)
            if (this.mc.gameSettings.thirdPersonView == 0) {
              this.mc.entityRenderer.loadEntityShader(this.mc.getRenderViewEntity());
            } else if (this.mc.gameSettings.thirdPersonView == 1) {
              this.mc.entityRenderer.loadEntityShader(null);
            }  
        } 
      } 
      if (this.mc.gameSettings.showDebugInfo && this.mc.gameSettings.showDebugProfilerChart) {
        if (key == 11)
          this.mc.updateDebugProfilerName(0); 
        for (int i = 0; i < 9; i++) {
          if (key == 2 + i)
            this.mc.updateDebugProfilerName(i + 1); 
        } 
      } 
    } 
    EventHandler.call((Event)new KeyInputEvent(key));
  }
}
