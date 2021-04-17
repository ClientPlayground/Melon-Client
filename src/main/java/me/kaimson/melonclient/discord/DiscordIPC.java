package me.kaimson.melonclient.discord;

import com.jagrosh.discordipc.*;
import com.jagrosh.discordipc.exceptions.*;
import me.kaimson.melonclient.*;
import com.jagrosh.discordipc.entities.pipe.*;
import com.jagrosh.discordipc.entities.*;
import java.time.*;
import org.json.*;

public class DiscordIPC implements IPCListener
{
    public static final DiscordIPC INSTANCE;
    private IPCClient client;
    
    public void init() {
        (this.client = new IPCClient(814512771873767484L)).setListener(this);
        try {
            this.client.connect();
        }
        catch (NoDiscordClientException e) {
            e.printStackTrace();
        }
        catch (Exception e2) {
            Client.error("UNKOWN ERROR");
            e2.printStackTrace();
        }
        Client.info("IPC {} -> {}", this.client.getStatus(), this.client.getDiscordBuild());
    }
    
    public void shutdown() {
        if (this.client != null && this.client.getStatus() == PipeStatus.CONNECTED) {
            this.client.close();
            Client.info("Discord IPC closed!");
        }
    }
    
    public void onReady(final IPCClient client) {
        final RichPresence.Builder builder = new RichPresence.Builder().setState("Idle").setDetails("Minecraft 1.8.9").setLargeImage("logo", "Melon Client 1.8.9").setStartTimestamp(OffsetDateTime.now());
        client.sendRichPresence(builder.build());
    }
    
    public void onClose(final IPCClient client, final JSONObject json) {
    }
    
    public void onDisconnect(final IPCClient client, final Throwable t) {
    }
    
    static {
        INSTANCE = new DiscordIPC();
    }
}
