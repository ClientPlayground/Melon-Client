package me.kaimson.melonclient.ingames.utils.ReplayMod.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import me.kaimson.melonclient.Client;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.lwjgl.input.Keyboard;

public class KeyBindingRegistry {
  private final Map<String, KeyBinding> keyBindings = new HashMap<>();
  
  private final Multimap<KeyBinding, Runnable> keyBindingHandlers = (Multimap<KeyBinding, Runnable>)ArrayListMultimap.create();
  
  private final Multimap<KeyBinding, Runnable> repeatedKeyBindingHandlers = (Multimap<KeyBinding, Runnable>)ArrayListMultimap.create();
  
  private final Multimap<Integer, Runnable> rawHandlers = (Multimap<Integer, Runnable>)ArrayListMultimap.create();
  
  public void registerKeyBinding(String name, int keyCode, Runnable whenPressed) {
    this.keyBindingHandlers.put(registerKeyBinding(name, keyCode), whenPressed);
  }
  
  public void registerRepeatedKeyBinding(String name, int keyCode, Runnable whenPressed) {
    this.repeatedKeyBindingHandlers.put(registerKeyBinding(name, keyCode), whenPressed);
  }
  
  private KeyBinding registerKeyBinding(String name, int keyCode) {
    KeyBinding keyBinding = this.keyBindings.get(name);
    if (keyBinding == null) {
      keyBinding = new KeyBinding(name, keyCode, "replaymod.title");
      this.keyBindings.put(name, keyBinding);
      Client.instance.registerKeybind(keyBinding);
    } 
    return keyBinding;
  }
  
  public void registerRaw(int keyCode, Runnable whenPressed) {
    this.rawHandlers.put(Integer.valueOf(keyCode), whenPressed);
  }
  
  public Map<String, KeyBinding> getKeyBindings() {
    return Collections.unmodifiableMap(this.keyBindings);
  }
  
  public void onKeyInput() {
    handleKeyBindings();
    handleRaw();
  }
  
  public void onTick() {
    handleRepeatedKeyBindings();
  }
  
  public void handleRepeatedKeyBindings() {
    for (Map.Entry<KeyBinding, Collection<Runnable>> entry : (Iterable<Map.Entry<KeyBinding, Collection<Runnable>>>)this.repeatedKeyBindingHandlers.asMap().entrySet()) {
      if (((KeyBinding)entry.getKey()).isKeyDown())
        invokeKeyBindingHandlers(entry.getKey(), entry.getValue()); 
    } 
  }
  
  public void handleKeyBindings() {
    for (Map.Entry<KeyBinding, Collection<Runnable>> entry : (Iterable<Map.Entry<KeyBinding, Collection<Runnable>>>)this.keyBindingHandlers.asMap().entrySet()) {
      while (((KeyBinding)entry.getKey()).isPressed())
        invokeKeyBindingHandlers(entry.getKey(), entry.getValue()); 
    } 
  }
  
  private void invokeKeyBindingHandlers(KeyBinding keyBinding, Collection<Runnable> handlers) {
    for (Runnable runnable : handlers) {
      try {
        runnable.run();
      } catch (Throwable cause) {
        CrashReport crashReport = CrashReport.makeCrashReport(cause, "Handling Key Binding");
        CrashReportCategory category = crashReport.makeCategory("Key Binding");
        category.addCrashSection("Key Binding", keyBinding);
        category.addCrashSectionCallable("Handler", runnable::toString);
        throw new ReportedException(crashReport);
      } 
    } 
  }
  
  public void handleRaw() {
    int keyCode = (Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey();
    for (Runnable runnable : this.rawHandlers.get(Integer.valueOf(keyCode))) {
      try {
        runnable.run();
      } catch (Throwable cause) {
        CrashReport crashReport = CrashReport.makeCrashReport(cause, "Handling Raw Key Binding");
        CrashReportCategory category = crashReport.makeCategory("Key Binding");
        category.addCrashSection("Key Code", Integer.valueOf(keyCode));
        category.addCrashSectionCallable("Handler", () -> runnable);
        throw new ReportedException(crashReport);
      } 
    } 
  }
}
