package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

public class ChatStyle {
  private ChatStyle parentStyle;
  
  private EnumChatFormatting color;
  
  private Boolean bold;
  
  private Boolean italic;
  
  private Boolean underlined;
  
  private Boolean strikethrough;
  
  private Boolean obfuscated;
  
  private ClickEvent chatClickEvent;
  
  private HoverEvent chatHoverEvent;
  
  private String insertion;
  
  private static final ChatStyle rootStyle = new ChatStyle() {
      public EnumChatFormatting getColor() {
        return null;
      }
      
      public boolean getBold() {
        return false;
      }
      
      public boolean getItalic() {
        return false;
      }
      
      public boolean getStrikethrough() {
        return false;
      }
      
      public boolean getUnderlined() {
        return false;
      }
      
      public boolean getObfuscated() {
        return false;
      }
      
      public ClickEvent getChatClickEvent() {
        return null;
      }
      
      public HoverEvent getChatHoverEvent() {
        return null;
      }
      
      public String getInsertion() {
        return null;
      }
      
      public ChatStyle setColor(EnumChatFormatting color) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setBold(Boolean boldIn) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setItalic(Boolean italic) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setStrikethrough(Boolean strikethrough) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setUnderlined(Boolean underlined) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setObfuscated(Boolean obfuscated) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setChatClickEvent(ClickEvent event) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setChatHoverEvent(HoverEvent event) {
        throw new UnsupportedOperationException();
      }
      
      public ChatStyle setParentStyle(ChatStyle parent) {
        throw new UnsupportedOperationException();
      }
      
      public String toString() {
        return "Style.ROOT";
      }
      
      public ChatStyle createShallowCopy() {
        return this;
      }
      
      public ChatStyle createDeepCopy() {
        return this;
      }
      
      public String getFormattingCode() {
        return "";
      }
    };
  
  public EnumChatFormatting getColor() {
    return (this.color == null) ? getParent().getColor() : this.color;
  }
  
  public boolean getBold() {
    return (this.bold == null) ? getParent().getBold() : this.bold.booleanValue();
  }
  
  public boolean getItalic() {
    return (this.italic == null) ? getParent().getItalic() : this.italic.booleanValue();
  }
  
  public boolean getStrikethrough() {
    return (this.strikethrough == null) ? getParent().getStrikethrough() : this.strikethrough.booleanValue();
  }
  
  public boolean getUnderlined() {
    return (this.underlined == null) ? getParent().getUnderlined() : this.underlined.booleanValue();
  }
  
  public boolean getObfuscated() {
    return (this.obfuscated == null) ? getParent().getObfuscated() : this.obfuscated.booleanValue();
  }
  
  public boolean isEmpty() {
    return (this.bold == null && this.italic == null && this.strikethrough == null && this.underlined == null && this.obfuscated == null && this.color == null && this.chatClickEvent == null && this.chatHoverEvent == null);
  }
  
  public ClickEvent getChatClickEvent() {
    return (this.chatClickEvent == null) ? getParent().getChatClickEvent() : this.chatClickEvent;
  }
  
  public HoverEvent getChatHoverEvent() {
    return (this.chatHoverEvent == null) ? getParent().getChatHoverEvent() : this.chatHoverEvent;
  }
  
  public String getInsertion() {
    return (this.insertion == null) ? getParent().getInsertion() : this.insertion;
  }
  
  public ChatStyle setColor(EnumChatFormatting color) {
    this.color = color;
    return this;
  }
  
  public ChatStyle setBold(Boolean boldIn) {
    this.bold = boldIn;
    return this;
  }
  
  public ChatStyle setItalic(Boolean italic) {
    this.italic = italic;
    return this;
  }
  
  public ChatStyle setStrikethrough(Boolean strikethrough) {
    this.strikethrough = strikethrough;
    return this;
  }
  
  public ChatStyle setUnderlined(Boolean underlined) {
    this.underlined = underlined;
    return this;
  }
  
  public ChatStyle setObfuscated(Boolean obfuscated) {
    this.obfuscated = obfuscated;
    return this;
  }
  
  public ChatStyle setChatClickEvent(ClickEvent event) {
    this.chatClickEvent = event;
    return this;
  }
  
  public ChatStyle setChatHoverEvent(HoverEvent event) {
    this.chatHoverEvent = event;
    return this;
  }
  
  public ChatStyle setInsertion(String insertion) {
    this.insertion = insertion;
    return this;
  }
  
  public ChatStyle setParentStyle(ChatStyle parent) {
    this.parentStyle = parent;
    return this;
  }
  
