package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.base64.Base64;
import com.github.steveice10.netty.handler.codec.base64.Base64Dialect;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLHandshakeException;

final class SslUtils {
  static final String PROTOCOL_SSL_V2_HELLO = "SSLv2Hello";
  
  static final String PROTOCOL_SSL_V2 = "SSLv2";
  
  static final String PROTOCOL_SSL_V3 = "SSLv3";
  
  static final String PROTOCOL_TLS_V1 = "TLSv1";
  
  static final String PROTOCOL_TLS_V1_1 = "TLSv1.1";
  
  static final String PROTOCOL_TLS_V1_2 = "TLSv1.2";
  
  static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;
  
  static final int SSL_CONTENT_TYPE_ALERT = 21;
  
  static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;
  
  static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;
  
  static final int SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24;
  
  static final int SSL_RECORD_HEADER_LENGTH = 5;
  
  static final int NOT_ENOUGH_DATA = -1;
  
  static final int NOT_ENCRYPTED = -2;
  
  static final String[] DEFAULT_CIPHER_SUITES = new String[] { "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA" };
  
  static void addIfSupported(Set<String> supported, List<String> enabled, String... names) {
    for (String n : names) {
      if (supported.contains(n))
        enabled.add(n); 
    } 
  }
  
  static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, Iterable<String> fallbackCiphers) {
    if (defaultCiphers.isEmpty())
      for (String cipher : fallbackCiphers) {
        if (cipher.startsWith("SSL_") || cipher.contains("_RC4_"))
          continue; 
        defaultCiphers.add(cipher);
      }  
  }
  
  static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, String... fallbackCiphers) {
    useFallbackCiphersIfDefaultIsEmpty(defaultCiphers, Arrays.asList(fallbackCiphers));
  }
  
  static SSLHandshakeException toSSLHandshakeException(Throwable e) {
    if (e instanceof SSLHandshakeException)
      return (SSLHandshakeException)e; 
    return (SSLHandshakeException)(new SSLHandshakeException(e.getMessage())).initCause(e);
  }
  
  static int getEncryptedPacketLength(ByteBuf buffer, int offset) {
    boolean tls;
    int packetLength = 0;
    switch (buffer.getUnsignedByte(offset)) {
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
        tls = true;
        break;
      default:
        tls = false;
        break;
    } 
    if (tls) {
      int majorVersion = buffer.getUnsignedByte(offset + 1);
      if (majorVersion == 3) {
        packetLength = unsignedShortBE(buffer, offset + 3) + 5;
        if (packetLength <= 5)
          tls = false; 
      } else {
        tls = false;
      } 
    } 
    if (!tls) {
      int headerLength = ((buffer.getUnsignedByte(offset) & 0x80) != 0) ? 2 : 3;
      int majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
      if (majorVersion == 2 || majorVersion == 3) {
        packetLength = (headerLength == 2) ? ((shortBE(buffer, offset) & Short.MAX_VALUE) + 2) : ((shortBE(buffer, offset) & 0x3FFF) + 3);
        if (packetLength <= headerLength)
          return -1; 
      } else {
        return -2;
      } 
    } 
    return packetLength;
  }
  
  private static int unsignedShortBE(ByteBuf buffer, int offset) {
    return (buffer.order() == ByteOrder.BIG_ENDIAN) ? buffer
      .getUnsignedShort(offset) : buffer.getUnsignedShortLE(offset);
  }
  
  private static short shortBE(ByteBuf buffer, int offset) {
    return (buffer.order() == ByteOrder.BIG_ENDIAN) ? buffer
      .getShort(offset) : buffer.getShortLE(offset);
  }
  
  private static short unsignedByte(byte b) {
    return (short)(b & 0xFF);
  }
  
  private static int unsignedShortBE(ByteBuffer buffer, int offset) {
    return shortBE(buffer, offset) & 0xFFFF;
  }
  
  private static short shortBE(ByteBuffer buffer, int offset) {
    return (buffer.order() == ByteOrder.BIG_ENDIAN) ? buffer
      .getShort(offset) : ByteBufUtil.swapShort(buffer.getShort(offset));
  }
  
  static int getEncryptedPacketLength(ByteBuffer[] buffers, int offset) {
    ByteBuffer buffer = buffers[offset];
    if (buffer.remaining() >= 5)
      return getEncryptedPacketLength(buffer); 
    ByteBuffer tmp = ByteBuffer.allocate(5);
    do {
      buffer = buffers[offset++].duplicate();
      if (buffer.remaining() > tmp.remaining())
        buffer.limit(buffer.position() + tmp.remaining()); 
      tmp.put(buffer);
    } while (tmp.hasRemaining());
    tmp.flip();
    return getEncryptedPacketLength(tmp);
  }
  
  private static int getEncryptedPacketLength(ByteBuffer buffer) {
    boolean tls;
    int packetLength = 0;
    int pos = buffer.position();
    switch (unsignedByte(buffer.get(pos))) {
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
        tls = true;
        break;
      default:
        tls = false;
        break;
    } 
    if (tls) {
      int majorVersion = unsignedByte(buffer.get(pos + 1));
      if (majorVersion == 3) {
        packetLength = unsignedShortBE(buffer, pos + 3) + 5;
        if (packetLength <= 5)
          tls = false; 
      } else {
        tls = false;
      } 
    } 
    if (!tls) {
      int headerLength = ((unsignedByte(buffer.get(pos)) & 0x80) != 0) ? 2 : 3;
      int majorVersion = unsignedByte(buffer.get(pos + headerLength + 1));
      if (majorVersion == 2 || majorVersion == 3) {
        packetLength = (headerLength == 2) ? ((shortBE(buffer, pos) & Short.MAX_VALUE) + 2) : ((shortBE(buffer, pos) & 0x3FFF) + 3);
        if (packetLength <= headerLength)
          return -1; 
      } else {
        return -2;
      } 
    } 
    return packetLength;
  }
  
  static void handleHandshakeFailure(ChannelHandlerContext ctx, Throwable cause, boolean notify) {
    ctx.flush();
    if (notify)
      ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause)); 
    ctx.close();
  }
  
  static void zeroout(ByteBuf buffer) {
    if (!buffer.isReadOnly())
      buffer.setZero(0, buffer.capacity()); 
  }
  
  static void zerooutAndRelease(ByteBuf buffer) {
    zeroout(buffer);
    buffer.release();
  }
  
  static ByteBuf toBase64(ByteBufAllocator allocator, ByteBuf src) {
    ByteBuf dst = Base64.encode(src, src.readerIndex(), src
        .readableBytes(), true, Base64Dialect.STANDARD, allocator);
    src.readerIndex(src.writerIndex());
    return dst;
  }
}
