package club.minnced.discord.rpc;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiscordRichPresence extends Structure {
  private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList(new String[] { 
          "state", "details", "startTimestamp", "endTimestamp", "largeImageKey", "largeImageText", "smallImageKey", "smallImageText", "partyId", "partySize", 
          "partyMax", "matchSecret", "joinSecret", "spectateSecret", "instance" }));
  
  public String state;
  
  public String details;
  
  public long startTimestamp;
  
  public long endTimestamp;
  
  public String largeImageKey;
  
  public String largeImageText;
  
  public String smallImageKey;
  
  public String smallImageText;
  
  public String partyId;
  
  public int partySize;
  
  public int partyMax;
  
  public String matchSecret;
  
  public String joinSecret;
  
  public String spectateSecret;
  
  public byte instance;
  
  public DiscordRichPresence(String encoding) {
    setStringEncoding(encoding);
  }
  
  public DiscordRichPresence() {
    this("UTF-8");
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (!(o instanceof DiscordRichPresence))
      return false; 
    DiscordRichPresence presence = (DiscordRichPresence)o;
    return (this.startTimestamp == presence.startTimestamp && this.endTimestamp == presence.endTimestamp && this.partySize == presence.partySize && this.partyMax == presence.partyMax && this.instance == presence.instance && 
      
      Objects.equals(this.state, presence.state) && 
      Objects.equals(this.details, presence.details) && 
      Objects.equals(this.largeImageKey, presence.largeImageKey) && 
      Objects.equals(this.largeImageText, presence.largeImageText) && 
      Objects.equals(this.smallImageKey, presence.smallImageKey) && 
      Objects.equals(this.smallImageText, presence.smallImageText) && 
      Objects.equals(this.partyId, presence.partyId) && 
      Objects.equals(this.matchSecret, presence.matchSecret) && 
      Objects.equals(this.joinSecret, presence.joinSecret) && 
      Objects.equals(this.spectateSecret, presence.spectateSecret));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.state, this.details, Long.valueOf(this.startTimestamp), Long.valueOf(this.endTimestamp), this.largeImageKey, this.largeImageText, this.smallImageKey, this.smallImageText, this.partyId, 
          Integer.valueOf(this.partySize), 
          Integer.valueOf(this.partyMax), this.matchSecret, this.joinSecret, this.spectateSecret, Byte.valueOf(this.instance) });
  }
  
  protected List<String> getFieldOrder() {
    return FIELD_ORDER;
  }
}
