package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.CorruptedFrameException;
import com.github.steveice10.netty.util.CharsetUtil;

public class DefaultDnsRecordDecoder implements DnsRecordDecoder {
  static final String ROOT = ".";
  
  public final DnsQuestion decodeQuestion(ByteBuf in) throws Exception {
    String name = decodeName(in);
    DnsRecordType type = DnsRecordType.valueOf(in.readUnsignedShort());
    int qClass = in.readUnsignedShort();
    return new DefaultDnsQuestion(name, type, qClass);
  }
  
  public final <T extends DnsRecord> T decodeRecord(ByteBuf in) throws Exception {
    int startOffset = in.readerIndex();
    String name = decodeName(in);
    int endOffset = in.writerIndex();
    if (endOffset - startOffset < 10) {
      in.readerIndex(startOffset);
      return null;
    } 
    DnsRecordType type = DnsRecordType.valueOf(in.readUnsignedShort());
    int aClass = in.readUnsignedShort();
    long ttl = in.readUnsignedInt();
    int length = in.readUnsignedShort();
    int offset = in.readerIndex();
    if (endOffset - offset < length) {
      in.readerIndex(startOffset);
      return null;
    } 
    DnsRecord dnsRecord = decodeRecord(name, type, aClass, ttl, in, offset, length);
    in.readerIndex(offset + length);
    return (T)dnsRecord;
  }
  
  protected DnsRecord decodeRecord(String name, DnsRecordType type, int dnsClass, long timeToLive, ByteBuf in, int offset, int length) throws Exception {
    if (type == DnsRecordType.PTR)
      return new DefaultDnsPtrRecord(name, dnsClass, timeToLive, 
          decodeName0(in.duplicate().setIndex(offset, offset + length))); 
    return new DefaultDnsRawRecord(name, type, dnsClass, timeToLive, in
        .retainedDuplicate().setIndex(offset, offset + length));
  }
  
  protected String decodeName0(ByteBuf in) {
    return decodeName(in);
  }
  
  public static String decodeName(ByteBuf in) {
    int position = -1;
    int checked = 0;
    int end = in.writerIndex();
    int readable = in.readableBytes();
    if (readable == 0)
      return "."; 
    StringBuilder name = new StringBuilder(readable << 1);
    while (in.isReadable()) {
      int len = in.readUnsignedByte();
      boolean pointer = ((len & 0xC0) == 192);
      if (pointer) {
        if (position == -1)
          position = in.readerIndex() + 1; 
        if (!in.isReadable())
          throw new CorruptedFrameException("truncated pointer in a name"); 
        int next = (len & 0x3F) << 8 | in.readUnsignedByte();
        if (next >= end)
          throw new CorruptedFrameException("name has an out-of-range pointer"); 
        in.readerIndex(next);
        checked += 2;
        if (checked >= end)
          throw new CorruptedFrameException("name contains a loop."); 
        continue;
      } 
      if (len != 0) {
        if (!in.isReadable(len))
          throw new CorruptedFrameException("truncated label in a name"); 
        name.append(in.toString(in.readerIndex(), len, CharsetUtil.UTF_8)).append('.');
        in.skipBytes(len);
      } 
    } 
    if (position != -1)
      in.readerIndex(position); 
    if (name.length() == 0)
      return "."; 
    if (name.charAt(name.length() - 1) != '.')
      name.append('.'); 
    return name.toString();
  }
}
