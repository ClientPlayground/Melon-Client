package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.handler.codec.socksx.SocksVersion;
import java.util.List;

public class Socks5CommandResponseDecoder extends ReplayingDecoder<Socks5CommandResponseDecoder.State> {
  private final Socks5AddressDecoder addressDecoder;
  
  enum State {
    INIT, SUCCESS, FAILURE;
  }
  
  public Socks5CommandResponseDecoder() {
    this(Socks5AddressDecoder.DEFAULT);
  }
  
  public Socks5CommandResponseDecoder(Socks5AddressDecoder addressDecoder) {
    super(State.INIT);
    if (addressDecoder == null)
      throw new NullPointerException("addressDecoder"); 
    this.addressDecoder = addressDecoder;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      byte version;
      int readableBytes;
      Socks5CommandStatus status;
      Socks5AddressType addrType;
      String addr;
      int port;
      switch ((State)state()) {
        case INIT:
          version = in.readByte();
          if (version != SocksVersion.SOCKS5.byteValue())
            throw new DecoderException("unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5
                .byteValue() + ')'); 
          status = Socks5CommandStatus.valueOf(in.readByte());
          in.skipBytes(1);
          addrType = Socks5AddressType.valueOf(in.readByte());
          addr = this.addressDecoder.decodeAddress(addrType, in);
          port = in.readUnsignedShort();
          out.add(new DefaultSocks5CommandResponse(status, addrType, addr, port));
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
    Socks5Message m = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4, null, 0);
    m.setDecoderResult(DecoderResult.failure((Throwable)decoderException));
    out.add(m);
  }
}
