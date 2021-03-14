package com.github.steveice10.netty.channel.unix;

public final class Limits {
  public static final int IOV_MAX = LimitsStaticallyReferencedJniMethods.iovMax();
  
  public static final int UIO_MAX_IOV = LimitsStaticallyReferencedJniMethods.uioMaxIov();
  
  public static final long SSIZE_MAX = LimitsStaticallyReferencedJniMethods.ssizeMax();
  
  public static final int SIZEOF_JLONG = LimitsStaticallyReferencedJniMethods.sizeOfjlong();
}
