package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

@Deprecated
public class SlicedByteBuf extends AbstractUnpooledSlicedByteBuf {
  private int length;
  
  public SlicedByteBuf(ByteBuf buffer, int index, int length) {
    super(buffer, index, length);
  }
  
  final void initLength(int length) {
    this.length = length;
  }
  
  final int length() {
    return this.length;
  }
  
  public int capacity() {
    return this.length;
  }
}
