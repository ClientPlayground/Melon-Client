package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;

public abstract class ZlibDecoder extends ByteToMessageDecoder {
  public abstract boolean isClosed();
}
