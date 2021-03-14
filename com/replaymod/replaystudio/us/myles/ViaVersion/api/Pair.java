package com.replaymod.replaystudio.us.myles.ViaVersion.api;

public class Pair<X, Y> {
  private X key;
  
  private Y value;
  
  public void setKey(X key) {
    this.key = key;
  }
  
  public void setValue(Y value) {
    this.value = value;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Pair))
      return false; 
    Pair<?, ?> other = (Pair<?, ?>)o;
    if (!other.canEqual(this))
      return false; 
    Object this$key = getKey(), other$key = other.getKey();
    if ((this$key == null) ? (other$key != null) : !this$key.equals(other$key))
      return false; 
    Object this$value = getValue(), other$value = other.getValue();
    return !((this$value == null) ? (other$value != null) : !this$value.equals(other$value));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Pair;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    Object $key = getKey();
    result = result * 59 + (($key == null) ? 43 : $key.hashCode());
    Object $value = getValue();
    return result * 59 + (($value == null) ? 43 : $value.hashCode());
  }
  
  public X getKey() {
    return this.key;
  }
  
  public Y getValue() {
    return this.value;
  }
  
  public Pair(X key, Y value) {
    this.key = key;
    this.value = value;
  }
  
  public String toString() {
    return "Pair{" + this.key + ", " + this.value + '}';
  }
}
