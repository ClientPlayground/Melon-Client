package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.base64.Base64;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class WebSocketUtil {
  private static final FastThreadLocal<MessageDigest> MD5 = new FastThreadLocal<MessageDigest>() {
      protected MessageDigest initialValue() throws Exception {
        try {
          return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
          throw new InternalError("MD5 not supported on this platform - Outdated?");
        } 
      }
    };
  
  private static final FastThreadLocal<MessageDigest> SHA1 = new FastThreadLocal<MessageDigest>() {
      protected MessageDigest initialValue() throws Exception {
        try {
          return MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
          throw new InternalError("SHA-1 not supported on this platform - Outdated?");
        } 
      }
    };
  
  static byte[] md5(byte[] data) {
    return digest(MD5, data);
  }
  
  static byte[] sha1(byte[] data) {
    return digest(SHA1, data);
  }
  
  private static byte[] digest(FastThreadLocal<MessageDigest> digestFastThreadLocal, byte[] data) {
    MessageDigest digest = (MessageDigest)digestFastThreadLocal.get();
    digest.reset();
    return digest.digest(data);
  }
  
  static String base64(byte[] data) {
    ByteBuf encodedData = Unpooled.wrappedBuffer(data);
    ByteBuf encoded = Base64.encode(encodedData);
    String encodedString = encoded.toString(CharsetUtil.UTF_8);
    encoded.release();
    return encodedString;
  }
  
  static byte[] randomBytes(int size) {
    byte[] bytes = new byte[size];
    for (int index = 0; index < size; index++)
      bytes[index] = (byte)randomNumber(0, 255); 
    return bytes;
  }
  
  static int randomNumber(int minimum, int maximum) {
    return (int)(Math.random() * maximum + minimum);
  }
}