  public String getFormattingCode() {
    if (isEmpty())
      return (this.parentStyle != null) ? this.parentStyle.getFormattingCode() : ""; 
    StringBuilder stringbuilder = new StringBuilder();
    if (getColor() != null)
      stringbuilder.append(getColor()); 
    if (getBold())
      stringbuilder.append(EnumChatFormatting.BOLD); 
    if (getItalic())
      stringbuilder.append(EnumChatFormatting.ITALIC); 
    if (getUnderlined())
      stringbuilder.append(EnumChatFormatting.UNDERLINE); 
    if (getObfuscated())
      stringbuilder.append(EnumChatFormatting.OBFUSCATED); 
    if (getStrikethrough())
      stringbuilder.append(EnumChatFormatting.STRIKETHROUGH); 
    return stringbuilder.toString();
  }
  
  private ChatStyle getParent() {
    return (this.parentStyle == null) ? rootStyle : this.parentStyle;
  }
  
  public String toString() {
    return "Style{hasParent=" + ((this.parentStyle != null) ? 1 : 0) + ", color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", obfuscated=" + this.obfuscated + ", clickEvent=" + getChatClickEvent() + ", hoverEvent=" + getChatHoverEvent() + ", insertion=" + getInsertion() + '}';
  }
  
  public boolean equals(Object p_equals_1_) {
    if (this == p_equals_1_)
      return true; 
    if (!(p_equals_1_ instanceof ChatStyle))
      return false; 
    ChatStyle chatstyle = (ChatStyle)p_equals_1_;
    if (getBold() == chatstyle.getBold() && getColor() == chatstyle.getColor() && getItalic() == chatstyle.getItalic() && getObfuscated() == chatstyle.getObfuscated() && getStrikethrough() == chatstyle.getStrikethrough() && getUnderlined() == chatstyle.getUnderlined()) {
      if (getChatClickEvent() != null) {
        if (!getChatClickEvent().equals(chatstyle.getChatClickEvent())) {
          boolean flag = false;
          return flag;
        } 
      } else if (chatstyle.getChatClickEvent() != null) {
        return false;
      } 
      if (getChatHoverEvent() != null) {
        if (!getChatHoverEvent().equals(chatstyle.getChatHoverEvent()))
          return false; 
      } else if (chatstyle.getChatHoverEvent() != null) {
        return false;
      } 
      if (getInsertion() != null) {
        if (getInsertion().equals(chatstyle.getInsertion())) {
          boolean flag = true;
          return flag;
        } 
      } else if (chatstyle.getInsertion() == null) {
        return true;
      } 
    } 
    return false;
  }
  
  public int hashCode() {
    int i = this.color.hashCode();
    i = 31 * i + this.bold.hashCode();
    i = 31 * i + this.italic.hashCode();
    i = 31 * i + this.underlined.hashCode();
    i = 31 * i + this.strikethrough.hashCode();
    i = 31 * i + this.obfuscated.hashCode();
    i = 31 * i + this.chatClickEvent.hashCode();
    i = 31 * i + this.chatHoverEvent.hashCode();
    i = 31 * i + this.insertion.hashCode();
    return i;
  }
  
  public ChatStyle createShallowCopy() {
    ChatStyle chatstyle = new ChatStyle();
    chatstyle.bold = this.bold;
    chatstyle.italic = this.italic;
    chatstyle.strikethrough = this.strikethrough;
    chatstyle.underlined = this.underlined;
    chatstyle.obfuscated = this.obfuscated;
    chatstyle.color = this.color;
    chatstyle.chatClickEvent = this.chatClickEvent;
    chatstyle.chatHoverEvent = this.chatHoverEvent;
    chatstyle.parentStyle = this.parentStyle;
    chatstyle.insertion = this.insertion;
    return chatstyle;
  }
  
  public ChatStyle createDeepCopy() {
    ChatStyle chatstyle = new ChatStyle();
    chatstyle.setBold(Boolean.valueOf(getBold()));
    chatstyle.setItalic(Boolean.valueOf(getItalic()));
    chatstyle.setStrikethrough(Boolean.valueOf(getStrikethrough()));
    chatstyle.setUnderlined(Boolean.valueOf(getUnderlined()));
    chatstyle.setObfuscated(Boolean.valueOf(getObfuscated()));
    chatstyle.setColor(getColor());
    chatstyle.setChatClickEvent(getChatClickEvent());
    chatstyle.setChatHoverEvent(getChatHoverEvent());
    chatstyle.setInsertion(getInsertion());
    return chatstyle;
  }
  
