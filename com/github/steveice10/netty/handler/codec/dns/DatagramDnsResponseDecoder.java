package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.socket.DatagramPacket;
import com.github.steveice10.netty.handler.codec.CorruptedFrameException;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;
import java.util.List;

@Sharable
public class DatagramDnsResponseDecoder extends MessageToMessageDecoder<DatagramPacket> {
  private final DnsRecordDecoder recordDecoder;
  
  public DatagramDnsResponseDecoder() {
    this(DnsRecordDecoder.DEFAULT);
  }
  
  public DatagramDnsResponseDecoder(DnsRecordDecoder recordDecoder) {
    this.recordDecoder = (DnsRecordDecoder)ObjectUtil.checkNotNull(recordDecoder, "recordDecoder");
  }
  
  protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
    ByteBuf buf = (ByteBuf)packet.content();
    DnsResponse response = newResponse(packet, buf);
    boolean success = false;
    try {
      int questionCount = buf.readUnsignedShort();
      int answerCount = buf.readUnsignedShort();
      int authorityRecordCount = buf.readUnsignedShort();
      int additionalRecordCount = buf.readUnsignedShort();
      decodeQuestions(response, buf, questionCount);
      decodeRecords(response, DnsSection.ANSWER, buf, answerCount);
      decodeRecords(response, DnsSection.AUTHORITY, buf, authorityRecordCount);
      decodeRecords(response, DnsSection.ADDITIONAL, buf, additionalRecordCount);
      out.add(response);
      success = true;
    } finally {
      if (!success)
        response.release(); 
    } 
  }
  
  private static DnsResponse newResponse(DatagramPacket packet, ByteBuf buf) {
    int id = buf.readUnsignedShort();
    int flags = buf.readUnsignedShort();
    if (flags >> 15 == 0)
      throw new CorruptedFrameException("not a response"); 
    DnsResponse response = new DatagramDnsResponse((InetSocketAddress)packet.sender(), (InetSocketAddress)packet.recipient(), id, DnsOpCode.valueOf((byte)(flags >> 11 & 0xF)), DnsResponseCode.valueOf((byte)(flags & 0xF)));
    response.setRecursionDesired(((flags >> 8 & 0x1) == 1));
    response.setAuthoritativeAnswer(((flags >> 10 & 0x1) == 1));
    response.setTruncated(((flags >> 9 & 0x1) == 1));
    response.setRecursionAvailable(((flags >> 7 & 0x1) == 1));
    response.setZ(flags >> 4 & 0x7);
    return response;
  }
  
  private void decodeQuestions(DnsResponse response, ByteBuf buf, int questionCount) throws Exception {
    for (int i = questionCount; i > 0; i--)
      response.addRecord(DnsSection.QUESTION, this.recordDecoder.decodeQuestion(buf)); 
  }
  
  private void decodeRecords(DnsResponse response, DnsSection section, ByteBuf buf, int count) throws Exception {
    for (int i = count; i > 0; i--) {
      DnsRecord r = this.recordDecoder.decodeRecord(buf);
      if (r == null)
        break; 
      response.addRecord(section, r);
    } 
  }
}
