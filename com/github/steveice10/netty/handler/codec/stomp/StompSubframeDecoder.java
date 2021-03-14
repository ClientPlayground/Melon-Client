package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.handler.codec.TooLongFrameException;
import com.github.steveice10.netty.util.internal.AppendableCharSequence;
import java.util.List;
import java.util.Locale;

public class StompSubframeDecoder extends ReplayingDecoder<StompSubframeDecoder.State> {
  private static final int DEFAULT_CHUNK_SIZE = 8132;
  
  private static final int DEFAULT_MAX_LINE_LENGTH = 1024;
  
  private final int maxLineLength;
  
  private final int maxChunkSize;
  
  private final boolean validateHeaders;
  
  private int alreadyReadChunkSize;
  
  private LastStompContentSubframe lastContent;
  
  enum State {
    SKIP_CONTROL_CHARACTERS, READ_HEADERS, READ_CONTENT, FINALIZE_FRAME_READ, BAD_FRAME, INVALID_CHUNK;
  }
  
  private long contentLength = -1L;
  
  public StompSubframeDecoder() {
    this(1024, 8132);
  }
  
  public StompSubframeDecoder(boolean validateHeaders) {
    this(1024, 8132, validateHeaders);
  }
  
  public StompSubframeDecoder(int maxLineLength, int maxChunkSize) {
    this(maxLineLength, maxChunkSize, false);
  }
  
  public StompSubframeDecoder(int maxLineLength, int maxChunkSize, boolean validateHeaders) {
    super(State.SKIP_CONTROL_CHARACTERS);
    if (maxLineLength <= 0)
      throw new IllegalArgumentException("maxLineLength must be a positive integer: " + maxLineLength); 
    if (maxChunkSize <= 0)
      throw new IllegalArgumentException("maxChunkSize must be a positive integer: " + maxChunkSize); 
    this.maxChunkSize = maxChunkSize;
    this.maxLineLength = maxLineLength;
    this.validateHeaders = validateHeaders;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    StompCommand command;
    StompHeadersSubframe frame;
    switch ((State)state()) {
      case SKIP_CONTROL_CHARACTERS:
        skipControlCharacters(in);
        checkpoint(State.READ_HEADERS);
      case READ_HEADERS:
        command = StompCommand.UNKNOWN;
        frame = null;
        try {
          command = readCommand(in);
          frame = new DefaultStompHeadersSubframe(command);
          checkpoint(readHeaders(in, frame.headers()));
          out.add(frame);
        } catch (Exception e) {
          if (frame == null)
            frame = new DefaultStompHeadersSubframe(command); 
          frame.setDecoderResult(DecoderResult.failure(e));
          out.add(frame);
          checkpoint(State.BAD_FRAME);
          return;
        } 
        break;
      case BAD_FRAME:
        in.skipBytes(actualReadableBytes());
        return;
    } 
    try {
      int toRead;
      switch ((State)state()) {
        case READ_CONTENT:
          toRead = in.readableBytes();
          if (toRead == 0)
            return; 
          if (toRead > this.maxChunkSize)
            toRead = this.maxChunkSize; 
          if (this.contentLength >= 0L) {
            int remainingLength = (int)(this.contentLength - this.alreadyReadChunkSize);
            if (toRead > remainingLength)
              toRead = remainingLength; 
            ByteBuf chunkBuffer = ByteBufUtil.readBytes(ctx.alloc(), in, toRead);
            if ((this.alreadyReadChunkSize += toRead) >= this.contentLength) {
              this.lastContent = new DefaultLastStompContentSubframe(chunkBuffer);
              checkpoint(State.FINALIZE_FRAME_READ);
            } else {
              out.add(new DefaultStompContentSubframe(chunkBuffer));
              return;
            } 
          } else {
            int nulIndex = ByteBufUtil.indexOf(in, in.readerIndex(), in.writerIndex(), (byte)0);
            if (nulIndex == in.readerIndex()) {
              checkpoint(State.FINALIZE_FRAME_READ);
            } else {
              if (nulIndex > 0) {
                toRead = nulIndex - in.readerIndex();
              } else {
                toRead = in.writerIndex() - in.readerIndex();
              } 
              ByteBuf chunkBuffer = ByteBufUtil.readBytes(ctx.alloc(), in, toRead);
              this.alreadyReadChunkSize += toRead;
              if (nulIndex > 0) {
                this.lastContent = new DefaultLastStompContentSubframe(chunkBuffer);
                checkpoint(State.FINALIZE_FRAME_READ);
              } else {
                out.add(new DefaultStompContentSubframe(chunkBuffer));
                return;
              } 
            } 
          } 
        case FINALIZE_FRAME_READ:
          skipNullCharacter(in);
          if (this.lastContent == null)
            this.lastContent = LastStompContentSubframe.EMPTY_LAST_CONTENT; 
          out.add(this.lastContent);
          resetDecoder();
          break;
      } 
    } catch (Exception e) {
      StompContentSubframe errorContent = new DefaultLastStompContentSubframe(Unpooled.EMPTY_BUFFER);
      errorContent.setDecoderResult(DecoderResult.failure(e));
      out.add(errorContent);
      checkpoint(State.BAD_FRAME);
    } 
  }
  
