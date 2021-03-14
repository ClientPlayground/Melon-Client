package me.kaimson.melonclient.discord;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import club.minnced.discord.rpc.DiscordUser;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.ingames.IngameDisplay;

public class RichPresence {
  public Thread worker;
  
  private long currentTimestamp;
  
  private long endTimestamp;
  
  private String state;
  
  private String details;
  
  public void setState(String state) {
    this.state = state;
  }
  
  public void setDetails(String details) {
    this.details = details;
  }
  
  private final DiscordRPC rpc = DiscordRPC.INSTANCE;
  
  private final DiscordRichPresence presence = new DiscordRichPresence();
  
  private final RichPresenceEvents eventhandler = new RichPresenceEvents();
  
  public static final RichPresence INSTANCE = new RichPresence();
  
  public void init() {
    if (!IngameDisplay.DISCORD_INTEGRATION.isEnabled())
      return; 
    EventHandler.register(this.eventhandler);
    DiscordEventHandlers handlers = new DiscordEventHandlers();
    handlers.ready = (user -> Client.log("Discord Rich Presence is ready!"));
    this.rpc.Discord_Initialize("807186345243049994", handlers, true, "Test");
    this.presence.startTimestamp = (this.currentTimestamp == 0L) ? (this.currentTimestamp = System.currentTimeMillis()) : this.currentTimestamp;
    this.presence.state = this.state;
    this.presence.details = this.details;
    this.presence.largeImageKey = "logo";
    this.rpc.Discord_UpdatePresence(this.presence);
    this.worker = new Thread(this::run, "Rich Presence");
    this.worker.start();
  }
  
  private void run() {
    while (this.worker != null && !this.worker.isInterrupted()) {
      if (!IngameDisplay.DISCORD_INTEGRATION.isEnabled())
        shutdown(); 
      this.rpc.Discord_RunCallbacks();
      this.presence.state = this.state;
      this.presence.details = this.details;
      updateTimestamp();
      this.rpc.Discord_UpdatePresence(this.presence);
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException interruptedException) {}
    } 
  }
  
  private void updateTimestamp() {
    if (this.presence.startTimestamp != this.currentTimestamp / 1000L)
      this.presence.startTimestamp = this.currentTimestamp / 1000L; 
    if (this.presence.endTimestamp != this.endTimestamp / 1000L)
      this.presence.endTimestamp = this.endTimestamp / 1000L; 
  }
  
  public void shutdown() {
    EventHandler.unregister(this.eventhandler);
    this.rpc.Discord_Shutdown();
    this.worker.interrupt();
    this.worker = null;
  }
}
