package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;

@Deprecated
public interface ByteBufProcessor extends ByteProcessor {
  @Deprecated
  public static final ByteBufProcessor FIND_NUL = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value != 0);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_NON_NUL = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value == 0);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_CR = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value != 13);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_NON_CR = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value == 13);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_LF = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value != 10);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_NON_LF = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value == 10);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_CRLF = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value != 13 && value != 10);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_NON_CRLF = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value == 13 || value == 10);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_LINEAR_WHITESPACE = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value != 32 && value != 9);
      }
    };
  
  @Deprecated
  public static final ByteBufProcessor FIND_NON_LINEAR_WHITESPACE = new ByteBufProcessor() {
      public boolean process(byte value) throws Exception {
        return (value == 32 || value == 9);
      }
    };
}