  public static class Serializer implements JsonDeserializer<ChatStyle>, JsonSerializer<ChatStyle> {
    public ChatStyle deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
      if (p_deserialize_1_.isJsonObject()) {
        ChatStyle chatstyle = new ChatStyle();
        JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
        if (jsonobject == null)
          return null; 
        if (jsonobject.has("bold"))
          chatstyle.bold = Boolean.valueOf(jsonobject.get("bold").getAsBoolean()); 
        if (jsonobject.has("italic"))
          chatstyle.italic = Boolean.valueOf(jsonobject.get("italic").getAsBoolean()); 
        if (jsonobject.has("underlined"))
          chatstyle.underlined = Boolean.valueOf(jsonobject.get("underlined").getAsBoolean()); 
        if (jsonobject.has("strikethrough"))
          chatstyle.strikethrough = Boolean.valueOf(jsonobject.get("strikethrough").getAsBoolean()); 
        if (jsonobject.has("obfuscated"))
          chatstyle.obfuscated = Boolean.valueOf(jsonobject.get("obfuscated").getAsBoolean()); 
        if (jsonobject.has("color"))
          chatstyle.color = (EnumChatFormatting)p_deserialize_3_.deserialize(jsonobject.get("color"), EnumChatFormatting.class); 
        if (jsonobject.has("insertion"))
          chatstyle.insertion = jsonobject.get("insertion").getAsString(); 
        if (jsonobject.has("clickEvent")) {
          JsonObject jsonobject1 = jsonobject.getAsJsonObject("clickEvent");
          if (jsonobject1 != null) {
            JsonPrimitive jsonprimitive = jsonobject1.getAsJsonPrimitive("action");
            ClickEvent.Action clickevent$action = (jsonprimitive == null) ? null : ClickEvent.Action.getValueByCanonicalName(jsonprimitive.getAsString());
            JsonPrimitive jsonprimitive1 = jsonobject1.getAsJsonPrimitive("value");
            String s = (jsonprimitive1 == null) ? null : jsonprimitive1.getAsString();
            if (clickevent$action != null && s != null && clickevent$action.shouldAllowInChat())
              chatstyle.chatClickEvent = new ClickEvent(clickevent$action, s); 
          } 
        } 
        if (jsonobject.has("hoverEvent")) {
          JsonObject jsonobject2 = jsonobject.getAsJsonObject("hoverEvent");
          if (jsonobject2 != null) {
            JsonPrimitive jsonprimitive2 = jsonobject2.getAsJsonPrimitive("action");
            HoverEvent.Action hoverevent$action = (jsonprimitive2 == null) ? null : HoverEvent.Action.getValueByCanonicalName(jsonprimitive2.getAsString());
            IChatComponent ichatcomponent = (IChatComponent)p_deserialize_3_.deserialize(jsonobject2.get("value"), IChatComponent.class);
            if (hoverevent$action != null && ichatcomponent != null && hoverevent$action.shouldAllowInChat())
              chatstyle.chatHoverEvent = new HoverEvent(hoverevent$action, ichatcomponent); 
          } 
        } 
        return chatstyle;
      } 
      return null;
    }
    
    public JsonElement serialize(ChatStyle p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
      if (p_serialize_1_.isEmpty())
        return null; 
      JsonObject jsonobject = new JsonObject();
      if (p_serialize_1_.bold != null)
        jsonobject.addProperty("bold", p_serialize_1_.bold); 
      if (p_serialize_1_.italic != null)
        jsonobject.addProperty("italic", p_serialize_1_.italic); 
      if (p_serialize_1_.underlined != null)
        jsonobject.addProperty("underlined", p_serialize_1_.underlined); 
      if (p_serialize_1_.strikethrough != null)
        jsonobject.addProperty("strikethrough", p_serialize_1_.strikethrough); 
      if (p_serialize_1_.obfuscated != null)
        jsonobject.addProperty("obfuscated", p_serialize_1_.obfuscated); 
      if (p_serialize_1_.color != null)
        jsonobject.add("color", p_serialize_3_.serialize(p_serialize_1_.color)); 
      if (p_serialize_1_.insertion != null)
        jsonobject.add("insertion", p_serialize_3_.serialize(p_serialize_1_.insertion)); 
      if (p_serialize_1_.chatClickEvent != null) {
        JsonObject jsonobject1 = new JsonObject();
        jsonobject1.addProperty("action", p_serialize_1_.chatClickEvent.getAction().getCanonicalName());
        jsonobject1.addProperty("value", p_serialize_1_.chatClickEvent.getValue());
        jsonobject.add("clickEvent", (JsonElement)jsonobject1);
      } 
      if (p_serialize_1_.chatHoverEvent != null) {
        JsonObject jsonobject2 = new JsonObject();
        jsonobject2.addProperty("action", p_serialize_1_.chatHoverEvent.getAction().getCanonicalName());
        jsonobject2.add("value", p_serialize_3_.serialize(p_serialize_1_.chatHoverEvent.getValue()));
        jsonobject.add("hoverEvent", (JsonElement)jsonobject2);
      } 
      return (JsonElement)jsonobject;
    }
  }
}
