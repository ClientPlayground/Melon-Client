package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.DecoderResultProvider;

public interface HttpObject extends DecoderResultProvider {
  @Deprecated
  DecoderResult getDecoderResult();
}
