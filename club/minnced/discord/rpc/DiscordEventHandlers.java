package club.minnced.discord.rpc;

import com.sun.jna.Callback;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiscordEventHandlers extends Structure {
  private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList(new String[] { "ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest" }));
  
  public OnReady ready;
  
  public OnStatus disconnected;
  
  public OnStatus errored;
  
  public OnGameUpdate joinGame;
  
  public OnGameUpdate spectateGame;
  
  public OnJoinRequest joinRequest;
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (!(o instanceof DiscordEventHandlers))
      return false; 
    DiscordEventHandlers that = (DiscordEventHandlers)o;
    return (Objects.equals(this.ready, that.ready) && 
      Objects.equals(this.disconnected, that.disconnected) && 
      Objects.equals(this.errored, that.errored) && 
      Objects.equals(this.joinGame, that.joinGame) && 
      Objects.equals(this.spectateGame, that.spectateGame) && 
      Objects.equals(this.joinRequest, that.joinRequest));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.ready, this.disconnected, this.errored, this.joinGame, this.spectateGame, this.joinRequest });
  }
  
  protected List<String> getFieldOrder() {
    return FIELD_ORDER;
  }
  
  public static interface OnJoinRequest extends Callback {
    void accept(DiscordUser param1DiscordUser);
  }
  
  public static interface OnGameUpdate extends Callback {
    void accept(String param1String);
  }
  
  public static interface OnStatus extends Callback {
    void accept(int param1Int, String param1String);
  }
  
  public static interface OnReady extends Callback {
    void accept(DiscordUser param1DiscordUser);
  }
}
