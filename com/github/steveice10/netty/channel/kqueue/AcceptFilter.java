package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.util.internal.ObjectUtil;

public final class AcceptFilter {
  static final AcceptFilter PLATFORM_UNSUPPORTED = new AcceptFilter("", "");
  
  private final String filterName;
  
  private final String filterArgs;
  
  public AcceptFilter(String filterName, String filterArgs) {
    this.filterName = (String)ObjectUtil.checkNotNull(filterName, "filterName");
    this.filterArgs = (String)ObjectUtil.checkNotNull(filterArgs, "filterArgs");
  }
  
  public String filterName() {
    return this.filterName;
  }
  
  public String filterArgs() {
    return this.filterArgs;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof AcceptFilter))
      return false; 
    AcceptFilter rhs = (AcceptFilter)o;
    return (this.filterName.equals(rhs.filterName) && this.filterArgs.equals(rhs.filterArgs));
  }
  
  public int hashCode() {
    return 31 * (31 + this.filterName.hashCode()) + this.filterArgs.hashCode();
  }
  
  public String toString() {
    return this.filterName + ", " + this.filterArgs;
  }
}
