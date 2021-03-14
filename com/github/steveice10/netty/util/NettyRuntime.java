package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import java.util.Locale;

public final class NettyRuntime {
  static class AvailableProcessorsHolder {
    private int availableProcessors;
    
    synchronized void setAvailableProcessors(int availableProcessors) {
      ObjectUtil.checkPositive(availableProcessors, "availableProcessors");
      if (this.availableProcessors != 0) {
        String message = String.format(Locale.ROOT, "availableProcessors is already set to [%d], rejecting [%d]", new Object[] { Integer.valueOf(this.availableProcessors), 
              Integer.valueOf(availableProcessors) });
        throw new IllegalStateException(message);
      } 
      this.availableProcessors = availableProcessors;
    }
    
    @SuppressForbidden(reason = "to obtain default number of available processors")
    synchronized int availableProcessors() {
      if (this.availableProcessors == 0) {
        int availableProcessors = SystemPropertyUtil.getInt("com.github.steveice10.netty.availableProcessors", 
            
            Runtime.getRuntime().availableProcessors());
        setAvailableProcessors(availableProcessors);
      } 
      return this.availableProcessors;
    }
  }
  
  private static final AvailableProcessorsHolder holder = new AvailableProcessorsHolder();
  
  public static void setAvailableProcessors(int availableProcessors) {
    holder.setAvailableProcessors(availableProcessors);
  }
  
  public static int availableProcessors() {
    return holder.availableProcessors();
  }
}
