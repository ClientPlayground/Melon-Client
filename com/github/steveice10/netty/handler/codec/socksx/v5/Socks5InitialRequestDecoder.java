package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.handler.codec.socksx.SocksVersion;
import java.util.List;

public class Socks5InitialRequestDecoder extends ReplayingDecoder<Socks5InitialRequestDecoder.State> {
  enum State {
    INIT, SUCCESS, FAILURE;
  }
  
  public Socks5InitialRequestDecoder() {
    super(State.INIT);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      byte version;
      int readableBytes;
      int authMethodCnt;
      Socks5AuthMethod[] authMethods;
      int i;
      switch ((State)state()) {
        case INIT:
          version = in.readByte();
          if (version != SocksVersion.SOCKS5.byteValue())
            throw new DecoderException("unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5
                .byteValue() + ')'); 
          authMethodCnt = in.readUnsignedByte();
          if (actualReadableBytes() < authMethodCnt)
            break; 
          authMethods = new Socks5AuthMethod[authMethodCnt];
          for (i = 0; i < authMethodCnt; i++)
            authMethods[i] = Socks5AuthMethod.valueOf(in.readByte()); 
          out.add(new DefaultSocks5InitialRequest(authMethods));
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
    Socks5Message m = new DefaultSocks5InitialRequest(new Socks5AuthMethod[] { Socks5AuthMethod.NO_AUTH });
    m.setDecoderResult(DecoderResult.failure((Throwable)decoderException));
    out.add(m);
  }
}
