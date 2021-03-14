package com.github.steveice10.netty.channel.unix;

final class LimitsStaticallyReferencedJniMethods {
  static native long ssizeMax();
  
  static native int iovMax();
  
  static native int uioMaxIov();
  
  static native int sizeOfjlong();
  
  static native int udsSunPathSize();
}
