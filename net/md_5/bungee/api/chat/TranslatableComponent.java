package net.md_5.bungee.api.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.TranslationRegistry;

public final class TranslatableComponent extends BaseComponent {
  public void setTranslate(String translate) {
    this.translate = translate;
  }
  
  public String toString() {
    return "TranslatableComponent(format=" + getFormat() + ", translate=" + getTranslate() + ", with=" + getWith() + ")";
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof TranslatableComponent))
      return false; 
    TranslatableComponent other = (TranslatableComponent)o;
    if (!other.canEqual(this))
      return false; 
    if (!super.equals(o))
      return false; 
    Object this$format = getFormat(), other$format = other.getFormat();
    if ((this$format == null) ? (other$format != null) : !this$format.equals(other$format))
      return false; 
    Object this$translate = getTranslate(), other$translate = other.getTranslate();
    if ((this$translate == null) ? (other$translate != null) : !this$translate.equals(other$translate))
      return false; 
    Object<BaseComponent> this$with = (Object<BaseComponent>)getWith(), other$with = (Object<BaseComponent>)other.getWith();
    return !((this$with == null) ? (other$with != null) : !this$with.equals(other$with));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof TranslatableComponent;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = super.hashCode();
    Object $format = getFormat();
    result = result * 59 + (($format == null) ? 43 : $format.hashCode());
    Object $translate = getTranslate();
    result = result * 59 + (($translate == null) ? 43 : $translate.hashCode());
    Object<BaseComponent> $with = (Object<BaseComponent>)getWith();
    return result * 59 + (($with == null) ? 43 : $with.hashCode());
  }
  
  private final Pattern format = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
  
  private String translate;
  
  private List<BaseComponent> with;
  
  public Pattern getFormat() {
    return this.format;
  }
  
  public String getTranslate() {
    return this.translate;
  }
  
  public List<BaseComponent> getWith() {
    return this.with;
  }
  
  public TranslatableComponent(TranslatableComponent original) {
    super(original);
    setTranslate(original.getTranslate());
    if (original.getWith() != null) {
      List<BaseComponent> temp = new ArrayList<>();
      for (BaseComponent baseComponent : original.getWith())
        temp.add(baseComponent.duplicate()); 
      setWith(temp);
    } 
  }
  
  public TranslatableComponent(String translate, Object... with) {
    setTranslate(translate);
    if (with != null && with.length != 0) {
      List<BaseComponent> temp = new ArrayList<>();
      for (Object w : with) {
        if (w instanceof BaseComponent) {
          temp.add((BaseComponent)w);
        } else {
          temp.add(new TextComponent(String.valueOf(w)));
        } 
      } 
      setWith(temp);
    } 
  }
  
  public BaseComponent duplicate() {
    return new TranslatableComponent(this);
  }
  
  public void setWith(List<BaseComponent> components) {
    for (BaseComponent component : components)
      component.parent = this; 
    this.with = components;
  }
  
  public void addWith(String text) {
    addWith(new TextComponent(text));
  }
  
  public void addWith(BaseComponent component) {
    if (this.with == null)
      this.with = new ArrayList<>(); 
    component.parent = this;
    this.with.add(component);
  }
  
  protected void toPlainText(StringBuilder builder) {
    String trans = TranslationRegistry.INSTANCE.translate(this.translate);
    Matcher matcher = this.format.matcher(trans);
    int position = 0;
    int i = 0;
    while (matcher.find(position)) {
      String withIndex;
      int pos = matcher.start();
      if (pos != position)
        builder.append(trans.substring(position, pos)); 
      position = matcher.end();
      String formatCode = matcher.group(2);
      switch (formatCode.charAt(0)) {
        case 'd':
        case 's':
          withIndex = matcher.group(1);
          ((BaseComponent)this.with.get((withIndex != null) ? (Integer.parseInt(withIndex) - 1) : i++)).toPlainText(builder);
        case '%':
          builder.append('%');
      } 
    } 
    if (trans.length() != position)
      builder.append(trans.substring(position, trans.length())); 
    super.toPlainText(builder);
  }
  
  protected void toLegacyText(StringBuilder builder) {
    String trans = TranslationRegistry.INSTANCE.translate(this.translate);
    Matcher matcher = this.format.matcher(trans);
    int position = 0;
    int i = 0;
    while (matcher.find(position)) {
      String withIndex;
      int pos = matcher.start();
      if (pos != position) {
        addFormat(builder);
        builder.append(trans.substring(position, pos));
      } 
      position = matcher.end();
      String formatCode = matcher.group(2);
      switch (formatCode.charAt(0)) {
        case 'd':
        case 's':
          withIndex = matcher.group(1);
          ((BaseComponent)this.with.get((withIndex != null) ? (Integer.parseInt(withIndex) - 1) : i++)).toLegacyText(builder);
        case '%':
          addFormat(builder);
          builder.append('%');
      } 
    } 
    if (trans.length() != position) {
      addFormat(builder);
      builder.append(trans.substring(position, trans.length()));
    } 
    super.toLegacyText(builder);
  }
  
  private void addFormat(StringBuilder builder) {
    builder.append(getColor());
    if (isBold())
      builder.append(ChatColor.BOLD); 
    if (isItalic())
      builder.append(ChatColor.ITALIC); 
    if (isUnderlined())
      builder.append(ChatColor.UNDERLINE); 
    if (isStrikethrough())
      builder.append(ChatColor.STRIKETHROUGH); 
    if (isObfuscated())
      builder.append(ChatColor.MAGIC); 
  }
  
  public TranslatableComponent() {}
}
