package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class ChatRewriter {
  public static void toClient(JsonObject obj, UserConnection user) {
    if (obj.get("translate") != null && obj.get("translate").getAsString().equals("gameMode.changed")) {
      String gameMode = ((EntityTracker)user.get(EntityTracker.class)).getGameMode().getText();
      JsonObject gameModeObject = new JsonObject();
      gameModeObject.addProperty("text", gameMode);
      gameModeObject.addProperty("color", "gray");
      gameModeObject.addProperty("italic", Boolean.valueOf(true));
      JsonArray array = new JsonArray();
      array.add((JsonElement)gameModeObject);
      obj.add("with", (JsonElement)array);
    } 
  }
}
