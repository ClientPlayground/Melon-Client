package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatRewriter {
  private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
  
  public static BaseComponent[] fromLegacyText(String message, ChatColor defaultColor) {
    ArrayList<BaseComponent> components = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    TextComponent component = new TextComponent();
    Matcher matcher = url.matcher(message);
    for (int i = 0; i < message.length(); i++) {
      char c = message.charAt(i);
      if (c == '§') {
        if (++i >= message.length())
          break; 
        c = message.charAt(i);
        if (c >= 'A' && c <= 'Z')
          c = (char)(c + 32); 
        ChatColor format = ChatColor.getByChar(c);
        if (format != null) {
          if (builder.length() > 0) {
            TextComponent old = component;
            component = new TextComponent(old);
            old.setText(builder.toString());
            builder = new StringBuilder();
            components.add(old);
          } 
          switch (format) {
            case BOLD:
              component.setBold(Boolean.valueOf(true));
              break;
            case ITALIC:
              component.setItalic(Boolean.valueOf(true));
              break;
            case UNDERLINE:
              component.setUnderlined(Boolean.valueOf(true));
              break;
            case STRIKETHROUGH:
              component.setStrikethrough(Boolean.valueOf(true));
              break;
            case MAGIC:
              component.setObfuscated(Boolean.valueOf(true));
              break;
            case RESET:
              format = defaultColor;
            default:
              component = new TextComponent();
              component.setColor(format);
              component.setBold(Boolean.valueOf(false));
              component.setItalic(Boolean.valueOf(false));
              component.setUnderlined(Boolean.valueOf(false));
              component.setStrikethrough(Boolean.valueOf(false));
              component.setObfuscated(Boolean.valueOf(false));
              break;
          } 
        } 
      } else {
        int pos = message.indexOf(' ', i);
        if (pos == -1)
          pos = message.length(); 
        if (matcher.region(i, pos).find()) {
          if (builder.length() > 0) {
            TextComponent textComponent = component;
            component = new TextComponent(textComponent);
            textComponent.setText(builder.toString());
            builder = new StringBuilder();
            components.add(textComponent);
          } 
          TextComponent old = component;
          component = new TextComponent(old);
          String urlString = message.substring(i, pos);
          component.setText(urlString);
          component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, 
                urlString.startsWith("http") ? urlString : ("http://" + urlString)));
          components.add(component);
          i += pos - i - 1;
          component = old;
        } else {
          builder.append(c);
        } 
      } 
    } 
    component.setText(builder.toString());
    components.add(component);
    return components.<BaseComponent>toArray(new BaseComponent[0]);
  }
  
  public static String legacyTextToJson(String legacyText) {
    return ComponentSerializer.toString(fromLegacyText(legacyText, ChatColor.WHITE));
  }
  
  public static String jsonTextToLegacy(String value) {
    return TextComponent.toLegacyText(ComponentSerializer.parse(value));
  }
  
  public static String processTranslate(String value) {
    BaseComponent[] components = ComponentSerializer.parse(value);
    for (BaseComponent component : components)
      processTranslate(component); 
    if (components.length == 1)
      return ComponentSerializer.toString(components[0]); 
    return ComponentSerializer.toString(components);
  }
  
  private static void processTranslate(BaseComponent component) {
    if (component == null)
      return; 
    if (component instanceof TranslatableComponent) {
      String oldTranslate = ((TranslatableComponent)component).getTranslate();
      String newTranslate = (String)MappingData.translateMapping.get(oldTranslate);
      if (newTranslate == null)
        MappingData.mojangTranslation.get(oldTranslate); 
      if (newTranslate != null)
        ((TranslatableComponent)component).setTranslate(newTranslate); 
      if (((TranslatableComponent)component).getWith() != null)
        for (BaseComponent baseComponent : ((TranslatableComponent)component).getWith())
          processTranslate(baseComponent);  
    } 
    if (component.getHoverEvent() != null)
      for (BaseComponent baseComponent : component.getHoverEvent().getValue())
        processTranslate(baseComponent);  
    if (component.getExtra() != null)
      for (BaseComponent baseComponent : component.getExtra())
        processTranslate(baseComponent);  
  }
}
