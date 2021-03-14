package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.util.CharsetUtil;
import java.util.List;

public class Socks5PasswordAuthRequestDecoder extends ReplayingDecoder<Socks5PasswordAuthRequestDecoder.State> {
  enum State {
    INIT, SUCCESS, FAILURE;
  }
  
  public Socks5PasswordAuthRequestDecoder() {
    super(State.INIT);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      int startOffset;
      int readableBytes;
      byte version;
      int usernameLength;
      int passwordLength;
      int totalLength;
      switch ((State)state()) {
        case INIT:
          startOffset = in.readerIndex();
          version = in.getByte(startOffset);
          if (version != 1)
            throw new DecoderException("unsupported subnegotiation version: " + version + " (expected: 1)"); 
          usernameLength = in.getUnsignedByte(startOffset + 1);
          passwordLength = in.getUnsignedByte(startOffset + 2 + usernameLength);
          totalLength = usernameLength + passwordLength + 3;
          in.skipBytes(totalLength);
          out.add(new DefaultSocks5PasswordAuthRequest(in
                .toString(startOffset + 2, usernameLength, CharsetUtil.US_ASCII), in
                .toString(startOffset + 3 + usernameLength, passwordLength, CharsetUtil.US_ASCII)));
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
    Socks5Message m = new DefaultSocks5PasswordAuthRequest("", "");
    m.setDecoderResult(DecoderResult.failure((Throwable)decoderException));
    out.add(m);
  }
}
