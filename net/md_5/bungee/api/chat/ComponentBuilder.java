package net.md_5.bungee.api.chat;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;

public final class ComponentBuilder {
  private BaseComponent current;
  
  private final List<BaseComponent> parts = new ArrayList<>();
  
  public ComponentBuilder(ComponentBuilder original) {
    this.current = original.current.duplicate();
    for (BaseComponent baseComponent : original.parts)
      this.parts.add(baseComponent.duplicate()); 
  }
  
  public ComponentBuilder(String text) {
    this.current = new TextComponent(text);
  }
  
  public ComponentBuilder(BaseComponent component) {
    this.current = component.duplicate();
  }
  
  public ComponentBuilder append(BaseComponent component) {
    return append(component, FormatRetention.ALL);
  }
  
  public ComponentBuilder append(BaseComponent component, FormatRetention retention) {
    this.parts.add(this.current);
    BaseComponent previous = this.current;
    this.current = component.duplicate();
    this.current.copyFormatting(previous, retention, false);
    return this;
  }
  
  public ComponentBuilder append(BaseComponent[] components) {
    return append(components, FormatRetention.ALL);
  }
  
  public ComponentBuilder append(BaseComponent[] components, FormatRetention retention) {
    Preconditions.checkArgument((components.length != 0), "No components to append");
    BaseComponent previous = this.current;
    for (BaseComponent component : components) {
      this.parts.add(this.current);
      this.current = component.duplicate();
      this.current.copyFormatting(previous, retention, false);
    } 
    return this;
  }
  
  public ComponentBuilder append(String text) {
    return append(text, FormatRetention.ALL);
  }
  
  public ComponentBuilder appendLegacy(String text) {
    return append(TextComponent.fromLegacyText(text));
  }
  
  public ComponentBuilder append(String text, FormatRetention retention) {
    this.parts.add(this.current);
    BaseComponent old = this.current;
    this.current = new TextComponent(text);
    this.current.copyFormatting(old, retention, false);
    return this;
  }
  
  public ComponentBuilder append(Joiner joiner) {
    return joiner.join(this, FormatRetention.ALL);
  }
  
  public ComponentBuilder append(Joiner joiner, FormatRetention retention) {
    return joiner.join(this, retention);
  }
  
  public ComponentBuilder color(ChatColor color) {
    this.current.setColor(color);
    return this;
  }
  
  public ComponentBuilder bold(boolean bold) {
    this.current.setBold(Boolean.valueOf(bold));
    return this;
  }
  
  public ComponentBuilder italic(boolean italic) {
    this.current.setItalic(Boolean.valueOf(italic));
    return this;
  }
  
  public ComponentBuilder underlined(boolean underlined) {
    this.current.setUnderlined(Boolean.valueOf(underlined));
    return this;
  }
  
  public ComponentBuilder strikethrough(boolean strikethrough) {
    this.current.setStrikethrough(Boolean.valueOf(strikethrough));
    return this;
  }
  
  public ComponentBuilder obfuscated(boolean obfuscated) {
    this.current.setObfuscated(Boolean.valueOf(obfuscated));
    return this;
  }
  
  public ComponentBuilder insertion(String insertion) {
    this.current.setInsertion(insertion);
    return this;
  }
  
  public ComponentBuilder event(ClickEvent clickEvent) {
    this.current.setClickEvent(clickEvent);
    return this;
  }
  
  public ComponentBuilder event(HoverEvent hoverEvent) {
    this.current.setHoverEvent(hoverEvent);
    return this;
  }
  
  public ComponentBuilder reset() {
    return retain(FormatRetention.NONE);
  }
  
  public ComponentBuilder retain(FormatRetention retention) {
    this.current.retain(retention);
    return this;
  }
  
  public BaseComponent[] create() {
    BaseComponent[] result = this.parts.<BaseComponent>toArray(new BaseComponent[this.parts.size() + 1]);
    result[this.parts.size()] = this.current;
    return result;
  }
  
  public static interface Joiner {
    ComponentBuilder join(ComponentBuilder param1ComponentBuilder, ComponentBuilder.FormatRetention param1FormatRetention);
  }
  
  public enum FormatRetention {
    NONE, FORMATTING, EVENTS, ALL;
  }
}
