package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;

final class CompressionUtil {
  static void checkChecksum(ByteBufChecksum checksum, ByteBuf uncompressed, int currentChecksum) {
    checksum.reset();
    checksum.update(uncompressed, uncompressed
        .readerIndex(), uncompressed.readableBytes());
    int checksumResult = (int)checksum.getValue();
    if (checksumResult != currentChecksum)
      throw new DecompressionException(String.format("stream corrupted: mismatching checksum: %d (expected: %d)", new Object[] { Integer.valueOf(checksumResult), Integer.valueOf(currentChecksum) })); 
  }
  
  static ByteBuffer safeNioBuffer(ByteBuf buffer) {
    return (buffer.nioBufferCount() == 1) ? buffer.internalNioBuffer(buffer.readerIndex(), buffer.readableBytes()) : buffer
      .nioBuffer();
  }
}
