package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.handler.codec.socksx.SocksVersion;
import java.util.List;

public class Socks5InitialResponseDecoder extends ReplayingDecoder<Socks5InitialResponseDecoder.State> {
  enum State {
    INIT, SUCCESS, FAILURE;
  }
  
  public Socks5InitialResponseDecoder() {
    super(State.INIT);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      byte version;
      int readableBytes;
      Socks5AuthMethod authMethod;
      switch ((State)state()) {
        case INIT:
          version = in.readByte();
          if (version != SocksVersion.SOCKS5.byteValue())
            throw new DecoderException("unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5
                .byteValue() + ')'); 
          authMethod = Socks5AuthMethod.valueOf(in.readByte());
          out.add(new DefaultSocks5InitialResponse(authMethod));
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
    Socks5Message m = new DefaultSocks5InitialResponse(Socks5AuthMethod.UNACCEPTED);
    m.setDecoderResult(DecoderResult.failure((Throwable)decoderException));
    out.add(m);
  }
}
