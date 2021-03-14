package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.socket.DatagramPacket;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;

public class DatagramPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
  private final MessageToMessageDecoder<ByteBuf> decoder;
  
  public DatagramPacketDecoder(MessageToMessageDecoder<ByteBuf> decoder) {
    this.decoder = (MessageToMessageDecoder<ByteBuf>)ObjectUtil.checkNotNull(decoder, "decoder");
  }
  
  public boolean acceptInboundMessage(Object msg) throws Exception {
    if (msg instanceof DatagramPacket)
      return this.decoder.acceptInboundMessage(((DatagramPacket)msg).content()); 
    return false;
  }
  
  protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
    this.decoder.decode(ctx, msg.content(), out);
  }
  
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelRegistered(ctx);
  }
  
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelUnregistered(ctx);
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelActive(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelInactive(ctx);
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelReadComplete(ctx);
  }
  
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    this.decoder.userEventTriggered(ctx, evt);
  }
  
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelWritabilityChanged(ctx);
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    this.decoder.exceptionCaught(ctx, cause);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.decoder.handlerAdded(ctx);
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    this.decoder.handlerRemoved(ctx);
  }
  
  public boolean isSharable() {
    return this.decoder.isSharable();
  }
}