  private StompCommand readCommand(ByteBuf in) {
    String commandStr = readLine(in, 16);
    StompCommand command = null;
    try {
      command = StompCommand.valueOf(commandStr);
    } catch (IllegalArgumentException illegalArgumentException) {}
    if (command == null) {
      commandStr = commandStr.toUpperCase(Locale.US);
      try {
        command = StompCommand.valueOf(commandStr);
      } catch (IllegalArgumentException illegalArgumentException) {}
    } 
    if (command == null)
      throw new DecoderException("failed to read command from channel"); 
    return command;
  }
  
  private State readHeaders(ByteBuf buffer, StompHeaders headers) {
    AppendableCharSequence buf = new AppendableCharSequence(128);
    while (true) {
      boolean headerRead = readHeader(headers, buf, buffer);
      if (!headerRead) {
        if (headers.contains(StompHeaders.CONTENT_LENGTH)) {
          this.contentLength = getContentLength(headers, 0L);
          if (this.contentLength == 0L)
            return State.FINALIZE_FRAME_READ; 
        } 
        return State.READ_CONTENT;
      } 
    } 
  }
  
  private static long getContentLength(StompHeaders headers, long defaultValue) {
    long contentLength = headers.getLong(StompHeaders.CONTENT_LENGTH, defaultValue);
    if (contentLength < 0L)
      throw new DecoderException(StompHeaders.CONTENT_LENGTH + " must be non-negative"); 
    return contentLength;
  }
  
  private static void skipNullCharacter(ByteBuf buffer) {
    byte b = buffer.readByte();
    if (b != 0)
      throw new IllegalStateException("unexpected byte in buffer " + b + " while expecting NULL byte"); 
  }
  
  private static void skipControlCharacters(ByteBuf buffer) {
    while (true) {
      byte b = buffer.readByte();
      if (b != 13 && b != 10) {
        buffer.readerIndex(buffer.readerIndex() - 1);
        return;
      } 
    } 
  }
  
  private String readLine(ByteBuf buffer, int initialBufferSize) {
    AppendableCharSequence buf = new AppendableCharSequence(initialBufferSize);
    int lineLength = 0;
    while (true) {
      byte nextByte = buffer.readByte();
      if (nextByte == 13)
        continue; 
      if (nextByte == 10)
        return buf.toString(); 
      if (lineLength >= this.maxLineLength)
        invalidLineLength(); 
      lineLength++;
      buf.append((char)nextByte);
    } 
  }
  
  private boolean readHeader(StompHeaders headers, AppendableCharSequence buf, ByteBuf buffer) {
    buf.reset();
    int lineLength = 0;
    String key = null;
    boolean valid = false;
    while (true) {
      byte nextByte = buffer.readByte();
      if (nextByte == 58 && key == null) {
        key = buf.toString();
        valid = true;
        buf.reset();
        continue;
      } 
      if (nextByte == 13)
        continue; 
      if (nextByte == 10) {
        if (key == null && lineLength == 0)
          return false; 
        if (valid) {
          headers.add(key, buf.toString());
        } else if (this.validateHeaders) {
          invalidHeader(key, buf.toString());
        } 
        return true;
      } 
      if (lineLength >= this.maxLineLength)
        invalidLineLength(); 
      if (nextByte == 58 && key != null)
        valid = false; 
      lineLength++;
      buf.append((char)nextByte);
    } 
  }
  
  private void invalidHeader(String key, String value) {
    String line = (key != null) ? (key + ":" + value) : value;
    throw new IllegalArgumentException("a header value or name contains a prohibited character ':', " + line);
  }
  
  private void invalidLineLength() {
    throw new TooLongFrameException("An STOMP line is larger than " + this.maxLineLength + " bytes.");
  }
  
  private void resetDecoder() {
    checkpoint(State.SKIP_CONTROL_CHARACTERS);
    this.contentLength = -1L;
    this.alreadyReadChunkSize = 0;
    this.lastContent = null;
  }
}
