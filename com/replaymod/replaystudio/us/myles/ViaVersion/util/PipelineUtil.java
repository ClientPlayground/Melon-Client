package com.replaymod.replaystudio.us.myles.ViaVersion.util;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PipelineUtil {
  private static Method DECODE_METHOD;
  
  private static Method ENCODE_METHOD;
  
  private static Method MTM_DECODE;
  
  static {
    try {
      DECODE_METHOD = ByteToMessageDecoder.class.getDeclaredMethod("decode", new Class[] { ChannelHandlerContext.class, ByteBuf.class, List.class });
      DECODE_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } 
    try {
      ENCODE_METHOD = MessageToByteEncoder.class.getDeclaredMethod("encode", new Class[] { ChannelHandlerContext.class, Object.class, ByteBuf.class });
      ENCODE_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } 
    try {
      MTM_DECODE = MessageToMessageDecoder.class.getDeclaredMethod("decode", new Class[] { ChannelHandlerContext.class, Object.class, List.class });
      MTM_DECODE.setAccessible(true);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } 
  }
  
  public static List<Object> callDecode(ByteToMessageDecoder decoder, ChannelHandlerContext ctx, Object input) throws InvocationTargetException {
    List<Object> output = new ArrayList();
    try {
      DECODE_METHOD.invoke(decoder, new Object[] { ctx, input, output });
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } 
    return output;
  }
  
  public static void callEncode(MessageToByteEncoder encoder, ChannelHandlerContext ctx, Object msg, ByteBuf output) throws InvocationTargetException {
    try {
      ENCODE_METHOD.invoke(encoder, new Object[] { ctx, msg, output });
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } 
  }
  
  public static List<Object> callDecode(MessageToMessageDecoder decoder, ChannelHandlerContext ctx, Object msg) throws InvocationTargetException {
    List<Object> output = new ArrayList();
    try {
      MTM_DECODE.invoke(decoder, new Object[] { ctx, msg, output });
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } 
    return output;
  }
  
  public static boolean containsCause(Throwable t, Class<? extends Throwable> c) {
    while (true) {
      if (t != null) {
        if (c.isAssignableFrom(t.getClass()))
          return true; 
        t = t.getCause();
      } 
      if (t == null)
        return false; 
    } 
  }
  
  public static ChannelHandlerContext getContextBefore(String name, ChannelPipeline pipeline) {
    boolean mark = false;
    for (String s : pipeline.names()) {
      if (mark)
        return pipeline.context(pipeline.get(s)); 
      if (s.equalsIgnoreCase(name))
        mark = true; 
    } 
    return null;
  }
  
  public static ChannelHandlerContext getPreviousContext(String name, ChannelPipeline pipeline) {
    String previous = null;
    for (String entry : pipeline.toMap().keySet()) {
      if (entry.equals(name))
        return pipeline.context(previous); 
      previous = entry;
    } 
    return null;
  }
}
