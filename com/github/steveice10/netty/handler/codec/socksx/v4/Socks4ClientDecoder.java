package com.github.steveice10.netty.handler.codec.socksx.v4;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.util.NetUtil;
import java.util.List;

public class Socks4ClientDecoder extends ReplayingDecoder<Socks4ClientDecoder.State> {
  enum State {
    START, SUCCESS, FAILURE;
  }
  
  public Socks4ClientDecoder() {
    super(State.START);
    setSingleDecode(true);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      int version;
      int readableBytes;
      Socks4CommandStatus status;
      int dstPort;
      String dstAddr;
      switch ((State)state()) {
        case START:
          version = in.readUnsignedByte();
          if (version != 0)
            throw new DecoderException("unsupported reply version: " + version + " (expected: 0)"); 
          status = Socks4CommandStatus.valueOf(in.readByte());
          dstPort = in.readUnsignedShort();
          dstAddr = NetUtil.intToIpAddress(in.readInt());
          out.add(new DefaultSocks4CommandResponse(status, dstAddr, dstPort));
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
    Socks4CommandResponse m = new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED);
    m.setDecoderResult(DecoderResult.failure((Throwable)decoderException));
    out.add(m);
    checkpoint(State.FAILURE);
  }
}
