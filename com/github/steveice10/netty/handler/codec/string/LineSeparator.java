package com.github.steveice10.netty.handler.codec.string;

import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

public final class LineSeparator {
  public static final LineSeparator DEFAULT = new LineSeparator(StringUtil.NEWLINE);
  
  public static final LineSeparator UNIX = new LineSeparator("\n");
  
  public static final LineSeparator WINDOWS = new LineSeparator("\r\n");
  
  private final String value;
  
  public LineSeparator(String lineSeparator) {
    this.value = (String)ObjectUtil.checkNotNull(lineSeparator, "lineSeparator");
  }
  
  public String value() {
    return this.value;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (!(o instanceof LineSeparator))
      return false; 
    LineSeparator that = (LineSeparator)o;
    return (this.value != null) ? this.value.equals(that.value) : ((that.value == null));
  }
  
  public int hashCode() {
    return (this.value != null) ? this.value.hashCode() : 0;
  }
  
  public String toString() {
    return ByteBufUtil.hexDump(this.value.getBytes(CharsetUtil.UTF_8));
  }
}
