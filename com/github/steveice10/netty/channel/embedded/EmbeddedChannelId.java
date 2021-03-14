package com.github.steveice10.netty.channel.embedded;

import com.github.steveice10.netty.channel.ChannelId;

final class EmbeddedChannelId implements ChannelId {
  private static final long serialVersionUID = -251711922203466130L;
  
  static final ChannelId INSTANCE = new EmbeddedChannelId();
  
  public String asShortText() {
    return toString();
  }
  
  public String asLongText() {
    return toString();
  }
  
  public int compareTo(ChannelId o) {
    if (o instanceof EmbeddedChannelId)
      return 0; 
    return asLongText().compareTo(o.asLongText());
  }
  
  public int hashCode() {
    return 0;
  }
  
  public boolean equals(Object obj) {
    return obj instanceof EmbeddedChannelId;
  }
  
  public String toString() {
    return "embedded";
  }
}
