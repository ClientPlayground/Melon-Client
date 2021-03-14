package com.github.steveice10.netty.handler.codec;

public interface DecoderResultProvider {
  DecoderResult decoderResult();
  
  void setDecoderResult(DecoderResult paramDecoderResult);
}
