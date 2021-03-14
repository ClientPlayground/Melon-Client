package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.handler.codec.socksx.SocksVersion;
import java.util.List;

public class Socks5CommandRequestDecoder extends ReplayingDecoder<Socks5CommandRequestDecoder.State> {
  private final Socks5AddressDecoder addressDecoder;
  
  enum State {
    INIT, SUCCESS, FAILURE;
  }
  
  public Socks5CommandRequestDecoder() {
    this(Socks5AddressDecoder.DEFAULT);
  }
  
  public Socks5CommandRequestDecoder(Socks5AddressDecoder addressDecoder) {
    super(State.INIT);
    if (addressDecoder == null)
      throw new NullPointerException("addressDecoder"); 
    this.addressDecoder = addressDecoder;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      byte version;
      int readableBytes;
      Socks5CommandType type;
      Socks5AddressType dstAddrType;
      String dstAddr;
      int dstPort;
      switch ((State)state()) {
        case INIT:
          version = in.readByte();
          if (version != SocksVersion.SOCKS5.byteValue())
            throw new DecoderException("unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5
                .byteValue() + ')'); 
          type = Socks5CommandType.valueOf(in.readByte());
          in.skipBytes(1);
          dstAddrType = Socks5AddressType.valueOf(in.readByte());
          dstAddr = this.addressDecoder.decodeAddress(dstAddrType, in);
          dstPort = in.readUnsignedShort();
          out.add(new DefaultSocks5CommandRequest(type, dstAddrType, dstAddr, dstPort));
          checkpoint(State.SUCCESS);
        case SUCCESS:
          readableBytes = actualReadableBytes();
          if (readableBytes > 0)
            out.add(in.readRetainedSlice(readableBytes)); 
          break;
        case FAILURE:
          in.skipBytes(actualReadableBytes());
          break;
      } 
    } catch (Exception e) {
      fail(out, e);
    } 
  }
  
  private void fail(List<Object> out, Exception cause) {
    DecoderException decoderException;
    if (!(cause instanceof DecoderException))
      decoderException = new DecoderException(cause); 
    checkpoint(State.FAILURE);
    Socks5Message m = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, Socks5AddressType.IPv4, "0.0.0.0", 1);
    m.setDecoderResult(DecoderResult.failure((Throwable)decoderException));
    out.add(m);
  }
}
