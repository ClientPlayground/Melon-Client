package com.replaymod.replaystudio.us.myles.ViaVersion.api;

public class Triple<A, B, C> {
  private A first;
  
  private B second;
  
  private C third;
  
  public void setFirst(A first) {
    this.first = first;
  }
  
  public void setSecond(B second) {
    this.second = second;
  }
  
  public void setThird(C third) {
    this.third = third;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Triple))
      return false; 
    Triple<?, ?, ?> other = (Triple<?, ?, ?>)o;
    if (!other.canEqual(this))
      return false; 
    Object this$first = getFirst(), other$first = other.getFirst();
    if ((this$first == null) ? (other$first != null) : !this$first.equals(other$first))
      return false; 
    Object this$second = getSecond(), other$second = other.getSecond();
    if ((this$second == null) ? (other$second != null) : !this$second.equals(other$second))
      return false; 
    Object this$third = getThird(), other$third = other.getThird();
    return !((this$third == null) ? (other$third != null) : !this$third.equals(other$third));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Triple;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    Object $first = getFirst();
    result = result * 59 + (($first == null) ? 43 : $first.hashCode());
    Object $second = getSecond();
    result = result * 59 + (($second == null) ? 43 : $second.hashCode());
    Object $third = getThird();
    return result * 59 + (($third == null) ? 43 : $third.hashCode());
  }
  
  public Triple(A first, B second, C third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }
  
  public A getFirst() {
    return this.first;
  }
  
  public B getSecond() {
    return this.second;
  }
  
  public C getThird() {
    return this.third;
  }
  
  public String toString() {
    return "Triple{" + this.first + ", " + this.second + ", " + this.third + '}';
  }
}
