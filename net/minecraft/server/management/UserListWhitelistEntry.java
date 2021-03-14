package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class UserListWhitelistEntry extends UserListEntry<GameProfile> {
  public UserListWhitelistEntry(GameProfile profile) {
    super(profile);
  }
  
  public UserListWhitelistEntry(JsonObject json) {
    super(gameProfileFromJsonObject(json), json);
  }
  
  protected void onSerialization(JsonObject data) {
    if (getValue() != null) {
      data.addProperty("uuid", (getValue().getId() == null) ? "" : getValue().getId().toString());
      data.addProperty("name", getValue().getName());
      super.onSerialization(data);
    } 
  }
  
  private static GameProfile gameProfileFromJsonObject(JsonObject json) {
    if (json.has("uuid") && json.has("name")) {
      UUID uuid;
      String s = json.get("uuid").getAsString();
      try {
        uuid = UUID.fromString(s);
      } catch (Throwable var4) {
        return null;
      } 
      return new GameProfile(uuid, json.get("name").getAsString());
    } 
    return null;
  }
}
