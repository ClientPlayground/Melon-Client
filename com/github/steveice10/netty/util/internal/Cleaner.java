package com.github.steveice10.netty.util.internal;

import java.nio.ByteBuffer;

interface Cleaner {
  void freeDirectBuffer(ByteBuffer paramByteBuffer);
}
