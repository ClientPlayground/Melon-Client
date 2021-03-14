package me.kaimson.melonclient;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.EventListener;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import me.kaimson.melonclient.blur.BlurShader;
import me.kaimson.melonclient.config.Config;
import me.kaimson.melonclient.discord.RichPresence;
import me.kaimson.melonclient.gui.GuiHudEditor;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameManager;
import me.kaimson.melonclient.ingames.render.RenderManager;
import me.kaimson.melonclient.ingames.utils.BehindYou;
import me.kaimson.melonclient.ingames.utils.CPS;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ToggleSprint;
import me.kaimson.melonclient.ingames.utils.itemphysics.ItemPhysics;
import me.kaimson.melonclient.util.Keybind;
import me.kaimson.melonclient.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Logger;

public class Client {
  public static final String name = "Melon Client";
  
  public static final String resourceLocation = "melonclient";
  
  public static Client instance = new Client();
  
  public static final CPS cps = new CPS();
  
  public static final Utils utils = new Utils();
  
  public static final File clientDirectory = new File((Minecraft.getMinecraft()).mcDataDir + "/" + "Melon Client");
  
  public static final Config config = new Config(new File(clientDirectory, "settings.json"));
  
  public static final RenderManager renderManager = new RenderManager();
  
  public static final Logger logger = LogManager.getLogger();
  
  public static final ReplayCore replayCore = new ReplayCore();
  
  public static final IngameManager ingameManager = new IngameManager();
  
  public static final Logger coreLogger = (Logger)LogManager.getLogger();
  
  private Keybind toggleSprintBind;
  
  private Keybind behindYou;
  
  public static final ToggleSprint toggleSprint = new ToggleSprint();
  
  public static final float BLUR_RADIUS = 5.0F;
  
  public static final BlurShader blurShader = new BlurShader();
  
  public final List<Keybind> keyBinds = Lists.newArrayList();
  
  public Client() {
    new GuiUtils();
    EventHandler.register(this);
  }
  
  public void init() {
    config.loadLanguageFile();
    config.loadConfig();
    ingameManager.init();
    ItemPhysics.INSTANCE.init();
    RichPresence.INSTANCE.init();
    (new EventListener()).init();
  }
  
  @TypeEvent
  private void onOverlayRender(TickEvent.RenderTick.Overlay e) {
    if (e.phase != TickEvent.Phase.END)
      return; 
    renderManager.onRenderTick();
    blurShader.onRenderTick();
    cps.onTick();
    toggleSprint.onTick(this.toggleSprintBind);
    BehindYou.INSTANCE.onTick(this.behindYou);
  }
  
  public void onKeyPress(int keycode) {
    for (Keybind keybind : this.keyBinds) {
      if (keybind.getKeycode() == keycode)
        keybind.getOnPress().accept(keybind); 
    } 
  }
  
  public void registerKeybinds() {
    ingameManager.patch();
    registerKeybind(new Keybind("Open settings", 54, keybind -> Minecraft.getMinecraft().displayGuiScreen((GuiScreen)new GuiHudEditor())));
    registerKeybind(this.toggleSprintBind = new Keybind("Toggle Sprint", 19, keybind -> {
          
          }));
    registerKeybind(this.behindYou = new Keybind("Behind you", 47, keybind -> {
          
          }));
  }
  
  private void registerKeybind(Keybind keyBind) {
    this.keyBinds.add(keyBind);
    (Minecraft.getMinecraft()).gameSettings.keyBindings = (KeyBinding[])ArrayUtils.add((Object[])(Minecraft.getMinecraft()).gameSettings.keyBindings, keyBind.getKeyBinding());
  }
  
  public void registerKeybind(KeyBinding keyBinding) {
    (Minecraft.getMinecraft()).gameSettings.keyBindings = (KeyBinding[])ArrayUtils.add((Object[])(Minecraft.getMinecraft()).gameSettings.keyBindings, keyBinding);
  }
  
  public static void sendChatMessage(String s) {
    (Minecraft.getMinecraft()).ingameGUI.getChatGUI().printChatMessage(IChatComponent.Serializer.jsonToComponent("{\"text\":\"" + s + "\"}"));
  }
  
  public static void log(Object obj) {
    logger.log(Level.INFO, "[Melon Client] " + obj);
  }
  
  public static void warn(Object obj, Object... objects) {
    logger.log(Level.WARN, "[Melon Client] " + obj, objects);
  }
  
  public static void debug(Object obj, Object... objects) {
    logger.log(Level.DEBUG, "[Melon Client] " + obj, objects);
  }
  
  public static void error(Object obj, Object... objects) {
    logger.log(Level.ERROR, "[Melon Client] " + obj, objects);
  }
}
