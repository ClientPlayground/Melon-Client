package net.md_5.bungee.api.chat;

import net.md_5.bungee.api.ChatColor;

public final class KeybindComponent extends BaseComponent {
  private String keybind;
  
  public void setKeybind(String keybind) {
    this.keybind = keybind;
  }
  
  public String toString() {
    return "KeybindComponent(keybind=" + getKeybind() + ")";
  }
  
  public KeybindComponent() {}
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof KeybindComponent))
      return false; 
    KeybindComponent other = (KeybindComponent)o;
    if (!other.canEqual(this))
      return false; 
    if (!super.equals(o))
      return false; 
    Object this$keybind = getKeybind(), other$keybind = other.getKeybind();
    return !((this$keybind == null) ? (other$keybind != null) : !this$keybind.equals(other$keybind));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof KeybindComponent;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = super.hashCode();
    Object $keybind = getKeybind();
    return result * 59 + (($keybind == null) ? 43 : $keybind.hashCode());
  }
  
  public String getKeybind() {
    return this.keybind;
  }
  
  public KeybindComponent(KeybindComponent original) {
    super(original);
    setKeybind(original.getKeybind());
  }
  
  public KeybindComponent(String keybind) {
    setKeybind(keybind);
  }
  
  public BaseComponent duplicate() {
    return new KeybindComponent(this);
  }
  
  protected void toPlainText(StringBuilder builder) {
    builder.append(getKeybind());
    super.toPlainText(builder);
  }
  
  protected void toLegacyText(StringBuilder builder) {
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
    builder.append(getKeybind());
    super.toLegacyText(builder);
  }
}
