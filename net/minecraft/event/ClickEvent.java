package net.minecraft.event;

import com.google.common.collect.Maps;
import java.util.Map;

public class ClickEvent {
  private final Action action;
  
  private final String value;
  
  public ClickEvent(Action theAction, String theValue) {
    this.action = theAction;
    this.value = theValue;
  }
  
  public Action getAction() {
    return this.action;
  }
  
  public String getValue() {
    return this.value;
  }
  
  public boolean equals(Object p_equals_1_) {
    if (this == p_equals_1_)
      return true; 
    if (p_equals_1_ != null && getClass() == p_equals_1_.getClass()) {
      ClickEvent clickevent = (ClickEvent)p_equals_1_;
      if (this.action != clickevent.action)
        return false; 
      if (this.value != null) {
        if (!this.value.equals(clickevent.value))
          return false; 
      } else if (clickevent.value != null) {
        return false;
      } 
      return true;
    } 
    return false;
  }
  
  public String toString() {
    return "ClickEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
  }
  
  public int hashCode() {
    int i = this.action.hashCode();
    i = 31 * i + ((this.value != null) ? this.value.hashCode() : 0);
    return i;
  }
  
  public enum Action {
    OPEN_URL("open_url", true),
    OPEN_FILE("open_file", false),
    RUN_COMMAND("run_command", true),
    TWITCH_USER_INFO("twitch_user_info", false),
    SUGGEST_COMMAND("suggest_command", true),
    CHANGE_PAGE("change_page", true);
    
    private static final Map<String, Action> nameMapping = Maps.newHashMap();
    
    private final boolean allowedInChat;
    
    private final String canonicalName;
    
    static {
      for (Action clickevent$action : values())
        nameMapping.put(clickevent$action.getCanonicalName(), clickevent$action); 
    }
    
    Action(String canonicalNameIn, boolean allowedInChatIn) {
      this.canonicalName = canonicalNameIn;
      this.allowedInChat = allowedInChatIn;
    }
    
    public boolean shouldAllowInChat() {
      return this.allowedInChat;
    }
    
    public String getCanonicalName() {
      return this.canonicalName;
    }
    
    public static Action getValueByCanonicalName(String canonicalNameIn) {
      return nameMapping.get(canonicalNameIn);
    }
  }
}
