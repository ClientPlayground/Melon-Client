package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserListOps extends UserList<GameProfile, UserListOpsEntry> {
  public UserListOps(File saveFile) {
    super(saveFile);
  }
  
  protected UserListEntry<GameProfile> createEntry(JsonObject entryData) {
    return new UserListOpsEntry(entryData);
  }
  
  public String[] getKeys() {
    String[] astring = new String[getValues().size()];
    int i = 0;
    for (UserListOpsEntry userlistopsentry : getValues().values())
      astring[i++] = userlistopsentry.getValue().getName(); 
    return astring;
  }
  
  public boolean bypassesPlayerLimit(GameProfile profile) {
    UserListOpsEntry userlistopsentry = getEntry(profile);
    return (userlistopsentry != null) ? userlistopsentry.bypassesPlayerLimit() : false;
  }
  
  protected String getObjectKey(GameProfile obj) {
    return obj.getId().toString();
  }
  
  public GameProfile getGameProfileFromName(String username) {
    for (UserListOpsEntry userlistopsentry : getValues().values()) {
      if (username.equalsIgnoreCase(userlistopsentry.getValue().getName()))
        return userlistopsentry.getValue(); 
    } 
    return null;
  }
}
