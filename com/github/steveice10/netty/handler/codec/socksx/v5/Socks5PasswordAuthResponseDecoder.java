package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import java.util.List;

public class Socks5PasswordAuthResponseDecoder extends ReplayingDecoder<Socks5PasswordAuthResponseDecoder.State> {
  enum State {
    INIT, SUCCESS, FAILURE;
  }
  
  public Socks5PasswordAuthResponseDecoder() {
    super(State.INIT);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      byte version;
      int readableBytes;
      switch ((State)state()) {
        case INIT:
          version = in.readByte();
          if (version != 1)
            throw new DecoderException("unsupported subnegotiation version: " + version + " (expected: 1)"); 
          out.add(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.valueOf(in.readByte())));
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
    Socks5Message m = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
    m.setDecoderResult(DecoderResult.failure((Throwable)decoderException));
    out.add(m);
  }
}
