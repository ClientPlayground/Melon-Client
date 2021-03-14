package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.annotations.SerializedName;

public class Item {
  @SerializedName(value = "identifier", alternate = {"id"})
  private int identifier;
  
  private byte amount;
  
  private short data;
  
  private CompoundTag tag;
  
  public void setIdentifier(int identifier) {
    this.identifier = identifier;
  }
  
  public void setAmount(byte amount) {
    this.amount = amount;
  }
  
  public void setData(short data) {
    this.data = data;
  }
  
  public void setTag(CompoundTag tag) {
    this.tag = tag;
  }
  
  public Item() {}
  
  public Item(int identifier, byte amount, short data, CompoundTag tag) {
    this.identifier = identifier;
    this.amount = amount;
    this.data = data;
    this.tag = tag;
  }
  
  public String toString() {
    return "Item(identifier=" + getIdentifier() + ", amount=" + getAmount() + ", data=" + getData() + ", tag=" + getTag() + ")";
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Item))
      return false; 
    Item other = (Item)o;
    if (!other.canEqual(this))
      return false; 
    if (getIdentifier() != other.getIdentifier())
      return false; 
    if (getAmount() != other.getAmount())
      return false; 
    if (getData() != other.getData())
      return false; 
    Object this$tag = getTag(), other$tag = other.getTag();
    return !((this$tag == null) ? (other$tag != null) : !this$tag.equals(other$tag));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Item;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + getIdentifier();
    result = result * 59 + getAmount();
    result = result * 59 + getData();
    Object $tag = getTag();
    return result * 59 + (($tag == null) ? 43 : $tag.hashCode());
  }
  
  public int getIdentifier() {
    return this.identifier;
  }
  
  public byte getAmount() {
    return this.amount;
  }
  
  public short getData() {
    return this.data;
  }
  
  public CompoundTag getTag() {
    return this.tag;
  }
  
  @Deprecated
  public short getId() {
    return (short)this.identifier;
  }
  
  @Deprecated
  public void setId(short id) {
    this.identifier = id;
  }
  
  @Deprecated
  public Item(short id, byte amount, short data, CompoundTag tag) {
    this.identifier = id;
    this.amount = amount;
    this.data = data;
    this.tag = tag;
  }
}
